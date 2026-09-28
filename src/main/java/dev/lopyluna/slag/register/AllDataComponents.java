package dev.lopyluna.slag.register;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import static dev.lopyluna.slag.SlagEmbers.REGISTER;

public class AllDataComponents {



    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TagKey<Item>>> CAST_TYPE = REGISTER.components()
            .registerComponentType("cast_type", b -> b
                    .persistent(TagKey.codec(Registries.ITEM)).networkSynchronized(AllUtils.tagKeyStreamCodec(Registries.ITEM)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> USES = REGISTER.components()
            .registerComponentType("uses", b -> b
                    .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> CUTOUT = REGISTER.components()
            .registerComponentType("cutout", b -> b
                    .persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> BUILT = REGISTER.components()
            .registerComponentType("built", b -> b
                    .persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> SELECTED = REGISTER.components()
            .registerComponentType("selected", b -> b
                    .persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DataDynamicParts>> DYNAMIC_PARTS = REGISTER.components()
            .registerComponentType("dynamic_parts", b -> b
                    .persistent(DataDynamicParts.CODEC).networkSynchronized(DataDynamicParts.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> MATERIAL_TYPE = REGISTER.components()
            .registerComponentType("material_type", b -> b
                    .persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> PART_TYPE = REGISTER.components()
            .registerComponentType("part_type", b -> b
                    .persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> MODULAR_TYPE = REGISTER.components()
            .registerComponentType("modular_type", b -> b
                    .persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding());



    public static void register() {}
}
