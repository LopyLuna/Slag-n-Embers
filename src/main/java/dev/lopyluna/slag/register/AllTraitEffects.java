package dev.lopyluna.slag.register;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.traits.TraitEffect;
import dev.lopyluna.slag.content.traits.effects.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class AllTraitEffects {
    public static final ResourceKey<Registry<MapCodec<? extends TraitEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(SlagEmbers.loc("trait_effect_type"));
    private static final DeferredRegister<MapCodec<? extends TraitEffect>> EFFECTS = DeferredRegister.create(REGISTRY_KEY, SlagEmbers.MOD_ID);
    public static final Registry<MapCodec<? extends TraitEffect>> REGISTRY = EFFECTS.makeRegistry(builder -> {});

    public static final Holder<AttributeEffect> ATTRIBUTE = register("attribute", () -> AttributeEffect.CODEC);
    public static final Holder<StatEffect> STAT = register("stat", () -> StatEffect.CODEC);
    public static final Holder<FlagEffect> FLAG = register("flag", () -> FlagEffect.CODEC);
    public static final Holder<MiningEffect> MINING = register("mining", () -> MiningEffect.CODEC);
    public static final Holder<AbilitiesEffect> ABILITIES = register("abilities", () -> AbilitiesEffect.CODEC);
    public static final Holder<BlockActionEffect> BLOCK_ACTION = register("block_action", () -> BlockActionEffect.CODEC);
    public static final Holder<EquipmentSlotEffect> EQUIPMENT_SLOT = register("equipment_slot", () -> EquipmentSlotEffect.CODEC);
    public static final Holder<ComponentsEffect> COMPONENTS = register("components", () -> ComponentsEffect.CODEC);
    public static final Holder<DamageImmunityEffect> DAMAGE_IMMUNITY = register("damage_immunity", () -> DamageImmunityEffect.CODEC);
    public static final Holder<ItemTagsEffect> ITEM_TAGS = register("item_tags", () -> ItemTagsEffect.CODEC);
    public static final Holder<ChainBreakEffect> CHAIN_BREAK = register("chain_break", () -> ChainBreakEffect.CODEC);
    public static final Holder<AutoSmeltEffect> AUTO_SMELT = register("auto_smelt", () -> AutoSmeltEffect.CODEC);

    private static <T extends TraitEffect> Holder<T> register(String name, Supplier<MapCodec<T>> codec) {
        return new Holder<>(EFFECTS.register(name, codec));
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }

    public record Holder<A extends TraitEffect>(DeferredHolder<MapCodec<? extends TraitEffect>, MapCodec<A>> holder) implements Supplier<MapCodec<A>> {
        @Override public MapCodec<A> get() { return holder.get(); }
    }
}
