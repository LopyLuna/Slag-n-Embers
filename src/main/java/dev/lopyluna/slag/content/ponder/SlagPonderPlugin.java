package dev.lopyluna.slag.content.ponder;

import dev.lopyluna.slag.SlagEmbers;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nonnull;

public class SlagPonderPlugin implements PonderPlugin {
    @Override
    public @Nonnull String getModId() {
        return SlagEmbers.MOD_ID;
    }

    @Override
    public void registerScenes(@Nonnull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        AllPonderScenes.register(helper);
    }
}
