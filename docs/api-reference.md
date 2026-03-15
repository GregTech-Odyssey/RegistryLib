---
title: API Reference
nav_order: 6
has_children: true
permalink: /api-reference/
---

# API Reference

This page is for quick lookup only, not full teaching. Full examples stay in the tutorial pages; this page answers “which entry point should I start from?” and “what do common chains usually look like?”

{: .note }
> Deduplication rule: this page only provides method families, responsibility boundaries, and common chain lookup. It does not repeat full tutorial examples.

{: .note }
> The code snippets on this page are short excerpts from the runnable `RegistryLibTest` examples, not invented pseudo-code.

## Which Entry Point Should I Start From?

| What you want to do | Entry point | What usually comes next |
| --- | --- | --- |
| Register a regular or composite Item | `item("id", factory)` | `lang`, `defaultModel`, `tab`, `tooltip`, `attach` |
| Register a component-driven Item with attachments | `componentItem("id")` or `componentItem("id", factory)` | `lang`, `defaultModel`, `tooltip`, `attach` |
| Register a Block | `block("id", factory)` | `initialProperties`, `simpleItem` or `item`, `loot`, `tag` |
| Register a Fluid | `fluid("id", still, flow)` | `lang`, `clientExtension`, `properties`, `block`, `bucket` |
| Register a BlockEntity | `blockEntity("id", factory)` | `validBlock` or `validBlocks`, `renderer` |
| Register a generic object in another registry | `generic("id", registryKey, factory)` or `simple(...)` | `register` or immediate completion |
| Register a creative tab | `creativeTab("id")` | title, icon, content population |
| Share defaults across many entries | `group("name")` | `langPrefix`, `tab`, `blockProperties`, `itemProperties` |

## Builder Family Quick Lookup

| Builder | What it is responsible for | Typical endpoint |
| --- | --- | --- |
| `ItemBuilder` | Item properties, model, tooltip, tab, recipe, tag | `ItemEntry` |
| `BlockBuilder` | Block properties, drops, block item, recipe, tag | `BlockEntry` |
| `FluidBuilder` | Fluid type, rendering, block, bucket, tag | `FluidEntry` |
| `BlockEntityBuilder` | Host block binding and renderer | `BlockEntityEntry` |

## Entry Type Quick Lookup

| Type | Typical use |
| --- | --- |
| `ItemEntry<T>` | Reference an Item from another registration chain, recipe, or gameplay logic; item-providing entries can also create `ItemStack` and `ItemResource` values directly |
| `BlockEntry<T>` | Reference a Block, its default state, and holder-style APIs that expect a `Holder<Block>` |
| `FluidEntry<T>` | Access source, type, block, bucket, `FluidStack`, and `FluidResource` values together |
| `BlockEntityEntry<T>` | Reference a `BlockEntityType` and complete host binding |

## Entry Helper Quick Lookup

| Entry helper | What it gives you |
| --- | --- |
| `ItemEntry.asStack()` | A default `ItemStack` without reconstructing the item manually |
| `ItemEntry.asResource()` | An `ItemResource` wrapper for transfer-related APIs |
| `BlockEntry.getDefaultState()` | The block's default state for world placement or state configuration |
| `FluidEntry.getSource()` | The matching source fluid instance |
| `FluidEntry.getType()` | The `FluidType` associated with the family |
| `FluidEntry.getBlock()` / `getBucket()` | The related fluid block or bucket when they exist |
| `FluidEntry.asStack()` / `asResource()` | Transfer-friendly fluid values without rebuilding them by hand |

{: .note }
> Several Entry wrappers now also satisfy holder-style usage directly. When another API expects a `Holder<Item>`, `Holder<Block>`, or `Holder<Fluid>`, the RegistryLib entry wrapper is often already usable as that value.

## Common Chain Lookup

### Minimal Item

```java
RegistryLibTest.REGISTRYLIB.item("copper_coin", Item::new)
        .langCn("铜币")
        .lang("Copper Coin")
        .register();
```

### Component Item with Attachments

```java
RegistryLibTest.REGISTRYLIB.componentItem("magic_wand")
        .initialProperties(() -> new Item.Properties().stacksTo(1))
        .properties(Item.Properties::fireResistant)
        .lang("Magic Wand")
        .defaultModel()
        .attach(new InspectAttachment())
        .register();
```

### Minimal Block

```java
RegistryLibTest.REGISTRYLIB.block(RegistryLibTest.REGISTRYLIB, "decorative_stone", Block::new)
        .langCn("装饰石")
        .initialProperties(() -> Blocks.STONE)
        .lang("Decorative Stone")
        .simpleItem()
        .register();
```

### Block Inside a Group

```java
MACHINES.block("crusher", Block::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .defaultLoot()
        .simpleItem()
        .register();
```

## Related Links

- [Content Guides]({{ '/content-guides/' | relative_url }})
- [Core Systems]({{ '/systems-overview/' | relative_url }})
- [Advanced Topics]({{ '/advanced-topics/' | relative_url }})
