package dev.lopyluna.slag.content.blocks.table;

import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.casting.CastingInventory;
import dev.lopyluna.slag.content.items.dynamic_mold.DynamicMoldItem;
import dev.lopyluna.slag.content.utils.FluidInput;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllRecipes;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.*;

public class TableBE extends CastingBE {
    public static final int MOLD = 1;
    public static final float COOLING_RATE = 0.25f;
    private static final Map<Fluid, Map<TagKey<Item>, TableCastingRecipe>> handlers = new IdentityHashMap<>();
    private static final List<TableCastingRecipe> itemHandlers = new ArrayList<>();
    private static Collection<RecipeHolder<?>> handlerSource;

    public ItemStack moldBase = ItemStack.EMPTY;
    public ItemStack moldCutout = ItemStack.EMPTY;

    public TableBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 250);
        maxLight = 7;
        coolingRate = COOLING_RATE;
        chaseSpeed = 0.75f;
        lazyChaseSpeed = 0.25f;
    }

    @Override
    protected CastingInventory createInventory() {
        return new TableInventory(this);
    }

    @Override
    protected @Nullable Handler findHandler(Fluid fluid) {
        if (level == null) return null;
        refreshHandlers();
        var cast = itemInventory.getItem(MOLD).get(AllDataComponents.CAST_TYPE);
        var placed = itemInventory.getItem(RESULT);
        if (!placed.isEmpty()) {
            for (var recipe : itemHandlers) {
                var castItem = recipe.getCastItem();
                if (castItem == null || !FluidInput.test(recipe.getInput(), fluid) || !castItem.matches(placed)) continue;
                if (!Objects.equals(recipe.getCastType(), cast)) continue;
                return handler(recipe.getInput().amount(), recipe.getDuration(), recipe.getSpeed(), recipe.getResult(), castItem);
            }
            return null;
        }
        var casts = handlers.get(fluid);
        var recipe = casts == null ? null : casts.get(cast);
        if (recipe == null || recipe.getOutput().isEmpty()) return null;
        return handler(recipe.getInput().amount(), recipe.getDuration(), recipe.getSpeed(), recipe.getResult(), null);
    }

    @Override
    public boolean isCastItem(ItemStack stack) {
        if (level == null || stack.isEmpty()) return false;
        refreshHandlers();
        for (var recipe : itemHandlers) {
            var castItem = recipe.getCastItem();
            if (castItem != null && castItem.matches(stack)) return true;
        }
        return false;
    }

    private void refreshHandlers() {
        if (level == null) return;
        var manager = level.getRecipeManager();
        if (manager.getRecipes() == handlerSource) return;
        handlerSource = manager.getRecipes();
        handlers.clear();
        itemHandlers.clear();
        for (var holder : manager.getAllRecipesFor(AllRecipes.TABLE_CASTING.get())) {
            var recipe = holder.value();
            if (recipe.getCastItem() != null) itemHandlers.add(recipe);
            else for (var fluid : recipe.getInput().getFluids()) {
                var casts = handlers.computeIfAbsent(fluid.getFluid(), f -> new HashMap<>());
                var known = casts.get(recipe.getCastType());
                if (FluidInput.narrower(recipe.getInput(), known == null ? null : known.getInput())) casts.put(recipe.getCastType(), recipe);
            }
        }
    }

    @Override
    protected void store(ItemStack stack) {
        if (stack.getItem() instanceof DynamicMoldItem && stack.has(AllDataComponents.CAST_TYPE) && itemInventory.getItem(MOLD).isEmpty()) itemInventory.setItem(MOLD, stack);
        else super.store(stack);
    }

    @Override
    protected void onCast() {
        var mold = itemInventory.getItem(MOLD);
        if (mold.isEmpty()) return;
        if (mold.is(AllTags.MOLDS_SINGLE)) {
            itemInventory.setItem(MOLD, ItemStack.EMPTY);
            return;
        }
        if (!(mold.getItem() instanceof DynamicMoldItem item) || item.uses <= 0) return;
        var used = item.used(mold) + 1;
        if (used < item.uses) {
            mold.set(AllDataComponents.USES, used);
            itemInventory.setItem(MOLD, mold);
            return;
        }
        itemInventory.setItem(MOLD, ItemStack.EMPTY);
        if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.6f, 1f);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        refreshMold();
    }

    public void refreshMold() {
        var mold = itemInventory.getItem(MOLD);
        if (mold.isEmpty()) {
            moldBase = ItemStack.EMPTY;
            moldCutout = ItemStack.EMPTY;
            return;
        }
        moldBase = mold.getItem().getDefaultInstance();
        moldCutout = mold.copy();
        moldBase.set(AllDataComponents.CUTOUT, Unit.INSTANCE);
        moldCutout.set(AllDataComponents.CUTOUT, Unit.INSTANCE);
    }
}
