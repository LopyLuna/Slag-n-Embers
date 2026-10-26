package dev.lopyluna.slag.compat.sable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("unused")
public class SableCompat {
    public static final boolean LOADED = false;
    private static final Vec3 DOWN = new Vec3(0, -1, 0);

    public static Vec3 toWorld(Level level, Vec3 pos) {
        return pos;
    }

    public static @Nullable List<Vec3> localPoints(Level level, BlockPos pos, AABB box) {
        return null;
    }

    public static Vec3 localDown(Level level, BlockPos pos) {
        return DOWN;
    }

    public static List<Ray> localRays(Level level, Vec3 from, Vec3 to) {
        return List.of();
    }

    public static Vec3 renderLocal(BlockEntity be, Vec3 pos, float pt) {
        return pos;
    }

    public static boolean inSubLevel(Level level, BlockPos pos) {
        return false;
    }

    public record Ray(Vec3 from, Vec3 to) {}
}
