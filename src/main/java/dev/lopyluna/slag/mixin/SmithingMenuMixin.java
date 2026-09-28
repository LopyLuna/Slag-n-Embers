package dev.lopyluna.slag.mixin;

import dev.lopyluna.slag.content.smithing.ModularSmithingMenu;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin extends ItemCombinerMenu {
    public SmithingMenuMixin(@Nullable MenuType<?> type, int id, Inventory inventory, ContainerLevelAccess access) {
        super(type, id, inventory, access);
    }

    @ModifyArg(method = "createInputSlotDefinitions", index = 3,
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;withSlot(IIILjava/util/function/Predicate;)Lnet/minecraft/world/inventory/ItemCombinerMenuSlotDefinition$Builder;"))
    private Predicate<ItemStack> template(Predicate<ItemStack> original) {
        return stack -> original.test(stack) || ModularSmithingMenu.isTemplate(stack);
    }

    @Inject(method = "canMoveIntoInputSlots", at = @At("HEAD"), cancellable = true)
    private void quickMove(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ModularSmithingMenu.isTemplate(stack)) cir.setReturnValue(true);
    }

    @Inject(method = "createResult", at = @At("HEAD"))
    private void modular(CallbackInfo ci) {
        var stack = this.inputSlots.getItem(SmithingMenu.TEMPLATE_SLOT);
        if (!(this.player instanceof ServerPlayer serverPlayer) || !ModularSmithingMenu.isTemplate(stack)) return;
        var template = stack.copy();
        this.inputSlots.setItem(SmithingMenu.TEMPLATE_SLOT, ItemStack.EMPTY);
        this.access.execute((level, pos) -> serverPlayer.serverLevel().getServer().execute(() ->
                ModularSmithingMenu.open(serverPlayer, (id, inventory, p) -> new ModularSmithingMenu(id, inventory, ContainerLevelAccess.create(level, pos), template), AllLangs.container("modular_smithing"))));
    }
}
