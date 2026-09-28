package dev.lopyluna.slag.content.blocks.multiblock;

import dev.lopyluna.slag.config.SlagServerConfigs;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;

public final class MultiQueue {
    public static final int CHANGED = 8;
    private static final Map<Level, MultiQueue> QUEUES = new WeakHashMap<>();

    private final Level level;
    private final List<Job> jobs = new ArrayList<>();
    private final List<Job> incoming = new ArrayList<>();
    private final Map<FluidMultiBlockEntity, Settle> settles = new Reference2ObjectOpenHashMap<>();
    private int spent;
    private int paused;
    private boolean draining;

    private MultiQueue(Level level) {
        this.level = level;
    }

    public interface Job {
        int run(int budget, boolean urgent);
        boolean done();
        default void finish() {}
    }

    public static int updates() {
        return SlagServerConfigs.SPEC.isLoaded() ? SlagServerConfigs.CRUCIBLE_UPDATES_PER_TICK.get() : 2048;
    }

    public static MultiQueue of(Level level) {
        return QUEUES.computeIfAbsent(level, MultiQueue::new);
    }

    public static void tick(Level level) {
        var queue = QUEUES.get(level);
        if (queue == null) return;
        queue.drain();
        queue.spent = 0;
    }

    public static void unload(Level level) {
        QUEUES.remove(level);
    }

    public static void settle(FluidMultiBlockEntity ctrl, List<Sweep> urgent, List<Sweep> later) {
        var level = ctrl.getLevel();
        if (level == null || level.isClientSide) return;
        var queue = of(level);
        var job = queue.settles.get(ctrl);
        if (job == null) {
            job = queue.new Settle(ctrl);
            queue.settles.put(ctrl, job);
            queue.add(job);
        }
        job.urgent.addAll(urgent);
        job.later.addAll(later);
        ctrl.settling = true;
        queue.flush();
    }

    public static void pause(Level level) {
        if (!level.isClientSide) of(level).paused++;
    }

    public static void resume(Level level) {
        if (level.isClientSide) return;
        var queue = of(level);
        if (--queue.paused == 0) queue.flush();
    }

    public static void connect(Level level, Area area, boolean inside) {
        if (level == null || level.isClientSide) return;
        var queue = of(level);
        queue.add(queue.new Connect(area, inside));
    }

    public void add(Job job) {
        (draining ? incoming : jobs).add(job);
    }

    public void flush() {
        if (!draining && paused == 0 && !level.captureBlockSnapshots) drain();
    }

    private void drain() {
        draining = true;
        var budget = updates() * CHANGED;
        try {
            for (var round = 0; round < 3 && spent < budget && !jobs.isEmpty(); round++) {
                for (var pass = 0; pass < 2; pass++) for (var job : jobs) {
                    if (spent >= budget) break;
                    spent += job.run(budget - spent, pass == 0);
                }
                jobs.removeIf(job -> {
                    if (!job.done()) return false;
                    job.finish();
                    return true;
                });
                jobs.addAll(incoming);
                incoming.clear();
            }
        } finally {
            draining = false;
            jobs.addAll(incoming);
            incoming.clear();
        }
    }

    private final class Settle implements Job {
        private final FluidMultiBlockEntity ctrl;
        private final ArrayDeque<Sweep> urgent = new ArrayDeque<>();
        private final ArrayDeque<Sweep> later = new ArrayDeque<>();

        Settle(FluidMultiBlockEntity ctrl) {
            this.ctrl = ctrl;
        }

        @Override
        public int run(int budget, boolean first) {
            if (ctrl.isRemoved() && !level.isLoaded(ctrl.getBlockPos())) {
                urgent.clear();
                later.clear();
                return 1;
            }
            var sweeps = first ? urgent : later;
            var used = 0;
            while (used < budget && !sweeps.isEmpty()) {
                var sweep = sweeps.peek();
                if (sweep.next()) used += ctrl.settle(sweep.pos());
                else sweeps.poll();
            }
            return used;
        }

        @Override
        public boolean done() {
            return urgent.isEmpty() && later.isEmpty();
        }

        @Override
        public void finish() {
            settles.remove(ctrl);
            if (!ctrl.isRemoved()) ctrl.settled();
        }
    }

    private final class Connect implements Job {
        private final Area area;
        private final boolean inside;
        private boolean done;

        Connect(Area area, boolean inside) {
            this.area = area;
            this.inside = inside;
        }

