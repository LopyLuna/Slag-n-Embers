package dev.lopyluna.slag.events;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.HeatModel;
import dev.lopyluna.slag.content.blocks.basin.BasinRenderer;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleRenderer;
import dev.lopyluna.slag.content.blocks.crucible_interface.InterfaceRenderer;
import dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen;
import dev.lopyluna.slag.content.blocks.drain.DrainRenderer;
import dev.lopyluna.slag.content.blocks.forge.client.ForgeScreen;
import dev.lopyluna.slag.content.blocks.melter.MelterRenderer;
import dev.lopyluna.slag.content.blocks.melter.client.MelterScreen;
import dev.lopyluna.slag.content.blocks.table.TableRenderer;
import dev.lopyluna.slag.content.smithing.client.ModularSmithingScreen;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = SlagEmbers.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    static void registerBER(EntityRenderersEvent.RegisterRenderers e) {
        e.registerBlockEntityRenderer(AllBETypes.INTERFACE.get(), InterfaceRenderer::new);
        e.registerBlockEntityRenderer(AllBETypes.CRUCIBLE.get(), CrucibleRenderer::new);
        e.registerBlockEntityRenderer(AllBETypes.TABLE.get(), TableRenderer::new);
        e.registerBlockEntityRenderer(AllBETypes.BASIN.get(), BasinRenderer::new);
        e.registerBlockEntityRenderer(AllBETypes.DRAIN.get(), DrainRenderer::new);
        e.registerBlockEntityRenderer(AllBETypes.MELTER.get(), MelterRenderer::new);
    }

    @SubscribeEvent
    static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        HeatModel.wrap(event);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(AllMenuTypes.FORGE.get(), ForgeScreen::new);
        event.register(AllMenuTypes.INTERFACE.get(), InterfaceScreen::new);
        event.register(AllMenuTypes.MELTER.get(), MelterScreen::new);
        event.register(AllMenuTypes.MODULAR_SMITHING.get(), ModularSmithingScreen::new);
    }
}
