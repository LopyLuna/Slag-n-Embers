package dev.lopyluna.slag.events;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.MaterialTextures;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@SuppressWarnings({"removal"})
@EventBusSubscriber(modid = SlagEmbers.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class GameClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        MaterialTextures.tick();
    }
}
