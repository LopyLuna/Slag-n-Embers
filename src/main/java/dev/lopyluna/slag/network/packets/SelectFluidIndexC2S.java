package dev.lopyluna.slag.network.packets;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record SelectFluidIndexC2S(int index) implements CustomPacketPayload {
    public static final Type<SelectFluidIndexC2S> TYPE = new Type<>(SlagEmbers.loc("select_fluid_index"));

    public static final StreamCodec<FriendlyByteBuf, SelectFluidIndexC2S> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SelectFluidIndexC2S::index,
            SelectFluidIndexC2S::new
    );

    public static void handle(SelectFluidIndexC2S msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (msg.index < 0 || !(player.containerMenu instanceof InterfaceMenu menu) || !menu.stillValid(player)) return;
            var be = menu.getCrucible();
            if (be == null || !be.tankInventory.moveFluidToFront(msg.index)) return;
            be.setChanged();
            player.level().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
        });
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
