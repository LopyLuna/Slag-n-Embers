package dev.lopyluna.slag.register;

import dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceMenu;
import dev.lopyluna.slag.content.blocks.forge.client.ForgeMenu;
import dev.lopyluna.slag.content.blocks.melter.client.MelterMenu;
import dev.lopyluna.slag.content.smithing.ModularSmithingMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

import static dev.lopyluna.slag.SlagEmbers.REGISTER;

public class AllMenuTypes {
    public static final DeferredHolder<MenuType<?>, MenuType<ForgeMenu>> FORGE = REGISTER.menus()
            .register("forge", () -> IMenuTypeExtension.create((i, inventory, buf) -> new ForgeMenu(i, inventory)));
    public static final DeferredHolder<MenuType<?>, MenuType<InterfaceMenu>> INTERFACE = REGISTER.menus()
            .register("interface", () -> IMenuTypeExtension.create(InterfaceMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MelterMenu>> MELTER = REGISTER.menus()
            .register("melter", () -> IMenuTypeExtension.create(MelterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ModularSmithingMenu>> MODULAR_SMITHING = REGISTER.menus()
            .register("modular_smithing", () -> IMenuTypeExtension.create((i, inventory, buf) -> new ModularSmithingMenu(i, inventory)));

    public static void register() {}
}
