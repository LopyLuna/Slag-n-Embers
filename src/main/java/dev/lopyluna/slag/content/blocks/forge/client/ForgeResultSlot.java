package dev.lopyluna.slag.content.blocks.forge.client;

import dev.lopyluna.slag.content.blocks.forge.ForgeBE;
import dev.lopyluna.slag.mixin.FurnaceResultSlotAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;

public class ForgeResultSlot extends FurnaceResultSlot {
    private final Player player;
    public ForgeResultSlot(Player player, Container container, int slot, int xPosition, int yPosition) {
        super(player, container, slot, xPosition, yPosition);
        this.player = player;
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        var access = ((FurnaceResultSlotAccessor)this);
        stack.onCraftedBy(this.player, access.getRemoveCount());
        if (player instanceof ServerPlayer serverplayer && container instanceof ForgeBE be) be.awardUsedRecipesAndPopExperience(serverplayer);
        access.setRemoveCount(0);
        EventHooks.firePlayerSmeltedEvent(this.player, stack, access.getRemoveCount());
    }
}
