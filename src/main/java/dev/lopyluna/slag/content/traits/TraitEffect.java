package dev.lopyluna.slag.content.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.register.AllTraitEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.function.Function;

@SuppressWarnings("unused")
public interface TraitEffect {
    Codec<TraitEffect> CODEC = AllTraitEffects.REGISTRY.byNameCodec().dispatch(TraitEffect::codec, Function.identity());

    MapCodec<? extends TraitEffect> codec();

    void collect(Trait trait, Traits.Builder builder);

    default InteractionResult useOn(Trait trait, UseOnContext context) {
        return InteractionResult.PASS;
    }

    default void onBlockBreak(Trait trait, ItemStack stack, BlockEvent.BreakEvent event) {}

    default void onBlockDrops(Trait trait, ItemStack stack, BlockDropsEvent event) {}

    default void onHurtEnemy(Trait trait, ItemStack stack, LivingEntity target, LivingEntity attacker) {}

    default void inventoryTick(Trait trait, ItemStack stack, Level level, Entity entity, int slot, boolean selected) {}
}
