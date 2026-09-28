package dev.lopyluna.slag.content.jei.category;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lopyluna.slag.client.FluidRenderHelper;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.content.temperature.Temperatures;
import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.content.temperature.Temperatures.Type;
import dev.lopyluna.slag.register.AllLangs;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class HeatingCategory extends AbstractRecipeCategory<HeatingCategory.Heater> {
    private static final int WIDTH = 150, HEIGHT = 40, BLOCK_X = 16, BLOCK_Y = 20, BLOCK_SIZE = 32, TEXT_X = 36, LINE = 10, ROWS = 4, CYCLE = 1500;
    private static final int GRAY = 0xAAAAAA, HIGHLIGHT = 0x50FFFFFF;
    private static final float BLOCK_SCALE = 20f, FLUID_HEIGHT = 8f / 9f;
    private static final float[] SHADES = {0.5f, 1f, 0.8f, 0.8f, 0.6f, 0.6f};
    private static final int[] LIGHTS = {LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT};
    private static final Direction[] SIDES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};
    private static final long SEED = 42L;
    private static final Map<BlockState, Optional<BlockEntity>> ENTITIES = new HashMap<>();

    public record Heater(List<Block> blocks, @Nullable TagKey<Block> tag, List<ItemStack> stacks, List<Temperatures.Entry> entries, List<List<BlockState>> states, int hottest, String name) {}

    public HeatingCategory(IGuiHelper guiHelper) {
        super(
                EmbersRecipesJEI.HEATING,
                Component.translatableWithFallback("gui.slag.category.block_temperatures", "Block Temperatures"),
                guiHelper.createDrawableItemLike(Items.LAVA_BUCKET),
                WIDTH, HEIGHT);
    }

    public static List<Heater> heaters() {
        ENTITIES.clear();
        var result = new ArrayList<Heater>();
        var blocks = Temperatures.blocks;
        for (var entry : blocks.entrySet()) add(result, List.of(entry.getKey()), null, entry.getValue());
        var seen = new HashSet<>(blocks.keySet());
        for (var tag : Temperatures.tags) {
            var members = new ArrayList<Block>();
            for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag.getFirst())) if (seen.add(holder.value())) members.add(holder.value());
            add(result, members, tag.getFirst(), tag.getSecond());
        }
        result.sort(Comparator.comparingInt((Heater heater) -> -heater.hottest()).thenComparing(Heater::name));
        return result;
    }

    private static void add(List<Heater> result, List<Block> blocks, @Nullable TagKey<Block> tag, List<Temperatures.Entry> entries) {
        if (entries.isEmpty()) return;
        var shown = new ArrayList<Block>();
        var stacks = new ArrayList<ItemStack>();
        var states = new ArrayList<List<BlockState>>();
        for (var block : blocks) {
            var blockStates = new ArrayList<BlockState>();
            for (var entry : entries) blockStates.add(state(block, entry));
            if (!renders(block, blockStates)) continue;
            shown.add(block);
            states.add(List.copyOf(blockStates));
            var stack = AllUtils.getStackFromBlock(block, false);
            if (!stack.isEmpty()) stacks.add(stack);
        }
        if (shown.isEmpty()) return;
        var hottest = -1;
        for (var entry : entries) if (entry.heat.tier != null) hottest = Math.max(hottest, entry.heat.tier.ordinal());
        var name = tag == null ? shown.getFirst().getName().getString() : "#" + tag.location();
        result.add(new Heater(List.copyOf(shown), tag, List.copyOf(stacks), List.copyOf(entries), List.copyOf(states), hottest, name));
    }

    private static boolean renders(Block block, List<BlockState> states) {
        for (var state : states) if (state.getRenderShape() == RenderShape.INVISIBLE && state.getFluidState().isEmpty() && !(block instanceof EntityBlock)) return false;
        return true;
    }

    private static BlockState state(Block block, Temperatures.Entry entry) {
        var state = block.defaultBlockState();
        var definition = block.getStateDefinition();
        for (var i = 0; i < entry.names.length; i++) {
            var property = definition.getProperty(entry.names[i]);
            if (property != null) state = with(state, property, entry.values[i]);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String value) {
        return property.getValue(value.toLowerCase(Locale.ROOT)).map(found -> state.setValue(property, found)).orElse(state);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Heater heater, IFocusGroup focuses) {
        if (!heater.stacks().isEmpty()) builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(heater.stacks());
    }

    @Override
    public void draw(Heater heater, IRecipeSlotsView view, GuiGraphics g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        var entries = heater.entries();
        var shown = shown(entries.size());
        var top = top(entries.size());
        var hovered = lineAt(heater, mouseX, mouseY);
        var active = hovered >= 0 ? hovered : (int) (Util.getMillis() / CYCLE % shown);

        renderBlock(g, heater.states().get(blockIndex(heater)).get(active));

        for (var i = 0; i < shown; i++) {
            var text = line(entries.get(i));
            var y = top + i * LINE;
            if (i == active && (shown > 1 || hovered == 0)) g.fill(TEXT_X - 2, y - 1, TEXT_X + font.width(text) + 1, y + LINE - 1, HIGHLIGHT);
            g.drawString(font, text, TEXT_X, y, GRAY, true);
        }
        if (entries.size() > shown) g.drawString(font, "+" + (entries.size() - shown) + "...", TEXT_X, top + shown * LINE, GRAY, true);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, Heater heater, IRecipeSlotsView view, double mouseX, double mouseY) {
        var half = BLOCK_SIZE / 2;
        if (mouseX < 0 || mouseX >= BLOCK_X + half || mouseY < BLOCK_Y - half || mouseY >= BLOCK_Y + half) return;
        tooltip.add(heater.blocks().get(blockIndex(heater)).getName());
        if (heater.tag() != null) tooltip.add(Component.literal("#" + heater.tag().location()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static int blockIndex(Heater heater) {
        return (int) (Util.getMillis() / ((long) CYCLE * shown(heater.entries().size())) % heater.blocks().size());
    }

    private static int shown(int count) {
        return count > ROWS ? ROWS - 1 : count;
    }

    private static int top(int count) {
        return (HEIGHT - Math.min(count, ROWS) * LINE) / 2 + 1;
    }

    private static int lineAt(Heater heater, double mouseX, double mouseY) {
        var count = heater.entries().size();
        if (mouseX < TEXT_X - 2 || mouseX >= WIDTH) return -1;
        var row = (int) Math.floor((mouseY - top(count) + 1) / LINE);
        return row >= 0 && row < shown(count) ? row : -1;
    }

    private static void renderBlock(GuiGraphics g, BlockState state) {
        var pose = g.pose();
        var buffer = g.bufferSource();
        var entity = entity(state);
        g.flush();
        pose.pushPose();
        pose.translate(BLOCK_X, BLOCK_Y, 100);
        pose.scale(BLOCK_SCALE, -BLOCK_SCALE, BLOCK_SCALE);
        pose.mulPose(Axis.XP.rotationDegrees(22.5f));
        pose.mulPose(Axis.YP.rotationDegrees(146.25f));
        pose.translate(-0.5f, -0.5f, -0.5f);
        if (state.getRenderShape() == RenderShape.MODEL) renderModel(pose, buffer, state, entity == null ? ModelData.EMPTY : entity.getModelData());
        var fluid = state.getFluidState();
        if (!fluid.isEmpty()) FluidRenderHelper.renderFluidBlock(FluidRenderHelper.toStack(fluid), FLUID_HEIGHT, buffer, pose, LightTexture.FULL_BRIGHT);
        Lighting.setupFor3DItems();
        if (entity != null) renderEntity(pose, buffer, state, entity);
        g.flush();
        pose.popPose();
    }

    private static void renderModel(PoseStack pose, MultiBufferSource buffer, BlockState state, ModelData data) {
        var minecraft = Minecraft.getInstance();
        var model = minecraft.getBlockRenderer().getBlockModel(state);
        var colors = minecraft.getBlockColors();
        var random = RandomSource.create();
        var last = pose.last();
        random.setSeed(SEED);
        for (var type : model.getRenderTypes(state, random, data)) {
            var consumer = buffer.getBuffer(type);
            for (var side : SIDES) {
                random.setSeed(SEED);
                for (var quad : model.getQuads(state, side, random, data, type)) {
                    var shade = quad.isShade() ? SHADES[quad.getDirection().ordinal()] : 1f;
                    var color = quad.isTinted() ? colors.getColor(state, minecraft.level, BlockPos.ZERO, quad.getTintIndex()) : -1;
                    var red = color == -1 ? 1f : (color >> 16 & 255) / 255f;
                    var green = color == -1 ? 1f : (color >> 8 & 255) / 255f;
                    var blue = color == -1 ? 1f : (color & 255) / 255f;
                    consumer.putBulkData(last, quad, new float[]{shade, shade, shade, shade}, red, green, blue, 1f, LIGHTS, OverlayTexture.NO_OVERLAY, false);
                }
            }
        }
    }

    private static void renderEntity(PoseStack pose, MultiBufferSource buffer, BlockState state, BlockEntity entity) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != null) entity.setLevel(minecraft.level);
        var renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(entity);
        if (renderer == null) return;
        try {
            renderer.render(entity, 0f, pose, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        } catch (Exception e) {
            ENTITIES.put(state, Optional.empty());
        }
    }

    private static @Nullable BlockEntity entity(BlockState state) {
        if (!(state.getBlock() instanceof EntityBlock block)) return null;
        return ENTITIES.computeIfAbsent(state, key -> {
            try {
                return Optional.ofNullable(block.newBlockEntity(BlockPos.ZERO, key));
            } catch (Exception e) {
                return Optional.empty();
            }
        }).orElse(null);
    }

    private static Component line(Temperatures.Entry entry) {
        var heat = entry.heat;
        if (heat.tier == null) return Component.empty();
        var text = AllLangs.tr("temperature." + heat.tier.id);
        if (heat.type != Type.NONE) text.append(" (").append(AllLangs.tr("heat_type." + heat.type.id)).append(")");
        return text.withColor(color(heat));
    }

    private static int color(Heat heat) {
        return switch (heat.type) {
            case RED -> 0xFF3E51;
            case ORANGE -> 0xFF8A3C;
            case YELLOW -> 0xFFC83C;
            case SULFUR -> 0xF2CD3B;
            case GREEN -> 0xD7D23A;
            case CYAN -> 0x3ADAB4;
            case TEAL -> 0x3ABCD9;
            case BLUE -> 0x3C78FF;
            case PURPLE -> 0xB33CFF;
            case PINK -> 0xF93CA1;
            case NONE -> heat.tier == null ? GRAY : switch (heat.tier) {
                case FROZEN -> 0xFFFFFF;
                case FREEZING -> 0xAFCCFF;
                case COOL -> 0x6D95F3;
                default -> GRAY;
            };
        };
    }
}
