package dev.lopyluna.slag.content.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class NBTHelper {
    public static BlockPos readBlockPos(CompoundTag nbt, String key) {
        var pos = nbt.read(key, BlockPos.CODEC);
        if (pos.isPresent()) return pos.get();
        var oldTag = nbt.getCompoundOrEmpty(key);
        return new BlockPos(oldTag.getIntOr("X", 0), oldTag.getIntOr("Y", 0), oldTag.getIntOr("Z", 0));
    }

    public static void writeBlockPos(CompoundTag nbt, String key, BlockPos pos) {
        nbt.store(key, BlockPos.CODEC, pos);
    }

    public Vec3i loadVec3i(CompoundTag nbt, String key) {
        var tag = nbt.getCompoundOrEmpty(key);
        return tag.isEmpty() ? null : new Vec3i(tag.getIntOr("X", 0), tag.getIntOr("Y", 0), tag.getIntOr("Z", 0));
    }

    public void saveVec3i(CompoundTag nbt, String key, Vec3i vec) {
        if (vec == null) return;
        var tag = new CompoundTag();
        for (var axis : Direction.Axis.values()) tag.putInt(axis.getName(), vec.get(axis));
        nbt.put(key, tag);
    }

    public static ListTag writeFluidList(List<FluidStack> stacks, HolderLookup.Provider registries) {
        ListTag listNBT = new ListTag();
        for (FluidStack stack : stacks) listNBT.add(writeFluid(stack, registries));
        return listNBT;
    }

    public static List<FluidStack> readFluidList(ListTag stacks, HolderLookup.Provider registries) {
        List<FluidStack> list = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) list.add(i, readFluid(stacks.getCompoundOrEmpty(i), registries));
        return list;
    }

    public static Tag writeFluid(FluidStack stack, HolderLookup.Provider registries) {
        return write(FluidStack.OPTIONAL_CODEC, stack, registries);
    }

    public static FluidStack readFluid(Tag tag, HolderLookup.Provider registries) {
        return read(FluidStack.OPTIONAL_CODEC, tag, registries, FluidStack.EMPTY);
    }

    public static Tag writeItem(ItemStack stack, HolderLookup.Provider registries) {
        return write(ItemStack.OPTIONAL_CODEC, stack, registries);
    }

    public static ItemStack readItem(Tag tag, HolderLookup.Provider registries) {
        return read(ItemStack.OPTIONAL_CODEC, tag, registries, ItemStack.EMPTY);
    }

    public static <T> Tag write(Codec<T> codec, T value, HolderLookup.Provider registries) {
        return codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), value).result().orElseGet(CompoundTag::new);
    }

    public static <T> T read(Codec<T> codec, Tag tag, HolderLookup.Provider registries, T fallback) {
        return codec.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(fallback);
    }
}
