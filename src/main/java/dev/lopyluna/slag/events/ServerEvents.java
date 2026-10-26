package dev.lopyluna.slag.events;

import dev.lopyluna.slag.content.blocks.multiblock.MultiQueue;
import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.register.AllRecipes;
import dev.lopyluna.slag.register.AllTags;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Set;
import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent.ServerDataLoad event) {
        var types = new ObjectOpenHashSet<FluidType>();
        event.getRegistries().lookupOrThrow(Registries.FLUID).get(AllTags.HOT_FLUIDS).ifPresent(tag -> tag.forEach(h -> types.add(h.value().getFluidType())));
        hotTypes = types;
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(AllRecipes.DOUBLE_SMELTING.get(), AllRecipes.ALLOYING.get(), AllRecipes.MELTING.get(), AllRecipes.BASIN_CASTING.get(), AllRecipes.TABLE_CASTING.get());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide()) MultiQueue.tick(event.getLevel());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) MultiQueue.unload(level);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Entity entity && entity.isInFluidType((_, type, _) -> hotTypes.contains(type))) {
            entity.lavaHurt();
            entity.fallDistance *= entity.getFluidFallDistanceModifier(NeoForgeMod.LAVA_TYPE.value());
        }
    }

    private static volatile Set<FluidType> hotTypes = Set.of();

    @SubscribeEvent
    public static void onTakeItem(GrindstoneEvent.OnTakeItem event) {
        var stackA = event.getTopItem();
        var stackB = event.getBottomItem();

        if (stackA.getItem() instanceof ModularItem itemA && stackB.getItem() instanceof ModularItem itemB) {
            var modularA = itemA.getModularType(stackA);
            var modularB = itemB.getModularType(stackB);
            var partsA = itemA.getParts(stackA);
            var partsB = itemB.getParts(stackB);
            if (!(modularA == null || modularB == null) && !modularA.equals(modularB)) event.setCanceled(true);
            else if (!(partsA == null || partsB == null) && !partsA.equals(partsB)) event.setCanceled(true);

        }
    }
    @SubscribeEvent
    public static void OnPlaceItem(GrindstoneEvent.OnPlaceItem event) {
        var stackA = event.getTopItem();
        var stackB = event.getBottomItem();

        if (stackA.getItem() instanceof ModularItem itemA && stackB.getItem() instanceof ModularItem itemB) {
            var modularA = itemA.getModularType(stackA);
            var modularB = itemB.getModularType(stackB);
            var partsA = itemA.getParts(stackA);
            var partsB = itemB.getParts(stackB);
            if (!(modularA == null || modularB == null) && !modularA.equals(modularB)) event.setCanceled(true);
            else if (!(partsA == null || partsB == null) && !partsA.equals(partsB)) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAnvilChange(AnvilUpdateEvent event) {
        var stackA = event.getLeft();
        var stackB = event.getRight();

        if (stackA.getItem() instanceof ModularItem itemA && stackB.getItem() instanceof ModularItem itemB) {
            var modularA = itemA.getModularType(stackA);
            var modularB = itemB.getModularType(stackB);
            var partsA = itemA.getParts(stackA);
            var partsB = itemB.getParts(stackB);
            if (!(modularA == null || modularB == null) && !modularA.equals(modularB)) event.setCanceled(true);
            else if (!(partsA == null || partsB == null) && !partsA.equals(partsB)) event.setCanceled(true);
        }
    }
}
