package com.gto.registrylibtest.gametest;

import com.gto.registrylibtest.RegistryLibTest;
import com.gto.registrylibtest.block.SimpleBlockExample;
import com.gto.registrylibtest.crop.SimpleCropExample;
import com.gto.registrylibtest.state.SimpleStateExample;
import com.gto.registrylibtest.worldgen.SimpleWorldgenExample;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class RegistryLibFeatureGameTests {

    private RegistryLibFeatureGameTests() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegistryLibFeatureGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = RegistryLibGameTestSupport.registerEnvironment(event, "feature_environment");
        RegistryLibGameTestSupport.register(
                event,
                environment,
                "state_debug_commands",
                40,
                RegistryLibFeatureGameTests::stateDebugCommands);
        RegistryLibGameTestSupport.register(
                event, environment, "essence_crop", 120, RegistryLibFeatureGameTests::essenceCrop);
        RegistryLibGameTestSupport.register(
                event, environment, "essence_worldgen", 80, RegistryLibFeatureGameTests::essenceWorldgen);
    }

    private static void stateDebugCommands(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos pos = level.getChunk(helper.absolutePos(BlockPos.ZERO)).getPos();
        CommandDispatcher<CommandSourceStack> dispatcher = level.getServer().getCommands().getDispatcher();
        CommandSourceStack admin = source(helper, 4);

        assertCommandSucceeds(helper, dispatcher, admin, "registrylib state list");
        assertCommandSucceeds(
                helper, dispatcher, admin, "registrylib state debug registrylibtest:essence_epoch");
        assertCommandSucceeds(
                helper,
                dispatcher,
                admin,
                "registrylib state debug registrylibtest:ambient_essence " + pos.x() + " " + pos.z());

        assertCommandSucceeds(
                helper, dispatcher, admin, "registrylib state set registrylibtest:essence_epoch 21");
        helper.assertValueEqual(
                SimpleStateExample.ESSENCE_EPOCH.getIfPresent(level).orElse(-1),
                21,
                Component.literal("world state command set"));

        assertCommandSucceeds(
                helper,
                dispatcher,
                admin,
                "registrylib state set registrylibtest:ambient_essence " + pos.x() + " " + pos.z() + " 9");
        helper.assertValueEqual(
                SimpleStateExample.AMBIENT_ESSENCE.getIfLoaded(level, pos).orElse(-1),
                9,
                Component.literal("chunk state command set"));

        assertCommandSucceeds(
                helper, dispatcher, admin, "registrylib state get registrylibtest:essence_epoch");
        assertCommandSucceeds(
                helper,
                dispatcher,
                admin,
                "registrylib state get registrylibtest:ambient_essence " + pos.x() + " " + pos.z());

        assertCommandFails(dispatcher, source(helper, 1), "registrylib state list");
        assertCommandFails(
                dispatcher, admin, "registrylib state set registrylibtest:ambient_essence 12");
        assertCommandFails(
                dispatcher, admin, "registrylib state set registrylibtest:essence_epoch nope");
        assertCommandFails(
                dispatcher, admin, "registrylib state set registrylibtest:essence_epoch \"text\"");

        assertSuggests(
                helper,
                dispatcher,
                admin,
                "registrylib state get registrylibtest:a",
                "registrylibtest:ambient_essence");
        assertSuggests(
                helper,
                dispatcher,
                admin,
                "registrylib state set registrylibtest:e",
                "registrylibtest:essence_epoch");
        helper.succeed();
    }

    private static void essenceCrop(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos cropPos = new BlockPos(2, 2, 2);
        BlockPos absoluteCropPos = helper.absolutePos(cropPos);
        var crop = SimpleCropExample.ESSENCE_CARROT.get();

        helper.setBlock(cropPos.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(cropPos, crop.getStateForAge(0));
        helper.assertValueEqual(crop.getMaxAge(), 7, Component.literal("crop max age"));
        helper.assertTrue(
                helper.getBlockState(cropPos).canSurvive(level, absoluteCropPos),
                Component.literal("crop survives on farmland"));

        BlockPos wrongCropPos = new BlockPos(4, 2, 2);
        helper.setBlock(wrongCropPos.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(wrongCropPos, crop.getStateForAge(0));
        helper.assertFalse(
                helper.getBlockState(wrongCropPos).canSurvive(level, helper.absolutePos(wrongCropPos)),
                Component.literal("crop rejects wrong soil"));

        ChunkPos chunkPos = level.getChunk(absoluteCropPos).getPos();
        SimpleStateExample.AMBIENT_ESSENCE.set(level, chunkPos, 7);
        int ageBeforeTicks = crop.getAge(helper.getBlockState(cropPos));
        for (int i = 0; i < 80 && crop.getAge(helper.getBlockState(cropPos)) == ageBeforeTicks; i++) {
            helper.randomTick(cropPos);
        }
        helper.assertTrue(
                crop.getAge(helper.getBlockState(cropPos)) > ageBeforeTicks,
                Component.literal("crop growth reads high ambient essence"));

        crop.performBonemeal(
                level, RandomSource.create(9L), absoluteCropPos, helper.getBlockState(cropPos));
        helper.assertTrue(
                crop.getAge(helper.getBlockState(cropPos)) <= 7,
                Component.literal("bonemeal does not exceed max age"));

        List<net.minecraft.world.item.ItemStack> drops = Block.getDrops(crop.getStateForAge(7), level, absoluteCropPos, null);
        var seedItem = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, "essence_carrot_seeds"));
        helper.assertTrue(
                drops.stream().anyMatch(stack -> stack.is(Items.CARROT)),
                Component.literal("mature crop loot includes produce"));
        helper.assertTrue(
                seedItem != null && drops.stream().anyMatch(stack -> stack.is(seedItem)),
                Component.literal("mature crop loot includes seeds"));

        SimpleStateExample.ESSENCE_EPOCH.set(level, 30);
        helper.setBlock(cropPos, crop.getStateForAge(7));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        InteractionResult result = helper
                .getBlockState(cropPos)
                .useWithoutItem(
                        level,
                        player,
                        new net.minecraft.world.phys.BlockHitResult(
                                Vec3.atCenterOf(absoluteCropPos),
                                net.minecraft.core.Direction.UP,
                                absoluteCropPos,
                                true));
        helper.assertTrue(
                result.consumesAction(),
                Component.literal("mature crop right-click harvest consumes action"));
        helper.assertValueEqual(
                crop.getAge(helper.getBlockState(cropPos)),
                0,
                Component.literal("harvest replants age zero"));
        helper.assertValueEqual(
                SimpleStateExample.ESSENCE_EPOCH.getIfPresent(level).orElse(-1),
                31,
                Component.literal("harvest callback fires once"));
        helper.succeed();
    }

    private static void essenceWorldgen(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var configured = level
                .registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .get(SimpleWorldgenExample.ESSENCE_NODE_PATCH.configuredKey());
        var placed = level
                .registryAccess()
                .lookupOrThrow(Registries.PLACED_FEATURE)
                .get(SimpleWorldgenExample.ESSENCE_NODE_PATCH.placedKey());
        var modifier = level
                .registryAccess()
                .lookupOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS)
                .get(
                        Identifier.fromNamespaceAndPath(
                                RegistryLibTest.MOD_ID, "essence_node_patch_add_feature"));

        helper.assertTrue(
                configured.isPresent(), Component.literal("configured feature is registered"));
        helper.assertTrue(placed.isPresent(), Component.literal("placed feature is registered"));
        helper.assertTrue(modifier.isPresent(), Component.literal("biome modifier is registered"));
        helper.assertTrue(
                modifier.orElseThrow().value() instanceof BiomeModifiers.AddFeaturesBiomeModifier addFeatures && addFeatures.step() == GenerationStep.Decoration.VEGETAL_DECORATION,
                Component.literal("biome modifier step"));
        helper.assertTrue(
                placed.orElseThrow().value().placement().stream()
                        .anyMatch(modifierEntry -> modifierEntry.type() == PlacementModifierType.HEIGHTMAP),
                Component.literal("placed feature uses heightmap placement"));
        helper.assertTrue(
                placed.orElseThrow().value().placement().stream()
                        .anyMatch(modifierEntry -> modifierEntry.type() == PlacementModifierType.BIOME_FILTER),
                Component.literal("placed feature uses biome filter"));

        BlockPos origin = new BlockPos(2, 2, 2);
        prepareWorldgenSurface(helper, origin, Blocks.GRASS_BLOCK.defaultBlockState());
        boolean placedResult = tryPlace(placed.orElseThrow(), level, helper.absolutePos(origin.above()));
        int generated = countBlocks(
                helper,
                origin.offset(-1, -1, -1),
                origin.offset(18, 8, 18),
                SimpleBlockExample.DECORATIVE_STONE.get().defaultBlockState());
        helper.assertTrue(
                placedResult && generated > 0, Component.literal("placed feature generates on grass"));

        BlockPos negativeOrigin = new BlockPos(34, 2, 2);
        prepareWorldgenSurface(helper, negativeOrigin, Blocks.STONE.defaultBlockState());
        boolean negativeResult = tryPlace(placed.orElseThrow(), level, helper.absolutePos(negativeOrigin.above()));
        int negativeGenerated = countBlocks(
                helper,
                negativeOrigin.offset(-1, -1, -1),
                negativeOrigin.offset(18, 8, 18),
                SimpleBlockExample.DECORATIVE_STONE.get().defaultBlockState());
        helper.assertFalse(
                negativeResult || negativeGenerated > 0,
                Component.literal("placed feature rejects wrong base"));
        helper.succeed();
    }

    private static boolean tryPlace(
                                    Holder.Reference<net.minecraft.world.level.levelgen.placement.PlacedFeature> placed,
                                    ServerLevel level,
                                    BlockPos origin) {
        net.minecraft.world.level.levelgen.placement.PlacedFeature feature = new net.minecraft.world.level.levelgen.placement.PlacedFeature(
                placed.value().feature(),
                placed.value().placement().stream()
                        .filter(modifier -> modifier.type() == PlacementModifierType.BLOCK_PREDICATE_FILTER)
                        .toList());
        for (long seed = 0; seed < 64; seed++) {
            if (feature.place(
                    level, level.getChunkSource().getGenerator(), RandomSource.create(seed), origin)) {
                return true;
            }
        }
        return false;
    }

    private static CommandSourceStack source(GameTestHelper helper, int permission) {
        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        return helper
                .getLevel()
                .getServer()
                .createCommandSourceStack()
                .withLevel(helper.getLevel())
                .withPosition(Vec3.atCenterOf(pos))
                .withPermission(permissionSet(permission))
                .withSuppressedOutput();
    }

    private static PermissionSet permissionSet(int level) {
        return permission -> level >= requiredLevel(permission);
    }

    private static int requiredLevel(Permission permission) {
        if (permission == Permissions.COMMANDS_OWNER) return 4;
        if (permission == Permissions.COMMANDS_ADMIN) return 3;
        if (permission == Permissions.COMMANDS_GAMEMASTER) return 2;
        if (permission == Permissions.COMMANDS_MODERATOR) return 1;
        return 0;
    }

    private static void assertCommandSucceeds(
                                              GameTestHelper helper,
                                              CommandDispatcher<CommandSourceStack> dispatcher,
                                              CommandSourceStack source,
                                              String command) {
        try {
            helper.assertTrue(
                    dispatcher.execute(command, source) > 0,
                    Component.literal("command succeeds: " + command));
        } catch (CommandSyntaxException exception) {
            helper.fail(
                    Component.literal(
                            "command failed unexpectedly: " + command + " -> " + exception.getMessage()));
        }
    }

    private static void assertCommandFails(
                                           CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source, String command) {
        try {
            dispatcher.execute(command, source);
            throw new AssertionError("command succeeded unexpectedly: " + command);
        } catch (CommandSyntaxException expected) {
            // Expected command failure path.
        }
    }

    private static void assertSuggests(
                                       GameTestHelper helper,
                                       CommandDispatcher<CommandSourceStack> dispatcher,
                                       CommandSourceStack source,
                                       String command,
                                       String expected) {
        try {
            ParseResults<CommandSourceStack> parsed = dispatcher.parse(command, source);
            List<String> suggestions = dispatcher.getCompletionSuggestions(parsed).get().getList().stream()
                    .map(suggestion -> suggestion.getText())
                    .toList();
            helper.assertTrue(
                    suggestions.contains(expected), Component.literal("suggestions contain " + expected));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            helper.fail(Component.literal("suggestion interrupted: " + command));
        } catch (ExecutionException exception) {
            helper.fail(
                    Component.literal("suggestion failed: " + command + " -> " + exception.getMessage()));
        }
    }

    private static void prepareWorldgenSurface(
                                               GameTestHelper helper, BlockPos origin, BlockState base) {
        for (int x = -1; x <= 18; x++) {
            for (int z = -1; z <= 18; z++) {
                BlockPos floor = origin.offset(x, 0, z);
                for (int y = -8; y < 0; y++) {
                    helper
                            .getLevel()
                            .setBlock(helper.absolutePos(floor.above(y)), Blocks.STONE.defaultBlockState(), 3);
                }
                helper.getLevel().setBlock(helper.absolutePos(floor), base, 3);
                for (int y = 1; y <= 8; y++) {
                    helper
                            .getLevel()
                            .setBlock(helper.absolutePos(floor.above(y)), Blocks.AIR.defaultBlockState(), 3);
                }
                helper
                        .getLevel()
                        .setBlock(helper.absolutePos(floor.above(9)), Blocks.BARRIER.defaultBlockState(), 3);
            }
        }
        net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(
                helper.getLevel().getChunk(helper.absolutePos(origin)),
                EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG));
        net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(
                helper.getLevel().getChunk(helper.absolutePos(origin.offset(18, 0, 18))),
                EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG));
    }

    private static int countBlocks(
                                   GameTestHelper helper, BlockPos from, BlockPos to, BlockState state) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (helper.getBlockState(pos).is(state.getBlock())) {
                count++;
            }
        }
        return count;
    }
}
