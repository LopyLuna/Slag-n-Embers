package dev.lopyluna.slag.content.items.modular;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.RegistrateDistExecutor;
import dev.lopyluna.slag.client.ClientTooltips;
import dev.lopyluna.slag.client.render.SimpleCustomRenderer;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.dynamic_part.IModularItem;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllLangs;
import net.bettercombat.api.component.BetterCombatDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class ModularItem extends Item implements IModularItem {
    public ModularItem(Properties properties) {
        super(properties.durability(924).stacksTo(1).component(AllDataComponents.DYNAMIC_PARTS, DataDynamicParts.EMPTY));
    }

    public boolean hasModularType(ItemStack stack) {
        return stack.has(AllDataComponents.MODULAR_TYPE);
    }

    public ModularType getModularType(ItemStack stack) {
        var loc = stack.get(AllDataComponents.MODULAR_TYPE);
        if (loc == null) return null;
        return AllDynamicTypes.getModular(loc).orElse(null);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var pass = super.use(level, player, usedHand);
        var stack = player.getItemInHand(usedHand);
        if (stack.getCount() != 1 || stack.has(AllDataComponents.MODULAR_TYPE)) return pass;
        if (player.isShiftKeyDown()) {
            var parts = getParts(stack);
            if (parts == null) return pass;
            var modularType = getModularTypeFromParts(parts);
            if (modularType == null) return pass;
            var result = modularType.getResultStack();
            if (!result.isEmpty()) {
                playBuildSound(player, null);
                return InteractionResultHolder.success(result);
            }

            var typeID = modularType.id;
            var toolParts = parts.itemsCopy();
            var fireImmune = false;
            for (var part : toolParts) {
                if (!fireImmune && part.getItem() instanceof IDynamicPart p) fireImmune = p.isFireImmune(part);
                part.set(AllDataComponents.BUILT, typeID);
            }
            stack.set(AllDataComponents.MODULAR_TYPE, typeID);
            if (ModList.get().isLoaded("bettercombat") && modularType.betterCombatPreset != null && modularType.betterCombatPreset.isPresent()) {
                stack.set(BetterCombatDataComponents.WEAPON_PRESET_ID, modularType.betterCombatPreset.get());
            }
            if (fireImmune) stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
            setParts(stack, toolParts);
            playBuildSound(player, null);
            return InteractionResultHolder.success(stack);
        }
        return pass;
    }

    public boolean containsStack(List<ItemStack> stacks, ItemStack stack) {
        if (stack.isEmpty()) return true;
        for (var part : stacks) if (stack.is(part.getItem()) && stack.getCount() >= part.getCount()) return true;
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack print, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (print.getCount() != 1 || print.has(AllDataComponents.MODULAR_TYPE)) return false;
        if (slot.allowModification(player)) {
            var parts = getParts(print);
            var bool = false;
            if (other.isEmpty() && parts.isEmpty()) return false;
            var copyParts = parts.itemsCopy();
            if (copyParts == null) return false;
            var possibleParts = parts.getPossibleParts();
            if (other.getItem() instanceof IDynamicPart part) {
                var possible = parts.getPossibleTags(possibleParts);
                if (!possible.contains(part.getPartSegment(other))) return false;
                playInsertSound(player);
                copyParts.add(other.copyWithCount(1));
                other.shrink(1);
                bool = true;
            } else if (!other.isEmpty()) {
                var possible = parts.getPossibleStacks(possibleParts);
                if (!containsStack(possible, other)) return false;
                playInsertSound(player);
                if (action == ClickAction.PRIMARY) {
                    var count = parts.getLargestPossibleCount(other, possible);
                    if (count == 0) return false;
                    copyParts.add(other.copyWithCount(count));
                    other.shrink(count);
                } else {
                    copyParts.add(other.copyWithCount(1));
                    other.shrink(1);
                }
                bool = true;
            } else if (action == ClickAction.PRIMARY) {
                var stack = copyParts.getFirst();
                playRemoveOneSound(player);
                access.set(stack);
                copyParts.removeFirst();
                bool = true;
            }
            if (bool) {
                setParts(print, copyParts);
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tooltip, flag);

        if (!hasModularType(stack)) {
            var parts = getParts(stack);
            if (parts == null) return;

            var modularType = getModularTypeFromParts(parts);
            if (modularType != null) {
                var count = modularType.getResultStack().getCount();
                tooltip.add(Component.literal(RegistrateLangProvider.toEnglishName(modularType.id.getPath())).append(count > 1 ? " x" + count : "").withStyle(ChatFormatting.YELLOW));
                tooltip.add(AllLangs.trArgs("construct", AllLangs.tr("shift"), AllLangs.tr("rmb")).withStyle(ChatFormatting.GRAY));
            }

            var copyParts = parts.itemsCopy();
            if (copyParts == null || copyParts.isEmpty()) return;
            var possibleModulars = parts.getPossibleModulars();
            if (modularType == null) {
                if (!possibleModulars.isEmpty()) tooltip.add(Component.literal("Possible Items:").withStyle(ChatFormatting.GRAY));
                for (var modular : possibleModulars) tooltip.add(Component.literal(" ").append(RegistrateLangProvider.toEnglishName(modular.id.getPath())).withStyle(ChatFormatting.GRAY));
            }
            var possibleParts = parts.getPossibleParts();
            if (!possibleParts.isEmpty() && !possibleModulars.isEmpty()) tooltip.add(Component.literal(" "));
            if (!possibleParts.isEmpty()) tooltip.add(Component.literal("Possible Parts:").withStyle(ChatFormatting.GRAY));
            for (var part : possibleParts) {
                if (part instanceof ItemStack partStack) tooltip.add(Component.literal(" ").append(partStack.getHoverName()).append(" x" + partStack.getCount()).withStyle(ChatFormatting.GRAY));
                if (part instanceof TagKey<?> partTag) tooltip.add(Component.literal(" " + RegistrateLangProvider.toEnglishName(Arrays.stream(partTag.location().toString().split("/")).toList().getLast())).withStyle(ChatFormatting.GRAY));
            }
        }
        Level level = ctx.level();
        if (level == null) return;
        if (level.isClientSide()) RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTooltips.appendHoverTextModularTool(this, stack, ctx, tooltip, flag));
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new ModularItemRenderer()));
    }

    private void playFailSound(Entity entity) {
        entity.playSound(SoundEvents.CRAFTER_FAIL, 1.25F, 0.5F + entity.level().getRandom().nextFloat() * 0.4F);
        entity.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5F, 0.5F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.DECORATED_POT_INSERT, 0.8F, 0.5F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.DECORATED_POT_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    @SuppressWarnings("SameParameterValue")
    private void playBuildSound(Entity entity, @Nullable SoundEvent event) {
        entity.playSound(event != null ? event : SoundEvents.CRAFTER_CRAFT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
        var id = super.getDescriptionId(stack);
        var modularType = stack.get(AllDataComponents.MODULAR_TYPE);
        if (modularType == null) return id;
        var parts = getParts(stack);
        if (parts == null) return id;
        var materialTypes = getMaterialTypes(parts);
        if (materialTypes.isEmpty()) return "item." + modularType.toString().replace(":", ".");
        StringBuilder materials = new StringBuilder();
        for (var material : materialTypes) materials.append(material.id.getPath()).append("_");
        return "item." + modularType.getNamespace() + "." + materials + modularType.getPath();
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        var id = getDescriptionId(stack);
        var name = id.split("\\.")[2];
        return Component.translatableWithFallback(id, RegistrateLangProvider.toEnglishName(name));
    }


    public boolean isTool(ItemStack stack) {
        var modularType = getModularType(stack);
        return modularType != null && modularType.actions.contains("isTool");
    }

    public boolean isArmor(ItemStack stack) {
        var modularType = getModularType(stack);
        return modularType != null && modularType.actions.contains("isArmor");
    }
}
