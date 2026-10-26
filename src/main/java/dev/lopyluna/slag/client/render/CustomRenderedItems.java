package dev.lopyluna.slag.client.render;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.dynamic_mold.DynamicMoldRenderer;
import dev.lopyluna.slag.content.items.dynamic_part.DynamicPartRenderer;
import dev.lopyluna.slag.content.items.modular.ModularItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID, value = Dist.CLIENT)
public class CustomRenderedItems {
	public static final SimpleCustomRenderer DYNAMIC_PART = SimpleCustomRenderer.create(DynamicPartRenderer::new);
	public static final SimpleCustomRenderer MODULAR = SimpleCustomRenderer.create(ModularItemRenderer::new);
	public static final SimpleCustomRenderer MOLD = SimpleCustomRenderer.create(DynamicMoldRenderer::new);

	@SubscribeEvent
	public static void registerItemModels(RegisterItemModelsEvent event) {
		event.register(SlagEmbers.loc("dynamic_part"), DYNAMIC_PART.type());
		event.register(SlagEmbers.loc("modular"), MODULAR.type());
		event.register(SlagEmbers.loc("mold"), MOLD.type());
	}
}
