package dev.lopyluna.slag.compat.create;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.registry.CreateRegistries;
import dev.lopyluna.slag.content.blocks.multiblock.ConnectivityHandler;
import dev.lopyluna.slag.register.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

public class CreateCompat {
    public static final DeferredRegister<MountedFluidStorageType<?>> FLUID_STORAGES = DeferredRegister.create(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE, MOD_ID);
    public static final DeferredHolder<MountedFluidStorageType<?>, CrucibleMountedStorageType> CRUCIBLE = FLUID_STORAGES.register("crucible", CrucibleMountedStorageType::new);

    public static void register(IEventBus bus) {
        FLUID_STORAGES.register(bus);
        bus.addListener(CreateCompat::setup);
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MountedFluidStorageType.REGISTRY.register(AllBlocks.CRUCIBLE.get(), CRUCIBLE.get());
            BlockSpoutingBehaviour.BY_BLOCK.register(AllBlocks.TABLE.get(), SpoutFilling.TABLE);
            BlockSpoutingBehaviour.BY_BLOCK.register(AllBlocks.BASIN.get(), SpoutFilling.BASIN);
            BlockMovementChecks.registerAttachedCheck(CreateCompat::attached);
            CreateRecycling.register();
        });
    }

    private static BlockMovementChecks.CheckResult attached(BlockState state, Level level, BlockPos pos, Direction dir) {
        if (!state.is(AllBlocks.CRUCIBLE.get())) return BlockMovementChecks.CheckResult.PASS;
        return ConnectivityHandler.isConnected(level, pos, pos.relative(dir)) ? BlockMovementChecks.CheckResult.SUCCESS : BlockMovementChecks.CheckResult.PASS;
    }
}
