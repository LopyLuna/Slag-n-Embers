package dev.lopyluna.slag.content.items.dynamic_mold;

import dev.lopyluna.slag.client.render.SimpleCustomRenderer;
import dev.lopyluna.slag.config.SlagCommonConfigs;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class DynamicMoldItem extends Item {
    public final boolean cast;
    public final int uses;

    public DynamicMoldItem(Properties properties) {
        this(properties, false, 0);
    }

    public DynamicMoldItem(Properties properties, boolean cast, int uses) {
        super(properties);
        this.cast = cast;
        this.uses = uses;
    }

    public int used(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.USES.get(), 0);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return uses > 0 && used(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f - (float) used(stack) * 13f / uses);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0f, 1 - (float) used(stack) / uses) / 3f, 1, 1);
    }

    public boolean imprinting() {
        return !cast || (SlagCommonConfigs.SPEC.isLoaded() && SlagCommonConfigs.CAST_MOLD_IMPRINTING.get());
    }

    public boolean canImprint(Player player) {
        return imprinting() || player.isCreative();
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        var hasType = stack.has(AllDataComponents.CAST_TYPE);
        var empty = other.isEmpty();
        if (action != ClickAction.SECONDARY || !slot.allowModification(player) || other.getItem() instanceof DynamicMoldItem) return false;
        if (!canImprint(player)) return false;
        if (!hasType && empty) return false;
        var level = player.level();
        var random = level.getRandom();
        var b = true;
        if (empty) {
            stack.remove(AllDataComponents.CAST_TYPE);
            player.playSound(SoundEvents.DECORATED_POT_INSERT_FAIL, 0.8F, 0.8F + random.nextFloat() * 0.4F);
            player.playSound(SoundEvents.CRAFTER_CRAFT, 0.8F, 0.8F + random.nextFloat() * 0.4F);
            b = false;
        } else {
            var type = stack.get(AllDataComponents.CAST_TYPE);
            for (var tag : new ArrayList<>(other.getTags().toList())) if (tag.location().getPath().split("/")[0].equals("cast")) {
                if (type != null && type.equals(tag)) continue;
                stack.set(AllDataComponents.CAST_TYPE, tag);
                player.playSound(SoundEvents.DECORATED_POT_INSERT_FAIL, 0.8F, 0.8F + random.nextFloat() * 0.4F);
                player.playSound(SoundEvents.CRAFTER_CRAFT, 0.8F, 0.8F + random.nextFloat() * 0.4F);
                b = false;
                break;
            }
        }
        if (b) player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F, 0.8F + random.nextFloat() * 0.4F);
        return true;
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var use = super.use(level, player, usedHand);
        if (!player.isShiftKeyDown() || !canImprint(player)) return use;
        var stack = player.getItemInHand(usedHand);
        if (!stack.has(AllDataComponents.CAST_TYPE)) return use;
        stack.remove(AllDataComponents.CAST_TYPE);
        var random = level.getRandom();
        player.playSound(SoundEvents.DECORATED_POT_INSERT_FAIL, 0.8F, 0.8F + random.nextFloat() * 0.4F);
        player.playSound(SoundEvents.CRAFTER_CRAFT, 0.8F, 0.8F + random.nextFloat() * 0.4F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tooltip, flag);
        var used = used(stack);
        if (uses > 0 && used > 0 && flag.isAdvanced()) tooltip.add(Component.translatable("item.durability", uses - used, uses));
        if (!imprinting()) return;
        var key = stack.has(AllDataComponents.CAST_TYPE) ? "clear_imprint" : "imprint";
        tooltip.add(AllLangs.tr(key).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" ").append(AllLangs.tr(key + ".desc")).withStyle(ChatFormatting.BLUE));
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new DynamicMoldRenderer()));
    }
}
