package dev.lopyluna.slag;

import dev.lopyluna.slag.client.BlockPreviewRenderer;
import dev.lopyluna.slag.content.ponder.SlagPonderPlugin;
import net.createmod.catnip.api.client.gui.render.pip.GuiBlockEntityRenderState;
import net.createmod.catnip.api.client.gui.render.pip.GuiBlockModelRenderState;
import net.createmod.catnip.api.client.gui.render.pip.GuiFluidStateRenderState;
import net.createmod.catnip.impl.client.gui.element.pip.GuiBlockEntityRenderer;
import net.createmod.catnip.impl.client.gui.element.pip.GuiBlockModelRenderer;
import net.createmod.catnip.impl.client.gui.element.pip.GuiFluidStateRenderer;
import net.createmod.catnip.impl.neoforge.service.NeoForgeClientHooksHelper;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderState;
import net.createmod.ponder.impl.client.gui.PonderSceneRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

import java.util.function.Function;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@SuppressWarnings("UnstableApiUsage")
@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class SlagEmbersClient {
    public SlagEmbersClient(IEventBus modEventBus) {
        modEventBus.addListener(SlagEmbersClient::clientInit);
        modEventBus.addListener(SlagEmbersClient::loadComplete);
        modEventBus.addListener(SlagEmbersClient::registerPips);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new SlagPonderPlugin());
    }

    public static void registerPips(final RegisterPictureInPictureRenderersEvent event) {
        pip(event, GuiBlockModelRenderState.class, GuiBlockModelRenderer::new);
        pip(event, GuiBlockEntityRenderState.class, GuiBlockEntityRenderer::new);
        pip(event, GuiFluidStateRenderState.class, GuiFluidStateRenderer::new);
        pip(event, PonderSceneRenderState.class, PonderSceneRenderer::new);
        event.register(BlockPreviewRenderer.State.class, BlockPreviewRenderer::new);
    }

    private static <T extends PictureInPictureRenderState> void pip(RegisterPictureInPictureRenderersEvent event, Class<T> state, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory) {
        if (!NeoForgeClientHooksHelper.PIP_RENDERERS.containsKey(state)) event.register(state, factory);
    }

    public static void loadComplete(final FMLLoadCompleteEvent event) {
        event.enqueueWork(SlagEmbersClient::loadLateClientConfigs);
    }

    private static void loadLateClientConfigs() {
        if (ModConfigs.getConfigSet(ModConfig.Type.CLIENT).stream().allMatch(config -> config.getLoadedConfig() != null)) return;
        ConfigTracker.INSTANCE.unloadConfigs(ModConfig.Type.CLIENT);
        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
    }
}
