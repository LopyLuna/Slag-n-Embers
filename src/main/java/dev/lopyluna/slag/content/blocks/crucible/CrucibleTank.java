package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.content.utils.NBTHelper;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class CrucibleTank extends FluidTank {
    private static final Comparator<FluidStack> BY_AMOUNT = Comparator.comparingInt(FluidStack::getAmount).reversed();
    private List<FluidStack> fluids = new ArrayList<>();
    private final Consumer<List<FluidStack>> updateCallback;
    public FluidStack first = FluidStack.EMPTY;
    public boolean sortByAmount;
    private float alloyBudget;
    private static Collection<RecipeHolder<?>> alloySource;
    private static List<AlloyingRecipe> alloyRecipes = List.of();
    public int total;
    public boolean hot;

    public CrucibleTank(int capacity, Consumer<List<FluidStack>> updateCallback) {
        super(capacity);
        this.updateCallback = updateCallback;
        fluids.add(FluidStack.EMPTY);
    }

    @Override
    protected void onContentsChanged() {
        recount();
        updateCallback.accept(getFluids());
    }

    private void recount() {
        total = 0;
        hot = false;
        first = FluidStack.EMPTY;
        for (var fluid : fluids) {
            if (fluid.isEmpty()) continue;
            total += fluid.getAmount();
            hot |= fluid.getFluidType().getTemperature() >= 1000;
            if (first.isEmpty()) first = fluid;
        }
    }

    public List<FluidStack> getFluids() {
        compress();
        return fluids;
    }

    public List<FluidStack> getFluidCopy() {
        if (getFluids() == null || fluids.isEmpty()) return new ArrayList<>();
        return getFluids().stream().filter(s -> !s.isEmpty()).map(FluidStack::copy).toList();
    }

    public void setFluids(List<FluidStack> list) {
        fluids = new ArrayList<>();
        for (var fluid : list) if (!fluid.isEmpty()) fluids.add(fluid.copy());
        if (fluids.isEmpty()) fluids.add(FluidStack.EMPTY);
        onContentsChanged();
    }

    public void trim() {
        if (total <= capacity) return;
        compress();
        updateCallback.accept(fluids);
    }

    public void clear() {
        fluids.clear();
        onContentsChanged();
    }

    public void insert(List<FluidStack> resources) {
        var space = getSpace();
        for (var resource : resources) {
            if (space <= 0) break;
            if (resource.isEmpty()) continue;
            var amount = Math.min(space, resource.getAmount());
            mergeIn(resource.copyWithAmount(amount));
            resource.shrink(amount);
            space -= amount;
        }
        resources.removeIf(FluidStack::isEmpty);
        fluids.removeIf(FluidStack::isEmpty);
        if (fluids.isEmpty()) fluids.add(FluidStack.EMPTY);
        if (sortByAmount) fluids.sort(BY_AMOUNT);
        onContentsChanged();
    }

    public boolean containsLiquid(Fluid fluid) {
        if (fluids.isEmpty()) return false;
        for (var target : fluids) if (target.is(fluid)) return true;
        return false;
    }

    public boolean canExtinguishEntity(Entity entity) {
        if (hot) return false;
        for (var fluid : fluids) if (fluid.getFluidType().canExtinguish(entity)) return true;
        return false;
    }

    public void noCoverTick() {
        List<FluidStack> list = this.fluids;
        if (list == null || list.isEmpty()) return;

        var changed = false;
        for (var fluid : fluids) {
            if (fluid.isEmpty()) continue;
            if (!fluid.getFluidType().isLighterThanAir()) continue;
            fluid.shrink(25);
            changed = true;
        }
        if (!changed) return;
        onContentsChanged();
        compress();
    }

    public boolean moveFluidToFront(int targetIdx) {
        List<FluidStack> list = this.fluids;
        if (list == null || list.isEmpty()) return false;
        if (targetIdx == 0) {
            var f = list.removeFirst();
            list.addLast(f);
            onContentsChanged();
            compress();
            return true;
        }
        if (targetIdx <= 0 || targetIdx >= list.size()) return false;

        var sel = list.remove(targetIdx);
        list.addFirst(sel);
        onContentsChanged();
        compress();
        return true;
    }

    public @Nonnull FluidStack getFluidFiltered(Fluid fluid) {
        if (fluids.isEmpty()) return FluidStack.EMPTY;
        for (var target : fluids) if (target.is(fluid)) return target;
        return FluidStack.EMPTY;
    }

    @Override
    public @Nonnull FluidStack getFluid() {
        return fluids.isEmpty() ? FluidStack.EMPTY : fluids.getFirst();
    }

    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    @Override
    public @Nonnull FluidTank readFromNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        fluids.clear();
        fluids = NBTHelper.readFluidList(nbt.getList("Fluids", Tag.TAG_COMPOUND), provider);
        recount();
        return this;
    }

    @Override
    public @Nonnull CompoundTag writeToNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        nbt.put("Fluids", NBTHelper.writeFluidList(fluids, provider));
        return nbt;
    }

    @SuppressWarnings("all")
    @Override
    public boolean isFluidValid(FluidStack stack) {
        return this.validator != null && super.isFluidValid(stack);
    }

    public int fill(List<FluidStack> resources, IFluidHandler.FluidAction action) {
        if (resources.isEmpty()) return 0;
        var i = 0;
        for (var f : resources) i += fill(f, action);
        return i;
    }

    @SuppressWarnings("all")
    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null || resource.isEmpty() || validator == null || !isFluidValid(resource) || action == null) return 0;

        int match = findFirstMatch(resource);
        int spaceTotal = getSpace();

        if (match >= 0) {
            int fillable = Math.min(spaceTotal, resource.getAmount());
            if (fillable <= 0) return 0;
            if (action.simulate()) return fillable;

            fluids.get(match).grow(fillable);
            if (sortByAmount) fluids.sort(BY_AMOUNT);
            onContentsChanged();
            return fillable;
        }
        int fillable = Math.min(spaceTotal, resource.getAmount());
        if (fillable <= 0) return 0;
        if (action.simulate()) return fillable;

        fluids.add(resource.copyWithAmount(fillable));
        if (fluids.getFirst().isEmpty() && fluids.size() > 1) moveFirstNonEmptyToFront();
        if (sortByAmount) fluids.sort(BY_AMOUNT);
        onContentsChanged();
        return fillable;
    }

    @Override
    public @Nonnull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        compress();

        int matchIndex = findFirstMatch(resource);
        if (matchIndex < 0) return FluidStack.EMPTY;

        FluidStack matched = fluids.get(matchIndex);
        if (matched.isEmpty()) return FluidStack.EMPTY;

        int drained = Math.min(matched.getAmount(), resource.getAmount());
        FluidStack out = matched.copyWithAmount(drained);

        if (action.execute() && drained > 0) {
            matched.shrink(drained);
            onContentsChanged();
        }

        return out;
    }

    @Override
    public @Nonnull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        compress();
        if (maxDrain <= 0 || fluids.isEmpty()) return FluidStack.EMPTY;

        var top = fluids.getFirst();
        if (top.isEmpty()) return FluidStack.EMPTY;

        int drained = Math.min(top.getAmount(), maxDrain);
        FluidStack out = top.copyWithAmount(drained);

        if (action.execute() && drained > 0) {
            top.shrink(drained);
            onContentsChanged();
        }
        return out;
    }

    @Override
    public void setFluid(FluidStack stack) {
        if (fluids.isEmpty()) fluids.add(stack);
        else fluids.set(0, stack);
        recount();
        compress();
        updateCallback.accept(fluids);
    }

    @Override
    public int getTanks() {
        return fluids.size();
    }

    @Override
    public boolean isEmpty() {
        return total == 0;
    }

    public int getSpace() {
        return Math.max(0, capacity - total);
    }

    private int findFirstMatch(FluidStack target) {
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack f = fluids.get(i);
            if (!f.isEmpty() && FluidStack.isSameFluidSameComponents(f, target)) return i;
        }
        return -1;
    }

    private void compress() {
        var changed = fluids.removeIf(FluidStack::isEmpty);
        for (int i = 0; i + 1 < fluids.size(); ) {
            FluidStack a = fluids.get(i);
            FluidStack b = fluids.get(i + 1);
            if (FluidStack.isSameFluidSameComponents(a, b)) {
                a.grow(b.getAmount());
                fluids.remove(i + 1);
                changed = true;
            } else i++;
        }
        if (fluids.isEmpty()) fluids.add(FluidStack.EMPTY);
        var overflow = total - capacity;
        if (overflow > 0) {
            trimFromEnd(overflow);
            changed = true;
        }
        if (changed) recount();
    }

    private void moveFirstNonEmptyToFront() {
        for (int i = 1; i < fluids.size(); i++) if (!fluids.get(i).isEmpty()) {
            FluidStack f = fluids.remove(i);
            fluids.set(0, f);
            return;
        }
    }

    private void trimFromEnd(int toRemove) {
        for (int i = fluids.size() - 1; i >= 0 && toRemove > 0; i--) {
            FluidStack f = fluids.get(i);
            int take = Math.min(f.getAmount(), toRemove);
            f.shrink(take);
            toRemove -= take;
            if (f.isEmpty()) fluids.remove(i);
        }
        if (fluids.isEmpty()) fluids.add(FluidStack.EMPTY);
    }

    public boolean tryAlloy(Level level, Heat heat) { return tryAlloy(level, 1, heat); }

    public boolean tryAlloy(Level level, int maxCraftsPerCall, Heat heat) {
        if (level.isClientSide || heat.tier == null) return false;

        boolean changed = false;

        fluids.removeIf(FluidStack::isEmpty);
        if (fluids.isEmpty()) return false;

        var manager = level.getRecipeManager();
        var source = manager.getRecipes();
        if (source != alloySource) {
            alloySource = source;
            alloyRecipes = manager.getAllRecipesFor(AllRecipes.ALLOYING.get()).stream().map(RecipeHolder::value).toList();
        }
        var recipes = alloyRecipes;
        if (recipes.isEmpty()) return false;

        alloyBudget = Math.min(alloyBudget + maxCraftsPerCall, maxCraftsPerCall * 2f);
        while (true) {
            AlloyingRecipe r = findCraftable(recipes, heat);
            if (r == null) break;

            var cost = (r.duration > 0 ? r.duration : 1f) / (heat.speed(r.temperature, r.heatType, r.strict) * Math.max(0.01f, r.speed));
            if (alloyBudget < cost) break;

            FluidStack out = r.getOutput();
            if (!canAccept(out)) break;

            alloyBudget -= cost;
            for (var needed : r.getInputs()) {
                var kind = resolve(needed);
                if (kind == null) continue;
                subtractAcross(kind, needed.amount());
            }
            mergeIn(out);
            changed = true;
        }

        coalesce();
        if (fluids.isEmpty()) fluids.add(FluidStack.EMPTY);
        recount();
        return changed;
    }

    private AlloyingRecipe findCraftable(List<AlloyingRecipe> recipes, Heat heat) {
        for (var r : recipes) if (heat.speed(r.temperature, r.heatType, r.strict) > 0 && canCraft(r)) return r;
        return null;
    }

    private boolean canCraft(AlloyingRecipe r) {
        var req = r.getInputs();
        if (req == null || req.isEmpty()) return false;
        for (var needed : req) if (resolve(needed) == null) return false;
        return true;
    }

    private @Nullable FluidStack resolve(SizedFluidIngredient needed) {
        for (var f : fluids) if (!f.isEmpty() && needed.ingredient().test(f) && countOf(f) >= needed.amount()) return f.copyWithAmount(1);
        return null;
    }
    private boolean canAccept(FluidStack add) {
        return !add.isEmpty();
    }

    private static boolean sameKind(FluidStack a, FluidStack b) {
        return FluidStack.isSameFluidSameComponents(a, b);
    }

    private int countOf(FluidStack kind) {
        int total = 0;
        for (var f : fluids) if (!f.isEmpty() && sameKind(f, kind)) total += f.getAmount();
        return total;
    }

    private void subtractAcross(FluidStack kind, int amount) {
        if (amount <= 0) return;
        for (int i = 0; i < fluids.size() && amount > 0; i++) {
            FluidStack f = fluids.get(i);
            if (f.isEmpty() || !sameKind(f, kind)) continue;
            int take = Math.min(f.getAmount(), amount);
            f.shrink(take);
            amount -= take;
            if (f.isEmpty()) { fluids.remove(i); i--; }
        }
    }

    private void mergeIn(FluidStack add) {
        if (add.isEmpty()) return;
        for (var f : fluids) if (!f.isEmpty() && sameKind(f, add)) {
            f.grow(add.getAmount());
            return;
        }
        fluids.add(add.copy());
    }

    @Override
    public @Nonnull FluidStack getFluidInTank(int tank) {
        if (tank >= fluids.size() || 0 >= tank) return super.getFluidInTank(tank);
        return fluids.get(tank);
    }

    private void coalesce() {
        if (fluids.isEmpty()) return;
        List<FluidStack> out = new ArrayList<>(fluids.size());
        for (var f : fluids) {
            if (f.isEmpty()) continue;
            boolean merged = false;
            for (var g : out) if (sameKind(f, g)) {
                g.grow(f.getAmount());
                merged = true;
                break;
            }
            if (!merged) out.add(f.copy());
        }
        fluids.clear();
        fluids.addAll(out);
    }
}
