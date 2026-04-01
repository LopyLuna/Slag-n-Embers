package dev.lopyluna.slag.content.datagen;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllItems;

import java.util.Comparator;
import java.util.List;

public class LangProviderDatagen {
    public static void lang(RegistrateLangProvider provider) {
        // item groups
        provider.add("itemGroup.slag.base", "Slag n' Embers");
        provider.add("itemGroup.slag.tools_parts", "Tools & Parts");
        // jei categories
        provider.add("gui.slag.category.melting", "Melting");
        provider.add("gui.slag.category.double_smelting", "Double Smelting");
        // dynamic parts
        List<MaterialType> materials = AllDynamicTypes.getAllMaterials()
            .stream()
            .sorted(Comparator.comparingInt(type -> type.sortOrder))
            .toList();
        List<PartType> parts = AllDynamicTypes.getAllParts()
            .stream()
            .sorted(Comparator.comparingInt(type -> type.sortOrder))
            .toList();
        for (var material : materials) {
            for (var part : parts) {
                var item = AllItems.DYNAMIC_PART.get();
                var stack = item.getDefaultInstance();

                item.setMaterialType(stack, material);
                item.setPartType(stack, part);

                var id = item.getDescriptionId(stack);
                var name = id.split("\\.")[2];
                provider.add(id, RegistrateLangProvider.toEnglishName(name));
            }
        }
    }
}
