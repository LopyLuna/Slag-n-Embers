package dev.lopyluna.slag.content.utils;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static dev.lopyluna.slag.SlagEmbers.LOGGER;
import static dev.lopyluna.slag.SlagEmbers.MOD_ID;
import static net.minecraft.core.registries.Registries.*;

@SuppressWarnings({"unused", "removal"})
public record EmbersRegistration(String modID) {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(MOD_ID);
    public static final DeferredRegister<DamageType> DAMAGES = DeferredRegister.create(DAMAGE_TYPE, MOD_ID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(MOB_EFFECT, MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(PARTICLE_TYPE, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(RECIPE_SERIALIZER, MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(RECIPE_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(MENU, MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(SOUND_EVENT, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredRegister<Level> DIMENSIONS = DeferredRegister.create(DIMENSION, MOD_ID);
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);

    public DeferredRegister.DataComponents components() {return DATA_COMPONENTS;}
    public DeferredRegister<CreativeModeTab> creativeTab() {return CREATIVE_MODE_TABS;}
    public DeferredRegister<DamageType> damages() {return DAMAGES;}
    public DeferredRegister<MobEffect> mobEffects() {return MOB_EFFECTS;}
    public DeferredRegister<ParticleType<?>> particles() {return PARTICLES;}
    public DeferredRegister<RecipeSerializer<?>> recipeSer() {return RECIPE_SERIALIZERS;}
    public DeferredRegister<RecipeType<?>> recipes() {return RECIPES;}
    public DeferredRegister<MenuType<?>> menus() {return MENUS;}
    public DeferredRegister<SoundEvent> sounds() {return SOUNDS;}
    public DeferredRegister<Level> dimensions() {return DIMENSIONS;}
    public DeferredRegister<MapCodec<? extends ICondition>> conditions() {return CONDITIONS;}

    public void register(IEventBus bus) {
        LOGGER.info("Registering " + SlagEmbers.NAME + " Data Components...");
        DATA_COMPONENTS.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Creative Tabs...");
        CREATIVE_MODE_TABS.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Damage Types...");
        DAMAGES.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Mob Effects...");
        MOB_EFFECTS.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Particles...");
        PARTICLES.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Recipes Serializers...");
        RECIPE_SERIALIZERS.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Recipes...");
        RECIPES.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Menus...");
        MENUS.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Sounds...");
        SOUNDS.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Dimensions...");
        DIMENSIONS.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Conditions...");
        CONDITIONS.register(bus);
        LOGGER.info("Registering " + SlagEmbers.NAME + " Done");
    }
}