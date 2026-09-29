package dev.lopyluna.slag.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.api.RecipeGenerators;
import dev.lopyluna.slag.content.blocks.melter.Recycling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@SuppressWarnings("DataFlowIssue")
@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Inject(method = "updateRegistryTags()V", at = @At("TAIL"))
    private void slag$generateMelting(CallbackInfo ci) {
        try {
            var accessor = (RecipeManagerAccessor) ((ReloadableServerResources) (Object) this).getRecipeManager();
            var byName = accessor.slag$getByName();
            var registries = accessor.slag$getRegistries();
            var generated = new ArrayList<>(RecipeGenerators.generate(byName.values(), registries));
            var all = new ArrayList<>(byName.values());
            all.addAll(generated);
            generated.addAll(Recycling.generate(all, registries));
            if (generated.isEmpty()) return;
            var types = ImmutableMultimap.<RecipeType<?>, RecipeHolder<?>>builder().putAll(accessor.slag$getByType());
            var names = ImmutableMap.<ResourceLocation, RecipeHolder<?>>builder().putAll(byName);
            for (var holder : generated) {
                if (byName.containsKey(holder.id())) continue;
                types.put(holder.value().getType(), holder);
                names.put(holder.id(), holder);
            }
            accessor.slag$setByType(types.build());
            accessor.slag$setByName(names.build());
        } catch (Exception e) {
            SlagEmbers.LOGGER.error("Couldn't generate recipes", e);
        }
    }
}
