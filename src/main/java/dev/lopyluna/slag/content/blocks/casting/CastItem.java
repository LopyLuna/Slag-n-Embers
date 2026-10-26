package dev.lopyluna.slag.content.blocks.casting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import javax.annotation.Nonnull;
import java.util.Optional;

public record CastItem(Ingredient input, Mode mode, Optional<ItemStackTemplate> replacing, int damage, boolean imprint, boolean fireproof) {
    public static final Codec<CastItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CastItem::input),
            Mode.CODEC.optionalFieldOf("mode", Mode.CONSUME).forGetter(CastItem::mode),
            ItemStackTemplate.CODEC.optionalFieldOf("replacement").forGetter(CastItem::replacing),
            Codec.INT.optionalFieldOf("damage", 0).forGetter(CastItem::damage),
            Codec.BOOL.optionalFieldOf("imprint", false).forGetter(CastItem::imprint),
            Codec.BOOL.optionalFieldOf("keep_fireproof", false).forGetter(CastItem::fireproof)
    ).apply(instance, CastItem::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastItem> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CastItem::input,
            Mode.STREAM_CODEC, CastItem::mode,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), CastItem::replacing,
            ByteBufCodecs.VAR_INT, CastItem::damage,
            ByteBufCodecs.BOOL, CastItem::imprint,
            ByteBufCodecs.BOOL, CastItem::fireproof,
            CastItem::new);

    public static CastItem of(Ingredient input, Mode mode) {
        return new CastItem(input, mode, Optional.empty(), 0, false, false);
    }

    public static CastItem imprint(Ingredient input) {
        return new CastItem(input, Mode.CONSUME, Optional.empty(), 0, true, true);
    }

    public ItemStack replacement() {
        return replacing.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    public Component describe() {
        return switch (mode) {
            case CONSUME -> fireproof
                    ? Component.translatableWithFallback("gui.slag.cast_item.consume_fireproof", "Consumed unless fire resistant")
                    : Component.translatableWithFallback("gui.slag.cast_item.consume", "Consumed when cast");
            case KEEP -> damage > 0
                    ? Component.translatableWithFallback("gui.slag.cast_item.keep_damage", "Reusable, loses %s durability", damage)
                    : Component.translatableWithFallback("gui.slag.cast_item.keep", "Reusable");
            case REPLACE -> Component.translatableWithFallback("gui.slag.cast_item.replace", "Turns into %s", replacement().getHoverName());
        };
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && input.test(stack);
    }

    public ItemStack leftover(ItemStack stack) {
        if (fireproof && stack.has(DataComponents.DAMAGE_RESISTANT)) return stack.copyWithCount(1);
        return switch (mode) {
            case CONSUME -> ItemStack.EMPTY;
            case REPLACE -> replacement();
            case KEEP -> {
                var kept = stack.copyWithCount(1);
                if (damage <= 0 || !kept.isDamageableItem()) yield kept;
                var damaged = kept.getDamageValue() + damage;
                if (damaged >= kept.getMaxDamage()) yield ItemStack.EMPTY;
                kept.setDamageValue(damaged);
                yield kept;
            }
        };
    }

    public enum Mode implements StringRepresentable {
        CONSUME("consume"), KEEP("keep"), REPLACE("replace");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        public static final StreamCodec<RegistryFriendlyByteBuf, Mode> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> values()[i], Mode::ordinal).cast();

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public @Nonnull String getSerializedName() {
            return name;
        }
    }
}
