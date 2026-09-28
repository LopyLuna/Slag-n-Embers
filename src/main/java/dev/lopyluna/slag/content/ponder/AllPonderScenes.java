package dev.lopyluna.slag.content.ponder;

import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.lopyluna.slag.content.blocks.casting.CastingBE;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleBE;
import dev.lopyluna.slag.content.blocks.drain.DrainBE;
import dev.lopyluna.slag.content.blocks.drain.DrainState;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.multiblock.LerpedFloat;
import dev.lopyluna.slag.content.blocks.table.TableBE;
import dev.lopyluna.slag.content.temperature.Temperatures.Heat;
import dev.lopyluna.slag.content.temperature.Temperatures.Tiers;
import dev.lopyluna.slag.content.temperature.Temperatures.Type;
import dev.lopyluna.slag.register.*;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public class AllPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(AllBlocks.CRUCIBLE)
                .addStoryBoard("crucible", AllPonderScenes::crucible);
        HELPER.forComponents(AllBlocks.MELTER)
                .addStoryBoard("melter", AllPonderScenes::melter);
        HELPER.forComponents(AllBlocks.DRAIN, AllBlocks.TABLE, AllBlocks.BASIN)
                .addStoryBoard("interface_basin_table", AllPonderScenes::casting);
        HELPER.forComponents(AllBlocks.INTERFACE, AllBlocks.DRAIN)
                .addStoryBoard("interface_basin_table", AllPonderScenes::fluidInterface);
        HELPER.forComponents(AllBlocks.CRUCIBLE, AllBlocks.INTERFACE, AllBlocks.BASIN, AllBlocks.TABLE, AllBlocks.DRAIN, AllBlocks.MELTER)
                .addStoryBoard("smeltery_new", AllPonderScenes::smeltery);
    }

    public static void smeltery(SceneBuilder scene, SceneBuildingUtil util) {
        intro(scene, util, "smeltery", "Building a Smeltery");
        for (var pos : List.of(util.grid().at(0, 5, 0), util.grid().at(1, 5, 0), util.grid().at(0, 5, 2), util.grid().at(3, 6, 0), util.grid().at(2, 8, 0), util.grid().at(0, 2, 0)))
            heat(scene, util.select().position(pos), CrucibleBE.class, Tiers.HEATED, Type.ORANGE);

        scene.world().showSection(util.select().fromTo(0, 1, 0, 2, 1, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60).text("Crucibles are heated by the blocks placed beneath them").pointAt(util.vector().topOf(2, 1, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        scene.overlay().showText(80).text("Crucibles placed next to or on top of each other connect into one big tank").pointAt(util.vector().topOf(2, 2, 2)).placeNearTarget().attachKeyFrame();
        var single = util.select().position(0, 5, 0);
        int[][] spots = {{0, 0}, {0, 1}, {1, 1}, {1, 0}, {0, 2}, {1, 2}, {2, 2}, {2, 1}, {2, 0}};
        var singles = new ArrayList<ElementLink<WorldSectionElement>>();
        for (var spot : spots) {
            var link = scene.world().showIndependentSection(single, Direction.DOWN);
            scene.world().moveSection(link, util.vector().of(spot[0], -3, spot[1]), 0);
            singles.add(link);
            scene.idle(4);
        }
        fade(scene, singles.subList(0, 4));
        var x2 = shown(scene, util.select().fromTo(1, 5, 0, 2, 5, 1), util.vector().of(-1, -3, 0));
        scene.idle(4);
        fade(scene, List.of(singles.get(4), singles.get(5), x2));
        var x2x3 = shown(scene, util.select().fromTo(3, 6, 0, 4, 6, 2), util.vector().of(-3, -4, 0));
        scene.idle(6);
        fade(scene, List.of(x2x3, singles.get(6), singles.get(7), singles.get(8)));

        var layer = util.select().fromTo(2, 8, 0, 4, 8, 2);
        var layerA = shown(scene, layer, util.vector().of(-2, -6, 0));
        scene.idle(4);
        var layerB = scene.world().showIndependentSection(layer, Direction.DOWN);
        scene.world().moveSection(layerB, util.vector().of(-2, -5, 0), 0);
        scene.idle(4);
        var layerC = scene.world().showIndependentSection(layer, Direction.DOWN);
        scene.world().moveSection(layerC, util.vector().of(-2, -4, 0), 0);
        scene.idle(8);
        fade(scene, List.of(layerA, layerB));
        var bottom = shown(scene, util.select().fromTo(0, 5, 2, 2, 5, 4), util.vector().of(0, -3, -2));
        var top = shown(scene, util.select().fromTo(0, 7, 2, 2, 7, 4), util.vector().of(0, -4, -2));
        scene.idle(6);
        fade(scene, List.of(bottom, top, layerC));
        var cube = shown(scene, util.select().fromTo(0, 5, 2, 2, 7, 4), util.vector().of(0, -3, -2));
        scene.idle(20);

        var side = util.vector().blockSurface(util.grid().at(2, 3, 1), Direction.EAST);
        scene.overlay().showControls(side, Pointing.DOWN, 40).rightClick().whileSneaking();
        scene.idle(7);
        fade(scene, List.of(cube));
        scene.world().showIndependentSectionImmediately(util.select().fromTo(0, 2, 0, 2, 4, 2));
        scene.overlay().showText(60).text("Sneak and Right-click with an empty hand to toggle the windows").pointAt(side).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(0, 1, 3, 1, 1, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(0, 2, 3, 1, 2, 3), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60).text("Melters turn items into molten fluid and pour it into the tank behind them").pointAt(util.vector().blockSurface(util.grid().at(1, 2, 3), Direction.SOUTH)).placeNearTarget().attachKeyFrame();
        melter(scene, util.grid().at(0, 2, 3), new ItemStack(Items.IRON_INGOT, 8));
        melter(scene, util.grid().at(1, 2, 3), new ItemStack(Items.IRON_INGOT, 8));
        scene.idle(30);
        for (var i = 7; i >= 0; i -= 2) {
            melter(scene, util.grid().at(0, 2, 3), new ItemStack(Items.IRON_INGOT, i), part(AllFluids.MOLTEN_IRON, 0.02f));
            melter(scene, util.grid().at(1, 2, 3), new ItemStack(Items.IRON_INGOT, i), part(AllFluids.MOLTEN_IRON, 0.02f));
            fill(scene, util.grid().at(0, 2, 0), 10, part(AllFluids.MOLTEN_IRON, 0.1f * (8 - i) / 2f));
            scene.idle(10);
        }
        melter(scene, util.grid().at(0, 2, 3), ItemStack.EMPTY);
        melter(scene, util.grid().at(1, 2, 3), ItemStack.EMPTY);
        scene.idle(20);

        scene.world().showSection(util.select().position(2, 2, 3), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(50).text("The Fluid Interface shows every fluid inside the Crucible").pointAt(util.vector().blockSurface(util.grid().at(2, 2, 3), Direction.SOUTH).add(0, 0, -0.5)).placeNearTarget().attachKeyFrame();
        scene.idle(60);

        var table = util.grid().at(3, 1, 1);
        var basin = util.grid().at(3, 1, 0);
        mold(scene, table, ingotMold());
        scene.world().showSection(util.select().fromTo(3, 1, 0, 3, 1, 1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 2, 0, 3, 2, 1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 3, 0, 3, 3, 1), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60).text("Drains move fluid from the container behind them into any fluid container below").pointAt(util.vector().blockSurface(util.grid().at(3, 2, 1), Direction.EAST)).placeNearTarget().attachKeyFrame();
        scene.idle(20);

        drain(scene, util.grid().at(3, 2, 1), AllFluids.MOLTEN_IRON, table, DrainState.POURING);
        cast(scene, table, AllFluids.MOLTEN_IRON, MelterBE.INGOT_SIZE, 15);
        fill(scene, util.grid().at(0, 2, 0), 15, part(AllFluids.MOLTEN_IRON, 0.38f));
        scene.idle(15);
        drain(scene, util.grid().at(3, 2, 1), null, table, DrainState.OFF);
        cool(scene, table, new ItemStack(Items.IRON_INGOT), 30);

        scene.world().toggleRedstonePower(util.select().position(3, 3, 0));
        scene.effects().indicateRedstone(util.grid().at(3, 3, 0));
        drain(scene, util.grid().at(3, 2, 0), AllFluids.MOLTEN_IRON, basin, DrainState.POWERED);
        cast(scene, basin, AllFluids.MOLTEN_IRON, MelterBE.BLOCK_SIZE, 30);
        fill(scene, util.grid().at(0, 2, 0), 30, part(AllFluids.MOLTEN_IRON, 0.2f));
        scene.idle(30);
        result(scene, table, new ItemStack(Items.IRON_INGOT));
        drain(scene, util.grid().at(3, 2, 0), null, basin, DrainState.POWERED);
        cool(scene, basin, new ItemStack(Items.IRON_BLOCK), 40);
        scene.idle(40);
        result(scene, basin, new ItemStack(Items.IRON_BLOCK));
        scene.idle(20);
        scene.markAsFinished();
    }

    public static void crucible(SceneBuilder scene, SceneBuildingUtil util) {
        intro(scene, util, "crucible", "Heating and Filling Crucibles");
        var ctrl = util.grid().at(1, 2, 1);
        var tank = util.select().fromTo(1, 2, 1, 2, 3, 2);

        for (var pos : List.of(util.grid().at(1, 1, 1), util.grid().at(2, 1, 1), util.grid().at(1, 1, 2), util.grid().at(2, 1, 2))) {
            scene.world().showSection(util.select().position(pos), Direction.DOWN);
            scene.idle(4);
        }
        scene.idle(6);
        scene.overlay().showText(70).text("Crucibles take their heat from the blocks directly below them").pointAt(util.vector().topOf(2, 1, 2)).placeNearTarget().attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(tank, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "floor", util.select().fromTo(1, 1, 1, 2, 1, 2), 90);
        scene.overlay().showText(70).text("Their temperature is the average of every block underneath, so cover the whole floor for the most heat").pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.EAST)).placeNearTarget().attachKeyFrame();
        scene.idle(100);

        var topSide = util.vector().topOf(2, 3, 2);
        scene.overlay().showControls(topSide, Pointing.DOWN, 30).rightClick().withItem(bucket(AllFluids.MOLTEN_GOLD));
        scene.idle(7);
        fill(scene, ctrl, 15, part(AllFluids.MOLTEN_GOLD, 0.2f));
        scene.overlay().showText(60).text("Molten fluids can be poured in with Buckets, or fed in by Melters").pointAt(topSide).placeNearTarget().attachKeyFrame();
        scene.idle(35);
        scene.overlay().showControls(topSide, Pointing.DOWN, 30).rightClick().withItem(bucket(AllFluids.MOLTEN_COPPER));
        scene.idle(7);
        fill(scene, ctrl, 15, part(AllFluids.MOLTEN_GOLD, 0.2f), part(AllFluids.MOLTEN_COPPER, 0.2f));
        scene.idle(40);

        scene.overlay().showText(70).text("When the Crucible is hot enough, fluids that make an alloy will mix on their own").pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.EAST)).placeNearTarget().attachKeyFrame();
        scene.idle(20);
        fill(scene, ctrl, 20, part(AllFluids.MOLTEN_ROSE_GOLD, 0.14f), part(AllFluids.MOLTEN_GOLD, 0.13f), part(AllFluids.MOLTEN_COPPER, 0.13f));
        scene.idle(20);
        fill(scene, ctrl, 20, part(AllFluids.MOLTEN_ROSE_GOLD, 0.28f), part(AllFluids.MOLTEN_GOLD, 0.06f), part(AllFluids.MOLTEN_COPPER, 0.06f));
        scene.idle(20);
        fill(scene, ctrl, 20, part(AllFluids.MOLTEN_ROSE_GOLD, 0.4f));
        scene.idle(40);

        var side = util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.EAST).add(0, -0.5, -0.5);
        scene.overlay().showControls(side, Pointing.DOWN, 30).rightClick().whileSneaking();
        scene.idle(7);
        windows(scene, tank, ctrl, false);
        scene.overlay().showText(60).text("Sneak and Right-click with an empty hand to toggle the windows").pointAt(side).placeNearTarget().attachKeyFrame();
        scene.idle(50);
        scene.overlay().showControls(side, Pointing.DOWN, 30).rightClick().whileSneaking();
        scene.idle(7);
        windows(scene, tank, ctrl, true);
        scene.idle(30);
        scene.markAsFinished();
    }

    public static void melter(SceneBuilder scene, SceneBuildingUtil util) {
        intro(scene, util, "melter", "Melting Items");
        var ctrl = util.grid().at(1, 1, 1);
        var hot = util.grid().at(2, 2, 3);
        var warm = util.grid().at(1, 2, 3);

        scene.world().showSection(util.select().fromTo(1, 1, 1, 2, 2, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(2, 1, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(hot), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60).text("Melters turn items into molten fluid").pointAt(util.vector().blockSurface(hot, Direction.SOUTH)).placeNearTarget().attachKeyFrame();
        scene.idle(70);
        scene.overlay().showText(60).text("They need a heat source directly below them").pointAt(util.vector().blockSurface(util.grid().at(2, 1, 3), Direction.SOUTH)).placeNearTarget();
        scene.idle(70);

        var item = scene.world().createItemEntity(util.vector().centerOf(2, 4, 3), Vec3.ZERO, new ItemStack(Items.IRON_INGOT, 4));
        scene.idle(12);
        scene.world().modifyEntity(item, Entity::discard);
        melter(scene, hot, new ItemStack(Items.IRON_INGOT, 4));
        scene.overlay().showText(70).text("Items can be thrown in, inserted with Hoppers, or placed through its menu").pointAt(util.vector().topOf(hot)).placeNearTarget().attachKeyFrame();
        scene.idle(30);
        scene.overlay().showControls(util.vector().blockSurface(hot, Direction.SOUTH), Pointing.RIGHT, 30).rightClick();
        scene.idle(50);

        scene.overlay().showText(70).text("Molten fluid is poured into the tank directly behind it").pointAt(util.vector().topOf(2, 2, 2)).placeNearTarget().attachKeyFrame();
        for (var i = 3; i >= 0; i--) {
            melter(scene, hot, new ItemStack(Items.IRON_INGOT, i), part(AllFluids.MOLTEN_IRON, 0.02f));
            fill(scene, ctrl, 10, part(AllFluids.MOLTEN_IRON, 0.05f * (4 - i)));
            scene.idle(10);
        }
        melter(scene, hot, ItemStack.EMPTY);
        scene.idle(40);

        scene.world().showSection(util.select().position(1, 1, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(warm), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(70).text("Weaker heat sources, such as Magma Blocks, melt items more slowly").pointAt(util.vector().blockSurface(util.grid().at(1, 1, 3), Direction.SOUTH)).placeNearTarget().attachKeyFrame();
        melter(scene, hot, new ItemStack(Items.GOLD_INGOT, 4));
        melter(scene, warm, new ItemStack(Items.GOLD_INGOT, 4));
        scene.idle(20);
        var gold = 0f;
        for (var step = 1; step <= 8; step++) {
            if (step <= 4) melter(scene, hot, new ItemStack(Items.GOLD_INGOT, 4 - step), step == 4 ? null : part(AllFluids.MOLTEN_GOLD, 0.02f));
            if (step % 2 == 0) melter(scene, warm, new ItemStack(Items.GOLD_INGOT, 4 - step / 2), step == 8 ? null : part(AllFluids.MOLTEN_GOLD, 0.02f));
            gold += (step <= 4 ? 0.05f : 0) + (step % 2 == 0 ? 0.05f : 0);
            fill(scene, ctrl, 10, part(AllFluids.MOLTEN_IRON, 0.2f), part(AllFluids.MOLTEN_GOLD, gold));
            scene.idle(10);
        }
        scene.idle(30);
        scene.markAsFinished();
    }

    public static void casting(SceneBuilder scene, SceneBuildingUtil util) {
        intro(scene, util, "casting", "Casting Molten Fluids");
        var ctrl = util.grid().at(1, 1, 1);
        var table = util.grid().at(3, 1, 2);
        var basin = util.grid().at(3, 1, 1);
        var tableDrain = util.grid().at(3, 2, 2);
        var basinDrain = util.grid().at(3, 2, 1);
        fill(scene, ctrl, 0, part(AllFluids.MOLTEN_IRON, 0.5f));

        scene.world().showSection(util.select().fromTo(1, 1, 1, 2, 3, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(tableDrain), Direction.WEST);
        scene.idle(10);
        scene.overlay().showText(70).text("Drains take fluid from the container they are attached to, such as a Crucible, and pour it downwards").pointAt(util.vector().blockSurface(tableDrain, Direction.EAST).add(-0.5, 0, 0)).placeNearTarget().attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(util.select().position(table), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60).text("A Casting Table catches the fluid, but needs a mold to shape it").pointAt(util.vector().blockSurface(table, Direction.EAST)).placeNearTarget().attachKeyFrame();
        scene.idle(30);
        scene.overlay().showControls(util.vector().topOf(table), Pointing.DOWN, 30).rightClick().withItem(ingotMold());
        scene.idle(7);
        mold(scene, table, ingotMold());
        scene.idle(40);

        var drainSide = util.vector().blockSurface(tableDrain, Direction.EAST).add(-0.5, 0, 0);
        scene.overlay().showControls(drainSide, Pointing.DOWN, 30).rightClick();
        scene.idle(7);
        scene.overlay().showText(70).text("Right-click the Drain to start pouring. It stops once the container below is full").pointAt(drainSide).placeNearTarget().attachKeyFrame();
        drain(scene, tableDrain, AllFluids.MOLTEN_IRON, table, DrainState.POURING);
        cast(scene, table, AllFluids.MOLTEN_IRON, MelterBE.INGOT_SIZE, 20);
        fill(scene, ctrl, 20, part(AllFluids.MOLTEN_IRON, 0.47f));
        scene.idle(20);
        drain(scene, tableDrain, null, table, DrainState.OFF);
        cool(scene, table, new ItemStack(Items.IRON_INGOT), 40);
        scene.idle(60);
        scene.overlay().showText(60).text("Once it has cooled, Right-click with an empty hand to take out the result").pointAt(util.vector().topOf(table)).placeNearTarget();
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(table), Pointing.DOWN, 30).rightClick();
        scene.idle(7);
        result(scene, table, new ItemStack(Items.IRON_INGOT));
        scene.idle(33);
        result(scene, table, ItemStack.EMPTY);
        scene.idle(10);

        scene.world().showSection(util.select().position(basin), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(basinDrain), Direction.WEST);
        scene.idle(10);
        scene.overlay().showText(60).text("Casting Basins work the same way, but cast whole blocks without a mold").pointAt(util.vector().blockSurface(basin, Direction.EAST)).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(3, 3, 1, 3, 3, 2), Direction.DOWN);
        scene.idle(10);
        scene.world().toggleRedstonePower(util.select().position(3, 3, 1));
        scene.effects().indicateRedstone(util.grid().at(3, 3, 1));
        scene.overlay().showText(70).text("A powered Drain keeps pouring whenever there is room below").pointAt(util.vector().blockSurface(basinDrain, Direction.EAST).add(-0.5, 0, 0)).placeNearTarget().attachKeyFrame();
        drain(scene, basinDrain, AllFluids.MOLTEN_IRON, basin, DrainState.POWERED);
        cast(scene, basin, AllFluids.MOLTEN_IRON, MelterBE.BLOCK_SIZE, 40);
        fill(scene, ctrl, 40, part(AllFluids.MOLTEN_IRON, 0.32f));
        scene.idle(40);
        drain(scene, basinDrain, null, basin, DrainState.POWERED);
        cool(scene, basin, new ItemStack(Items.IRON_BLOCK), 50);
        scene.idle(50);
        result(scene, basin, new ItemStack(Items.IRON_BLOCK));
        scene.idle(30);
        scene.markAsFinished();
    }

    public static void fluidInterface(SceneBuilder scene, SceneBuildingUtil util) {
        intro(scene, util, "fluid_interface", "Choosing which Fluid to Pour");
        var ctrl = util.grid().at(1, 1, 1);
        var table = util.grid().at(3, 1, 2);
        var tableDrain = util.grid().at(3, 2, 2);
        var face = util.grid().at(2, 1, 3);
        fill(scene, ctrl, 0, part(AllFluids.MOLTEN_IRON, 0.15f), part(AllFluids.MOLTEN_GOLD, 0.15f), part(AllFluids.MOLTEN_COPPER, 0.15f));
        mold(scene, table, ingotMold());

        scene.world().showSection(util.select().fromTo(1, 1, 1, 2, 3, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60).text("A Crucible can hold many fluids at once, stacked in layers").pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.EAST).add(0, 0, -0.5)).placeNearTarget().attachKeyFrame();
        scene.idle(70);

        scene.world().showSection(util.select().position(face), Direction.NORTH);
        scene.idle(10);
        var front = util.vector().blockSurface(face, Direction.SOUTH).add(0, 0, -0.5);
        scene.overlay().showControls(front, Pointing.DOWN, 30).rightClick();
        scene.idle(7);
        scene.overlay().showText(60).text("Right-click the Fluid Interface to see every fluid inside").pointAt(front).placeNearTarget().attachKeyFrame();
        scene.idle(70);
        scene.overlay().showText(60).text("Selecting a fluid moves it to the bottom of the Crucible").pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.EAST).add(0, 0, -0.5)).placeNearTarget().attachKeyFrame();
        scene.idle(20);
        fill(scene, ctrl, 10, part(AllFluids.MOLTEN_GOLD, 0.15f), part(AllFluids.MOLTEN_IRON, 0.15f), part(AllFluids.MOLTEN_COPPER, 0.15f));
        scene.idle(50);

        scene.world().showSection(util.select().position(table), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(tableDrain), Direction.WEST);
        scene.idle(10);
        scene.overlay().showText(70).text("Drains always pour from the bottom layer first").pointAt(util.vector().blockSurface(tableDrain, Direction.EAST).add(-0.5, 0, 0)).placeNearTarget().attachKeyFrame();
        drain(scene, tableDrain, AllFluids.MOLTEN_GOLD, table, DrainState.POURING);
        cast(scene, table, AllFluids.MOLTEN_GOLD, MelterBE.INGOT_SIZE, 20);
        fill(scene, ctrl, 20, part(AllFluids.MOLTEN_GOLD, 0.12f), part(AllFluids.MOLTEN_IRON, 0.15f), part(AllFluids.MOLTEN_COPPER, 0.15f));
        scene.idle(20);
        drain(scene, tableDrain, null, table, DrainState.OFF);
        cool(scene, table, new ItemStack(Items.GOLD_INGOT), 40);
        scene.idle(40);
        result(scene, table, new ItemStack(Items.GOLD_INGOT));
        scene.idle(40);
        scene.markAsFinished();
    }

    private record Part(Fluid fluid, float share) {}

    private static Part part(FluidEntry<?> fluid, float share) {
        return new Part(fluid.getSource(), share);
    }

    private static void intro(SceneBuilder scene, SceneBuildingUtil util, String id, String title) {
        scene.title(id, title);
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.rotateCameraY(180);
        scene.idle(10);
    }

    private static ElementLink<WorldSectionElement> shown(SceneBuilder scene, Selection selection, Vec3 offset) {
        var link = scene.world().showIndependentSectionImmediately(selection);
        scene.world().moveSection(link, offset, 0);
        return link;
    }

    private static void fade(SceneBuilder scene, List<ElementLink<WorldSectionElement>> links) {
        for (var link : links) scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, link));
    }

    private static ItemStack bucket(FluidEntry<?> fluid) {
        return new ItemStack(fluid.getSource().getBucket());
    }

    private static ItemStack ingotMold() {
        var mold = AllItems.CAST_IRON_MOLD.asStack();
        mold.set(AllDataComponents.CAST_TYPE, AllTags.CAST_INGOTS);
        return mold;
    }

    private static LerpedFloat chase(@Nullable LerpedFloat level, float value, int ticks) {
        if (level == null || ticks <= 0) return LerpedFloat.linear().startWithValue(value);
        return level.chaseTimed(value, ticks);
    }

    private static List<FluidStack> stacks(int capacity, Part... parts) {
        var list = new ArrayList<FluidStack>();
        for (var part : parts) if (part != null && part.share > 0) list.add(new FluidStack(part.fluid, Math.max(1, Math.round(capacity * part.share))));
        return list;
    }

    private static void heat(SceneBuilder scene, Selection selection, Class<? extends BlockEntity> type, Tiers tier, Type heatType) {
        scene.world().modifyBlockEntityNBT(selection, type, nbt -> Heat.of(tier, heatType).write(nbt), true);
    }

    private static void fill(SceneBuilder scene, BlockPos ctrl, int ticks, Part... parts) {
        scene.world().modifyBlockEntity(ctrl, CrucibleBE.class, be -> {
            be.tankInventory.setFluids(stacks(be.tankInventory.getCapacity(), parts));
            be.fluidLevel = chase(be.fluidLevel, be.getFillState(), ticks);
        });
    }

    private static void melter(SceneBuilder scene, BlockPos pos, ItemStack items, Part... fluid) {
        scene.world().modifyBlockEntity(pos, MelterBE.class, be -> {
            be.getItemInventory().setStackInSlot(0, items.copy());
            be.getTankInventory().setFluids(stacks(MelterBE.CAPACITY, fluid));
            be.setFluidLevel(chase(be.getFluidLevel(), be.getFillState(), 5));
        });
    }

    private static void windows(SceneBuilder scene, Selection selection, BlockPos ctrl, boolean window) {
        scene.world().modifyBlocks(selection, state -> !state.is(AllBlocks.CRUCIBLE) || state.getValue(CrucibleBE.SHAPE) == CrucibleBE.Shape.INNER && !state.getValue(CrucibleBE.TOP) && !state.getValue(CrucibleBE.BOTTOM) ? state : state.setValue(CrucibleBE.WINDOW, window), false);
        scene.world().modifyBlockEntity(ctrl, CrucibleBE.class, be -> be.window = window);
    }

    private static void mold(SceneBuilder scene, BlockPos pos, ItemStack mold) {
        scene.world().modifyBlockEntity(pos, TableBE.class, be -> {
            be.itemInventory.setItem(TableBE.MOLD, mold.copy());
            be.refreshMold();
        });
    }

    private static void drain(SceneBuilder scene, BlockPos pos, @Nullable FluidEntry<?> fluid, BlockPos target, DrainState state) {
        scene.world().modifyBlockEntity(pos, DrainBE.class, be -> {
            be.drainState = state;
            be.drainingFluid = fluid == null ? FluidStack.EMPTY : new FluidStack((Fluid) fluid.getSource(), 1);
            be.target = fluid == null ? null : target;
        });
    }

    private static void cast(SceneBuilder scene, BlockPos pos, FluidEntry<?> fluid, int mb, int ticks) {
        scene.world().modifyBlockEntity(pos, CastingBE.class, be -> {
            be.tankInventory.setCapacity(mb);
            be.tankInventory.setFluid(new FluidStack((Fluid) fluid.getSource(), mb));
            be.fluidLevel = chase(LerpedFloat.linear().startWithValue(0), 1, ticks);
        });
    }

    private static void cool(SceneBuilder scene, BlockPos pos, ItemStack result, int ticks) {
        scene.world().modifyBlockEntity(pos, CastingBE.class, be -> {
            be.preview = result.copy();
            be.coolingTarget = ticks;
            be.coolingProgress = 1;
        });
    }

    private static void result(SceneBuilder scene, BlockPos pos, ItemStack result) {
        scene.world().modifyBlockEntity(pos, CastingBE.class, be -> {
            be.itemInventory.setItem(CastingBE.RESULT, result.copy());
            be.tankInventory.setFluid(FluidStack.EMPTY);
            be.fluidLevel = chase(null, 0, 0);
            be.preview = ItemStack.EMPTY;
            be.coolingProgress = 0;
            be.coolingTarget = 0;
        });
    }
}
