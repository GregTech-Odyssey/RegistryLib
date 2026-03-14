package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTable implements Validatable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<ResourceKey<LootTable>> KEY_CODEC = ResourceKey.codec(Registries.LOOT_TABLE);
    public static final ContextKeySet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
    public static final long RANDOMIZE_SEED = 0L;
    public static final Codec<LootTable> DIRECT_CODEC = Codec.lazyInitialized(
        () -> RecordCodecBuilder.create(
            i -> i.group(
                    LootContextParamSets.CODEC.lenientOptionalFieldOf("type", DEFAULT_PARAM_SET).forGetter(t -> t.paramSet),
                    Identifier.CODEC.optionalFieldOf("random_sequence").forGetter(t -> t.randomSequence),
                    net.neoforged.neoforge.common.CommonHooks.lootPoolsCodec(LootPool::setName).optionalFieldOf("pools", List.of()).forGetter(t -> t.pools),
                    net.neoforged.neoforge.common.conditions.ConditionalOps.decodeListWithElementConditions(LootItemFunctions.ROOT_CODEC).optionalFieldOf("functions", List.of()).forGetter(t -> t.functions)
                )
                .apply(i, LootTable::new)
        )
    );
    public static final Codec<Optional<net.neoforged.neoforge.common.conditions.WithConditions<LootTable>>> CONDITIONAL_DIRECT_CODEC =
            net.neoforged.neoforge.common.conditions.ConditionalOps.createConditionalCodecWithConditions(DIRECT_CODEC);
    public static final Codec<Holder<LootTable>> CODEC = RegistryFileCodec.create(Registries.LOOT_TABLE, DIRECT_CODEC);
    public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, Optional.empty(), List.of(), List.of());
    private final ContextKeySet paramSet;
    private final Optional<Identifier> randomSequence;
    private final List<LootPool> pools;
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    private LootTable(ContextKeySet paramSet, Optional<Identifier> randomSequence, List<LootPool> pools, List<LootItemFunction> functions) {
        this.paramSet = paramSet;
        this.randomSequence = randomSequence;
        this.pools = Lists.newArrayList(pools);
        this.functions = functions;
        this.compositeFunction = LootItemFunctions.compose(functions);
    }

    public static Consumer<ItemStack> createStackSplitter(ServerLevel level, Consumer<ItemStack> output) {
        return result -> {
            if (result.isItemEnabled(level.enabledFeatures())) {
                if (result.getCount() < result.getMaxStackSize()) {
                    output.accept(result);
                } else {
                    int count = result.getCount();

                    while (count > 0) {
                        ItemStack copy = result.copyWithCount(Math.min(result.getMaxStackSize(), count));
                        count -= copy.getCount();
                        output.accept(copy);
                    }
                }
            }
        };
    }

    @Deprecated // Use a non-'Raw' version of 'getRandomItems', so that the Forge Global Loot Modifiers will be applied
    public void getRandomItemsRaw(LootParams params, Consumer<ItemStack> output) {
        this.getRandomItemsRaw(new LootContext.Builder(params).create(this.randomSequence), output);
    }

    @Deprecated // Use a non-'Raw' version of 'getRandomItems', so that the Forge Global Loot Modifiers will be applied
    public void getRandomItemsRaw(LootContext context, Consumer<ItemStack> output) {
        LootContext.VisitedEntry<?> breadcrumb = LootContext.createVisitedEntry(this);
        if (context.pushVisitedElement(breadcrumb)) {
            Consumer<ItemStack> decoratedOutput = LootItemFunction.decorate(this.compositeFunction, output, context);

            for (LootPool pool : this.pools) {
                pool.addRandomItems(decoratedOutput, context);
            }

            context.popVisitedElement(breadcrumb);
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
        }
    }

    public void getRandomItems(LootParams params, long optionalLootTableSeed, Consumer<ItemStack> output) {
        this.getRandomItems((new LootContext.Builder(params)).withOptionalRandomSeed(optionalLootTableSeed).create(this.randomSequence)).forEach(output);
    }

    public void getRandomItems(LootParams params, Consumer<ItemStack> output) {
        this.getRandomItems(params).forEach(output);
    }

    public void getRandomItems(LootContext context, Consumer<ItemStack> output) {
        this.getRandomItems(context).forEach(output);
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams params, RandomSource randomSource) {
        return this.getRandomItems(new LootContext.Builder(params).withOptionalRandomSource(randomSource).create(this.randomSequence));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams params, long optionalLootTableSeed) {
        return this.getRandomItems(new LootContext.Builder(params).withOptionalRandomSeed(optionalLootTableSeed).create(this.randomSequence));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams params) {
        return this.getRandomItems(new LootContext.Builder(params).create(this.randomSequence));
    }

    private ObjectArrayList<ItemStack> getRandomItems(LootContext context) {
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        this.getRandomItemsRaw(context, createStackSplitter(context.getLevel(), result::add));
        result = net.neoforged.neoforge.common.CommonHooks.modifyLoot(this.getLootTableId(), result, context);
        return result;
    }

    public ContextKeySet getParamSet() {
        return this.paramSet;
    }

    @Override
    public void validate(ValidationContext context) {
        Validatable.validate(context, "pools", this.pools);
        Validatable.validate(context, "functions", this.functions);
    }

    public void fill(Container container, LootParams params, long optionalRandomSeed) {
        LootContext context = new LootContext.Builder(params).withOptionalRandomSeed(optionalRandomSeed).create(this.randomSequence);
        ObjectArrayList<ItemStack> itemStacks = this.getRandomItems(context);
        RandomSource random = context.getRandom();
        List<Integer> availableSlots = this.getAvailableSlots(container, random);
        this.shuffleAndSplitItems(itemStacks, availableSlots.size(), random);

        for (ItemStack itemStack : itemStacks) {
            if (availableSlots.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }

            if (itemStack.isEmpty()) {
                container.setItem(availableSlots.remove(availableSlots.size() - 1), ItemStack.EMPTY);
            } else {
                container.setItem(availableSlots.remove(availableSlots.size() - 1), itemStack);
            }
        }
    }

    private void shuffleAndSplitItems(ObjectArrayList<ItemStack> result, int availableSlots, RandomSource random) {
        List<ItemStack> splittableItems = Lists.newArrayList();
        Iterator<ItemStack> iterator = result.iterator();

        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next();
            if (itemStack.isEmpty()) {
                iterator.remove();
            } else if (itemStack.getCount() > 1) {
                splittableItems.add(itemStack);
                iterator.remove();
            }
        }

        while (availableSlots - result.size() - splittableItems.size() > 0 && !splittableItems.isEmpty()) {
            ItemStack itemStack = splittableItems.remove(Mth.nextInt(random, 0, splittableItems.size() - 1));
            int remove = Mth.nextInt(random, 1, itemStack.getCount() / 2);
            ItemStack copy = itemStack.split(remove);
            if (itemStack.getCount() > 1 && random.nextBoolean()) {
                splittableItems.add(itemStack);
            } else {
                result.add(itemStack);
            }

            if (copy.getCount() > 1 && random.nextBoolean()) {
                splittableItems.add(copy);
            } else {
                result.add(copy);
            }
        }

        result.addAll(splittableItems);
        Util.shuffle(result, random);
    }

    private List<Integer> getAvailableSlots(Container container, RandomSource random) {
        ObjectArrayList<Integer> slots = new ObjectArrayList<>();

        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).isEmpty()) {
                slots.add(i);
            }
        }

        Util.shuffle(slots, random);
        return slots;
    }

    public static LootTable.Builder lootTable() {
        return new LootTable.Builder();
    }

    // Neo: Implement LootTable freezing to prevent manipulation outside of Neo APIs
    private boolean isFrozen = false;

    public void freeze() {
        this.isFrozen = true;
        this.pools.forEach(LootPool::freeze);
    }

    public boolean isFrozen() {
        return this.isFrozen;
    }

    private void checkFrozen() {
        if (this.isFrozen())
            throw new RuntimeException("Attempted to modify LootTable after being finalized!");
    }

    // Neo: Linking the LootTable to its ID for easier retrieval
    private Identifier lootTableId;

    public void setLootTableId(final Identifier id) {
        if (this.lootTableId != null) throw new IllegalStateException("Attempted to rename loot table from '" + this.lootTableId + "' to '" + id + "': this is not supported");
        this.lootTableId = java.util.Objects.requireNonNull(id);
    }

    public Identifier getLootTableId() {
        return this.lootTableId;
    }

    // Neo: Retrieve LootPools by name
    @org.jspecify.annotations.Nullable
    public LootPool getPool(String name) {
        return pools.stream().filter(e -> name.equals(e.getName())).findFirst().orElse(null);
    }

    // Neo: Remove LootPools by name
    @org.jspecify.annotations.Nullable
    public LootPool removePool(String name) {
        checkFrozen();
        for (LootPool pool : this.pools) {
            if (name.equals(pool.getName())) {
                this.pools.remove(pool);
                return pool;
            }
        }
        return null;
    }

    // Neo: Allow adding new pools to LootTable
    public void addPool(LootPool pool) {
        checkFrozen();
        if (pools.stream().anyMatch(e -> e == pool || e.getName() != null && e.getName().equals(pool.getName())))
            throw new RuntimeException("Attempted to add a duplicate pool to loot table: " + pool.getName());
        this.pools.add(pool);
    }

    public static class Builder implements FunctionUserBuilder<LootTable.Builder> {
        private final ImmutableList.Builder<LootPool> pools = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
        private ContextKeySet paramSet = LootTable.DEFAULT_PARAM_SET;
        private Optional<Identifier> randomSequence = Optional.empty();
        private final List<net.neoforged.neoforge.common.conditions.ICondition> conditions = new java.util.ArrayList<>();

        public LootTable.Builder withPool(LootPool.Builder pool) {
            this.pools.add(pool.build());
            return this;
        }

        public LootTable.Builder setParamSet(ContextKeySet paramSet) {
            this.paramSet = paramSet;
            return this;
        }

        public LootTable.Builder setRandomSequence(Identifier key) {
            this.randomSequence = Optional.of(key);
            return this;
        }

        public LootTable.Builder apply(LootItemFunction.Builder function) {
            this.functions.add(function.build());
            return this;
        }

        public LootTable.Builder unwrap() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.paramSet, this.randomSequence, this.pools.build(), this.functions.build());
        }

        public LootTable.Builder withCondition(net.neoforged.neoforge.common.conditions.ICondition condition) {
            this.conditions.add(condition);
            return this;
        }

        public LootTable.Builder withConditions(List<net.neoforged.neoforge.common.conditions.ICondition> conditions) {
            this.conditions.addAll(conditions);
            return this;
        }

        public List<net.neoforged.neoforge.common.conditions.ICondition> buildConditions() {
            return List.copyOf(this.conditions);
        }
    }
}
