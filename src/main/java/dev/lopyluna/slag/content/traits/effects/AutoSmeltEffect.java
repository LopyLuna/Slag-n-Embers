package dev.lopyluna.slag.content.traits.effects;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.traits.Trait;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import javax.annotation.Nonnull;

public record AutoSmeltEffect() implements TraitEffect {
    public static final AutoSmeltEffect INSTANCE = new AutoSmeltEffect();
    public static final MapCodec<AutoSmeltEffect> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @Nonnull MapCodec<AutoSmeltEffect> codec() {
        return CODEC;
    }

    @Override
    public void collect(Trait trait, Traits.Builder builder) {
        builder.blockDrops(trait, this);
    }

    @Override
    public void onBlockDrops(Trait trait, ItemStack stack, BlockDropsEvent event) {
        var level = event.getLevel();
        var experience = 0f;
        for (var drop : event.getDrops()) {
            var item = drop.getItem();
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(item), level).orElse(null);
            if (recipe == null) continue;
            var result = recipe.value().getResultItem(level.registryAccess());
            if (result.isEmpty()) continue;
            drop.setItem(result.copyWithCount(result.getCount() * item.getCount()));
            experience += recipe.value().getExperience() * item.getCount();
        }
        var whole = Mth.floor(experience);
        if (level.random.nextFloat() < experience - whole) whole++;
        if (whole > 0) event.setDroppedExperience(event.getDroppedExperience() + whole);
    }
}
