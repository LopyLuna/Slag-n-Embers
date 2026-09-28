package dev.lopyluna.slag.compat.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SableCompat {
    public static final boolean LOADED = ModList.get().isLoaded("sable");
    private static final Vec3 DOWN = new Vec3(0, -1, 0);

    public static Vec3 toWorld(Level level, Vec3 pos) {
        return LOADED ? Impl.toWorld(level, pos) : pos;
    }

    public static @Nullable List<Vec3> localPoints(Level level, BlockPos pos, AABB box) {
        return LOADED ? Impl.localPoints(level, pos, box) : null;
    }

    public static Vec3 localDown(Level level, BlockPos pos) {
        return LOADED ? Impl.localDown(level, pos) : DOWN;
    }

    public static List<Ray> localRays(Level level, Vec3 from, Vec3 to) {
        return LOADED ? Impl.localRays(level, from, to) : List.of();
    }

    public static Vec3 renderLocal(BlockEntity be, Vec3 pos, float pt) {
        return LOADED ? Impl.renderLocal(be, pos, pt) : pos;
    }

    public static boolean inSubLevel(Level level, BlockPos pos) {
        return LOADED && Impl.inSubLevel(level, pos);
    }

    public record Ray(Vec3 from, Vec3 to) {}

    private static class Impl {
        private static Vec3 toWorld(Level level, Position pos) {
            return SableCompanion.INSTANCE.projectOutOfSubLevel(level, pos);
        }

        private static @Nullable List<Vec3> localPoints(Level level, BlockPos pos, AABB box) {
            var sub = SableCompanion.INSTANCE.getContaining(level, pos);
            if (sub == null) return null;
            var pose = sub.logicalPose();
            var points = new ArrayList<Vec3>();
            for (var x = 0; x <= 2; x++) for (var y = 0; y <= 4; y++) for (var z = 0; z <= 2; z++)
                points.add(pose.transformPositionInverse(new Vec3(Mth.lerp(x / 2d, box.minX, box.maxX), Mth.lerp(y / 4d, box.minY, box.maxY), Mth.lerp(z / 2d, box.minZ, box.maxZ))));
            return points;
        }

        private static List<Ray> localRays(Level level, Position from, Position to) {
            var rays = new ArrayList<Ray>();
            for (var sub : SableCompanion.INSTANCE.getAllIntersecting(level, new BoundingBox3d(from, to))) {
                var pose = sub.logicalPose();
                rays.add(new Ray(pose.transformPositionInverse(new Vec3(from.x(), from.y(), from.z())), pose.transformPositionInverse(new Vec3(to.x(), to.y(), to.z()))));
            }
            return rays;
        }

        private static Vec3 renderLocal(BlockEntity be, Vec3 pos, float pt) {
            var sub = SableCompanion.INSTANCE.getContainingClient(be);
            return sub == null ? pos : sub.renderPose(pt).transformPositionInverse(pos);
        }

        private static boolean inSubLevel(Level level, BlockPos pos) {
            return SableCompanion.INSTANCE.getContaining(level, pos) != null;
        }

        private static Vec3 localDown(Level level, BlockPos pos) {
            var sub = SableCompanion.INSTANCE.getContaining(level, pos);
            return sub == null ? DOWN : sub.logicalPose().transformNormalInverse(DOWN);
        }
    }
}
