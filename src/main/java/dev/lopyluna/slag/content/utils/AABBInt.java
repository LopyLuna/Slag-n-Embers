package dev.lopyluna.slag.content.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.List;

@SuppressWarnings("unused")
public record AABBInt(BlockPos min, BlockPos max) {
    public Vec3i sizeVec() { return new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1); }
    public Iterable<BlockPos> positions() { return BlockPos.betweenClosed(min, max); }


    public static AABBInt fromPositions(List<BlockPos> positions) {
        if (positions.isEmpty()) return null;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (var p : positions) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());

            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }
        return new AABBInt(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }
}
