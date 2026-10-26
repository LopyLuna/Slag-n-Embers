package dev.lopyluna.slag.events;

import dev.lopyluna.slag.content.blocks.SimpleBE;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllBlocks;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.tooltip.TooltipLocation;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var be : SimpleBE.regCap) be.registerCapabilities(event);
        FluidMultiBlockEntity.registerCapabilities(event);
        CastingBE.registerCapabilities(event, AllBETypes.BASIN.get());
        MelterBE.registerCapabilities(event);
        CastingBE.registerCapabilities(event, AllBETypes.TABLE.get());
    }

    @SubscribeEvent
    public static void registerTooltips(RegisterTooltipAppendersEvent event) {
        event.registerAppender(TooltipLocation.POST_CUSTOM, (stack, _, _, _, _, tooltip) -> {
            if (stack.is(AllBlocks.CRUCIBLE.asItem())) tooltip.accept(AllLangs.tr("dynamic_multiblock").withStyle(ChatFormatting.GRAY));
            if (!stack.is(AllBlocks.BASIN.asItem()) && !stack.is(AllBlocks.TABLE.asItem())) return;
            tooltip.accept(Component.empty());
            tooltip.accept(AllLangs.tr("cast_shift_clear").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.literal(" ").append(AllLangs.tr("cast_shift_clear.desc")).withStyle(ChatFormatting.BLUE));
        });
    }
}
