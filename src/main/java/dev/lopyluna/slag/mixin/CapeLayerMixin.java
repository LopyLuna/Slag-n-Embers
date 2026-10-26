package dev.lopyluna.slag.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lopyluna.slag.content.traits.Traits;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/CapeLayer;hasLayer(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;)Z"))
    private boolean wings(CapeLayer layer, ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Operation<Boolean> original, @Local(argsOnly = true, name = "state") AvatarRenderState state) {
        if (original.call(layer, itemStack, layerType)) return true;
        if (layerType != EquipmentClientInfo.LayerType.WINGS) return false;
        for (var equipment : new ItemStack[]{state.headEquipment, state.chestEquipment, state.legsEquipment, state.feetEquipment}) if (Traits.of(equipment).gliding) return true;
        return false;
    }
}
