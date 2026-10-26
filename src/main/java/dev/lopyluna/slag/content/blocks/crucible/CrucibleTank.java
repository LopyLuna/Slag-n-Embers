package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.content.utils.NBTHelper;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@ParametersAreNonnullByDefault
public class CrucibleTank extends SnapshotJournal<List<FluidStack>> implements ResourceHandler<FluidResource> {
    private static final Comparator<FluidStack> BY_AMOUNT = Comparator.comparingInt(FluidStack::getAmount).reversed();
    private List<FluidStack> fluids = new ArrayList<>();
    private final Consumer<List<FluidStack>> updateCallback;
    public FluidStack first = FluidStack.EMPTY;
    public boolean sortByAmount;
    private float alloyBudget;
    private static RecipeMap alloySource;
    private static List<AlloyingRecipe> alloyRecipes = List.of();
    public int total;
    public boolean hot;
    protected int capacity;

    public CrucibleTank(int capacity, Consumer<List<FluidStack>> updateCallback) {
        this.capacity = capacity;
        this.updateCallback = updateCallback;
        fluids.add(FluidStack.EMPTY);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

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

    public @Nonnull FluidStack getFluid() {
        return fluids.isEmpty() ? FluidStack.EMPTY : fluids.getFirst();
    }

    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    public CrucibleTank readFromNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        fluids.clear();
        fluids = NBTHelper.readFluidList(nbt.getListOrEmpty("Fluids"), provider);
        recount();
        return this;
    }

    public @Nonnull CompoundTag writeToNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        nbt.put("Fluids", NBTHelper.writeFluidList(fluids, provider));
        return nbt;
    }

    public int fill(List<FluidStack> resources, boolean simulate) {
        if (resources.isEmpty()) return 0;
        var i = 0;
        for (var f : resources) i += fill(f, simulate);
        return i;
    }

    public int fill(FluidStack resource, boolean simulate) {
        var filled = fillRaw(resource, simulate);
        if (!simulate && filled > 0) onContentsChanged();
        return filled;
    }

    public @Nonnull FluidStack drain(FluidStack resource, boolean simulate) {
        var out = drainRaw(resource, simulate);
        if (!simulate && !out.isEmpty()) onContentsChanged();
        return out;
    }

    public @Nonnull FluidStack drain(int maxDrain, boolean simulate) {
        compress();
        if (maxDrain <= 0 || fluids.isEmpty()) return FluidStack.EMPTY;

        var top = fluids.getFirst();
        if (top.isEmpty()) return FluidStack.EMPTY;

        int drained = Math.min(top.getAmount(), maxDrain);
        FluidStack out = top.copyWithAmount(drained);

        if (!simulate && drained > 0) {
            top.shrink(drained);
            onContentsChanged();
        }
        return out;
    }

    private int fillRaw(FluidStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;

        int match = findFirstMatch(resource);
        int fillable = Math.min(getSpace(), resource.getAmount());
        if (fillable <= 0 || simulate) return Math.max(0, fillable);

        if (match >= 0) fluids.get(match).grow(fillable);
        else {
            fluids.add(resource.copyWithAmount(fillable));
            if (fluids.getFirst().isEmpty() && fluids.size() > 1) moveFirstNonEmptyToFront();
        }
        if (sortByAmount) fluids.sort(BY_AMOUNT);
        return fillable;
    }

    private FluidStack drainRaw(FluidStack resource, boolean simulate) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        compress();

        int matchIndex = findFirstMatch(resource);
        if (matchIndex < 0) return FluidStack.EMPTY;

        FluidStack matched = fluids.get(matchIndex);
        if (matched.isEmpty()) return FluidStack.EMPTY;

        int drained = Math.min(matched.getAmount(), resource.getAmount());
        FluidStack out = matched.copyWithAmount(drained);
        if (!simulate && drained > 0) matched.shrink(drained);
        return out;
    }

    @Override
    public int size() {
        return fluids.size();
    }

    @Override
    public @Nonnull FluidResource getResource(int index) {
        return index < 0 || index >= fluids.size() ? FluidResource.EMPTY : FluidResource.of(fluids.get(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return index < 0 || index >= fluids.size() ? 0 : fluids.get(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return capacity;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return true;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return insert(resource, amount, transaction);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var stack = resource.toStack(amount);
        var filled = fillRaw(stack, true);
        if (filled <= 0) return 0;
        updateSnapshots(transaction);
        fillRaw(stack, false);
        recount();
        return filled;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return extract(resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        var stack = resource.toStack(amount);
        var drained = drainRaw(stack, true).getAmount();
        if (drained <= 0) return 0;
        updateSnapshots(transaction);
        drainRaw(stack, false);
        recount();
        return drained;
    }

    @Override
    protected List<FluidStack> createSnapshot() {
        var list = new ArrayList<FluidStack>(fluids.size());
        for (var fluid : fluids) list.add(fluid.copy());
        return list;
    }

    @Override
    protected void revertToSnapshot(List<FluidStack> snapshot) {
        fluids = new ArrayList<>(snapshot.size());
        for (var fluid : snapshot) fluids.add(fluid.copy());
        recount();
    }

    @Override
    protected void onRootCommit(List<FluidStack> originalState) {
        onContentsChanged();
    }

    public void setFluid(FluidStack stack) {
        if (fluids.isEmpty()) fluids.add(stack);
        else fluids.set(0, stack);
        recount();
        compress();
        updateCallback.accept(fluids);
    }

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
        if (level.isClientSide() || heat.tier == null) return false;

        boolean changed = false;

        fluids.removeIf(FluidStack::isEmpty);
        if (fluids.isEmpty()) return false;

        var manager = AllUtils.recipes(level);
        if (manager == null) return false;
        var source = manager.recipeMap();
        if (source != alloySource) {
            alloySource = source;
            alloyRecipes = source.byType(AllRecipes.ALLOYING.get()).stream().map(RecipeHolder::value).toList();
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
