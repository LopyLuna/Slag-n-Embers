package dev.lopyluna.slag.events;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.network.packets.SyncTemperaturesS2C;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = SlagEmbers.MOD_ID)
public class TemperatureEvents {
    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(Temperatures.RELOADER);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        var packet = new SyncTemperaturesS2C(Temperatures.json);
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, packet));
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        Temperatures.clearCache();
    }
}
