package dev.lopyluna.slag.content.blocks.forge.client;

import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import dev.lopyluna.slag.register.AllMenuTypes;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class ForgeMenu extends AbstractContainerMenu {
    private final Container inventory; // input1, input2, fuel, output
    private final ContainerData data;
    protected final Level level;
    private static final List<Ingredient> aIngredients = new ArrayList<>();
    private static final List<Ingredient> bIngredients = new ArrayList<>();

    public ForgeMenu(int syncId, Inventory inventory) {
        this(syncId, inventory, new SimpleContainer(4), new SimpleContainerData(4));
    }


    public ForgeMenu(int syncId, Inventory playerInventory, Container inventory, ContainerData data) {
        super(AllMenuTypes.FORGE.get(), syncId);
        checkContainerSize(inventory, 4);

        RecipeType<DoubleSmeltingRecipe> recipeType = AllRecipes.DOUBLE_SMELTING.get();
        this.inventory = inventory;
        this.data = data;
        this.level = playerInventory.player.level();

        if (aIngredients.isEmpty() || bIngredients.isEmpty() || (level.random.nextInt(100) >= 90)) {
            var recipes = this.level.getRecipeManager().getAllRecipesFor(recipeType);
            List<Ingredient> aIngredients = new ArrayList<>();
            List<Ingredient> bIngredients = new ArrayList<>();
            for (var holder : recipes) {
                var recipe = holder.value();
                aIngredients.add(recipe.getInputA());
                bIngredients.add(recipe.getInputB());
            }
            ForgeMenu.aIngredients.addAll(aIngredients);
            ForgeMenu.bIngredients.addAll(bIngredients);
        }

        addSlot(new Slot(inventory, 0, 46, 17)); // Input 1
        addSlot(new Slot(inventory, 1, 66, 17)); // Input 2
        addSlot(new ForgeFuelSlot(this, inventory, 2, 56, 53)); // Fuel
        addSlot(new ForgeResultSlot(playerInventory.player, inventory, 3, 116, 35)); // Output

        // Player inventory
        for (int m = 0; m < 3; ++m) for (int l = 0; l < 9; ++l) this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
        // Hotbar
        for (int m = 0; m < 9; ++m) this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));

        addDataSlots(data);
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(Player player, int index) {
        var copy = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasItem()) {
            var stack = slot.getItem();
            copy = stack.copy();
            var size = this.slots.size();
            if (index == 3) {
                if (!this.moveItemStackTo(stack, 4, size, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, copy);
            } else if (index != 2 && index != 1 && index != 0) {
                if (this.canSmelt(stack) != -1) {
                    if (!this.moveItemStackTo(stack, 0, 2, false)) return ItemStack.EMPTY;
                } else if (this.isFuel(stack)) {
                    if (!this.moveItemStackTo(stack, 2, 3, false)) return ItemStack.EMPTY;
                } else if (index >= 4 && index < 31) {
                    if (!this.moveItemStackTo(stack, 31, size, false)) return ItemStack.EMPTY;
                } else if (index >= 31 && index < size && !this.moveItemStackTo(stack, 4, 31, false)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 4, size, false)) return ItemStack.EMPTY;


            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }




    public float getBurnProgress() {
        int i = this.data.get(2);
        int j = this.data.get(3);
        return j != 0 && i != 0 ? Mth.clamp((float)i / (float)j, 0.0F, 1.0F) : 0.0F;
    }

    public float getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) i = 200;
        return Mth.clamp((float)this.data.get(0) / (float)i, 0.0F, 1.0F);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }


    protected int canSmelt(ItemStack stack) {
        for (var ingredient : aIngredients) if (ingredient.test(stack)) return 0;
        for (var ingredient : bIngredients) if (ingredient.test(stack)) return 1;
        return -1;
    }
    protected boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }
}
