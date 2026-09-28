package dev.lopyluna.slag.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lopyluna.slag.content.items.modular.ModularEquipablesItem;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    public LivingEntityMixin(EntityType<?> entityType, Level level) {super(entityType, level);}

    // LivingEntity#onEquipItem(EquipmentSlot, ItemStack, ItemStack)
    @Inject(method = "onEquipItem(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void equippedItemSound(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci) {
        var self = (LivingEntity)(Object)this;
        boolean flag = newItem.isEmpty() && oldItem.isEmpty();
        if (!flag && !ItemStack.isSameItemSameComponents(oldItem, newItem) && !firstTick) {
            if (!level().isClientSide() && !isSpectator()) {
                if (!isSilent() && newItem.getItem() instanceof ModularEquipablesItem item && item.getEquipmentSlot(newItem) == slot) level().playSeededSound(null, self, item.getEquipSound(newItem), getSoundSource(), 1.0F, 1.0F, random.nextLong());
            }
        }
    }

    // LivingEntity#doHurtEquipment(DamageSource, float, EquipmentSlot)
    @Inject(method = "doHurtEquipment(Lnet/minecraft/world/damagesource/DamageSource;F[Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"))
    protected void doHurtEquipment(DamageSource damageSource, float damageAmount, EquipmentSlot[] slots, CallbackInfo ci) {
        var self = (LivingEntity)(Object)this;
        if (!(damageAmount <= 0.0F)) {
            int i = (int)Math.max(1.0F, damageAmount / 4.0F);
            for (var slot : slots) {
                var stack = this.getItemBySlot(slot);
                if (stack.getItem() instanceof ModularEquipablesItem item && item.isArmor(stack) && stack.canBeHurtBy(damageSource)) stack.hurtAndBreak(i, self, slot);
            }
        }
    }

    @ModifyExpressionValue(method = "updateFallFlying()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack glider(ItemStack chest) {
        return Traits.glider((LivingEntity)(Object)this, chest, false);
    }
}
