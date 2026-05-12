package com.gto.registrylibtest.gametest;

import com.gto.registrylibtest.RegistryLibTest;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.function.Consumer;

final class RegistryLibGameTestSupport {

    private RegistryLibGameTestSupport() {}

    static Holder<TestEnvironmentDefinition<?>> registerDefaultEnvironment(RegisterGameTestsEvent event) {
        return registerEnvironment(event, "default_environment");
    }

    static Holder<TestEnvironmentDefinition<?>> registerEnvironment(RegisterGameTestsEvent event, String name) {
        return event.registerEnvironment(
                Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, name),
                new TestEnvironmentDefinition.AllOf());
    }

    static void register(
                         RegisterGameTestsEvent event,
                         Holder<TestEnvironmentDefinition<?>> environment,
                         String name,
                         int maxTicks,
                         Consumer<GameTestHelper> callback) {
        event.registerTest(
                Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, name),
                new CallbackGameTestInstance(testData(environment, maxTicks), callback));
    }

    private static TestData<Holder<TestEnvironmentDefinition<?>>> testData(
                                                                           Holder<TestEnvironmentDefinition<?>> environment,
                                                                           int maxTicks) {
        return new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                maxTicks,
                0,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                1);
    }

    private static final class CallbackGameTestInstance extends GameTestInstance {

        private final Consumer<GameTestHelper> callback;

        private CallbackGameTestInstance(
                                         TestData<Holder<TestEnvironmentDefinition<?>>> info,
                                         Consumer<GameTestHelper> callback) {
            super(info);
            this.callback = callback;
        }

        @Override
        public void run(GameTestHelper helper) {
            callback.accept(helper);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            throw new UnsupportedOperationException("Callback GameTests are registered directly and are not data-driven");
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("RegistryLib callback");
        }
    }
}
