package dev.lopyluna.slag.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.mixin.ItemStackRenderStateAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class PartialItemModelRenderer {
	private final PoseStack ms = new PoseStack();
	private final ItemStackRenderState output;
	private final ItemStack stack;
	private final ItemModelResolver resolver;
	private final ItemDisplayContext ctx;
	private final @Nullable ClientLevel level;
	private final @Nullable ItemOwner owner;
	private final int seed;

	private ItemTransform transform = ItemTransform.NO_TRANSFORM;
	private boolean blockLight;
	private @Nullable Material.Baked particle;

	private PartialItemModelRenderer(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext ctx, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		this.output = output;
		this.stack = stack;
		this.resolver = resolver;
		this.ctx = ctx;
		this.level = level;
		this.owner = owner;
		this.seed = seed;
	}

	public static PartialItemModelRenderer of(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext ctx, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		return new PartialItemModelRenderer(output, stack, resolver, ctx, level, owner, seed);
	}

	public PoseStack pose() {
		return ms;
	}

	public void animated() {
		output.setAnimated();
	}

	public void transform(CustomRenderedItemModel model) {
		var properties = model.properties;
		if (properties == null) return;
		transform = properties.transforms().getTransform(ctx);
		blockLight = properties.usesBlockLight();
		particle = properties.particleMaterial();
	}

	public void render(CustomRenderedItemModel model) {
		if (model.quads.isEmpty()) return;
		output.appendModelIdentityElement(model);
		var layer = output.newLayer();
		apply(layer);
		layer.setExtents(model.extents);
		layer.prepareQuadList().addAll(model.quads);
		if (!stack.hasFoil()) return;
		layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
		output.setAnimated();
	}

	public void render(ItemStack part) {
		if (part.isEmpty()) return;
		var accessor = (ItemStackRenderStateAccessor) output;
		var start = accessor.slag$count();
		resolver.appendItemLayers(output, part, ctx, level, owner, seed);
		var layers = accessor.slag$layers();
		for (var i = start; i < accessor.slag$count(); i++) apply(layers[i]);
	}

	private void apply(ItemStackRenderState.LayerRenderState layer) {
		var local = new Matrix4f().translation(0.5f, 0.5f, 0.5f).mul(ms.last().pose()).translate(-0.5f, -0.5f, -0.5f);
		output.appendModelIdentityElement(transform);
		output.appendModelIdentityElement(local);
		layer.setItemTransform(transform);
		layer.setLocalTransform(local);
		layer.setUsesBlockLight(blockLight);
		if (particle != null) layer.setParticleMaterial(particle);
	}
}
