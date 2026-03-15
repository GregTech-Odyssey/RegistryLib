---
title: Tooltip System
parent: Core Systems
nav_order: 2
permalink: /tooltip-system/
---

# Tooltip System

The Tooltip System is used to organize tooltip content that has outgrown vanilla's single-line description model. Its core value is ordering, partitioning, separate-box rendering, and bringing custom visual elements into one collection pipeline.

## When to Use It

- One static line is no longer enough.
- You need to insert multiple tooltip sections with explicit priority.
- You want secondary information inside a separate tooltip box.
- You want attachments or custom nodes to participate in tooltip assembly.

## Quick Example

```java
public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
        "mymod:detail_box", 10, true);

item.tooltip((collector, stack) -> {
    collector.node(
            new SubNode.Basic(Component.literal("§dMagic Wand"), 0),
            true, false);
    collector.node(
            DETAIL_BOX,
            new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
});
```

This example inserts a title line into the default tooltip area and renders an additional separate information box below it.

## Core Concepts

### `SubNode`

`SubNode` is the smallest rendering unit. It can be a line of text, a progress bar, an icon, or another custom element.

### `RootNode`

`RootNode` decides where a set of `SubNode`s is rendered:

| Mode | `separateBox` | Effect |
| --- | --- | --- |
| Inline | `false` | Content is appended inside the vanilla tooltip |
| Separate | `true` | Content is drawn inside a separate tooltip box |

### `TooltipNodeCollector`

The collector merges nodes from multiple sources, including the Item's tooltip callback, BlockItem tooltips, and nodes contributed by attachments.

{: .note }
> If you only need one fixed line, prefer `tooltip(Component)`. The Tooltip System is for ordering, layout, and multi-source composition, not for replacing every simple tooltip.

## Common Combinations

- Keep the main Item information in the default root and write debug or secondary information into a separate `RootNodeRef`.
- Let `ItemAttachment` on a `ComponentItem` contribute separate nodes through `collectTooltipNodes(...)`.
- Reuse the same tooltip pipeline for Blocks through `.item(item -> ...)`.

## Boundaries and Pitfalls

- `separatorAbove` and `separatorBelow` express layout intent. They are not absolute pixel-level positioning controls.
- For custom `SubNode` implementations, `getWidth()` and `getHeight()` must be accurate or layout will drift.
- If tooltip text needs localization, prefer `Component.translatable(...)` rather than relying on `Component.literal(...)` long term.

## Related Links

- [Core Systems]({{ '/systems-overview/' | relative_url }})
- [Registering Items]({{ '/register-items/' | relative_url }})
- [Registering Blocks]({{ '/register-blocks/' | relative_url }})
