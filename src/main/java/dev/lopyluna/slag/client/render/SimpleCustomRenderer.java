package dev.lopyluna.slag.client.render;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;

public class SimpleCustomRenderer implements IClientItemExtensions {
    protected CustomRenderedItemModelRenderer renderer;
    protected SimpleCustomRenderer(CustomRenderedItemModelRenderer renderer) {
        this.renderer = renderer;
    }
    public static SimpleCustomRenderer create(Item item, CustomRenderedItemModelRenderer renderer) {
        CustomRenderedItems.register(item);
        return new SimpleCustomRenderer(renderer);
    }
    @Override public @Nonnull CustomRenderedItemModelRenderer getCustomRenderer() {
        return renderer;
    }
}
