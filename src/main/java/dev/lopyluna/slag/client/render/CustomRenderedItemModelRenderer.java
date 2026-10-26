package dev.lopyluna.slag.client.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class CustomRenderedItemModelRenderer implements ItemModel {
    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext ctx, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        render(stack, PartialItemModelRenderer.of(output, stack, resolver, ctx, level, owner, seed), ctx);
    }

    public static CustomRenderedItemModel getModel(Identifier location) {
        var model = CustomRenderedItemModel.get(location);
        return model == null ? CustomRenderedItemModel.EMPTY : model;
    }

    protected abstract void render(ItemStack stack, PartialItemModelRenderer renderer, ItemDisplayContext ctx);
}
