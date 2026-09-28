package dev.lopyluna.slag.content.blocks.basin;

import dev.lopyluna.slag.content.blocks.casting.CastingInventory;

public class BasinInventory extends CastingInventory {
    public BasinInventory(BasinBE be) {
        super(be, 1, 64, true);
    }
}
