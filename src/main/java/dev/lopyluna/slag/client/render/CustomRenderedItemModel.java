package dev.lopyluna.slag.client.render;

import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Vector3fc;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CustomRenderedItemModel {
	private static final Map<Identifier, StandaloneModelKey<CustomRenderedItemModel>> KEYS = new ConcurrentHashMap<>();
	public static final CustomRenderedItemModel EMPTY = new CustomRenderedItemModel(List.of(), null);

	public final List<BakedQuad> quads;
	public final @Nullable ModelRenderProperties properties;
	public final Supplier<Vector3fc[]> extents;

	public CustomRenderedItemModel(List<BakedQuad> quads, @Nullable ModelRenderProperties properties) {
		this.quads = quads;
		this.properties = properties;
		extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(quads));
	}

	public static void register(ModelEvent.RegisterStandalone event, Identifier id) {
		var key = KEYS.computeIfAbsent(id, location -> new StandaloneModelKey<>(location::toString));
		event.register(key, new SimpleUnbakedStandaloneModel<>(id, CustomRenderedItemModel::bake));
	}

	public static @Nullable CustomRenderedItemModel get(Identifier id) {
		var key = KEYS.get(id);
		return key == null ? null : Minecraft.getInstance().getModelManager().getStandaloneModel(key);
	}

	private static CustomRenderedItemModel bake(ResolvedModel model, ModelBaker baker, ModelDebugName name) {
		var slots = model.getTopTextureSlots();
		return new CustomRenderedItemModel(model.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY).getAll(), ModelRenderProperties.fromResolvedModel(baker, model, slots));
	}
}
