package dev.lopyluna.slag.content.smithing;

import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllMenuTypes;
import net.bettercombat.api.component.BetterCombatDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantValue")
@ParametersAreNonnullByDefault
public class ModularSmithingMenu extends AbstractContainerMenu {
    public static final int BLUEPRINT = 0, SLOT_A = 1, SLOT_B = 2, RESULT = 3;
    public static final int AREA_X = 53, AREA_Y = 35, AREA_W = 58, AREA_H = 31;
    public static final int SELECT = 0, EXTRACT = 100, TEMPLATE = 200, EARLIER = 300, LATER = 400;

    private final ContainerLevelAccess access;
    private final Player player;
    private final Container inputs;
    private final ResultContainer result = new ResultContainer();
    private List<ModularType> constructible = List.of();
    private boolean updating;
    private boolean closing;

    public ModularSmithingMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL, ItemStack.EMPTY);
    }

    public ModularSmithingMenu(int id, Inventory inventory, ContainerLevelAccess access, ItemStack template) {
        super(AllMenuTypes.MODULAR_SMITHING.get(), id);
        this.access = access;
        this.player = inventory.player;
        this.inputs = new SimpleContainer(3) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(this);
            }
        };

        addSlot(new Slot(inputs, BLUEPRINT, AREA_X + (AREA_W - 16) / 2, AREA_Y + (AREA_H - 16) / 2) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player picker) { return false; }
        });
        addSlot(new Slot(inputs, SLOT_A, 8, 48) {
            @Override public boolean mayPlace(ItemStack stack) { return canInsert(stack); }
        });
        addSlot(new Slot(inputs, SLOT_B, 26, 48) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addSlot(new Slot(result, 0, 149, 48) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public void onTake(Player taker, ItemStack stack) { onBuild(taker); }
        });

        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));

        if (!template.isEmpty()) inputs.setItem(BLUEPRINT, template);
    }

    public static boolean isTemplate(ItemStack stack) {
        return stack.getCount() == 1 && stack.getItem() instanceof ModularItem item && !item.hasModularType(stack);
    }

    public static void open(ServerPlayer user, MenuConstructor constructor, Component title) {
        var carried = user.containerMenu.getCarried().copy();
        user.containerMenu.setCarried(ItemStack.EMPTY);
        user.openMenu(new Swap(constructor, title));
        if (carried.isEmpty()) return;
        var opened = user.containerMenu;
        opened.setCarried(carried);
        user.connection.send(new ClientboundContainerSetSlotPacket(-1, opened.incrementStateId(), -1, carried));
    }

    public ItemStack blueprint() {
        return inputs.getItem(BLUEPRINT);
    }

    public @Nullable DataDynamicParts parts() {
        var print = blueprint();
        return isTemplate(print) ? ((ModularItem) print.getItem()).getParts(print) : null;
    }

    public List<ModularType> constructible() {
        return constructible;
    }

    public int selected() {
        var id = blueprint().get(AllDataComponents.SELECTED);
        if (id == null) return 0;
        for (var i = 0; i < constructible.size(); i++) if (constructible.get(i).id.equals(id)) return i;
        return 0;
    }

    public boolean canInsert(ItemStack stack) {
        var parts = parts();
        if (stack.isEmpty() || parts == null) return false;
        var empty = DataDynamicParts.EMPTY;
        var possible = empty.getPossibleParts();
        if (stack.getItem() instanceof IDynamicPart part) {
            var segment = part.getPartSegment(stack);
            return empty.getPossibleTags(possible).contains(segment) && !parts.containsDynamicPartSegment(segment);
        }
        var count = empty.getLargestPossibleCount(stack, empty.getPossibleStacks(possible));
        return count > 0 && !parts.contains(stack.copyWithCount(count));
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container != inputs || updating) return;
        updating = true;
        if (!player.level().isClientSide()) absorb();
        update();
        updating = false;
        if (!closing && blueprint().isEmpty()) swapVanilla();
    }

    private void absorb() {
        var print = blueprint();
        if (!isTemplate(print)) return;
        var item = (ModularItem) print.getItem();
        var inserted = false;
        var stack = inputs.getItem(SLOT_A);
        while (canInsert(stack)) {
            var parts = item.getParts(print);
            if (parts == null) break;
            var empty = DataDynamicParts.EMPTY;
            var count = stack.getItem() instanceof IDynamicPart ? 1 : empty.getLargestPossibleCount(stack, empty.getPossibleStacks(empty.getPossibleParts()));
            if (count == 0) break;
            var copy = parts.itemsCopy();
            copy.add(stack.copyWithCount(count));
            item.setParts(print, copy);
            stack.shrink(count);
            inserted = true;
        }
        if (stack.isEmpty()) inputs.setItem(SLOT_A, ItemStack.EMPTY);
        if (inserted) player.playSound(SoundEvents.DECORATED_POT_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    private void update() {
        var parts = parts();
        constructible = parts == null ? List.of() : parts.getConstructibleModulars();
        if (!player.level().isClientSide()) createResult();
    }

    private void createResult() {
        result.setItem(0, ItemStack.EMPTY);
        var print = blueprint();
        var parts = parts();
        if (parts == null || constructible.isEmpty()) return;
        var type = constructible.get(selected());
        var match = parts.match(type);
        if (match == null) return;
        var stack = type.getResultStack();
        if (!stack.isEmpty()) {
            result.setItem(0, stack.copy());
            return;
        }
        var built = print.copy();
        var used = new ArrayList<ItemStack>();
        for (var part : match.used()) {
            var copy = part.copy();
            copy.set(AllDataComponents.BUILT, type.id);
            used.add(copy);
        }
        var item = (ModularItem) print.getItem();
        built.remove(AllDataComponents.SELECTED);
        built.set(AllDataComponents.MODULAR_TYPE, type.id);
        item.setParts(built, used);
        item.getTraits(built).applyComponents(built);
        if (ModList.get().isLoaded("bettercombat") && type.betterCombatPreset != null && type.betterCombatPreset.isPresent()) {
            built.set(BetterCombatDataComponents.WEAPON_PRESET_ID, type.betterCombatPreset.get());
        }
        result.setItem(0, built);
    }

    private void onBuild(Player taker) {
        var parts = parts();
        if (parts != null && !constructible.isEmpty()) {
            var match = parts.match(constructible.get(selected()));
            if (match != null) for (var left : match.leftover()) give(taker, left);
        }
        inputs.setItem(BLUEPRINT, ItemStack.EMPTY);
        taker.playSound(SoundEvents.CRAFTER_CRAFT, 0.8F, 0.8F + taker.level().getRandom().nextFloat() * 0.4F);
        access.execute((level, pos) -> level.levelEvent(1044, pos, 0));
    }

    @Override
    public boolean clickMenuButton(Player user, int id) {
        var print = blueprint();
        var parts = parts();
        if (parts == null) return false;
        if (id >= SELECT && id < EXTRACT) {
            if (id >= constructible.size()) return false;
            print.set(AllDataComponents.SELECTED, constructible.get(id).id);
            inputs.setChanged();
            return true;
        }
        if (id == TEMPLATE) {
            if (!parts.isEmpty() || !extract(print.copy())) return false;
            inputs.setItem(BLUEPRINT, ItemStack.EMPTY);
            inputs.setChanged();
            return true;
        }
        var copy = parts.itemsCopy();
        if (id < TEMPLATE) {
            var index = id - EXTRACT;
            if (index < 0 || index >= copy.size() || !extract(copy.get(index))) return false;
            copy.remove(index);
        } else if (id < LATER) {
            var index = id - EARLIER;
            if (index < 0 || index >= copy.size()) return false;
            copy.add(Math.floorMod(index - 1, copy.size()), copy.remove(index));
        } else {
            var index = id - LATER;
            if (index < 0 || index >= copy.size()) return false;
            copy.add(Math.floorMod(index + 1, copy.size()), copy.remove(index));
        }
        ((ModularItem) print.getItem()).setParts(print, copy);
        inputs.setChanged();
        return true;
    }

    private boolean extract(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (getCarried().isEmpty()) {
            setCarried(stack);
            return true;
        }
        if (!inputs.getItem(SLOT_B).isEmpty()) return false;
        inputs.setItem(SLOT_B, stack);
        return true;
    }

    private void give(Player user, ItemStack stack) {
        if (stack.isEmpty() || user.level().isClientSide()) return;
        user.getInventory().placeItemBackInInventory(stack);
    }

    private void swapVanilla() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        access.execute((level, pos) -> serverPlayer.serverLevel().getServer().execute(() -> {
            if (serverPlayer.containerMenu != this) return;
            for (var index : new int[]{BLUEPRINT, SLOT_A, SLOT_B}) {
                var stack = inputs.getItem(index);
                if (stack.isEmpty()) continue;
                inputs.setItem(index, ItemStack.EMPTY);
                if (getCarried().isEmpty()) setCarried(stack);
                else give(serverPlayer, stack);
            }
            open(serverPlayer, (id, inventory, p) -> new SmithingMenu(id, inventory, ContainerLevelAccess.create(level, pos)), Component.translatable("container.upgrade"));
        }));
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(Player mover, int index) {
        if (index == BLUEPRINT) return ItemStack.EMPTY;
        var slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var stack = slot.getItem();
        var copy = stack.copy();
        var size = slots.size();
        if (index == RESULT) {
            if (!moveItemStackTo(stack, RESULT + 1, size, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, copy);
        } else if (index <= SLOT_B) {
            if (!moveItemStackTo(stack, RESULT + 1, size, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, SLOT_A, SLOT_B, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(mover, stack);
        return copy;
    }

    @Override
    public void removed(Player user) {
        closing = true;
        super.removed(user);
        access.execute((level, pos) -> clearContainer(user, inputs));
    }

    @Override
    public boolean stillValid(Player user) {
        return stillValid(access, user, Blocks.SMITHING_TABLE);
    }

    private record Swap(MenuConstructor constructor, Component title) implements MenuProvider {
        @Override
        public boolean shouldTriggerClientSideContainerClosingOnOpen() {
            return false;
        }

        @Override
        public @Nonnull Component getDisplayName() {
            return title;
        }

        @Override
        public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player user) {
            return constructor.createMenu(id, inventory, user);
        }
    }
}
