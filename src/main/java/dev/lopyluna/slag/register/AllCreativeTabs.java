package dev.lopyluna.slag.register;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import net.bettercombat.api.component.BetterCombatDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Comparator;

import static dev.lopyluna.slag.SlagEmbers.REGISTER;

@SuppressWarnings("unused")
public class AllCreativeTabs {
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(BASE_TAB.getKey())) event.remove(AllItems.DYNAMIC_PART.asStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        if (event.getTabKey().equals(TOOLS_PARTS_TAB.getKey())) {
            var variants = new ArrayList<ItemStack>();

            var materials = AllDynamicTypes.getAllMaterials().stream().sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();
            var parts = AllDynamicTypes.getAllParts().stream().sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();
            var modulars = AllDynamicTypes.getAllModulars().stream().sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();

            for (var material : materials) for (var part : parts) {
                var item = AllItems.DYNAMIC_PART.get();
                var stack = item.getDefaultInstance();

                item.setMaterialType(stack, material);
                item.setPartType(stack, part);

                variants.add(stack);
            }

            for (var material : materials) for (var modular : modulars) {
                var result = modular.getResultStack();
                if (!result.isEmpty()) continue;
                var baseTool = AllItems.MODULAR_ITEM.asStack();
                var toolParts = new ArrayList<ItemStack>();
                for (var part : AllDynamicTypes.getAllPartsFromModular(modular)) {
                    var dynamicPart = AllItems.DYNAMIC_PART.get();
                    var stack = dynamicPart.getDefaultInstance();
                    dynamicPart.setMaterialType(stack, material);
                    dynamicPart.setPartType(stack, part);
                    stack.set(AllDataComponents.BUILT, modular.id);
                    toolParts.add(stack);
                }

                if (modular.finalSegmentStacks != null && !modular.finalSegmentStacks.isEmpty()) {
                    var newStacks = new ArrayList<>(modular.finalSegmentStacks);
                    for (var stack : newStacks) stack.set(AllDataComponents.BUILT, modular.id);
                    toolParts.addAll(newStacks);
                }

                if (ModList.get().isLoaded("bettercombat") && modular.betterCombatPreset != null && modular.betterCombatPreset.isPresent()) {
                    baseTool.set(BetterCombatDataComponents.WEAPON_PRESET_ID, modular.betterCombatPreset.get());
                }

                if (material.fireProof) baseTool.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);

                baseTool.set(AllDataComponents.DYNAMIC_PARTS, new DataDynamicParts(toolParts));
                baseTool.set(AllDataComponents.MODULAR_TYPE, modular.id);

                variants.add(baseTool);
            }

            event.acceptAll(variants, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE_TAB = REGISTER.creativeTab().register("base_tab", () -> CreativeModeTab.builder()
            .title(Component.translatableWithFallback("itemGroup." + SlagEmbers.MOD_ID + ".base", SlagEmbers.NAME))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(AllItems.MODULAR_ITEM::asStack)
            .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS_PARTS_TAB = REGISTER.creativeTab().register("tools_parts_tab", () -> CreativeModeTab.builder()
            .title(Component.translatableWithFallback("itemGroup." + SlagEmbers.MOD_ID + ".tools_parts", "Tools & Parts"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> {
                var material = AllMaterials.GOLD;
                var modular = AllModulars.MAUL;

                var baseTool = AllItems.MODULAR_ITEM.asStack();
                var dynamicPart = AllItems.DYNAMIC_PART.get();
                var toolParts = new ArrayList<ItemStack>();
                for (var part : AllDynamicTypes.getAllPartsFromModular(modular)) {
                    var stack = dynamicPart.getDefaultInstance();
                    dynamicPart.setMaterialType(stack, material);
                    dynamicPart.setPartType(stack, part);
                    stack.set(AllDataComponents.BUILT, modular.id);
                    toolParts.add(stack);
                }

                if (modular.finalSegmentStacks != null && !modular.finalSegmentStacks.isEmpty()) toolParts.addAll(modular.finalSegmentStacks);
                if (material.fireProof) baseTool.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);

                baseTool.set(AllDataComponents.DYNAMIC_PARTS, new DataDynamicParts(toolParts));
                baseTool.set(AllDataComponents.MODULAR_TYPE, modular.id);

                return baseTool;
            })
            .withSearchBar()
            .build());

    public static void register() {}
}
