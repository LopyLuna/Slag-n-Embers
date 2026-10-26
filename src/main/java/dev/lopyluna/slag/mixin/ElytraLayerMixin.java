package dev.lopyluna.slag.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WingsLayer.class)
public abstract class ElytraLayerMixin {
    @Unique private static final Equippable slag$WINGS = Equippable.builder(EquipmentSlot.CHEST).setAsset(EquipmentAssets.ELYTRA).build();

    @ModifyExpressionValue(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object slag$glider(Object equippable, @Local(argsOnly = true, name = "state") HumanoidRenderState state) {
        if (Traits.of(state.chestEquipment).gliding || Traits.of(state.headEquipment).gliding || Traits.of(state.legsEquipment).gliding || Traits.of(state.feetEquipment).gliding) return slag$WINGS;
        return equippable;
    }
}
