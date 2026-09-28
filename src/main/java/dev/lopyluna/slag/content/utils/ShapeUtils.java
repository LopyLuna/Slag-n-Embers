package dev.lopyluna.slag.content.utils;

import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static net.minecraft.core.Direction.UP;

public class ShapeUtils {
    private static final Map<Cuboid, VoxelShape> CUBOIDS = new ConcurrentHashMap<>();
    private static final Map<Joined, VoxelShape> JOINED = new ConcurrentHashMap<>();
    private static final Map<Shaped, VoxelShaper> SHAPERS = new ConcurrentHashMap<>();

    public static Builder shape(VoxelShape shape) {
        return new Builder(shape);
    }

    public static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    public static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return CUBOIDS.computeIfAbsent(new Cuboid(x1, y1, z1, x2, y2, z2), key -> Block.box(x1, y1, z1, x2, y2, z2));
    }

    public static VoxelShape join(VoxelShape first, VoxelShape second, BooleanOp op) {
        return JOINED.computeIfAbsent(new Joined(first, second, op), key -> Shapes.join(first, second, op));
    }

    @SuppressWarnings("unused")
    public static class Builder {

        private VoxelShape shape;

        public Builder(VoxelShape shape) {
            this.shape = shape;
        }

        public Builder add(VoxelShape shape) {
            this.shape = join(this.shape, shape, BooleanOp.OR);
            return this;
        }

        public Builder add(double x1, double y1, double z1, double x2, double y2, double z2) {
            return add(cuboid(x1, y1, z1, x2, y2, z2));
        }

        public Builder erase(double x1, double y1, double z1, double x2, double y2, double z2) {
            shape = join(shape, cuboid(x1, y1, z1, x2, y2, z2), BooleanOp.ONLY_FIRST);
            return this;
        }

        public VoxelShape build() {
            return shape;
        }

        public VoxelShaper build(BiFunction<VoxelShape, Direction, VoxelShaper> factory, Direction direction) {
            var base = shape;
            return SHAPERS.computeIfAbsent(new Shaped(base, factory, direction), key -> factory.apply(base, direction));
        }

        public VoxelShaper build(BiFunction<VoxelShape, Direction.Axis, VoxelShaper> factory, Direction.Axis axis) {
            var base = shape;
            return SHAPERS.computeIfAbsent(new Shaped(base, factory, axis), key -> factory.apply(base, axis));
        }

        public VoxelShaper forDirectional(Direction direction) {
            return build(VoxelShaper::forDirectional, direction);
        }

        public VoxelShaper forAxis() {
            return build(VoxelShaper::forAxis, Direction.Axis.Y);
        }

        public VoxelShaper forHorizontalAxis() {
            return build(VoxelShaper::forHorizontalAxis, Direction.Axis.Z);
        }

        public VoxelShaper forHorizontal(Direction direction) {
            return build(VoxelShaper::forHorizontal, direction);
        }

        public VoxelShaper forDirectional() {
            return forDirectional(UP);
        }

    }

    private record Cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {}

    private record Joined(VoxelShape first, VoxelShape second, BooleanOp op) {}

    private record Shaped(VoxelShape shape, Object factory, Object side) {}
}