        @Override
        public int run(int budget, boolean first) {
            if (first || done) return 0;
            done = true;
            var owners = new ReferenceLinkedOpenHashSet<FluidMultiBlockEntity>();
            var seen = new LongOpenHashSet();
            var cost = 0;
            var sweeps = inside ? List.of(Sweep.shell(area), Sweep.shell(area.grow())) : List.of(Sweep.points(LongList.of(area.origin().asLong())), Sweep.shell(area.grow()));
            for (var sweep : sweeps) while (sweep.next()) {
                cost++;
                var pos = sweep.pos();
                if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof FluidMultiBlockEntity part)) continue;
                if (!part.isController && part.notIntact()) {
                    part.removeController();
                    continue;
                }
                var at = part.getController();
                if (!seen.add(at.asLong()) || !level.isLoaded(at)) continue;
                if (level.getBlockEntity(at) instanceof FluidMultiBlockEntity owner && owner.isController && !owner.held && owner.getType() == part.getType()) owners.add(owner);
            }
            for (var owner : owners) if (!owner.isRemoved() && owner.isController) cost += 16 + ConnectivityHandler.formMulti(owner) / 4;
            return cost;
        }

        @Override
        public boolean done() {
            return done;
        }
    }

    public record Area(BlockPos origin, int widthX, int widthZ, int height) {
        public Area grow() {
            return new Area(origin.offset(-1, -1, -1), widthX + 2, widthZ + 2, height + 2);
        }

        public int volume() {
            return widthX * widthZ * height;
        }

        public int shell() {
            if (widthX <= 2 || widthZ <= 2 || height <= 2) return volume();
            return volume() - (widthX - 2) * (widthZ - 2) * (height - 2);
        }
    }

    @SuppressWarnings("unused")
    public static final class Sweep {
        private static final int FULL = 0;
        private static final int SHELL = 1;
        private static final int RING = 2;

        private final long[] points;
        private final Area area;
        private final int mode;
        private int index, x, y, z, x0, x1, z0, z1;
        private boolean started;
        private BlockPos pos = BlockPos.ZERO;

        private Sweep(long[] points, Area area, int mode) {
            this.points = points;
            this.area = area;
            this.mode = mode;
        }

        public static Sweep points(LongList points) {
            var sorted = points.toLongArray();
            LongArrays.quickSort(sorted, Sweep::byColumn);
            return new Sweep(sorted, null, FULL);
        }

        public static int byColumn(long a, long b) {
            var column = Long.compare(column(a), column(b));
            return column != 0 ? column : Long.compare(a, b);
        }

        private static long column(long pos) {
            return (long) (BlockPos.getX(pos) >> 4) << 32 | (BlockPos.getZ(pos) >> 4) & 0xFFFFFFFFL;
        }

        public static Sweep shell(Area area) {
            return new Sweep(null, area, SHELL);
        }

        public static Sweep ring(Area area) {
            return new Sweep(null, area, RING);
        }

        public static Sweep full(Area area) {
            return new Sweep(null, area, FULL);
        }

        public BlockPos pos() {
            return pos;
        }

        public boolean next() {
            if (points != null) {
                if (index >= points.length) return false;
                pos = BlockPos.of(points[index++]);
                return true;
            }
            var origin = area.origin();
            int widthX = area.widthX(), widthZ = area.widthZ(), height = area.height();
            if (!started) {
                started = true;
                x1 = Math.min(widthX, 16 - Math.floorMod(origin.getX(), 16));
                z1 = Math.min(widthZ, 16 - Math.floorMod(origin.getZ(), 16));
            }
            while (x0 < widthX) {
                if (y >= height) {
                    y = 0;
                    z0 = z1;
                    z1 = Math.min(widthZ, z0 + 16);
                    if (z0 >= widthZ) {
                        z0 = 0;
                        z1 = Math.min(widthZ, 16 - Math.floorMod(origin.getZ(), 16));
                        x0 = x1;
                        x1 = Math.min(widthX, x0 + 16);
                    }
                    x = x0;
                    z = z0;
                    continue;
                }
                if (x >= x1) {
                    x = x0;
                    y++;
                    continue;
                }
                if (z >= z1) {
                    z = z0;
                    x++;
                    continue;
                }
                var at = z++;
                if (mode != FULL && x > 0 && x < widthX - 1 && at > 0 && at < widthZ - 1 && (mode == RING || y > 0 && y < height - 1)) {
                    z = Math.min(widthZ - 1, z1);
                    continue;
                }
                pos = origin.offset(x, y, at);
                return true;
            }
            return false;
        }
    }
}
