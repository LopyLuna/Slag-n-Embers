package dev.lopyluna.slag.content.items.dynamic_mold;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItemModelRenderer;
import dev.lopyluna.slag.client.render.PartialItemModelRenderer;
import dev.lopyluna.slag.register.AllDataComponents;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
@ParametersAreNonnullByDefault
public class DynamicMoldRenderer extends CustomRenderedItemModelRenderer {
    @Override protected void render(ItemStack stack, ItemRenderer itemRenderer, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                                    ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buf, int light, int overlay) {
        var hasMold = stack.has(AllDataComponents.CAST_TYPE);
        var cast = stack.get(AllDataComponents.CAST_TYPE);
        var cutout = stack.has(AllDataComponents.CUTOUT);
        var shaper = itemRenderer.getItemModelShaper();
        var manager = shaper.getModelManager();
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        var modID = id.getNamespace();
        var item = id.getPath();
        if (!hasMold || cast == null) {
            //renderer.render(manager.getModel(ModelResourceLocation.standalone(Embers.loc(modID, "item/cutout/" + item))), light);
            if (cutout) renderer.render(getModel(SlagEmbers.loc(modID, "item/cutout/" + item), manager), light);
            else renderer.render(model.getOriginalModel(), light);
            return;
        }
        renderer.render(getModel(SlagEmbers.loc(modID, "item/" + ((cutout ? "cutout/" : "") + item + "/" + cast.location().getPath().split("/")[1])), manager), light);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional e) {
        var castTypes = new ArrayList<>(List.of("axe_heads", "balls", "dusts", "gems", "guards", "hoe_heads", "ingots", "nuggets", "pickaxe_heads", "rods", "shovel_heads", "sword_blades", "plates", "helmets", "chestplates", "leggings", "boots"));
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var item = entry.getValue();
            if (!(item instanceof DynamicMoldItem)) continue;
            var id = BuiltInRegistries.ITEM.getKey(item);
            var modID = id.getNamespace();
            var itemID = id.getPath();
            e.register(ModelResourceLocation.standalone(SlagEmbers.loc(modID, "item/cutout/" + itemID)));
            for (var cast : castTypes) {
                for (var cutout : Iterate.trueAndFalse) e.register(ModelResourceLocation.standalone(SlagEmbers.loc(modID, "item/" + ((cutout ? "cutout/" : "") + itemID + "/" + cast))));
            }
        }
    }
}
