package dev.lopyluna.slag.mixin;

import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.modular.ModularItem;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Ingredient.class)
public class IngredientMixin {
    @Shadow @Final private HolderSet<Item> values;

    @Unique private List<TagKey<Item>> slag$tags;
    @Unique public List<TagKey<Item>> slag$getTags() {
        if (this.slag$tags == null) {
            var tags = new ArrayList<TagKey<Item>>();
            if (this.values != null) this.values.unwrapKey().ifPresent(tags::add);
            this.slag$tags = tags;
        }
        return this.slag$tags;
    }

    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void test(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
        if (input == null) return;
        var item = input.getItem();
        if (!(item instanceof ModularItem) && !(item instanceof IDynamicPart)) return;
        for (var tag : this.slag$getTags()) if (input.is(tag)) {
            cir.setReturnValue(true);
            return;
        }
    }
}
