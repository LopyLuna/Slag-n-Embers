package dev.lopyluna.slag.client.render;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class SimpleCustomRenderer implements ItemModel.Unbaked {
    private final MapCodec<SimpleCustomRenderer> codec = MapCodec.unit(this);
    protected Supplier<? extends ItemModel> renderer;
    protected SimpleCustomRenderer(Supplier<? extends ItemModel> renderer) {
        this.renderer = renderer;
    }
    public static SimpleCustomRenderer create(Supplier<? extends ItemModel> renderer) {
        return new SimpleCustomRenderer(renderer);
    }
    @Override public @Nonnull MapCodec<SimpleCustomRenderer> type() {
        return codec;
    }
    @Override public void resolveDependencies(@Nonnull ResolvableModel.Resolver resolver) {}
    @Override public @Nonnull ItemModel bake(@Nonnull ItemModel.BakingContext context, @Nonnull Matrix4fc transformation) {
        return renderer.get();
    }
}
