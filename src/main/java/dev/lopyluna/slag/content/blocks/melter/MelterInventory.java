package dev.lopyluna.slag.content.blocks.melter;

import dev.lopyluna.slag.content.blocks.DirtyInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nonnull;

public class MelterInventory extends DirtyInventory<MelterBE> {
    MelterBE be;

    public MelterInventory(int slots, MelterBE be) {
        super(slots, be, 64, true);
        this.be = be;
    }

    @Override
    public void setChanged() {
        be.sendDataImmediately();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        be.sendDataImmediately();
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return Container.stillValidBlockEntity(be, player);
    }
}
