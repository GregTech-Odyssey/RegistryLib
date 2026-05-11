---
sidebar_position: 6
title: Tooltip System
description: Organize complex tooltip content with multiple sections and separate boxes.
---

# Tooltip System

## What You Will Learn

In this tutorial, you will learn how to use RegistryLib's Tooltip System to organize tooltip content that has outgrown a single line. By the end, you will be able to:

- Register `RootNode` references for inline and separate-box tooltips
- Add multiple `SubNode` entries with explicit priority ordering
- Combine tooltip contributions from multiple sources using `TooltipNodeCollector`

## Prerequisites

Make sure you are comfortable with [registering items](/how-to/register-items) and have a working `RegistryCore` instance.

## Step 1 —Decide Whether You Need the Tooltip System

The Tooltip System's core value is ordering, partitioning, separate-box rendering, and bringing custom visual elements into one collection pipeline.

Use it when:

- One static line is no longer enough.
- You need to insert multiple tooltip sections with explicit priority.
- You want secondary information inside a separate tooltip box.
- You want attachments or custom nodes to participate in tooltip assembly.

:::tip
If you only need one fixed line, prefer `addTooltip(Component)`. The Tooltip System is for ordering, layout, and multi-source composition, not for replacing every simple tooltip.
:::

## Step 2 —Understand the Core Components

### SubNode

`SubNode` is the smallest rendering unit. It can be a line of text, a progress bar, an icon, or another custom element. The most common type is `SubNode.Basic`, which wraps a `Component`:

```java
new SubNode.Basic(Component.literal("§dMagic Wand"), 0)
```

The second parameter is the **priority** —lower values appear first within the same root.

### RootNode

A `RootNode` decides where a set of `SubNode`s is rendered:

| Mode | `separateBox` | Effect |
| --- | --- | --- |
| Inline | `false` | Content is appended inside the vanilla tooltip |
| Separate | `true` | Content is drawn inside a separate tooltip box |

### TooltipNodeCollector

The collector merges nodes from multiple sources, including the Item's tooltip callback, BlockItem tooltips, and nodes contributed by attachments.

## Step 3 —Register a RootNode Reference

Before adding nodes, register a `RootNodeRef` that defines where content will be rendered. The parameters are: a unique ID, a priority for ordering among roots, and whether it renders in a separate box:

```java
public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
        "mymod:detail_box", 10, true);
```

This creates a separate tooltip box with priority 10. Higher-priority roots appear below lower-priority ones.

## Step 4 —Add Tooltip Content to an Item

Use `addTooltip` on your item builder to contribute nodes to the collector. You can add nodes to the default inline tooltip and to your custom separate box in the same callback:

```java
item.addTooltip((collector, stack) -> {
    // Add a title line to the default (inline) tooltip
    collector.node(
            new SubNode.Basic(Component.literal("§dMagic Wand"), 0),
            true, false);

    // Add detailed information to the separate box
    collector.node(
            DETAIL_BOX,
            new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
});
```

The first `collector.node(...)` call adds an inline node. The second places content into the `DETAIL_BOX` root, which renders as a separate tooltip area.

## Step 5 —Combine Multiple Tooltip Sources

The collector merges nodes from multiple sources, which makes it easy to compose tooltips from different parts of your code:

- Keep the main item information in the default root.
- Write debug or secondary information into a separate `RootNodeRef`.
- Let `ItemAttachment` on a `ComponentItem` contribute separate nodes through `collectTooltipNodes(...)`.
- Reuse the same tooltip pipeline for Blocks through `.item(item -> ...)`.

## Step 6 - Attach Tooltips to Existing Items

For vanilla or third-party items that you reference with `existingItem(...)`, use `tooltipExisting(...)` instead of calling `TooltipRegistry.register(...)` directly:

```java
ItemEntry<Item> VANILLA_IRON_INGOT = REGISTRYLIB.existingItem("minecraft:iron_ingot");

REGISTRYLIB.tooltipExisting(VANILLA_IRON_INGOT,
        Component.translatable("tooltip.example.iron_ingot"));
```

Use `tooltipExistingSupplier(...)` for a lazy supplier that is not an `ItemEntry`.

## Runtime Layout and Pagination

RegistryLib tooltip roots participate in vanilla tooltip positioning as one combined visual footprint. This lets vanilla move the tooltip upward when the full RegistryLib layout would otherwise run below the screen, while separate roots still render as independent boxes instead of being swallowed by the vanilla background.

When the separate-box content is still too tall, RegistryLib paginates the separate roots automatically:

- The vanilla tooltip and inline RegistryLib nodes stay in the normal tooltip box.
- Separate roots keep their own backgrounds and are split across pages when needed.
- A small page control is rendered below the visible separate roots.
- The page control shows the current page and the keys used to move between pages.
- The default page keys are Up and Down, and players can remap them in the controls screen under the RegistryLib Tooltip category.

Page state is display-only state. RegistryLib does not write page information into the `ItemStack`. The page resets when the hovered item changes, when the same item has different data components, or when a non-RegistryLib tooltip is shown.

The test mod includes `registrylibtest:tooltip_stress_tester` as a stress item with 20 separate roots and 100 total child nodes. Use it when checking overflow, pagination, and custom `SubNode` layout behavior in-game.

## Boundaries and Pitfalls

:::warning
- `separatorAbove` and `separatorBelow` express layout intent. They are **not** absolute pixel-level positioning controls.
- For custom `SubNode` implementations, `getWidth()` and `getHeight()` must be accurate or layout will drift.
- Custom image or separator nodes should render within the width passed to their own root or inline layout, not the total tooltip width.
- If tooltip text needs localization, prefer `Component.translatable(...)` rather than relying on `Component.literal(...)` long term. See the [Multi-Language Support](./multi-language) tutorial for details.
:::

## Next Steps

- Add localized tooltip text with the [Multi-Language Support](./multi-language) tutorial
- Browse the full API in the [API Reference](/reference/api-overview)
