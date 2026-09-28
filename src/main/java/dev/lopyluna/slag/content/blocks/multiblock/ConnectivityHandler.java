package dev.lopyluna.slag.content.blocks.multiblock;

import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue.Area;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;

public class ConnectivityHandler {
    private static final Comparator<BlockEntity> ORDER = Comparator.<BlockEntity>comparingInt(be -> be.getBlockPos().getY())
            .thenComparingInt(be -> be.getBlockPos().getX())
            .thenComparingInt(be -> be.getBlockPos().getZ());

    public static <T extends BlockEntity & IMultiBlockEntityContainer> int formMulti(T be) {
        var level = be.getLevel();
        if (level == null || !be.isController()) return 0;
        MultiQueue.pause(level);
        try {
            var cache = new Cache<T>(be.getType(), level);
            var multis = new ArrayList<T>();
            for (var candidate : candidates(be, cache)) {
                if (candidate.isRemoved() || !candidate.isController()) continue;
                var box = best(candidate, cache, multis);
                if (box != null) form(candidate, box, cache, new ArrayList<>(), true);
            }
            return cache.parts.size();
        } finally {
            MultiQueue.resume(level);
        }
    }

    @Nullable
    public static <T extends BlockEntity & IMultiBlockEntityContainer> Plan plan(T be) {
        var level = be.getLevel();
        if (level == null || !be.isController()) return null;
        var cache = new Cache<T>(be.getType(), level);
        var multis = new ArrayList<T>();
        for (var candidate : candidates(be, cache)) {
            if (!candidate.isController()) continue;
            var box = best(candidate, cache, multis);
            if (box == null) continue;
            var origin = candidate.getBlockPos();
            var data = candidate.getExtraData();
            var absorbed = new ArrayList<Area>();
            for (var multi : multis) {
                var at = multi.getBlockPos();
                if (!contains(at.getX() - origin.getX(), at.getY() - origin.getY(), at.getZ() - origin.getZ(), multi, box.widthX, box.widthZ, box.height)) continue;
                absorbed.add(new Area(at, multi.getWidthX(), multi.getWidthZ(), multi.getHeight()));
                if (multi != candidate) data = multi.modifyExtraData(data);
            }
            return new Plan(new Area(origin, box.widthX, box.widthZ, box.height), absorbed, data);
        }
        return null;
    }

    public record Plan(Area area, List<Area> absorbed, @Nullable Object data) {}

