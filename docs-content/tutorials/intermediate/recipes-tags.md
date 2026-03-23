---
sidebar_position: 4
title: Recipes and Tags
description: Add recipes and tags to your registered content.
---

# Recipes and Tags

## What You Will Learn

In this tutorial you will learn how to attach tags and generate recipes directly from your RegistryLib registration chains. By the end you will be able to:

- Add block, item, and fluid tags to individual entries.
- Share tags across multiple entries using the Group system.
- Generate recipes through the datagen pipeline with `ProviderType.RECIPE`.
- Combine tag and recipe datagen in a single registration flow.

## Step 1 — Add Tags to Individual Entries

Every builder provides a convenience `.addTag(...)` method that accepts one or more `TagKey` values.

### Block Tags

```java
REGISTRYLIB.block("ruby_ore", Block::new)
    .initialProperties(() -> Blocks.IRON_ORE)
    .addTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
    .simpleItem()
    .register();
```

`BlockBuilder.addTag(...)` routes to `ProviderType.BLOCK_TAGS` automatically.

### Item Tags

```java
REGISTRYLIB.item("ruby", Item::new)
    .addTag(ItemTags.DURABILITY_ENCHANTABLE)
    .register();
```

`ItemBuilder.addTag(...)` routes to `ProviderType.ITEM_TAGS` automatically.

### Block Item Tags

If you need to tag the **item form** of a block, use `.addItemTag(...)` on the `BlockBuilder`:

```java
REGISTRYLIB.block("ruby_block", Block::new)
    .initialProperties(() -> Blocks.IRON_BLOCK)
    .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
    .addItemTag(ItemTags.DURABILITY_ENCHANTABLE)
    .simpleItem()
    .register();
```

:::tip
`.addTag(...)` on a `BlockBuilder` adds **block** tags. `.addItemTag(...)` adds **item** tags to the block's item form. They do not conflict and can be used together.
:::

## Step 2 — Share Tags via the Group System

When multiple entries need the same tags, define them once on the [Group](../intermediate/group-system.md):

```java
public static final Group ORE_GROUP = REGISTRYLIB.group("ores")
        .langPrefix("Magic")
        .tab(MY_TAB)
        .addBlockTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
        .addItemTag(ItemTags.DURABILITY_ENCHANTABLE)
        .addFluidTag(FluidTags.WATER)  // if the group includes fluids
        .build();
```

Every block registered through `ORE_GROUP` automatically receives the pickaxe and iron-tool tags. Every item (including block items) receives the durability tag. Per-entry `.addTag(...)` calls **add** to the group tags — they do not replace them.

```java
public static final BlockEntry<Block> SAPPHIRE_ORE = ORE_GROUP
        .block("sapphire_ore", Block::new)
        .addTag(BlockTags.NEEDS_DIAMOND_TOOL)  // adds on top of group tags
        .simpleItem()
        .register();
```

## Step 3 — Use the Generic Tag Method

For tag types beyond block/item/fluid (e.g. entity tags), use the full-form `addTag` with an explicit `ProviderType`:

```java
REGISTRYLIB.entityType("magic_golem", MagicGolem::new)
    .addTag(ProviderType.ENTITY_TAGS, EntityTypeTags.FALL_DAMAGE_IMMUNE)
    .register();
```

This works on any builder through the base `AbstractBuilder.addTag(ProviderType, TagKey...)` method.

## Step 4 — Generate Recipes with ProviderType.RECIPE

RegistryLib integrates recipe generation into its datagen pipeline through `ProviderType.RECIPE`. Use `.addData(...)` on any builder to contribute recipes:

```java
REGISTRYLIB.item("ruby", Item::new)
    .addData(ProviderType.RECIPE, prov -> {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RUBY_BLOCK.get())
            .pattern("RRR")
            .pattern("RRR")
            .pattern("RRR")
            .define('R', RUBY.get())
            .unlockedBy("has_ruby",
                InventoryChangeTrigger.TriggerInstance.hasItems(RUBY.get()))
            .save(prov);
    })
    .register();
```

The `prov` parameter is a `RegistryLibRecipeProvider` which extends both `RecipeProvider` and `RecipeOutput`, so you can use all vanilla recipe builder methods directly.

:::note
`addData(ProviderType.RECIPE, ...)` can be called on **any** builder (block, item, fluid, etc.). The recipe callback runs during datagen regardless of which entry it is attached to.
:::

## Step 5 — Combine Tags and Recipes

A typical registration chain brings tags and recipes together:

```java
public static final BlockEntry<Block> RUBY_BLOCK = REGISTRYLIB
    .block("ruby_block", Block::new)
    .initialProperties(() -> Blocks.IRON_BLOCK)
    .addTag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
    .simpleItem()
    .addData(ProviderType.RECIPE, prov -> {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, RUBY.get(), 9)
            .requires(RUBY_BLOCK.get())
            .unlockedBy("has_ruby_block",
                InventoryChangeTrigger.TriggerInstance.hasItems(RUBY_BLOCK.get()))
            .save(prov);
    })
    .register();
```

## Common Patterns

| Pattern | Approach |
| --- | --- |
| All ores need the same mining tags | Define tags on the [Group](../intermediate/group-system.md) |
| One block needs an extra tag | Chain `.addTag(...)` after the group entry |
| Smelting recipe for an ore | Use `.addData(ProviderType.RECIPE, ...)` with `SimpleCookingRecipeBuilder` |
| Crafting recipe for a 3×3 block | Use `ShapedRecipeBuilder` inside the recipe callback |
| Stonecutting variant | Use `SingleItemRecipeBuilder.stonecutting(...)` |

:::warning
- Tags added via Group and per-entry `.addTag(...)` are **cumulative**. There is no way to remove a group tag from a single entry.
- `addData(ProviderType.RECIPE, ...)` runs during datagen only. It does not affect runtime behavior.
- Make sure recipe unlock criteria reference items that are actually obtainable, or the recipe will never appear in the recipe book.
:::

## Next Steps

- [Group System](../intermediate/group-system.md) — Share defaults including tags across entry families.
- [API Reference](../../reference/api-overview.md) — Full API surface for `ProviderType`, `AbstractBuilder.addTag`, and `AbstractBuilder.addData`.
