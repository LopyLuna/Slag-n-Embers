package dev.lopyluna.slag.content.items.dynamic_mold;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItemModelRenderer;
import dev.lopyluna.slag.client.render.PartialItemModelRenderer;
import dev.lopyluna.slag.register.AllDataComponents;
import net.createmod.catnip.api.data.Iterate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
@ParametersAreNonnullByDefault
public class DynamicMoldRenderer extends CustomRenderedItemModelRenderer {
    @Override protected void render(ItemStack stack, PartialItemModelRenderer renderer, ItemDisplayContext ctx) {
        var cast = stack.get(AllDataComponents.CAST_TYPE);
        var cutout = stack.has(AllDataComponents.CUTOUT);
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        var modID = id.getNamespace();
        var item = id.getPath();
        var base = getModel(SlagEmbers.loc(modID, "item/" + item));
        renderer.transform(base);
        if (cast == null) {
            renderer.render(cutout ? getModel(SlagEmbers.loc(modID, "item/cutout/" + item)) : base);
            return;
        }
        renderer.render(getModel(SlagEmbers.loc(modID, "item/" + ((cutout ? "cutout/" : "") + item + "/" + cast.location().getPath().split("/")[1]))));
    }

    @SubscribeEvent
    public static void registerStandalone(ModelEvent.RegisterStandalone e) {
        var castTypes = List.of("axe_heads", "balls", "dusts", "gems", "guards", "hoe_heads", "ingots", "nuggets", "pickaxe_heads", "rods", "shovel_heads", "sword_blades", "plates", "helmets", "chestplates", "leggings", "boots");
        for (var item : BuiltInRegistries.ITEM) {
            if (!(item instanceof DynamicMoldItem)) continue;
            var id = BuiltInRegistries.ITEM.getKey(item);
            var modID = id.getNamespace();
            var itemID = id.getPath();
            CustomRenderedItemModel.register(e, SlagEmbers.loc(modID, "item/" + itemID));
            CustomRenderedItemModel.register(e, SlagEmbers.loc(modID, "item/cutout/" + itemID));
            for (var cast : castTypes) {
                for (var cutout : Iterate.trueAndFalse) CustomRenderedItemModel.register(e, SlagEmbers.loc(modID, "item/" + ((cutout ? "cutout/" : "") + itemID + "/" + cast)));
            }
        }
    }
}
