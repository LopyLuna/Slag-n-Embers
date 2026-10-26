package dev.lopyluna.slag.events;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.MaterialTextures;
import dev.lopyluna.slag.content.AllUtils;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID, value = Dist.CLIENT)
public class GameClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        MaterialTextures.tick();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        AllUtils.clientRecipes = event.getRecipeMap();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        AllUtils.clientRecipes = RecipeMap.EMPTY;
    }
}
