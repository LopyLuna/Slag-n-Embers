package dev.lopyluna.slag.content.blocks.forge;

import dev.lopyluna.slag.content.blocks.forge.client.ForgeMenu;
import dev.lopyluna.slag.register.AllRecipes;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "NullableProblems"})
@ParametersAreNonnullByDefault
public class ForgeBE extends BaseContainerBlockEntity implements RecipeCraftingHolder, WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final Object2IntOpenHashMap<ResourceLocation> recipesUsed;
    private final RecipeManager.CachedCheck<DoubleSmeltingRecipe.DoubleRecipeInput, DoubleSmeltingRecipe> quickCheck;
    private int litTime;
    private int litDuration;
    private int cookingProgress;
    private int cookingTotalTime;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> litTime;
                case 1 -> litDuration;
                case 2 -> cookingProgress;
                case 3 -> cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int i, int j) {
            switch (i) {
                case 0 -> litTime = j;
                case 1 -> litDuration = j;
                case 2 -> cookingProgress = j;
                case 3 -> cookingTotalTime = j;
            }
        }
        @Override
        public int getCount() {
            return 4;
        }
    };

    public ForgeBE(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
        super(type, blockPos, blockState);
        this.recipesUsed = new Object2IntOpenHashMap<>();
        this.quickCheck = RecipeManager.createCheck(AllRecipes.DOUBLE_SMELTING.get());
    }

    @Override
    public @Nonnull Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected @Nonnull Component getDefaultName() {
        return getDisplayName();
    }

    @Override
    protected @Nonnull AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
        return new ForgeMenu(syncId, inventory, this, data);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    public @Nonnull NonNullList<ItemStack> getItems() {
        return items;
    }
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, items, provider);
        litTime = nbt.getInt("LitTime");
        cookingProgress = nbt.getInt("CookingProgress");
        cookingTotalTime = nbt.getInt("CookingTotalTime");
        litDuration = this.getBurnDuration(this.items.get(2));
        var tag = nbt.getCompound("RecipesUsed");
        for (var s : tag.getAllKeys()) this.recipesUsed.put(ResourceLocation.parse(s), tag.getInt(s));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        nbt.putInt("LitTime", litTime);
        nbt.putInt("CookingProgress", cookingProgress);
        nbt.putInt("CookingTotalTime", cookingTotalTime);
        ContainerHelper.saveAllItems(nbt, items, provider);
        var tag = new CompoundTag();
        this.recipesUsed.forEach((loc, i) -> tag.putInt(loc.toString(), i));
        nbt.put("RecipesUsed", tag);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        assert this.level != null;
        var slotStack = this.items.get(slot);
        boolean flag = !stack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, stack);
        items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        if (slot <= 1 && !flag) {
            this.cookingTotalTime = getTotalCookTime(this.level, this);
            this.cookingProgress = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 3) return false;
        if (index != 2) return true;
        return getBurnTime(stack) > 0 || stack.is(Items.BUCKET) && !this.items.get(2).is(Items.BUCKET);
    }

    public void fillStackedContents(StackedContents helper) {
        for (var stack : this.items) helper.accountStack(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public boolean isLit() {
        return litTime > 0;
    }

    protected int getBurnDuration(ItemStack fuel) {
        return fuel.isEmpty() ? 0 : fuel.getBurnTime(AllRecipes.DOUBLE_SMELTING.get());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ForgeBE be) {
        boolean flag = be.isLit();
        boolean flag1 = false;
        if (be.isLit()) --be.litTime;

        var fuel = be.items.get(2);
        var stackA = be.items.get(0);
        var stackB = be.items.get(1);

        var flag2 = !stackA.isEmpty() && !stackB.isEmpty();
        var flag3 = !fuel.isEmpty();

        var access = level.registryAccess();

        if (be.isLit() || flag3 && flag2) {
            RecipeHolder<?> recipeholder = null;
            if (flag2) recipeholder = be.quickCheck.getRecipeFor(new DoubleSmeltingRecipe.DoubleRecipeInput(stackA, stackB), level).orElse(null);

            int i = be.getMaxStackSize();
            if (!be.isLit() && canBurn(access, recipeholder, be.items, i, be)) {
                be.litTime = be.getBurnDuration(fuel);
                be.litDuration = be.litTime;
                if (be.isLit()) {
                    flag1 = true;
                    if (fuel.hasCraftingRemainingItem()) be.items.set(2, fuel.getCraftingRemainingItem());
                    else if (flag3) {
                        var fuelItem = fuel.getItem();
                        fuel.shrink(1);
                        if (fuel.isEmpty()) be.items.set(2, fuel.getCraftingRemainingItem());
                    }
                }
            }

            if (be.isLit() && canBurn(access, recipeholder, be.items, i, be)) {
                ++be.cookingProgress;
                if (be.cookingProgress >= be.cookingTotalTime) {
                    be.cookingProgress = 0;
                    be.cookingTotalTime = getTotalCookTime(level, be);
                    if (burn(access, recipeholder, be.items, i, be)) be.setRecipeUsed(recipeholder);
                    flag1 = true;
                }
            } else be.cookingProgress = 0;
        } else if (!be.isLit() && be.cookingProgress > 0) be.cookingProgress = Mth.clamp(be.cookingProgress - 2, 0, be.cookingTotalTime);

        if (flag != be.isLit()) {
            flag1 = true;
            state = state.setValue(ForgeBlock.LIT, be.isLit());
            level.setBlock(pos, state, 3);
        }
        if (flag1) setChanged(level, pos, state);
    }


    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (t instanceof ForgeBE be) serverTick(level, blockPos, blockState, be);
    }

    private static void createExperience(ServerLevel level, Vec3 popVec, int recipeIndex, float experience) {
        int i = Mth.floor((float)recipeIndex * experience);
        float f = Mth.frac((float)recipeIndex * experience);
        if (f != 0.0F && Math.random() < (double)f) ++i;
        ExperienceOrb.award(level, popVec, i);
    }

    private static int getBurnTime(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING);
    }

    private static int getTotalCookTime(Level level, ForgeBE be) {
        var input = new DoubleSmeltingRecipe.DoubleRecipeInput(be.getItem(0), be.getItem(1));
        return be.quickCheck.getRecipeFor(input, level).map((holder) -> holder.value().getCookingTime()).orElse(200);
    }

    private static boolean canBurn(RegistryAccess access, @Nullable RecipeHolder<?> holder, NonNullList<ItemStack> inventory, int maxStackSize, ForgeBE be) {
        if (!(inventory.get(0).isEmpty() || inventory.get(1).isEmpty()) && holder != null && holder.value() instanceof DoubleSmeltingRecipe recipe) {
            var assembled = recipe.assemble(new DoubleSmeltingRecipe.DoubleRecipeInput(be.getItem(0), be.getItem(1)), access);
            if (assembled.isEmpty()) return false;

            var result = inventory.get(3);
            if (result.isEmpty()) return true;
            if (!ItemStack.isSameItemSameComponents(result, assembled)) return false;

            return result.getCount() + assembled.getCount() <= maxStackSize && result.getCount() + assembled.getCount() <= result.getMaxStackSize() || result.getCount() + assembled.getCount() <= assembled.getMaxStackSize();
        }
        return false;
    }

    private static boolean burn(RegistryAccess access, @Nullable RecipeHolder<?> holder, NonNullList<ItemStack> inventory, int maxStackSize, ForgeBE be) {
        if (holder != null && holder.value() instanceof DoubleSmeltingRecipe recipe && canBurn(access, holder, inventory, maxStackSize, be)) {
            var stackA = inventory.get(0);
            var stackB = inventory.get(1);
            var assembled = recipe.assemble(new DoubleSmeltingRecipe.DoubleRecipeInput(be.getItem(0), be.getItem(1)), access);
            var result = inventory.get(3);
            if (result.isEmpty()) {inventory.set(3, assembled.copy());
            } else if (ItemStack.isSameItemSameComponents(result, assembled)) result.grow(assembled.getCount());

            stackA.shrink(1);
            stackB.shrink(1);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> items) {
    }

    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        if (recipe != null) this.recipesUsed.addTo(recipe.id(), 1);
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        var list = this.getRecipesToAwardAndPopExperience(player.serverLevel(), player.position());
        player.awardRecipes(list);
        for(var holder : list) if (holder != null) player.triggerRecipeCrafted(holder, this.items);
        this.recipesUsed.clear();
    }

    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 popVec) {
        List<RecipeHolder<?>> list = new ArrayList<>();
        for (Object2IntMap.Entry<ResourceLocation> resourceLocationEntry : this.recipesUsed.object2IntEntrySet()) level.getRecipeManager().byKey(resourceLocationEntry.getKey()).ifPresent((holder) -> {
            if (holder.value() instanceof DoubleSmeltingRecipe recipe) {
                list.add(holder);
                createExperience(level, popVec, resourceLocationEntry.getIntValue(), recipe.getExperience());
            }
        });
        return list;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        var facing = getBlockState().getValue(ForgeBlock.FACING);
        return direction == Direction.DOWN ? new int[]{3} : direction == Direction.UP ? new int[]{2} : facing == direction ? new int[0] : switch (facing) {
            case NORTH, SOUTH, WEST, EAST -> facing.getClockWise() == direction ? new int[]{0} : facing.getCounterClockWise() == direction ? new int[]{1} : new int[]{2};
            default -> new int[0];
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN || index != 2 || stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
    }
}
