package dev.lopyluna.slag.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

import javax.annotation.Nonnull;

public class CustomRenderedItemModel extends BakedModelWrapper<BakedModel> {
	public CustomRenderedItemModel(BakedModel originalModel) {
		super(originalModel);
	}
	@Override public boolean isCustomRenderer() {
		return true;
	}
	@Override public @Nonnull BakedModel applyTransform(@Nonnull ItemDisplayContext cameraItemDisplayContext, @Nonnull PoseStack mat, boolean leftHand) {
		super.applyTransform(cameraItemDisplayContext, mat, leftHand);
		return this;
	}
	public BakedModel getOriginalModel() {
		return originalModel;
	}
}
