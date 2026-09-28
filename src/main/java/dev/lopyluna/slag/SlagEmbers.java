package dev.lopyluna.slag;

import com.mojang.logging.LogUtils;
import dev.lopyluna.slag.compat.create.CreateCompat;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import dev.lopyluna.slag.config.SlagServerConfigs;
import dev.lopyluna.slag.content.EmbersDatagen;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.content.utils.EmbersRegistration;
import dev.lopyluna.slag.content.utils.ItemResult;
import dev.lopyluna.slag.content.utils.Registration;
import dev.lopyluna.slag.network.AllNetworks;
import dev.lopyluna.slag.register.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.slf4j.Logger;

import static dev.lopyluna.slag.register.AllCreativeTabs.BASE_TAB;

@SuppressWarnings("unused")
@Mod(SlagEmbers.MOD_ID)
public class SlagEmbers {
    public static final String NAME = "Slag n' Embers";
    public static final String MOD_ID = "slag";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static EmbersRegistration REGISTER = new EmbersRegistration(MOD_ID);
    public static Registration REG = new Registration(MOD_ID);

    public SlagEmbers(IEventBus modEventBus, ModContainer modContainer) {
        REGISTER.register(modEventBus);
        AllTraitEffects.register(modEventBus);
        AllCreativeTabs.register();
        REG.registerEventListeners(modEventBus);
        AllSoundEvents.prepare();
        REG.defaultCreativeTab(BASE_TAB, "base_tab");

        if (ModList.get().isLoaded("jei")) EmbersRecipesJEI.register();
        if (ModList.get().isLoaded("create")) CreateCompat.register(modEventBus);
        AllTags.addGenerators();
        AllDataComponents.register();
        AllTraits.register();
        AllMaterials.register();
        AllParts.register();
        AllModulars.register();
        AllItems.register();
        AllBlocks.register();
        AllBETypes.register();
        AllFluids.register();
        AllMenuTypes.register();
        AllRecipes.register();
        AllLangs.addTranslations();

        NeoForgeMod.enableMilkFluid();

        modEventBus.addListener(AllCreativeTabs::addCreative);
        modEventBus.addListener(AllNetworks::onRegisterPayloadHandlers);
        modEventBus.addListener(AllSoundEvents::register);
        modEventBus.addListener(EventPriority.HIGHEST, EmbersDatagen::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, EmbersDatagen::gatherData);
        modContainer.registerConfig(ModConfig.Type.COMMON, SlagCommonConfigs.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, SlagServerConfigs.SPEC);
        modEventBus.addListener(ModConfigEvent.Loading.class, event -> { if (event.getConfig().getSpec() == SlagServerConfigs.SPEC) ItemResult.invalidate(); });
        modEventBus.addListener(ModConfigEvent.Reloading.class, event -> { if (event.getConfig().getSpec() == SlagServerConfigs.SPEC) ItemResult.invalidate(); });

    }

    public static ResourceLocation loc(String loc) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, loc);
    }
    public static ResourceLocation loc(String modID, String loc) {
        return ResourceLocation.fromNamespaceAndPath(modID, loc);
    }
    public static ResourceLocation locMC(String loc) {
        return ResourceLocation.withDefaultNamespace(loc);
    }
    public static ResourceLocation empty() {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "empty");
    }
}
