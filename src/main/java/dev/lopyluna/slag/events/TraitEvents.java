package dev.lopyluna.slag.events;

import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.register.AllTraits;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class TraitEvents {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        var stack = event.getPlayer().getMainHandItem();
        for (var hook : Traits.of(stack).blockBreak) hook.effect().onBlockBreak(hook.trait(), stack, event);
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        var stack = event.getTool();
        for (var hook : Traits.of(stack).blockDrops) hook.effect().onBlockDrops(hook.trait(), stack, event);
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        var attacker = event.getEntity().getKillCredit();
        if (attacker != null && Traits.of(attacker.getMainHandItem()).has(AllTraits.KNIFE_MINING)) event.setStrength(event.getOriginalStrength() - 0.1f);
    }

    @SubscribeEvent
    public static void onGameEvent(VanillaGameEvent event) {
        if (!(event.getCause() instanceof LivingEntity living)) return;
        for (var slot : EquipmentSlot.values()) if (Traits.of(living.getItemBySlot(slot)).silent) {
            event.setCanceled(true);
            return;
        }
    }
}