    public static <T extends BlockEntity & IMultiBlockEntityContainer> void splitMulti(T be) {
        var level = be.getLevel();
        if (level == null) return;
        T ctrl = be.getControllerBE();
        if (ctrl == null) return;
        int widthX = ctrl.getWidthX(), widthZ = ctrl.getWidthZ(), height = ctrl.getHeight();
        if (widthX == 1 && widthZ == 1 && height == 1) return;
        MultiQueue.pause(level);
        try {
            split(ctrl, level, widthX, widthZ, height);
        } finally {
            MultiQueue.resume(level);
        }
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> void split(T ctrl, Level level, int widthX, int widthZ, int height) {
        var origin = ctrl.getBlockPos();
        var type = ctrl.getType();
        var data = ctrl.getExtraData();
        var pool = new ArrayList<FluidStack>();
        take(ctrl, pool);

        var area = widthX * widthZ;
        var parts = new ArrayList<T>(Collections.nCopies(area * height, null));
        var present = new boolean[area * height];
        for (var y = 0; y < height; y++) for (var x = 0; x < widthX; x++) for (var z = 0; z < widthZ; z++) {
            T part = partAt(type, level, origin.offset(x, y, z));
            if (part == null || !part.getController().equals(origin)) continue;
            var i = y * area + x * widthZ + z;
            parts.set(i, part);
            present[i] = true;
        }
        for (var part : parts) {
            if (part == null) continue;
            if (part != ctrl) take(part, pool);
            part.setExtraData(data);
            part.detachController();
        }

        var cache = new Cache<T>(type, level);
        var maxY = ctrl.getMaxLength(Direction.Axis.Y, Math.max(widthX, widthZ));
        for (var piece : Tiler.split(present, widthX, widthZ, height, ctrl.getMaxWidthX(), ctrl.getMaxWidthZ(), maxY)) {
            var at = piece[1] * area + piece[0] * widthZ + piece[2];
            var box = new Box(piece[3], piece[4], piece[5]);
            if (box.volume() > 1 && form(parts.get(at), box, cache, pool, false)) continue;
            for (var y = 0; y < box.height; y++) for (var x = 0; x < box.widthX; x++) for (var z = 0; z < box.widthZ; z++) {
                var part = parts.get(at + y * area + x * widthZ + z);
                if (part == null) continue;
                give(part, pool);
                part.removeController();
                part.preventConnectivityUpdate();
            }
        }
        MultiQueue.connect(level, new Area(origin, widthX, widthZ, height), true);
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> List<T> candidates(T be, Cache<T> cache) {
        var origin = be.getBlockPos();
        int maxX = be.getMaxWidthX(), maxZ = be.getMaxWidthZ(), maxY = maxLength(be, Math.max(maxX, maxZ));
        var found = new ArrayList<T>();
        var owners = new ReferenceOpenHashSet<T>();
        var seen = new LongOpenHashSet();
        var queue = new LongArrayFIFOQueue();
        queue.enqueue(origin.asLong());
        while (!queue.isEmpty()) {
            var pos = queue.dequeueLong();
            var dx = origin.getX() - BlockPos.getX(pos);
            var dy = origin.getY() - BlockPos.getY(pos);
            var dz = origin.getZ() - BlockPos.getZ(pos);
            if (dx < 0 || dy < 0 || dz < 0 || dx >= maxX || dy >= maxY || dz >= maxZ || !seen.add(pos)) continue;
            var owner = cache.owner(pos);
            if (owner == null || !owners.add(owner)) continue;
            found.add(owner);
            var at = owner.getBlockPos();
            var x0 = at.getX();
            var y0 = at.getY();
            var z0 = at.getZ();
            for (var x = 0; x < owner.getWidthX(); x++) for (var z = 0; z < owner.getWidthZ(); z++) queue.enqueue(BlockPos.asLong(x0 + x, y0 - 1, z0 + z));
            for (var y = 0; y < owner.getHeight(); y++) for (var z = 0; z < owner.getWidthZ(); z++) queue.enqueue(BlockPos.asLong(x0 - 1, y0 + y, z0 + z));
            for (var x = 0; x < owner.getWidthX(); x++) for (var y = 0; y < owner.getHeight(); y++) queue.enqueue(BlockPos.asLong(x0 + x, y0 + y, z0 - 1));
        }
        found.sort(ORDER);
        return found;
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> @Nullable Box best(T be, Cache<T> cache, List<T> multis) {
        var origin = be.getBlockPos();
        int ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
        int minX = be.getWidthX(), minZ = be.getWidthZ();
        int maxX = be.getMaxWidthX(), maxZ = be.getMaxWidthZ();
        if (maxX < minX || maxZ < minZ) return null;
        var maxY = maxLength(be, Math.max(maxX, maxZ));

        var tops = new int[maxX * maxZ];
        multis.clear();
        var seen = new ReferenceOpenHashSet<T>();
        for (var x = 0; x < maxX; x++) for (var z = 0; z < maxZ; z++) {
            var limit = maxY;
            if (x > 0) limit = Math.min(limit, tops[(x - 1) * maxZ + z]);
            if (z > 0) limit = Math.min(limit, tops[x * maxZ + z - 1]);
            var y = 0;
            while (y < limit) {
                var owner = cache.owner(BlockPos.asLong(ox + x, oy + y, oz + z));
                if (owner == null) break;
                if (owner.getWidthX() == 1 && owner.getWidthZ() == 1 && owner.getHeight() == 1) {
                    y++;
                    continue;
                }
                var at = owner.getBlockPos();
                var top = at.getY() + owner.getHeight() - oy;
                if (at.getY() - oy > y || top <= y || !covers(owner, ox + x, oz + z)) break;
                if (seen.add(owner)) multis.add(owner);
                y = top;
            }
            tops[x * maxZ + z] = Math.min(y, limit);
        }

        var best = (Box) null;
        var volume = minX * minZ * be.getHeight();
        for (var wX = maxX; wX >= minX; wX--) for (var wZ = maxZ; wZ >= minZ; wZ--) {
            if ((wX == 1) != (wZ == 1)) continue;
            var height = Math.min(be.getMaxLength(Direction.Axis.Y, Math.max(wX, wZ)), tops[(wX - 1) * maxZ + wZ - 1]);
            if (wX * wZ * height <= volume) continue;
            height = fit(origin, wX, wZ, height, multis);
            if (wX * wZ * height <= volume) continue;
            volume = wX * wZ * height;
            best = new Box(wX, wZ, height);
        }
        return best;
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> int fit(BlockPos origin, int widthX, int widthZ, int height, List<T> multis) {
        for (var changed = true; changed && height > 0; ) {
            changed = false;
            for (var multi : multis) {
                var at = multi.getBlockPos();
                var x = at.getX() - origin.getX();
                var y = at.getY() - origin.getY();
                var z = at.getZ() - origin.getZ();
                if (y >= height || x >= widthX || z >= widthZ) continue;
                if (contains(x, y, z, multi, widthX, widthZ, height)) continue;
                height = Math.max(0, y);
                changed = true;
            }
        }
        return height;
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> boolean form(T be, Box box, Cache<T> cache, List<FluidStack> pool, boolean merge) {
        var origin = be.getBlockPos();
        int ownX = be.getWidthX(), ownZ = be.getWidthZ(), ownY = be.getHeight();
        var own = merge && ownX * ownZ * ownY > 1;
        var parts = new ArrayList<T>();
        if (own) parts.add(be);
        for (var y = 0; y < box.height; y++) for (var x = 0; x < box.widthX; x++) for (var z = 0; z < box.widthZ; z++) {
            if (own && x < ownX && y < ownY && z < ownZ) continue;
            var part = cache.part(BlockPos.asLong(origin.getX() + x, origin.getY() + y, origin.getZ() + z));
            if (part == null) return false;
            parts.add(part);
        }
        var owners = new LongOpenHashSet();
        var fresh = new LongArrayList();
        var absorbed = new ArrayList<Area>();
        for (var part : parts) {
            var at = part.getController();
            if (!owners.add(at.asLong())) continue;
            var owner = cache.part(at.asLong());
            if (owner == null || !owner.isController()) return false;
            if (!contains(at.getX() - origin.getX(), at.getY() - origin.getY(), at.getZ() - origin.getZ(), owner, box.widthX, box.widthZ, box.height)) return false;
            if (!merge) continue;
            if (owner.getWidthX() * owner.getWidthZ() * owner.getHeight() == 1) fresh.add(at.asLong());
            else {
                if (owner != be) fresh.add(at.asLong());
                absorbed.add(new Area(at, owner.getWidthX(), owner.getWidthZ(), owner.getHeight()));
            }
        }

        var data = be.getExtraData();
        for (var part : parts) {
            take(part, pool);
            if (part != be && part.isController()) data = part.modifyExtraData(data);
        }
        for (var part : parts) {
            if (part == be) continue;
            part.setController(origin);
            part.preventConnectivityUpdate();
            part.setWidthX(box.widthX);
            part.setWidthZ(box.widthZ);
            part.setHeight(box.height);
        }
        be.setExtraData(data);
        be.preventConnectivityUpdate();
        be.setWidthX(box.widthX);
        be.setWidthZ(box.widthZ);
        be.setHeight(box.height);
        if (be instanceof IMultiBlockEntityContainer.FluidMulti fluid && fluid.hasTank()) fluid.setTankSize(box.volume());
        give(be, pool);
        be.formed(fresh, absorbed);
        return true;
    }

    private static boolean contains(int x, int y, int z, IMultiBlockEntityContainer multi, int widthX, int widthZ, int height) {
        return x >= 0 && y >= 0 && z >= 0 && x + multi.getWidthX() <= widthX && z + multi.getWidthZ() <= widthZ && y + multi.getHeight() <= height;
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> boolean covers(T multi, int x, int z) {
        var at = multi.getBlockPos();
        return x >= at.getX() && z >= at.getZ() && x < at.getX() + multi.getWidthX() && z < at.getZ() + multi.getWidthZ();
    }

    private static int maxLength(IMultiBlockEntityContainer be, int widths) {
        var max = 0;
        for (var width = 1; width <= widths; width++) max = Math.max(max, be.getMaxLength(Direction.Axis.Y, width));
        return max;
    }

    private static void take(Object be, List<FluidStack> pool) {
        if (!(be instanceof IMultiBlockEntityContainer.FluidMulti fluid) || !fluid.hasTank()) return;
        for (var stack : fluid.takeFluids()) merge(pool, stack);
    }

    private static void give(Object be, List<FluidStack> pool) {
        if (pool.isEmpty() || !(be instanceof IMultiBlockEntityContainer.FluidMulti fluid) || !fluid.hasTank()) return;
        fluid.giveFluids(pool);
    }

    private static void merge(List<FluidStack> pool, FluidStack stack) {
        if (stack.isEmpty()) return;
        for (var known : pool) if (FluidStack.isSameFluidSameComponents(known, stack)) {
            known.setAmount((int) Math.min(Integer.MAX_VALUE, (long) known.getAmount() + stack.getAmount()));
            return;
        }
        pool.add(stack.copy());
    }

    @Nullable
    public static <T extends BlockEntity & IMultiBlockEntityContainer> T partAt(BlockEntityType<?> type, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be.getType() == type && !be.isRemoved()) return checked(be);
        return null;
    }

    @SuppressWarnings("unused")
    public static <T extends BlockEntity & IMultiBlockEntityContainer> boolean isConnected(BlockGetter level, BlockPos pos, BlockPos other) {
        T one = checked(level.getBlockEntity(pos));
        T two = checked(level.getBlockEntity(other));
        if (one == null || two == null) return false;
        return one.getController().equals(two.getController());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity & IMultiBlockEntityContainer> T checked(BlockEntity be) {
        if (be instanceof IMultiBlockEntityContainer) return (T) be;
        return null;
    }

    private record Box(int widthX, int widthZ, int height) {
        int volume() {
            return widthX * widthZ * height;
        }
    }

    private static class Cache<T extends BlockEntity & IMultiBlockEntityContainer> {
        private final Long2ObjectOpenHashMap<T> parts = new Long2ObjectOpenHashMap<>();
        private final BlockEntityType<?> type;
        private final Level level;

        Cache(BlockEntityType<?> type, Level level) {
            this.type = type;
            this.level = level;
        }

        @Nullable T part(long pos) {
            if (parts.containsKey(pos)) return parts.get(pos);
            var at = BlockPos.of(pos);
            T part = level.isLoaded(at) ? partAt(type, level, at) : null;
            if (part instanceof FluidMultiBlockEntity fluid && fluid.held) part = null;
            parts.put(pos, part);
            return part;
        }

        @Nullable T owner(long pos) {
            var part = part(pos);
            if (part == null || part.isController()) return part;
            var owner = part(part.getController().asLong());
            return owner != null && owner.isController() ? owner : null;
        }
    }

    private static final class Tiler {
        private static final int BUDGET = 4000;
        private static final int MAINS = 16;
        private static final int FREE = -1;
        private static final int HOLE = -2;
        private static final Comparator<int[]> ORDER = Comparator.<int[]>comparingInt(c -> c[1]).thenComparingInt(c -> c[0]).thenComparingInt(c -> c[2]);

        private Tiler() {}

        static List<int[]> split(boolean[] present, int widthX, int widthZ, int height, int maxX, int maxZ, int maxY) {
            var best = tile(present, widthX, widthZ, height, maxX, maxZ, maxY);
            var bestVolume = 0;
            var area = widthX * widthZ;
            for (var main : mains(present, widthX, widthZ, height, maxX, maxZ, maxY)) {
                var volume = main[3] * main[4] * main[5];
                var rest = present.clone();
                for (var y = 0; y < main[5]; y++) for (var x = 0; x < main[3]; x++) {
                    var from = (main[1] + y) * area + (main[0] + x) * widthZ + main[2];
                    Arrays.fill(rest, from, from + main[4], false);
                }
                var pieces = tile(rest, widthX, widthZ, height, maxX, maxZ, maxY);
                if (pieces.size() + 1 > best.size() || pieces.size() + 1 == best.size() && volume <= bestVolume) continue;
                pieces.addFirst(main);
                best = pieces;
                bestVolume = volume;
            }
            return best;
        }

        private static List<int[]> mains(boolean[] present, int widthX, int widthZ, int height, int maxX, int maxZ, int maxY) {
            var area = widthX * widthZ;
            var holes = new ArrayList<int[]>();
            var hx = new BitSet(widthX);
            var hy = new BitSet(height);
            var hz = new BitSet(widthZ);
            for (var i = 0; i < present.length; i++) {
                if (present[i]) continue;
                int y = i / area, x = i % area / widthZ, z = i % widthZ;
                holes.add(new int[]{x, y, z});
                hx.set(x);
                hy.set(y);
                hz.set(z);
            }
            if (holes.isEmpty()) return widthX <= maxX && widthZ <= maxZ && height <= maxY ? List.of(new int[]{0, 0, 0, widthX, widthZ, height}) : List.of();
            int[] lx = lows(hx, widthX), ux = highs(hx, widthX), ly = lows(hy, height), uy = highs(hy, height), lz = lows(hz, widthZ), uz = highs(hz, widthZ);
            var combos = (long) lx.length * ux.length * ly.length * uy.length * lz.length * uz.length;
            if (combos * holes.size() > 4_000_000L) return List.of();
            var found = new ArrayList<int[]>();
            for (var x0 : lx) for (var x1 : ux) {
                var w = x1 - x0;
                if (w <= 0 || w > maxX) continue;
                for (var z0 : lz) for (var z1 : uz) {
                    var d = z1 - z0;
                    if (d <= 0 || d > maxZ || (w == 1) != (d == 1)) continue;
                    for (var y0 : ly) for (var y1 : uy) {
                        var h = y1 - y0;
                        if (h <= 0 || h > maxY || w * d * h <= 1 || !clear(holes, x0, y0, z0, x1, y1, z1)) continue;
                        found.add(new int[]{x0, y0, z0, w, d, h});
                    }
                }
            }
            found.sort(Comparator.comparingInt(c -> -c[3] * c[4] * c[5]));
            return found.subList(0, Math.min(MAINS, found.size()));
        }

        private static int[] lows(BitSet holes, int size) {
            var out = new BitSet(size);
            out.set(0);
            for (var i = holes.nextSetBit(0); i >= 0; i = holes.nextSetBit(i + 1)) for (var step = 1; step <= 2; step++) if (i + step < size) out.set(i + step);
            return out.stream().toArray();
        }

        private static int[] highs(BitSet holes, int size) {
            var out = new BitSet(size + 1);
            out.set(size);
            for (var i = holes.nextSetBit(0); i >= 0; i = holes.nextSetBit(i + 1)) for (var step = 0; step <= 1; step++) if (i - step > 0) out.set(i - step);
            return out.stream().toArray();
        }

        private static boolean clear(List<int[]> holes, int x0, int y0, int z0, int x1, int y1, int z1) {
            for (var hole : holes) if (hole[0] >= x0 && hole[0] < x1 && hole[1] >= y0 && hole[1] < y1 && hole[2] >= z0 && hole[2] < z1) return false;
            return true;
        }

        static List<int[]> tile(boolean[] present, int widthX, int widthZ, int height, int maxX, int maxZ, int maxY) {
            var area = widthX * widthZ;
            var memo = new HashMap<BitSet, List<int[]>>();
            var out = new ArrayList<int[]>();
            var open = new Long2ObjectOpenHashMap<int[]>();
            for (var y = 0; y < height; ) {
                var top = y + 1;
                while (top < height && same(present, y * area, top * area, area)) top++;
                var mask = new BitSet(area);
                for (var i = 0; i < area; i++) if (present[y * area + i]) mask.set(i);
                var rects = mask.isEmpty() ? List.<int[]>of() : memo.computeIfAbsent(mask, m -> region(m, widthZ, 0, 0, widthX, widthZ, maxX, maxZ));
                var next = new Long2ObjectOpenHashMap<int[]>();
                for (var rect : rects) {
                    var key = key(rect);
                    int from = y, size = top - y;
                    var prev = open.remove(key);
                    if (prev != null) {
                        if (prev[1] + size <= maxY) {
                            from = prev[0];
                            size += prev[1];
                        } else close(out, rect, prev[0], prev[1]);
                    }
                    for (; size > maxY; from += maxY, size -= maxY) close(out, rect, from, maxY);
                    next.put(key, new int[]{from, size, rect[0], rect[1], rect[2], rect[3]});
                }
                for (var prev : open.values()) close(out, prev);
                open = next;
                y = top;
            }
            for (var prev : open.values()) close(out, prev);
            out.sort(ORDER);
            return out;
        }

        private static boolean same(boolean[] present, int a, int b, int area) {
            for (var i = 0; i < area; i++) if (present[a + i] != present[b + i]) return false;
            return true;
        }

        private static long key(int[] rect) {
            return (long) rect[0] << 48 | (long) rect[1] << 32 | (long) rect[2] << 16 | rect[3];
        }

        private static void close(List<int[]> out, int[] rect, int y, int size) {
            out.add(new int[]{rect[0], y, rect[1], rect[2] - rect[0], rect[3] - rect[1], size});
        }

        private static void close(List<int[]> out, int[] open) {
            out.add(new int[]{open[2], open[0], open[3], open[4] - open[2], open[5] - open[3], open[1]});
        }

        private static boolean valid(int width, int depth, int maxX, int maxZ) {
            return (width == 1) == (depth == 1) && width <= maxX && depth <= maxZ;
        }

        private static List<int[]> region(BitSet mask, int stride, int x0, int z0, int x1, int z1, int maxX, int maxZ) {
            var rects = solve(mask, stride, x0, z0, x1, z1, maxX, maxZ);
            if (rects != null) return rects;
            var holesX = new boolean[x1 - x0];
            var holesZ = new boolean[z1 - z0];
            for (var x = x0; x < x1; x++) for (var z = z0; z < z1; z++) if (!mask.get(x * stride + z)) {
                holesX[x - x0] = true;
                holesZ[z - z0] = true;
            }
            var cutX = cut(holesX);
            var cutZ = cut(holesZ);
            if (cutX[0] == 0 && cutZ[0] == 0) return greedy(mask, stride, x0, z0, x1, z1, maxX, maxZ);
            var out = new ArrayList<int[]>();
            if (cutX[0] >= cutZ[0]) {
                out.addAll(region(mask, stride, x0, z0, x0 + cutX[1], z1, maxX, maxZ));
                out.addAll(region(mask, stride, x0 + cutX[1], z0, x1, z1, maxX, maxZ));
            } else {
                out.addAll(region(mask, stride, x0, z0, x1, z0 + cutZ[1], maxX, maxZ));
                out.addAll(region(mask, stride, x0, z0 + cutZ[1], x1, z1, maxX, maxZ));
            }
            return out;
        }

        private static int[] cut(boolean[] holes) {
            int gap = 0, at = 0, last = -1;
            for (var i = 0; i < holes.length; i++) {
                if (!holes[i]) continue;
                if (last >= 0 && i - last > gap) {
                    gap = i - last;
                    at = (last + i + 1) / 2;
                }
                last = i;
            }
            return new int[]{gap, at};
        }

        private static List<int[]> greedy(BitSet mask, int stride, int x0, int z0, int x1, int z1, int maxX, int maxZ) {
            var used = new BitSet();
            var out = new ArrayList<int[]>();
            for (var x = x0; x < x1; x++) for (var z = z0; z < z1; z++) {
                if (!mask.get(x * stride + z) || used.get(x * stride + z)) continue;
                int area = 1, endX = x + 1, endZ = z + 1, limit = z1;
                for (var ex = x; ex < x1 && ex - x < maxX; ex++) {
                    var ez = z;
                    while (ez < limit && mask.get(ex * stride + ez) && !used.get(ex * stride + ez)) ez++;
                    if (ez == z) break;
                    limit = ez;
                    int width = ex - x + 1, depth = Math.min(limit - z, maxZ);
                    if (valid(width, depth, maxX, maxZ) && width * depth > area) {
                        area = width * depth;
                        endX = ex + 1;
                        endZ = z + depth;
                    }
                }
                for (var a = x; a < endX; a++) for (var b = z; b < endZ; b++) used.set(a * stride + b);
                out.add(new int[]{x, z, endX, endZ});
            }
            return out;
        }

        private static List<int[]> solve(BitSet mask, int stride, int x0, int z0, int x1, int z1, int maxX, int maxZ) {
            var holes = new ArrayList<int[]>();
            for (var x = x0; x < x1; x++) for (var z = z0; z < z1; z++) if (!mask.get(x * stride + z)) holes.add(new int[]{x, z});
            if (holes.isEmpty() && valid(x1 - x0, z1 - z0, maxX, maxZ)) return List.of(new int[]{x0, z0, x1, z1});
            var solver = new Solver(coords(x0, x1, holes, 0, maxX), coords(z0, z1, holes, 1, maxZ), holes, maxX, maxZ);
            return solver.run();
        }

        private static int[] coords(int from, int to, List<int[]> holes, int axis, int max) {
            var marks = new boolean[to - from + 1];
            for (var c : new int[]{from, to, from + 1, from + 2, to - 1, to - 2}) mark(marks, from, to, c);
            for (var hole : holes) for (var d = -1; d <= 2; d++) mark(marks, from, to, hole[axis] + d);
            for (var k = from + max; k < to; k += max) {
                mark(marks, from, to, k);
                mark(marks, from, to, to - (k - from));
            }
            var count = 0;
            for (var mark : marks) if (mark) count++;
            var out = new int[count];
            for (int i = 0, n = 0; i < marks.length; i++) if (marks[i]) out[n++] = from + i;
            return out;
        }

        private static void mark(boolean[] marks, int from, int to, int c) {
            if (c >= from && c <= to) marks[c - from] = true;
        }

        private static final class Solver {
            private final int[] xs, zs, grid;
            private final int nx, nz, maxX, maxZ;
            private final int[][] around;
            private final List<int[]> stack = new ArrayList<>();
            private List<int[]> best;
            private int nodes;

            Solver(int[] xs, int[] zs, List<int[]> holes, int maxX, int maxZ) {
                this.xs = xs;
                this.zs = zs;
                this.maxX = maxX;
                this.maxZ = maxZ;
                nx = xs.length - 1;
                nz = zs.length - 1;
                grid = new int[nx * nz];
                Arrays.fill(grid, FREE);
                var cells = new int[holes.size()];
                for (var i = 0; i < cells.length; i++) {
                    var hole = holes.get(i);
                    cells[i] = Arrays.binarySearch(xs, hole[0]) * nz + Arrays.binarySearch(zs, hole[1]);
                    grid[cells[i]] = HOLE;
                }
                around = new int[cells.length][];
                for (var i = 0; i < cells.length; i++) {
                    int ci = cells[i] / nz, cj = cells[i] % nz, n = 0;
                    var near = new int[4];
                    if (ci + 1 < nx && grid[cells[i] + nz] != HOLE) near[n++] = cells[i] + nz;
                    if (ci > 0 && grid[cells[i] - nz] != HOLE) near[n++] = cells[i] - nz;
                    if (cj + 1 < nz && grid[cells[i] + 1] != HOLE) near[n++] = cells[i] + 1;
                    if (cj > 0 && grid[cells[i] - 1] != HOLE) near[n++] = cells[i] - 1;
                    around[i] = Arrays.copyOf(near, n);
                }
            }

            List<int[]> run() {
                search(0);
                if (best == null) return null;
                var out = new ArrayList<int[]>(best.size());
                for (var o : best) out.add(new int[]{xs[o[1]], zs[o[2]], xs[o[3] + 1], zs[o[4] + 1]});
                return out;
            }

            private void search(int start) {
                nodes++;
                var cell = first(start);
                if (cell < 0) {
                    if (best == null || stack.size() < best.size()) best = new ArrayList<>(stack);
                    return;
                }
                if (best != null && stack.size() + Math.max(1, lower()) >= best.size()) return;
                for (var option : options(cell)) {
                    if (nodes > BUDGET) return;
                    fill(option, stack.size());
                    stack.add(option);
                    search(cell);
                    stack.removeLast();
                    fill(option, FREE);
                }
            }

            private int first(int start) {
                for (var k = start; k < grid.length; k++) if (grid[k] == FREE) return k;
                return -1;
            }

            private int lower() {
                var bound = 0;
                for (var near : around) {
                    var free = 0;
                    for (var cell : near) if (grid[cell] == FREE) free++;
                    bound = Math.max(bound, free);
                }
                return bound;
            }

            private List<int[]> options(int cell) {
                int i = cell / nz, j = cell % nz, limit = nz;
                var out = new ArrayList<int[]>();
                for (var ei = i; ei < nx; ei++) {
                    if (grid[ei * nz + j] != FREE) break;
                    var width = xs[ei + 1] - xs[i];
                    if (width > maxX) break;
                    var ej = j;
                    while (ej < limit && grid[ei * nz + ej] == FREE) ej++;
                    limit = ej;
                    for (var e = j; e < limit; e++) {
                        var depth = zs[e + 1] - zs[j];
                        if (depth > maxZ) break;
                        if (valid(width, depth, maxX, maxZ)) out.add(new int[]{width * depth, i, j, ei, e});
                    }
                }
                out.sort((a, b) -> Integer.compare(b[0], a[0]));
                return out;
            }

            private void fill(int[] option, int value) {
                for (var a = option[1]; a <= option[3]; a++) for (var b = option[2]; b <= option[4]; b++) grid[a * nz + b] = value;
            }
        }
    }
}
