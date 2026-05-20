package dev.lopyluna.slag.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadModelsCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("slag")
                .then(Commands.literal("reloadModels")
                    .requires(source -> source.hasPermission(2))
                    .executes(ReloadModelsCommand::execute)
                )
        );
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (source.getPlayer() != null) {
            AllDynamicTypes.syncToPlayer(source.getPlayer());
            source.sendSuccess(() -> Component.translatable("slag.command.reload_models.start"), true);
        } else {
            source.sendFailure(Component.translatable("slag.command.reload_models.client_only"));
        }
        
        return 1;
    }
}

