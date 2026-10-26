package dev.lopyluna.slag.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("recipes") RecipeMap slag$getRecipes();
    @Accessor("recipes") void slag$setRecipes(RecipeMap recipes);
    @Accessor("registries") HolderLookup.Provider slag$getRegistries();
}
