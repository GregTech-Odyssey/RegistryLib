package net.minecraft.client.renderer.block.dispatch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.dispatch.multipart.MultiPartModel;
import net.minecraft.client.renderer.block.dispatch.multipart.Selector;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public record BlockStateModelDispatcher(
    Optional<BlockStateModelDispatcher.SimpleModelSelectors> simpleModels, Optional<BlockStateModelDispatcher.MultiPartDefinition> multiPart
    , Optional<net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition> customDefinition
) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final com.mojang.serialization.MapCodec<BlockStateModelDispatcher> VANILLA_CODEC = RecordCodecBuilder.<BlockStateModelDispatcher>mapCodec(
            i -> i.group(
                    BlockStateModelDispatcher.SimpleModelSelectors.CODEC.optionalFieldOf("variants").forGetter(BlockStateModelDispatcher::simpleModels),
                    BlockStateModelDispatcher.MultiPartDefinition.CODEC.optionalFieldOf("multipart").forGetter(BlockStateModelDispatcher::multiPart)
                )
                .apply(i, BlockStateModelDispatcher::new)
        )
        .validate(
            o -> o.simpleModels().isEmpty() && o.multiPart().isEmpty()
                ? DataResult.error(() -> "Neither 'variants' nor 'multipart' found")
                : DataResult.success(o)
        );
    public static final Codec<BlockStateModelDispatcher> CODEC = net.neoforged.neoforge.client.model.block.BlockStateModelHooks.makeDefinitionCodec();

    public BlockStateModelDispatcher(Optional<BlockStateModelDispatcher.SimpleModelSelectors> simpleModels, Optional<BlockStateModelDispatcher.MultiPartDefinition> multiPart) {
        this(simpleModels, multiPart, Optional.empty());
    }

    // Neo: convenience constructor for datagen of custom definitions
    public BlockStateModelDispatcher(net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition customDefinition) {
        this(Optional.empty(), Optional.empty(), Optional.of(customDefinition));
    }

    public Map<BlockState, BlockStateModel.UnbakedRoot> instantiate(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> source) {
        if (this.customDefinition.isPresent()) {
            return this.customDefinition.get().instantiate(stateDefinition, source);
        }
        return this.instantiateVanilla(stateDefinition, source);
    }

    // Neo: split off original implementation as a separate method to allow custom definitions to instantiate the original definition and post-process the result
    public Map<BlockState, BlockStateModel.UnbakedRoot> instantiateVanilla(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> source) {
        Map<BlockState, BlockStateModel.UnbakedRoot> matchedStates = new IdentityHashMap<>();
        this.simpleModels.ifPresent(s -> s.instantiate(stateDefinition, source, (state, model) -> {
            BlockStateModel.UnbakedRoot previousValue = matchedStates.put(state, model);
            if (previousValue != null) {
                throw new IllegalArgumentException("Overlapping definition on state: " + state);
            }
        }));
        this.multiPart.ifPresent(m -> {
            List<BlockState> possibleStates = stateDefinition.getPossibleStates();
            BlockStateModel.UnbakedRoot model = m.instantiate(stateDefinition);

            for (BlockState state : possibleStates) {
                matchedStates.putIfAbsent(state, model);
            }
        });
        return matchedStates;
    }

    @OnlyIn(Dist.CLIENT)
    public record MultiPartDefinition(List<Selector> selectors) {
        public static final Codec<BlockStateModelDispatcher.MultiPartDefinition> CODEC = ExtraCodecs.nonEmptyList(Selector.CODEC.listOf())
            .xmap(BlockStateModelDispatcher.MultiPartDefinition::new, BlockStateModelDispatcher.MultiPartDefinition::selectors);

        public MultiPartModel.Unbaked instantiate(StateDefinition<Block, BlockState> stateDefinition) {
            Builder<MultiPartModel.Selector<BlockStateModel.Unbaked>> instantiatedSelectors = ImmutableList.builderWithExpectedSize(this.selectors.size());

            for (Selector selector : this.selectors) {
                instantiatedSelectors.add(new MultiPartModel.Selector<>(selector.instantiate(stateDefinition), selector.variant()));
            }

            return new MultiPartModel.Unbaked(instantiatedSelectors.build());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record SimpleModelSelectors(Map<String, BlockStateModel.Unbaked> models) {
        public static final Codec<BlockStateModelDispatcher.SimpleModelSelectors> CODEC = ExtraCodecs.nonEmptyMap(
                Codec.unboundedMap(Codec.STRING, BlockStateModel.Unbaked.CODEC)
            )
            .xmap(BlockStateModelDispatcher.SimpleModelSelectors::new, BlockStateModelDispatcher.SimpleModelSelectors::models);

        public void instantiate(
            StateDefinition<Block, BlockState> stateDefinition, Supplier<String> source, BiConsumer<BlockState, BlockStateModel.UnbakedRoot> output
        ) {
            this.models
                .forEach(
                    (selectorString, model) -> {
                        try {
                            Predicate<StateHolder<Block, BlockState>> selector = VariantSelector.predicate(stateDefinition, selectorString);
                            BlockStateModel.UnbakedRoot wrapper = model.asRoot();

                            for (BlockState state : stateDefinition.getPossibleStates()) {
                                if (selector.test(state)) {
                                    output.accept(state, wrapper);
                                }
                            }
                        } catch (Exception var9) {
                            BlockStateModelDispatcher.LOGGER
                                .warn("Exception loading blockstate definition: '{}' for variant: '{}': {}", source.get(), selectorString, var9.getMessage());
                        }
                    }
                );
        }
    }
}
