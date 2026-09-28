package dev.lopyluna.slag.content.types;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.traits.TraitEntry;
import dev.lopyluna.slag.content.traits.TraitType;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MaterialType {
    public final ResourceLocation id;
    public int sortOrder;

    public final List<TraitEntry> traits;
    public final Incompatible incompatible;
    public final List<ICondition> conditions;

    public final String texture;
    public final Supplier<Ingredient> repairMaterials;
    public final Supplier<Fluid> moltenFluid;
    public final List<Integer> palette;

    public boolean dontRegister;

    private static final Codec<Integer> COLOR_CODEC = Codec.either(Codec.STRING.comapFlatMap(str -> { try {
        var hexStr = str.startsWith("0x") || str.startsWith("0X") ? str.substring(2) : str;
        int color = Integer.parseUnsignedInt(hexStr, 16);
        return DataResult.success(color);
    } catch (NumberFormatException e) {
        return DataResult.error(() -> "Invalid hex color: " + str);
    }}, color -> String.format("0x%06X", color)), Codec.INT).xmap(either -> either.map(i -> i, i -> i), Either::right);

    public static final Codec<MaterialType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(m -> m.id),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(m -> m.sortOrder),
            TraitEntry.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(m -> m.traits),
            Incompatible.CODEC.optionalFieldOf("incompatible", Incompatible.EMPTY).forGetter(m -> m.incompatible),
            ICondition.LIST_CODEC.optionalFieldOf("conditions", List.of()).forGetter(m -> m.conditions),
            Codec.STRING.optionalFieldOf("texture", "base").forGetter(m -> m.texture),
            Ingredient.CODEC.optionalFieldOf("repair_ingredient", Ingredient.EMPTY).forGetter(m -> m.repairMaterials.get()),
            ResourceLocation.CODEC.optionalFieldOf("molten_fluid").forGetter(m -> {
                Fluid fluid = m.moltenFluid.get();
                if (fluid == null) return Optional.empty();
                return Optional.of(BuiltInRegistries.FLUID.getKey(fluid));
            }),
            COLOR_CODEC.listOf().optionalFieldOf("palette", List.of()).forGetter(m -> m.palette))

            .apply(instance, MaterialType::new));

    @Override
    public int hashCode() {
        var fluidHash = 0;
        if (moltenFluid != null && moltenFluid.get() != null) fluidHash = moltenFluid.get().hashCode();
        var repairHash = 0;
        if (repairMaterials != null && !repairMaterials.get().isEmpty() && repairMaterials.get() != null) repairHash = repairMaterials.get().hashCode();
        return id.hashCode() + texture.hashCode() + repairHash + fluidHash + palette.hashCode() + traits.hashCode() + incompatible.hashCode() + conditions.hashCode() + Objects.hash(sortOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MaterialType other)) return false;
        if (!Objects.equals(other.texture, texture)) return false;
        if (!Objects.equals(other.repairMaterials.get(), repairMaterials.get())) return false;
        if (other.moltenFluid.get() != moltenFluid.get()) return false;
        if (!other.traits.equals(traits)) return false;
        if (!other.incompatible.equals(incompatible)) return false;
        if (!other.conditions.equals(conditions)) return false;
        if (!other.palette.equals(palette)) return false;
        if (other.sortOrder != sortOrder) return false;
        return other.id.equals(id);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public MaterialType(ResourceLocation id, int sortOrder, List<TraitEntry> traits, Incompatible incompatible, List<ICondition> conditions, String texture, Ingredient repairIngredient, Optional<ResourceLocation> moltenFluidId, List<Integer> palette) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.sortOrder = sortOrder;

        this.traits = traits;
        this.incompatible = incompatible;
        this.conditions = conditions;

        this.texture = texture;
        this.repairMaterials = () -> repairIngredient;
        this.moltenFluid = () -> moltenFluidId.map(BuiltInRegistries.FLUID::get).orElse(null);
        this.palette = palette;
    }

    public MaterialType(ResourceLocation id, int sortOrder, List<TraitEntry> traits, Incompatible incompatible, List<ICondition> conditions, String texture, Supplier<Ingredient> repairIngredient, Supplier<Fluid> moltenFluid, List<Integer> palette) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.sortOrder = sortOrder;

        this.traits = traits;
        this.incompatible = incompatible;
        this.conditions = conditions;

        this.texture = texture;
        this.repairMaterials = repairIngredient;
        this.moltenFluid = moltenFluid;
        this.palette = palette;
    }

    public Component getName() {
        return Component.translatableWithFallback("material." + id.getNamespace() + "." + id.getPath(), RegistrateLangProvider.toEnglishName(id.getPath()));
    }

    public boolean hasTrait(TraitType trait) {
        for (var entry : traits) if (entry.trait().equals(trait.id)) return true;
        return false;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private final List<TraitEntry> traits = new ArrayList<>();
        private final List<ICondition> conditions = new ArrayList<>();
        private final Incompatible.Builder incompatible = new Incompatible.Builder();

        private final Supplier<Ingredient> repair;
        private Supplier<Fluid> moltenFluid = () -> null;
        private List<Integer> palette = new ArrayList<>();

        private String texture = "base";
        private final ResourceLocation id;
        private int sortOrder = 0;

        public Builder(ResourceLocation id, Supplier<Ingredient> repairMaterial) {
            this.id = id;
            repair = repairMaterial;
        }
        public Builder(String id, Supplier<Ingredient> repairMaterial) {
            this.id = SlagEmbers.loc(id);
            repair = repairMaterial;
        }

        public Builder setSortOrder(int value) { sortOrder = value; return this; }

        public Builder trait(TraitEntry entry) { traits.add(entry); return this; }
        public Builder trait(TraitType trait) { return trait(TraitEntry.of(trait)); }
        public Builder trait(TraitType trait, float value) { return trait(TraitEntry.of(trait, value)); }
        public Builder multiply(TraitType trait, float value) { return trait(TraitEntry.multiply(trait, value)); }

        public Builder condition(ICondition... conditions) { this.conditions.addAll(List.of(conditions)); return this; }
        public Builder modLoaded(String modId) { return condition(new ModLoadedCondition(modId)); }
        public Builder requiresTag(TagKey<Item> tag) { return condition(AllTags.present(tag)); }
        public Builder incompatible(Consumer<Incompatible.Builder> builder) { builder.accept(incompatible); return this; }

        public Builder setTexture(String texture) { this.texture = texture; return this; }
        public Builder moltenFluid(Supplier<Fluid> moltenFluid) { this.moltenFluid = moltenFluid; return this; }
        public Builder palette(List<Integer> palette) { this.palette = palette; return this; }

        public Builder apply(Function<Builder, Builder> func) {
            return func.apply(this);
        }

        public MaterialType register() {
            return new MaterialType(id, sortOrder, List.copyOf(traits), incompatible.build(), List.copyOf(conditions), texture, repair, moltenFluid, palette);
        }
    }
}
