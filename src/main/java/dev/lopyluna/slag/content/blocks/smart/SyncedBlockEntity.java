package dev.lopyluna.slag.content.blocks.smart;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"unused", "NullableProblems", "deprecation"})
@ParametersAreNonnullByDefault
public class SyncedBlockEntity extends BlockEntity {
    public static final MapCodec<CompoundTag> TAG = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);

    public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @Nonnull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return writeClient(new CompoundTag(), registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        readClient(input.read(TAG).orElseGet(CompoundTag::new), input.lookup());
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        readClient(input.read(TAG).orElseGet(CompoundTag::new), input.lookup());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        loadTag(input.read(TAG).orElseGet(CompoundTag::new), input.lookup());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        var tag = new CompoundTag();
        saveTag(tag, registries());
        output.store(TAG, tag);
    }

    protected void loadTag(CompoundTag tag, HolderLookup.Provider registries) {}

    protected void saveTag(CompoundTag tag, HolderLookup.Provider registries) {}

    public HolderLookup.Provider registries() {
        return level != null ? level.registryAccess() : RegistryAccess.EMPTY;
    }

    // Special handling for client update packets
    public void readClient(CompoundTag tag, HolderLookup.Provider registries) {
        loadTag(tag, registries);
    }

    // Special handling for client update packets
    public CompoundTag writeClient(CompoundTag tag, HolderLookup.Provider registries) {
        saveTag(tag, registries);
        return tag;
    }

    public void sendData() {
        if (level instanceof ServerLevel serverLevel)
            serverLevel.getChunkSource().blockChanged(getBlockPos());
    }

    public void notifyUpdate() {
        setChanged();
        sendData();
    }

    public HolderGetter<Block> blockHolderGetter() {
        return level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK;
    }

}
