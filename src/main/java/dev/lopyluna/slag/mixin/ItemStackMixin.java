package dev.lopyluna.slag.mixin;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.stream.Stream;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    // ItemStack#is(TagKey<Item>)
    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void isTag(TagKey<Item> tag, CallbackInfoReturnable<Boolean> cir) {
        var self = (ItemStack)(Object)this;
        var item = self.getItem();
        if (item instanceof ModularItem tool) {
            var value = AllUtils.matchesAnyTag(tool, self, tag);
            if (value) cir.setReturnValue(value);
        }
        if (item instanceof IDynamicPart part) {
            var value = AllUtils.matchesAnyTag(part, self, tag);
            if (value) cir.setReturnValue(value);
        }
        if (Traits.of(self).itemTags.contains(tag)) cir.setReturnValue(true);
    }

    // ItemStack#is(HolderSet<Item>)
    @Inject(method = "is(Lnet/minecraft/core/HolderSet;)Z", at = @At("HEAD"), cancellable = true)
    private void isHolderSet(HolderSet<Item> set, CallbackInfoReturnable<Boolean> cir) {
        var self = (ItemStack)(Object)this;
        var item = self.getItem();
        if (item instanceof ModularItem tool && set instanceof HolderSet.Named<Item> named) {
            var value = AllUtils.matchesAnyTag(tool, self, named.key());
            if (value) cir.setReturnValue(value);
        }
        if (item instanceof IDynamicPart part && set instanceof HolderSet.Named<Item> named) {
            var value = AllUtils.matchesAnyTag(part, self, named.key());
            if (value) cir.setReturnValue(value);
        }
        if (set instanceof HolderSet.Named<Item> named && Traits.of(self).itemTags.contains(named.key())) cir.setReturnValue(true);
    }

    // ItemStack#getTags()
    @Inject(method = "getTags()Ljava/util/stream/Stream;", at = @At("RETURN"), cancellable = true)
    public void getTags(CallbackInfoReturnable<Stream<TagKey<Item>>> cir) {
        var tags = new ArrayList<>(cir.getReturnValue().toList());
        var self = (ItemStack)(Object)this;
        var item = self.getItem();
        if (item instanceof ModularItem tool) tags.addAll(AllUtils.getTags(tool, self));
        if (item instanceof IDynamicPart part) tags.addAll(AllUtils.getTags(part, self));
        tags.addAll(Traits.of(self).itemTags);
        cir.setReturnValue(tags.stream());
    }
}
