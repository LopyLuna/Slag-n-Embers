package dev.lopyluna.slag.network.packets;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record SelectFluidIndexC2S(BlockPos pos, int index) implements CustomPacketPayload {
    public static final Type<SelectFluidIndexC2S> TYPE = new Type<>(SlagEmbers.loc("select_fluid_index"));

    public static final StreamCodec<FriendlyByteBuf, SelectFluidIndexC2S> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SelectFluidIndexC2S::pos,
            ByteBufCodecs.INT, SelectFluidIndexC2S::index,
            SelectFluidIndexC2S::new
    );

    public static void handle(SelectFluidIndexC2S msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            int i = msg.index;
            if (i < 0) return;
            var level = player.level();
            if (!level.isLoaded(msg.pos) || !player.canInteractWithBlock(msg.pos, 4.0)) return;
            if (!(level.getBlockEntity(msg.pos) instanceof CrucibleBE cr) || !(cr.getControllerBE() instanceof CrucibleBE be)) return;
            var inv = be.tankInventory;
            if (inv == null || !inv.moveFluidToFront(i)) return;
            be.setChanged();
            level.sendBlockUpdated(msg.pos, be.getBlockState(), be.getBlockState(), 3);
        });
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
