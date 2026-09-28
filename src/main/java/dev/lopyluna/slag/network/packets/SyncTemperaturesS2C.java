package dev.lopyluna.slag.network.packets;

import com.google.gson.JsonParser;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.temperature.Temperatures;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import javax.annotation.Nonnull;

public record SyncTemperaturesS2C(String json) implements CustomPacketPayload {
    public static final Type<SyncTemperaturesS2C> TYPE = new Type<>(SlagEmbers.loc("sync_temperatures"));
    public static final StreamCodec<ByteBuf, SyncTemperaturesS2C> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(262144), SyncTemperaturesS2C::json,
            SyncTemperaturesS2C::new
    );

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTemperaturesS2C packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (ServerLifecycleHooks.getCurrentServer() != null) return;
            Temperatures.load(JsonParser.parseString(packet.json()).getAsJsonObject(), false);
        });
    }
}
