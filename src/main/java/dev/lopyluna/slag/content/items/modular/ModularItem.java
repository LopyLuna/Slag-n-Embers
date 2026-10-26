package dev.lopyluna.slag.content.items.modular;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.RegistrateDistExecutor;
import dev.lopyluna.slag.client.ClientTooltips;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.items.dynamic_part.IModularItem;
import dev.lopyluna.slag.content.traits.Traits;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllLangs;
import dev.lopyluna.slag.register.AllTraits;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "NullableProblems", "deprecation"})
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

    public boolean containsStack(List<ItemStack> stacks, ItemStack stack) {
        if (stack.isEmpty()) return true;
        for (var part : stacks) if (stack.is(part.getItem()) && stack.getCount() >= part.getCount()) return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        var tooltip = new ArrayList<Component>();
        appendHoverText(stack, ctx, tooltip, flag);
        tooltip.forEach(builder);
    }

    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        if (!hasModularType(stack)) {
            tooltip.add(AllLangs.tr("modular_template").withStyle(ChatFormatting.GRAY));
            var parts = getParts(stack);
            if (parts == null) return;

            var modularType = getModularTypeFromParts(parts);
            if (modularType != null) {
                var count = modularType.getResultStack().getCount();
                tooltip.add(modularType.getName().copy().append(count > 1 ? " x" + count : "").withStyle(ChatFormatting.YELLOW));
            }

            var copyParts = parts.itemsCopy();
            if (copyParts == null || copyParts.isEmpty()) return;
            var possibleModulars = parts.getPossibleModulars();
            if (modularType == null) {
                if (!possibleModulars.isEmpty()) tooltip.add(AllLangs.tr("modular_possible").append(":").withStyle(ChatFormatting.GRAY));
                for (var modular : possibleModulars) tooltip.add(Component.literal(" ").append(modular.getName()).withStyle(ChatFormatting.GRAY));
            }
            var possibleParts = parts.getPossibleParts();
            if (!possibleParts.isEmpty() && !possibleModulars.isEmpty()) tooltip.add(Component.literal(" "));
            if (!possibleParts.isEmpty()) tooltip.add(AllLangs.tr("modular_possible_parts").append(":").withStyle(ChatFormatting.GRAY));
            for (var part : possibleParts) {
                if (part instanceof ItemStack partStack) tooltip.add(Component.literal(" ").append(partStack.getHoverName()).append(" x" + partStack.getCount()).withStyle(ChatFormatting.GRAY));
                if (part instanceof TagKey<?> partTag) tooltip.add(Component.literal(" ").append(segmentName(partTag)).withStyle(ChatFormatting.GRAY));
            }
        }
        Level level = ctx.level();
        if (level == null) return;
        if (level.isClientSide()) RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTooltips.appendHoverTextModularTool(this, stack, ctx, tooltip, flag));
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
    public @Nonnull Component getName(ItemStack stack) {
        var id = stack.get(AllDataComponents.MODULAR_TYPE);
        var parts = getParts(stack);
        if (id == null || parts == null) return super.getName(stack);
        var name = ModularType.name(id);
        var materials = getMaterialTypes(parts);
        if (materials.isEmpty()) return name;
        var mats = materials.getFirst().getName();
        for (var i = 1; i < materials.size(); i++) mats = Component.translatable("item.slag.material_pair", mats, materials.get(i).getName());
        return Component.translatable("item.slag.modular_name", mats, name);
    }


    public boolean isTool(ItemStack stack) {
        return Traits.of(stack).tool;
    }

    public boolean isArmor(ItemStack stack) {
        return Traits.of(stack).armor;
    }

    private static Component segmentName(TagKey<?> tag) {
        for (var part : AllDynamicTypes.getAllParts()) if (part.segmentPart.location().equals(tag.location())) return part.getName();
        return Component.literal(RegistrateLangProvider.toEnglishName(Arrays.stream(tag.location().toString().split("/")).toList().getLast()));
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        if (!(user instanceof Player player) || !player.isCreative()) return true;
        var traits = Traits.of(player.getMainHandItem());
        if (traits.has(AllTraits.KNIFE_MINING)) return false;
        return !traits.has(AllTraits.SWORD_MINING) || traits.has(AllTraits.PICKAXE_MINING) || traits.has(AllTraits.AXE_MINING) || traits.has(AllTraits.SHOVEL_MINING) || traits.has(AllTraits.HOE_MINING);
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return !Traits.of(stack).immuneTo(source);
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return Traits.of(stack).piglinNeutral;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        var traits = Traits.of(stack);
        if (traits.tool != stack.has(DataComponents.WEAPON)) traits.applyComponents(stack);
        for (var hook : traits.inventoryTick) hook.effect().inventoryTick(hook.trait(), stack, level, entity, slot);
    }

    public void applyComponents(ItemStack stack, Traits traits) {
        if ((traits.tool || traits.armor) && traits.enchantability >= 1) stack.set(DataComponents.ENCHANTABLE, new Enchantable(Math.round(traits.enchantability)));
        else stack.remove(DataComponents.ENCHANTABLE);
        if (traits.tool) stack.set(DataComponents.WEAPON, new Weapon(1, traits.disablesShield ? 5f : 0f));
        else stack.remove(DataComponents.WEAPON);
        if (traits.gliding) stack.set(DataComponents.GLIDER, Unit.INSTANCE);
        else stack.remove(DataComponents.GLIDER);
        var repair = new ArrayList<Holder<Item>>();
        if (traits.tool || traits.armor) for (var pair : getDynamicParts(stack)) pair.getSecond().getMaterialType(pair.getFirst()).ifPresent(material -> {
            var ingredient = material.repairMaterials.get();
            if (ingredient != null) ingredient.items().forEach(repair::add);
        });
        if (repair.isEmpty()) stack.remove(DataComponents.REPAIRABLE);
        else stack.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(repair)));
        if (traits.immunities.isEmpty()) stack.remove(DataComponents.DAMAGE_RESISTANT);
        else AllUtils.registry(Registries.DAMAGE_TYPE).ifPresent(registry -> {
            var types = new ArrayList<Holder<DamageType>>();
            for (var tag : traits.immunities) registry.get(tag).ifPresent(set -> set.forEach(types::add));
            stack.set(DataComponents.DAMAGE_RESISTANT, new DamageResistant(HolderSet.direct(types)));
        });
    }
}
