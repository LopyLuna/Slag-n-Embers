package dev.lopyluna.slag.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyExpressionValue(method = "tryToStartFallFlying()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack glider(ItemStack chest) {
        return Traits.glider((Player)(Object)this, chest, false);
    }
}
