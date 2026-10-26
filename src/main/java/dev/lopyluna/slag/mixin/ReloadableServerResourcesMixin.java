package dev.lopyluna.slag.mixin;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.api.RecipeGenerators;
import dev.lopyluna.slag.content.blocks.melter.Recycling;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;

@Mixin(RecipeManager.class)
public class ReloadableServerResourcesMixin {
    @Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
    private void slag$generateMelting(FeatureFlagSet enabledFlags, CallbackInfo ci) {
        try {
            var accessor = (RecipeManagerAccessor) this;
            var recipes = accessor.slag$getRecipes();
            var registries = accessor.slag$getRegistries();
            var generated = new ArrayList<>(RecipeGenerators.generate(recipes.values(), registries));
            var all = new ArrayList<>(recipes.values());
            all.addAll(generated);
            generated.addAll(Recycling.generate(all, registries));
            if (generated.isEmpty()) return;
            var merged = new ArrayList<>(recipes.values());
            var names = new HashSet<>();
            for (var holder : merged) names.add(holder.id());
            for (var holder : generated) if (names.add(holder.id())) merged.add(holder);
            accessor.slag$setRecipes(RecipeMap.create(merged));
        } catch (Exception e) {
            SlagEmbers.LOGGER.error("Couldn't generate recipes", e);
        }
    }
}
