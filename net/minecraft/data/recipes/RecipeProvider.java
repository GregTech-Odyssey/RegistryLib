package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.DyeRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.jspecify.annotations.Nullable;

public abstract class RecipeProvider {
    protected final HolderLookup.Provider registries;
    protected final HolderGetter<Item> items;
    protected final RecipeOutput output;
    private static final Map<BlockFamily.Variant, RecipeProvider.FamilyCraftingRecipeProvider> SHAPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, RecipeProvider.FamilyCraftingRecipeProvider>builder()
        .put(BlockFamily.Variant.BUTTON, (context, result, base) -> context.buttonBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.CHISELED, (context, result, base) -> context.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, result, Ingredient.of(base)))
        .put(BlockFamily.Variant.CUT, (context, result, base) -> context.cutBuilder(RecipeCategory.BUILDING_BLOCKS, result, Ingredient.of(base)))
        .put(BlockFamily.Variant.DOOR, (context, result, base) -> context.doorBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.CUSTOM_FENCE, (context, result, base) -> context.fenceBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.FENCE, (context, result, base) -> context.fenceBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (context, result, base) -> context.fenceGateBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.FENCE_GATE, (context, result, base) -> context.fenceGateBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.SIGN, (context, result, base) -> context.signBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.SLAB, (context, result, base) -> context.slabBuilder(RecipeCategory.BUILDING_BLOCKS, result, Ingredient.of(base)))
        .put(BlockFamily.Variant.STAIRS, (context, result, base) -> context.stairBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.PRESSURE_PLATE, (context, result, base) -> context.pressurePlateBuilder(RecipeCategory.REDSTONE, result, Ingredient.of(base)))
        .put(BlockFamily.Variant.POLISHED, (context, result, base) -> context.polishedBuilder(RecipeCategory.BUILDING_BLOCKS, result, Ingredient.of(base)))
        .put(BlockFamily.Variant.TRAPDOOR, (context, result, base) -> context.trapdoorBuilder(result, Ingredient.of(base)))
        .put(BlockFamily.Variant.WALL, (context, result, base) -> context.wallBuilder(RecipeCategory.DECORATIONS, result, Ingredient.of(base)))
        .put(BlockFamily.Variant.BRICKS, (context, result, base) -> context.bricksBuilder(RecipeCategory.BUILDING_BLOCKS, result, Ingredient.of(base)))
        .put(BlockFamily.Variant.TILES, (context, result, base) -> context.tilesBuilder(RecipeCategory.BUILDING_BLOCKS, result, Ingredient.of(base)))
        .build();
    private static final Map<BlockFamily.Variant, RecipeProvider.FamilyStonecutterRecipeProvider> STONECUTTER_RECIPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, RecipeProvider.FamilyStonecutterRecipeProvider>builder()
        .put(BlockFamily.Variant.SLAB, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 2))
        .put(BlockFamily.Variant.STAIRS, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
        .put(BlockFamily.Variant.BRICKS, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
        .put(BlockFamily.Variant.WALL, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.DECORATIONS, result, base, 1))
        .put(BlockFamily.Variant.CHISELED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
        .put(BlockFamily.Variant.POLISHED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
        .put(BlockFamily.Variant.CUT, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
        .put(BlockFamily.Variant.TILES, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
        .put(BlockFamily.Variant.COBBLED, (context, result, base) -> context.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, result, base, 1))
        .build();

    protected RecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        this.registries = registries;
        this.items = registries.lookupOrThrow(Registries.ITEM);
        this.output = output;
    }

    protected abstract void buildRecipes();

    protected void generateForEnabledBlockFamilies(FeatureFlagSet flagSet) {
        BlockFamilies.getAllFamilies().forEach(family -> this.generateRecipes(family, flagSet));
    }

    protected void oneToOneConversionRecipe(ItemLike product, ItemLike resource, @Nullable String group) {
        this.oneToOneConversionRecipe(product, resource, group, 1);
    }

    protected void oneToOneConversionRecipe(ItemLike product, ItemLike resource, @Nullable String group, int productCount) {
        this.shapeless(RecipeCategory.MISC, product, productCount)
            .requires(resource)
            .group(group)
            .unlockedBy(getHasName(resource), this.has(resource))
            .save(this.output, getConversionRecipeName(product, resource));
    }

    protected void oreSmelting(
        List<ItemLike> smeltables,
        RecipeCategory craftingCategory,
        CookingBookCategory cookingCategory,
        ItemLike result,
        float experience,
        int cookingTime,
        String group
    ) {
        this.oreCooking(SmeltingRecipe::new, smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, "_from_smelting");
    }

    protected void oreBlasting(
        List<ItemLike> smeltables,
        RecipeCategory craftingCategory,
        CookingBookCategory cookingCategory,
        ItemLike result,
        float experience,
        int cookingTime,
        String group
    ) {
        this.oreCooking(BlastingRecipe::new, smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, "_from_blasting");
    }

    protected <T extends AbstractCookingRecipe> void oreCooking(
        AbstractCookingRecipe.Factory<T> factory,
        List<ItemLike> smeltables,
        RecipeCategory craftingCategory,
        CookingBookCategory cookingCategory,
        ItemLike result,
        float experience,
        int cookingTime,
        String group,
        String fromDesc
    ) {
        for (ItemLike item : smeltables) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(item), craftingCategory, cookingCategory, result, experience, cookingTime, factory)
                .group(group)
                .unlockedBy(getHasName(item), this.has(item))
                .save(this.output, getItemName(result) + fromDesc + "_" + getItemName(item));
        }
    }

    protected void netheriteSmithing(Item base, RecipeCategory category, Item result) {
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(base), this.tag(ItemTags.NETHERITE_TOOL_MATERIALS), category, result
            )
            .unlocks("has_netherite_ingot", this.has(ItemTags.NETHERITE_TOOL_MATERIALS))
            .save(this.output, getItemName(result) + "_smithing");
    }

    protected void trimSmithing(Item trimTemplate, ResourceKey<TrimPattern> patternId, ResourceKey<Recipe<?>> id) {
        Holder.Reference<TrimPattern> pattern = this.registries.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(patternId);
        SmithingTrimRecipeBuilder.smithingTrim(
                Ingredient.of(trimTemplate), this.tag(ItemTags.TRIMMABLE_ARMOR), this.tag(ItemTags.TRIM_MATERIALS), pattern, RecipeCategory.MISC
            )
            .unlocks("has_smithing_trim_template", this.has(trimTemplate))
            .save(this.output, id);
    }

    protected void twoByTwoPacker(RecipeCategory category, ItemLike result, ItemLike ingredient) {
        this.shaped(category, result, 1)
            .define('#', ingredient)
            .pattern("##")
            .pattern("##")
            .unlockedBy(getHasName(ingredient), this.has(ingredient))
            .save(this.output);
    }

    protected void threeByThreePacker(RecipeCategory category, ItemLike result, ItemLike ingredient, String unlockedBy) {
        this.shapeless(category, result).requires(ingredient, 9).unlockedBy(unlockedBy, this.has(ingredient)).save(this.output);
    }

    protected void threeByThreePacker(RecipeCategory category, ItemLike result, ItemLike ingredient) {
        this.threeByThreePacker(category, result, ingredient, getHasName(ingredient));
    }

    protected void planksFromLog(ItemLike result, TagKey<Item> logs, int count) {
        this.shapeless(RecipeCategory.BUILDING_BLOCKS, result, count).requires(logs).group("planks").unlockedBy("has_log", this.has(logs)).save(this.output);
    }

    protected void planksFromLogs(ItemLike result, TagKey<Item> logs, int count) {
        this.shapeless(RecipeCategory.BUILDING_BLOCKS, result, count).requires(logs).group("planks").unlockedBy("has_logs", this.has(logs)).save(this.output);
    }

    protected void woodFromLogs(ItemLike result, ItemLike log) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, result, 3)
            .define('#', log)
            .pattern("##")
            .pattern("##")
            .group("bark")
            .unlockedBy("has_log", this.has(log))
            .save(this.output);
    }

    protected void woodenBoat(ItemLike result, ItemLike planks) {
        this.shaped(RecipeCategory.TRANSPORTATION, result)
            .define('#', planks)
            .pattern("# #")
            .pattern("###")
            .group("boat")
            .unlockedBy("in_water", insideOf(Blocks.WATER))
            .save(this.output);
    }

    protected void chestBoat(ItemLike chestBoat, ItemLike boat) {
        this.shapeless(RecipeCategory.TRANSPORTATION, chestBoat)
            .requires(Blocks.CHEST)
            .requires(boat)
            .group("chest_boat")
            .unlockedBy("has_boat", this.has(ItemTags.BOATS))
            .save(this.output);
    }

    protected RecipeBuilder buttonBuilder(ItemLike result, Ingredient base) {
        return this.shapeless(RecipeCategory.REDSTONE, result).requires(base);
    }

    protected RecipeBuilder doorBuilder(ItemLike result, Ingredient base) {
        return this.shaped(RecipeCategory.REDSTONE, result, 3).define('#', base).pattern("##").pattern("##").pattern("##");
    }

    protected RecipeBuilder fenceBuilder(ItemLike result, Ingredient base) {
        int count = result == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item base2 = result == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return this.shaped(RecipeCategory.DECORATIONS, result, count).define('W', base).define('#', base2).pattern("W#W").pattern("W#W");
    }

    protected RecipeBuilder fenceGateBuilder(ItemLike result, Ingredient planks) {
        return this.shaped(RecipeCategory.REDSTONE, result).define('#', Items.STICK).define('W', planks).pattern("#W#").pattern("#W#");
    }

    protected void pressurePlate(ItemLike result, ItemLike base) {
        this.pressurePlateBuilder(RecipeCategory.REDSTONE, result, Ingredient.of(base)).unlockedBy(getHasName(base), this.has(base)).save(this.output);
    }

    protected RecipeBuilder pressurePlateBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return this.shaped(category, result).define('#', base).pattern("##");
    }

    protected void slab(RecipeCategory category, ItemLike result, ItemLike base) {
        this.slabBuilder(category, result, Ingredient.of(base)).unlockedBy(getHasName(base), this.has(base)).save(this.output);
    }

    protected void shelf(ItemLike result, ItemLike strippedLogs) {
        this.shaped(RecipeCategory.DECORATIONS, result, 6)
            .define('#', strippedLogs)
            .pattern("###")
            .pattern("   ")
            .pattern("###")
            .group("shelf")
            .unlockedBy(getHasName(strippedLogs), this.has(strippedLogs))
            .save(this.output);
    }

    protected RecipeBuilder slabBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return this.shaped(category, result, 6).define('#', base).pattern("###");
    }

    protected RecipeBuilder stairBuilder(ItemLike result, Ingredient base) {
        return this.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4).define('#', base).pattern("#  ").pattern("## ").pattern("###");
    }

    protected RecipeBuilder trapdoorBuilder(ItemLike result, Ingredient base) {
        return this.shaped(RecipeCategory.REDSTONE, result, 2).define('#', base).pattern("###").pattern("###");
    }

    protected RecipeBuilder signBuilder(ItemLike result, Ingredient planks) {
        return this.shaped(RecipeCategory.DECORATIONS, result, 3)
            .group("sign")
            .define('#', planks)
            .define('X', Items.STICK)
            .pattern("###")
            .pattern("###")
            .pattern(" X ");
    }

    protected void hangingSign(ItemLike result, ItemLike ingredient) {
        this.shaped(RecipeCategory.DECORATIONS, result, 6)
            .group("hanging_sign")
            .define('#', ingredient)
            .define('X', Items.IRON_CHAIN)
            .pattern("X X")
            .pattern("###")
            .pattern("###")
            .unlockedBy("has_stripped_logs", this.has(ingredient))
            .save(this.output);
    }

    protected void colorItemWithDye(List<Item> dyes, List<Item> items, String groupName, RecipeCategory category) {
        this.colorWithDye(dyes, items, null, groupName, category);
    }

    protected void colorWithDye(List<Item> dyes, List<Item> dyedItems, @Nullable Item uncoloredItem, String groupName, RecipeCategory category) {
        for (int dyeIndex = 0; dyeIndex < dyes.size(); dyeIndex++) {
            Item dye = dyes.get(dyeIndex);
            Item dyedItem = dyedItems.get(dyeIndex);
            Stream<Item> sourceItems = dyedItems.stream().filter(b -> !b.equals(dyedItem));
            if (uncoloredItem != null) {
                sourceItems = Stream.concat(sourceItems, Stream.of(uncoloredItem));
            }

            this.shapeless(category, dyedItem)
                .requires(dye)
                .requires(Ingredient.of(sourceItems))
                .group(groupName)
                .unlockedBy("has_needed_dye", this.has(dye))
                .save(this.output, "dye_" + getItemName(dyedItem));
        }
    }

    protected void carpet(ItemLike result, ItemLike sourceItem) {
        this.shaped(RecipeCategory.DECORATIONS, result, 3)
            .define('#', sourceItem)
            .pattern("##")
            .group("carpet")
            .unlockedBy(getHasName(sourceItem), this.has(sourceItem))
            .save(this.output);
    }

    protected void bedFromPlanksAndWool(ItemLike result, ItemLike wool) {
        this.shaped(RecipeCategory.DECORATIONS, result)
            .define('#', wool)
            .define('X', ItemTags.PLANKS)
            .pattern("###")
            .pattern("XXX")
            .group("bed")
            .unlockedBy(getHasName(wool), this.has(wool))
            .save(this.output);
    }

    protected void banner(ItemLike result, ItemLike wool) {
        this.shaped(RecipeCategory.DECORATIONS, result)
            .define('#', wool)
            .define('|', Items.STICK)
            .pattern("###")
            .pattern("###")
            .pattern(" | ")
            .group("banner")
            .unlockedBy(getHasName(wool), this.has(wool))
            .save(this.output);
        SpecialRecipeBuilder.special(() -> new BannerDuplicateRecipe(Ingredient.of(result), new ItemStackTemplate(result.asItem())))
            .save(this.output, getItemName(result) + "_duplicate");
    }

    protected void stainedGlassFromGlassAndDye(ItemLike result, ItemLike dye) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, result, 8)
            .define('#', Blocks.GLASS)
            .define('X', dye)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .group("stained_glass")
            .unlockedBy("has_glass", this.has(Blocks.GLASS))
            .save(this.output);
    }

    protected void dryGhast(ItemLike result) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, result, 1)
            .define('#', Items.GHAST_TEAR)
            .define('X', Items.SOUL_SAND)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .group("dry_ghast")
            .unlockedBy(getHasName(Items.GHAST_TEAR), this.has(Items.GHAST_TEAR))
            .save(this.output);
    }

    protected void harness(ItemLike result, ItemLike wool) {
        this.shaped(RecipeCategory.COMBAT, result)
            .define('#', wool)
            .define('G', Items.GLASS)
            .define('L', Items.LEATHER)
            .pattern("LLL")
            .pattern("G#G")
            .group("harness")
            .unlockedBy("has_dried_ghast", this.has(Blocks.DRIED_GHAST))
            .save(this.output);
    }

    protected void stainedGlassPaneFromStainedGlass(ItemLike result, ItemLike stainedGlass) {
        this.shaped(RecipeCategory.DECORATIONS, result, 16)
            .define('#', stainedGlass)
            .pattern("###")
            .pattern("###")
            .group("stained_glass_pane")
            .unlockedBy("has_glass", this.has(stainedGlass))
            .save(this.output);
    }

    protected void stainedGlassPaneFromGlassPaneAndDye(ItemLike result, ItemLike dye) {
        this.shaped(RecipeCategory.DECORATIONS, result, 8)
            .define('#', Blocks.GLASS_PANE)
            .define('$', dye)
            .pattern("###")
            .pattern("#$#")
            .pattern("###")
            .group("stained_glass_pane")
            .unlockedBy("has_glass_pane", this.has(Blocks.GLASS_PANE))
            .unlockedBy(getHasName(dye), this.has(dye))
            .save(this.output, getConversionRecipeName(result, Blocks.GLASS_PANE));
    }

    protected void coloredTerracottaFromTerracottaAndDye(ItemLike result, ItemLike dye) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, result, 8)
            .define('#', Blocks.TERRACOTTA)
            .define('X', dye)
            .pattern("###")
            .pattern("#X#")
            .pattern("###")
            .group("stained_terracotta")
            .unlockedBy("has_terracotta", this.has(Blocks.TERRACOTTA))
            .save(this.output);
    }

    protected void concretePowder(ItemLike result, ItemLike dye) {
        this.shapeless(RecipeCategory.BUILDING_BLOCKS, result, 8)
            .requires(dye)
            .requires(Blocks.SAND, 4)
            .requires(Blocks.GRAVEL, 4)
            .group("concrete_powder")
            .unlockedBy("has_sand", this.has(Blocks.SAND))
            .unlockedBy("has_gravel", this.has(Blocks.GRAVEL))
            .save(this.output);
    }

    protected void candle(ItemLike result, ItemLike dye) {
        this.shapeless(RecipeCategory.DECORATIONS, result)
            .requires(Blocks.CANDLE)
            .requires(dye)
            .group("dyed_candle")
            .unlockedBy(getHasName(dye), this.has(dye))
            .save(this.output);
    }

    protected void wall(RecipeCategory category, ItemLike result, ItemLike base) {
        this.wallBuilder(category, result, Ingredient.of(base)).unlockedBy(getHasName(base), this.has(base)).save(this.output);
    }

    protected RecipeBuilder wallBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return this.shaped(category, result, 6).define('#', base).pattern("###").pattern("###");
    }

    private RecipeBuilder bricksBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return this.shaped(category, result, 4).define('#', base).pattern("##").pattern("##");
    }

    private RecipeBuilder tilesBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return this.shaped(category, result, 4).define('#', base).pattern("##").pattern("##");
    }

    protected void polished(RecipeCategory category, ItemLike result, ItemLike base) {
        this.polishedBuilder(category, result, Ingredient.of(base)).unlockedBy(getHasName(base), this.has(base)).save(this.output);
    }

    protected RecipeBuilder polishedBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return this.shaped(category, result, 4).define('S', base).pattern("SS").pattern("SS");
    }

    protected void cut(RecipeCategory category, ItemLike result, ItemLike base) {
        this.cutBuilder(category, result, Ingredient.of(base)).unlockedBy(getHasName(base), this.has(base)).save(this.output);
    }

    protected ShapedRecipeBuilder cutBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return this.shaped(category, result, 4).define('#', base).pattern("##").pattern("##");
    }

    protected void chiseled(RecipeCategory category, ItemLike result, ItemLike base) {
        this.chiseledBuilder(category, result, Ingredient.of(base)).unlockedBy(getHasName(base), this.has(base)).save(this.output);
    }

    protected void mosaicBuilder(RecipeCategory category, ItemLike result, ItemLike base) {
        this.shaped(category, result).define('#', base).pattern("#").pattern("#").unlockedBy(getHasName(base), this.has(base)).save(this.output);
    }

    protected ShapedRecipeBuilder chiseledBuilder(RecipeCategory category, ItemLike result, Ingredient base) {
        return this.shaped(category, result).define('#', base).pattern("#").pattern("#");
    }

    protected void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base) {
        this.stonecutterResultFromBase(category, result, base, 1);
    }

    protected void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike base, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(base), category, result, count)
            .unlockedBy(getHasName(base), this.has(base))
            .save(this.output, getConversionRecipeName(result, base) + "_stonecutting");
    }

    protected void smeltingResultFromBase(ItemLike result, ItemLike base) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(base), RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, result, 0.1F, 200)
            .unlockedBy(getHasName(base), this.has(base))
            .save(this.output);
    }

    protected void nineBlockStorageRecipes(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm) {
        this.nineBlockStorageRecipes(
            unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, getSimpleRecipeName(packedForm), null, getSimpleRecipeName(unpackedForm), null
        );
    }

    protected void nineBlockStorageRecipesWithCustomPacking(
        RecipeCategory unpackedFormCategory,
        ItemLike unpackedForm,
        RecipeCategory packedFormCategory,
        ItemLike packedForm,
        String packingRecipeId,
        String packingRecipeGroup
    ) {
        this.nineBlockStorageRecipes(
            unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, packingRecipeId, packingRecipeGroup, getSimpleRecipeName(unpackedForm), null
        );
    }

    protected void nineBlockStorageRecipesRecipesWithCustomUnpacking(
        RecipeCategory unpackedFormCategory,
        ItemLike unpackedForm,
        RecipeCategory packedFormCategory,
        ItemLike packedForm,
        String unpackingRecipeId,
        String unpackingRecipeGroup
    ) {
        this.nineBlockStorageRecipes(
            unpackedFormCategory, unpackedForm, packedFormCategory, packedForm, getSimpleRecipeName(packedForm), null, unpackingRecipeId, unpackingRecipeGroup
        );
    }

    protected void nineBlockStorageRecipes(
        RecipeCategory unpackedFormCategory,
        ItemLike unpackedForm,
        RecipeCategory packedFormCategory,
        ItemLike packedForm,
        String packingRecipeId,
        @Nullable String packingRecipeGroup,
        String unpackingRecipeId,
        @Nullable String unpackingRecipeGroup
    ) {
        this.shapeless(unpackedFormCategory, unpackedForm, 9)
            .requires(packedForm)
            .group(unpackingRecipeGroup)
            .unlockedBy(getHasName(packedForm), this.has(packedForm))
            .save(this.output, ResourceKey.create(Registries.RECIPE, Identifier.parse(unpackingRecipeId)));
        this.shaped(packedFormCategory, packedForm)
            .define('#', unpackedForm)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .group(packingRecipeGroup)
            .unlockedBy(getHasName(unpackedForm), this.has(unpackedForm))
            .save(this.output, ResourceKey.create(Registries.RECIPE, Identifier.parse(packingRecipeId)));
    }

    protected void copySmithingTemplate(ItemLike smithingTemplate, ItemLike baseMaterial) {
        this.shaped(RecipeCategory.MISC, smithingTemplate, 2)
            .define('#', Items.DIAMOND)
            .define('C', baseMaterial)
            .define('S', smithingTemplate)
            .pattern("#S#")
            .pattern("#C#")
            .pattern("###")
            .unlockedBy(getHasName(smithingTemplate), this.has(smithingTemplate))
            .save(this.output);
    }

    protected void copySmithingTemplate(ItemLike smithingTemplate, Ingredient baseMaterials) {
        this.shaped(RecipeCategory.MISC, smithingTemplate, 2)
            .define('#', Items.DIAMOND)
            .define('C', baseMaterials)
            .define('S', smithingTemplate)
            .pattern("#S#")
            .pattern("#C#")
            .pattern("###")
            .unlockedBy(getHasName(smithingTemplate), this.has(smithingTemplate))
            .save(this.output);
    }

    protected <T extends AbstractCookingRecipe> void cookRecipes(String source, AbstractCookingRecipe.Factory<T> factory, int cookingTime) {
        this.simpleCookingRecipe(source, factory, cookingTime, Items.BEEF, Items.COOKED_BEEF, 0.35F);
        this.simpleCookingRecipe(source, factory, cookingTime, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
        this.simpleCookingRecipe(source, factory, cookingTime, Items.COD, Items.COOKED_COD, 0.35F);
        this.simpleCookingRecipe(source, factory, cookingTime, Items.KELP, Items.DRIED_KELP, 0.1F);
        this.simpleCookingRecipe(source, factory, cookingTime, Items.SALMON, Items.COOKED_SALMON, 0.35F);
        this.simpleCookingRecipe(source, factory, cookingTime, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
        this.simpleCookingRecipe(source, factory, cookingTime, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
        this.simpleCookingRecipe(source, factory, cookingTime, Items.POTATO, Items.BAKED_POTATO, 0.35F);
        this.simpleCookingRecipe(source, factory, cookingTime, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
    }

    protected <T extends AbstractCookingRecipe> void simpleCookingRecipe(
        String source, AbstractCookingRecipe.Factory<T> factory, int cookingTime, ItemLike base, ItemLike result, float experience
    ) {
        SimpleCookingRecipeBuilder.generic(Ingredient.of(base), RecipeCategory.FOOD, CookingBookCategory.FOOD, result, experience, cookingTime, factory)
            .unlockedBy(getHasName(base), this.has(base))
            .save(this.output, getItemName(result) + "_from_" + source);
    }

    protected void waxRecipes(FeatureFlagSet flagSet) {
        net.neoforged.neoforge.common.DataMapHooks.INVERSE_WAXABLES_DATAMAP
            .forEach(
                (block, waxedBlock) -> {
                    if (waxedBlock.requiredFeatures().isSubsetOf(flagSet)) {
                        Pair<RecipeCategory, String> pair = HoneycombItem.WAXED_RECIPES
                            .getOrDefault(waxedBlock, Pair.of(RecipeCategory.BUILDING_BLOCKS, getItemName(waxedBlock)));
                        RecipeCategory recipeCategory = pair.getFirst();
                        String group = pair.getSecond();
                        this.shapeless(recipeCategory, waxedBlock)
                            .requires(block)
                            .requires(Items.HONEYCOMB)
                            .group(group)
                            .unlockedBy(getHasName(block), this.has(block))
                            .save(this.output, getConversionRecipeName(waxedBlock, Items.HONEYCOMB));
                    }
                }
            );
    }

    protected void grate(Block grateBlock, Block material) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, grateBlock, 4)
            .define('M', material)
            .pattern(" M ")
            .pattern("M M")
            .pattern(" M ")
            .group(getItemName(grateBlock))
            .unlockedBy(getHasName(material), this.has(material))
            .save(this.output);
    }

    protected void copperBulb(Block copperBulb, Block copperMaterial) {
        this.shaped(RecipeCategory.REDSTONE, copperBulb, 4)
            .define('C', copperMaterial)
            .define('R', Items.REDSTONE)
            .define('B', Items.BLAZE_ROD)
            .pattern(" C ")
            .pattern("CBC")
            .pattern(" R ")
            .unlockedBy(getHasName(copperMaterial), this.has(copperMaterial))
            .group(getItemName(copperBulb))
            .save(this.output);
    }

    protected void waxedChiseled(Block result, Block material) {
        this.shaped(RecipeCategory.BUILDING_BLOCKS, result)
            .define('M', material)
            .pattern(" M ")
            .pattern(" M ")
            .group(getItemName(result))
            .unlockedBy(getHasName(material), this.has(material))
            .save(this.output);
    }

    protected void suspiciousStew(Item item, SuspiciousEffectHolder effectHolder) {
        ItemStackTemplate stew = new ItemStackTemplate(
            Items.SUSPICIOUS_STEW, DataComponentPatch.builder().set(DataComponents.SUSPICIOUS_STEW_EFFECTS, effectHolder.getSuspiciousEffects()).build()
        );
        this.shapeless(RecipeCategory.FOOD, stew)
            .requires(Items.BOWL)
            .requires(Items.BROWN_MUSHROOM)
            .requires(Items.RED_MUSHROOM)
            .requires(item)
            .group("suspicious_stew")
            .unlockedBy(getHasName(item), this.has(item))
            .save(this.output, getItemName(stew.item().value()) + "_from_" + getItemName(item));
    }

    protected void dyedItem(Item target, String group) {
        CustomCraftingRecipeBuilder.customCrafting(
                RecipeCategory.MISC,
                (commonInfo, bookInfo) -> new DyeRecipe(commonInfo, bookInfo, Ingredient.of(target), this.tag(ItemTags.DYES), new ItemStackTemplate(target))
            )
            .unlockedBy(getHasName(target), this.has(target))
            .group(group)
            .save(this.output, getItemName(target) + "_dyed");
    }

    protected void dyedShulkerBoxRecipe(Item dye, Item dyedResult) {
        TransmuteRecipeBuilder.transmute(RecipeCategory.DECORATIONS, this.tag(ItemTags.SHULKER_BOXES), Ingredient.of(dye), dyedResult)
            .group("shulker_box_dye")
            .unlockedBy("has_shulker_box", this.has(ItemTags.SHULKER_BOXES))
            .save(this.output);
    }

    protected void dyedBundleRecipe(Item dye, Item dyedResult) {
        TransmuteRecipeBuilder.transmute(RecipeCategory.TOOLS, this.tag(ItemTags.BUNDLES), Ingredient.of(dye), dyedResult)
            .group("bundle_dye")
            .unlockedBy(getHasName(dye), this.has(dye))
            .save(this.output);
    }

    protected void generateRecipes(BlockFamily family, FeatureFlagSet flagSet) {
        family.getVariants().forEach((variant, result) -> {
            if (result.requiredFeatures().isSubsetOf(flagSet)) {
                if (family.shouldGenerateCraftingRecipe()) {
                    ItemLike base = this.getBaseBlockForCrafting(family, variant);
                    this.generateCraftingRecipe(family, variant, result, base);
                    if (variant == BlockFamily.Variant.CRACKED) {
                        this.smeltingResultFromBase(result, base);
                    }
                }

                if (family.shouldGenerateStonecutterRecipe()) {
                    Block base = family.getBaseBlock();
                    this.generateStonecutterRecipe(family, variant, base);
                }
            }
        });
    }

    private void generateCraftingRecipe(BlockFamily family, BlockFamily.Variant variant, Block result, ItemLike base) {
        RecipeProvider.FamilyCraftingRecipeProvider recipeFunction = SHAPE_BUILDERS.get(variant);
        if (recipeFunction != null) {
            RecipeBuilder builder = recipeFunction.create(this, result, base);
            family.getRecipeGroupPrefix()
                .ifPresent(prefix -> builder.group(prefix + (variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getRecipeGroup())));
            builder.unlockedBy(family.getRecipeUnlockedBy().orElseGet(() -> getHasName(base)), this.has(base));
            builder.save(this.output);
        }
    }

    private void generateStonecutterRecipe(BlockFamily family, BlockFamily.Variant variant, Block base) {
        RecipeProvider.FamilyStonecutterRecipeProvider recipeFunction = STONECUTTER_RECIPE_BUILDERS.get(variant);
        if (recipeFunction != null) {
            recipeFunction.create(this, family.get(variant), base);
        }

        if (variant == BlockFamily.Variant.POLISHED
            || variant == BlockFamily.Variant.CUT
            || variant == BlockFamily.Variant.BRICKS
            || variant == BlockFamily.Variant.TILES
            || variant == BlockFamily.Variant.COBBLED) {
            BlockFamily childVariantFamily = BlockFamilies.getFamily(family.get(variant));
            if (childVariantFamily != null) {
                childVariantFamily.getVariants().forEach((childVariant, r) -> this.generateStonecutterRecipe(childVariantFamily, childVariant, base));
            }
        }
    }

    protected Block getBaseBlockForCrafting(BlockFamily family, BlockFamily.Variant variant) {
        if (variant == BlockFamily.Variant.CHISELED) {
            if (!family.getVariants().containsKey(BlockFamily.Variant.SLAB)) {
                throw new IllegalStateException("Slab is not defined for the family.");
            } else {
                return family.get(BlockFamily.Variant.SLAB);
            }
        } else {
            return family.getBaseBlock();
        }
    }

    protected static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) {
        return CriteriaTriggers.ENTER_BLOCK
            .createCriterion(new EnterBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
    }

    protected Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimal() {
        return CriteriaTriggers.BRED_ANIMALS
            .createCriterion(new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
    }

    protected Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(this.items, item).withCount(count));
    }

    protected Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(this.items, item));
    }

    protected Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(this.items, tag));
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... predicates) {
        return inventoryTrigger(Arrays.stream(predicates).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {
        return CriteriaTriggers.INVENTORY_CHANGED
            .createCriterion(
                new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(predicates))
            );
    }

    protected static String getHasName(ItemLike baseBlock) {
        return "has_" + getItemName(baseBlock);
    }

    protected static String getItemName(ItemLike itemLike) {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
    }

    protected static String getSimpleRecipeName(ItemLike itemLike) {
        return getItemName(itemLike);
    }

    protected static String getConversionRecipeName(ItemLike product, ItemLike material) {
        return getItemName(product) + "_from_" + getItemName(material);
    }

    protected static String getSmeltingRecipeName(ItemLike product) {
        return getItemName(product) + "_from_smelting";
    }

    protected static String getBlastingRecipeName(ItemLike product) {
        return getItemName(product) + "_from_blasting";
    }

    protected Ingredient tag(TagKey<Item> id) {
        return Ingredient.of(this.items.getOrThrow(id));
    }

    protected ShapedRecipeBuilder shaped(RecipeCategory category, ItemStackTemplate stack) {
        return ShapedRecipeBuilder.shaped(this.items, category, stack);
    }

    protected ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item) {
        return ShapedRecipeBuilder.shaped(this.items, category, item);
    }

    protected ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike item, int count) {
        return ShapedRecipeBuilder.shaped(this.items, category, item, count);
    }

    protected ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStackTemplate result) {
        return ShapelessRecipeBuilder.shapeless(this.items, category, result);
    }

    protected ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item) {
        return ShapelessRecipeBuilder.shapeless(this.items, category, item);
    }

    protected ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike item, int count) {
        return ShapelessRecipeBuilder.shapeless(this.items, category, item, count);
    }

    @FunctionalInterface
    private interface FamilyCraftingRecipeProvider {
        RecipeBuilder create(RecipeProvider context, ItemLike result, ItemLike base);
    }

    @FunctionalInterface
    private interface FamilyStonecutterRecipeProvider {
        void create(RecipeProvider context, ItemLike result, ItemLike base);
    }

    public abstract static class Runner implements DataProvider {
        private final PackOutput packOutput;
        private final CompletableFuture<HolderLookup.Provider> registries;

        protected Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            this.packOutput = packOutput;
            this.registries = registries;
        }

        @Override
        public final CompletableFuture<?> run(CachedOutput cache) {
            return this.registries
                .thenCompose(
                    registries -> {
                        final PackOutput.PathProvider recipePathProvider = this.packOutput.createRegistryElementsPathProvider(Registries.RECIPE);
                        final PackOutput.PathProvider advancementPathProvider = this.packOutput.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
                        final Set<ResourceKey<Recipe<?>>> allRecipes = Sets.newHashSet();
                        final List<CompletableFuture<?>> tasks = new ArrayList<>();
                        RecipeOutput recipeOutput = new RecipeOutput() {
                            @Override
                            public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder, net.neoforged.neoforge.common.conditions.ICondition... conditions) {
                                if (!allRecipes.add(id)) {
                                    throw new IllegalStateException("Duplicate recipe " + id.identifier());
                                } else {
                                    this.saveRecipe(id, recipe, conditions);
                                    if (advancementHolder != null) {
                                        this.saveAdvancement(advancementHolder, conditions);
                                    }
                                }
                            }

                            @Override
                            public Advancement.Builder advancement() {
                                return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                            }

                            @Override
                            public void includeRootAdvancement() {
                                AdvancementHolder root = Advancement.Builder.recipeAdvancement()
                                    .addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                                    .build(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                                this.saveAdvancement(root);
                            }

                            private void saveRecipe(ResourceKey<Recipe<?>> id, Recipe<?> recipe) {
                                saveRecipe(id, recipe, new net.neoforged.neoforge.common.conditions.ICondition[0]);
                            }
                            private void saveRecipe(ResourceKey<Recipe<?>> id, Recipe<?> recipe, net.neoforged.neoforge.common.conditions.ICondition... conditions) {
                                tasks.add(DataProvider.saveStable(cache, registries, Recipe.CONDITIONAL_CODEC, Optional.of(new net.neoforged.neoforge.common.conditions.WithConditions<>(recipe, conditions)), recipePathProvider.json(id.identifier())));
                            }

                            private void saveAdvancement(AdvancementHolder advancementHolder) {
                                saveAdvancement(advancementHolder, new net.neoforged.neoforge.common.conditions.ICondition[0]);
                            }
                            private void saveAdvancement(AdvancementHolder advancementHolder, net.neoforged.neoforge.common.conditions.ICondition... conditions) {
                                tasks.add(
                                    DataProvider.saveStable(
                                        cache, registries, Advancement.CONDITIONAL_CODEC, Optional.of(new net.neoforged.neoforge.common.conditions.WithConditions<>(advancementHolder.value(), conditions)), advancementPathProvider.json(advancementHolder.id())
                                    )
                                );
                            }
                        };
                        this.createRecipeProvider(registries, recipeOutput).buildRecipes();
                        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
                    }
                );
        }

        protected abstract RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output);
    }
}
