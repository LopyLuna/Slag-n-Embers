package dev.lopyluna.slag.content.blocks.smart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class CachedRenderBBBlockEntity extends SyncedBlockEntity {

    private AABB renderBoundingBox;

    public CachedRenderBBBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public AABB getRenderBoundingBox() {
        if (renderBoundingBox == null) renderBoundingBox = createRenderBoundingBox();
        return renderBoundingBox;
    }

    protected void invalidateRenderBoundingBox() {
        renderBoundingBox = null;
    }

    protected AABB createRenderBoundingBox() {
        return new AABB(getBlockPos());
    }

}
