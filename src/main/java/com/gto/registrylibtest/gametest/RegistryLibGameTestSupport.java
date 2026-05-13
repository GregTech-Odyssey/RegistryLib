package com.gto.registrylibtest.gametest;

import com.gto.registrylibtest.RegistryLibTest;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class RegistryLibGameTestSupport {

    private static final Map<Identifier, Consumer<GameTestHelper>> CALLBACKS = new LinkedHashMap<>();
    private static final MapCodec<CallbackGameTestInstance> CALLBACK_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    TestData.CODEC.forGetter(CallbackGameTestInstance::callbackInfo),
                    Identifier.CODEC.fieldOf("callback").forGetter(CallbackGameTestInstance::callbackId))
            .apply(instance, CallbackGameTestInstance::new));

    @SuppressWarnings("unused")
    private static final MapCodec<CallbackGameTestInstance> CALLBACK_TYPE = RegistryLibTest.REGISTRYLIB.registry(
            "callback_game_test",
            CALLBACK_CODEC,
            Registries.TEST_INSTANCE_TYPE);

    private RegistryLibGameTestSupport() {}

    public static void bootstrap() {
        // Force the custom test instance type registration to be queued during mod init.
    }

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
        Identifier callbackId = Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, name);
        CALLBACKS.put(callbackId, callback);
        event.registerTest(
                Identifier.fromNamespaceAndPath(RegistryLibTest.MOD_ID, name),
                new CallbackGameTestInstance(testData(environment, maxTicks), callbackId));
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

        private final Identifier callbackId;

        private CallbackGameTestInstance(
                                         TestData<Holder<TestEnvironmentDefinition<?>>> info,
                                         Identifier callbackId) {
            super(info);
            this.callbackId = callbackId;
        }

        private TestData<Holder<TestEnvironmentDefinition<?>>> callbackInfo() {
            return info();
        }

        private Identifier callbackId() {
            return callbackId;
        }

        @Override
        public void run(GameTestHelper helper) {
            Consumer<GameTestHelper> callback = CALLBACKS.get(callbackId);
            if (callback == null) {
                helper.fail(Component.literal("Missing RegistryLib GameTest callback: " + callbackId));
                return;
            }
            callback.accept(helper);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return CALLBACK_CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("RegistryLib callback");
        }
    }
}
