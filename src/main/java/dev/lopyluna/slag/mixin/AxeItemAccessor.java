package dev.lopyluna.slag.mixin;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AxeItem.class)
public interface AxeItemAccessor {
    @Invoker("playerHasBlockingItemUseIntent")
    static boolean playerHasBlockingItemUseIntent(UseOnContext context) {
        throw new AssertionError();
    }
}
