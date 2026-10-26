package dev.lopyluna.slag.register;

import com.mojang.brigadier.arguments.ArgumentType;
import net.createmod.catnip.impl.command.argument.ConfigIdArgument;
import net.createmod.catnip.impl.command.argument.ConfigPathArgument;
import net.createmod.catnip.impl.command.argument.ModIdArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

import static dev.lopyluna.slag.SlagEmbers.REGISTER;

@SuppressWarnings("unused")
public class AllArguments {
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ConfigIdArgument>> CATNIP_CONFIG_ID = catnip("catnip_config_id", ConfigIdArgument.class, ConfigIdArgument::configId);
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ConfigPathArgument>> CATNIP_CONFIG_PATH = catnip("catnip_config_path", ConfigPathArgument.class, ConfigPathArgument::path);
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ModIdArgument>> CATNIP_MOD_ID = catnip("catnip_mod_id", ModIdArgument.class, ModIdArgument::modId);

    private static <A extends ArgumentType<?>> DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<A>> catnip(String name, Class<A> type, Supplier<A> factory) {
        return REGISTER.arguments().register(name, () -> {
            var info = SingletonArgumentInfo.contextFree(factory);
            return ArgumentTypeInfos.isClassRecognized(type) ? info : ArgumentTypeInfos.registerByClass(type, info);
        });
    }

    public static void register() {}
}
