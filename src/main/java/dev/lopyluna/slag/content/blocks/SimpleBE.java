package dev.lopyluna.slag.content.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class SimpleBE extends BlockEntity {
    public static List<SimpleBE> regCap = new ArrayList<>();
    private int maxTicker = 5;
    private int ticker = 0;
    public SimpleBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        regCap.add(this);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
    }

    public void onRemoved(Level level, BlockPos pos, BlockState state) {

    }

    public void onPlaced(@Nullable LivingEntity placer, ItemStack stack, Level level, BlockPos pos, BlockState state) {

    }

    public ItemInteractionResult useItem(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return ItemInteractionResult.FAIL;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        ticker = ++ticker % maxTicker;
        if (ticker == 0) slowTick(level, pos, state);
    }

    public void slowTick(Level level, BlockPos pos, BlockState state) {

    }

    public void setSlowTicker(int value) {
        maxTicker = value;
    }
}
