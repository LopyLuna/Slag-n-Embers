package dev.lopyluna.slag.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("byName") Map<ResourceLocation, RecipeHolder<?>> slag$getByName();
    @Accessor("byName") void slag$setByName(Map<ResourceLocation, RecipeHolder<?>> byName);
    @Accessor("byType") Multimap<RecipeType<?>, RecipeHolder<?>> slag$getByType();
    @Accessor("byType") void slag$setByType(Multimap<RecipeType<?>, RecipeHolder<?>> byType);
    @Accessor("registries") HolderLookup.Provider slag$getRegistries();
}
