package dev.lopyluna.slag.mixin;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.stream.Stream;

@SuppressWarnings({"MissingUnique", "AddedMixinMembersNamePattern"})
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Holder<Item> typeHolder();

    public boolean is(TagKey<Item> tag) {
        return slag$matches(tag) || typeHolder().is(tag);
    }

    public boolean is(HolderSet<Item> set) {
        return set instanceof HolderSet.Named<Item> named && slag$matches(named.key()) || set.contains(typeHolder());
    }

    public Stream<TagKey<Item>> tags() {
        var self = (ItemStack)(Object)this;
        var item = self.getItem();
        var extra = new ArrayList<TagKey<Item>>();
        if (item instanceof ModularItem tool) extra.addAll(AllUtils.getTags(tool, self));
        if (item instanceof IDynamicPart part) extra.addAll(AllUtils.getTags(part, self));
        extra.addAll(Traits.of(self).itemTags);
        return extra.isEmpty() ? typeHolder().tags() : Stream.concat(typeHolder().tags(), extra.stream());
    }

    @Unique
    private boolean slag$matches(TagKey<Item> tag) {
        var self = (ItemStack)(Object)this;
        var item = self.getItem();
        if (item instanceof ModularItem tool && AllUtils.matchesAnyTag(tool, self, tag)) return true;
        if (item instanceof IDynamicPart part && AllUtils.matchesAnyTag(part, self, tag)) return true;
        return Traits.of(self).itemTags.contains(tag);
    }
}
