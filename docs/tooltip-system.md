---
title: Tooltip System
nav_order: 8
permalink: /tooltip-system/
---

# Tooltip System

RegistryLib extends vanilla Minecraft tooltips with a **two-level node model**. Instead of plain text lines, you work with **RootNodes** (box containers) and **SubNodes** (content leaves). This gives you priority-based ordering, automatic separator lines, independent floating boxes, and fully custom rendering — all configured through the same fluent builder API you use for items and blocks.

---

## Key Concepts

### How It Relates to Vanilla Tooltips

Vanilla Minecraft renders tooltips as a flat list of `Component` lines inside a single dark box. RegistryLib injects additional content into that pipeline via NeoForge's `RenderTooltipEvent.GatherComponents`. Your nodes appear **alongside** the vanilla tooltip content — you don't replace it, you extend it.

### SubNode — The Content Leaf

A `SubNode` is the smallest renderable unit inside a tooltip. The built-in `SubNode.Basic` wraps a `Component` for text display. Each SubNode carries an `int priority` — lower values appear higher.

```java
// priority 0 — appears near the top
new SubNode.Basic(Component.literal("§dTitle"), 0)

// priority 10 — appears below priority-0 nodes
new SubNode.Basic(Component.literal("§7Some info"), 10)
```

### RootNode — The Box Container

A `RootNode` groups SubNodes into a rendering area. There are two modes:

| Mode | `separateBox` | Behaviour |
| --- | --- | --- |
| **Inline** | `false` | SubNodes appear inside the vanilla tooltip frame, below vanilla content. |
| **Independent box** | `true` | SubNodes render in a separate bordered box below the vanilla tooltip. |

A built-in **default RootNode** (`separateBox=false`) is always available. When you call `collector.node(subNode)` without specifying a RootNode, the node goes into this default inline container.

### RootNodeRef — A Handle to a RootNode

You never interact with `RootNode` directly in registration code. Instead you hold a `RootNodeRef` — a lightweight ID-based handle that you pass to `collector.node(ref, subNode)`. Create one with:

```java
public static final RootNodeRef MY_BOX =
        TooltipRegistry.rootNode("mymod:my_box", 10, true);
```

### Separator Lines

When adding a SubNode, you can request a separator above and/or below it. The system automatically inserts a thin semi-transparent line between nodes — no manual management needed.

```java
collector.node(subNode, true, false);
//                      ↑      ↑
//           separatorAbove  separatorBelow
```

---

## Adding Tooltips to Items

### Single-Line Tooltip

The simplest way — one line of text appended to the vanilla tooltip:

```java
item.tooltip(Component.literal("§5A powerful magical artifact"));
```

### Multi-Line Dynamic Tooltip

Use the callback form for dynamic content based on the ItemStack:

```java
item.tooltip((collector, stack) -> {
    collector.node(
            new SubNode.Basic(Component.literal("§dMagic Wand"), 0),
            true, false);   // separator above this node
    collector.node(
            new SubNode.Basic(
                    Component.literal("§7Durability: §f"
                            + (stack.getMaxDamage() - stack.getDamageValue())),
                    10));
});
```

Nodes are sorted by priority within the default RootNode. The first node requests a separator above it, creating a visual break from vanilla content.

### Independent Floating Box

Define a custom `RootNodeRef` and route nodes into it:

```java
// 1) Declare a separate-box root node (once, in a static field)
public static final RootNodeRef DETAIL_BOX =
        TooltipRegistry.rootNode("mymod:detail_box", 10, true);

// 2) Write nodes into it during tooltip configuration
item.tooltip((collector, stack) -> {
    // Inline (default root)
    collector.node(new SubNode.Basic(Component.literal("§dTitle"), 0), true, false);

    // Independent box
    collector.node(DETAIL_BOX,
            new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
    collector.node(DETAIL_BOX,
            new SubNode.Basic(Component.literal("§7Fire resistant"), 10));
});
```

The independent box renders below the vanilla tooltip with its own dark background and border, positioned automatically.

---

## Adding Tooltips to Blocks

Block tooltips work through the **BlockItem**. Use `block.item(...)` to access the item builder, then call `.tooltip()` as usual:

```java
block.item(itemBuilder -> {
    itemBuilder.tooltip((collector, stack) -> {
        collector.node(
                new SubNode.Basic(Component.literal("§5Drops coins when mined")),
                true, false);
    });
});
```

All tooltip features (dynamic content, independent boxes, separators) are available on block items, since they share the same `ItemBuilder` API.

---

## Integration with CompositeItem Attachments

`CompositeItemAttachment` subclasses can contribute tooltip nodes by overriding `collectTooltipNodes()`. The system detects this override automatically and registers a unified tooltip config that invokes all relevant attachments.

```java
public class MyAttachment extends CompositeItemAttachment<CompositeItem> {

    @Override
    public void collectTooltipNodes(
            CompositeItem item, ItemStack stack, TooltipNodeCollector collector) {
        collector.node(new SubNode.Basic(
                Component.literal("§eContributed by attachment"), 100));
    }
}
```

Attach it during registration:

```java
item.attach(new MyAttachment());
```

The attachment's nodes are merged with all other tooltip configurations for the item, respecting priority and separator rules.

---

## Full Example

A complete item registration combining all tooltip features:

```java
public class FullItemExample {

    public static final RootNodeRef DETAIL_BOX =
            TooltipRegistry.rootNode("mymod:detail_box", 10, true);

    static class InspectAttachment extends CompositeItemAttachment<CompositeItem> {
        @Override
        public InteractionResult use(
                CompositeItem item, Level level, Player player, InteractionHand hand) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.literal("Inspecting magic wand..."));
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public void collectTooltipNodes(
                CompositeItem item, ItemStack stack, TooltipNodeCollector collector) {
            collector.node(
                    new SubNode.Basic(Component.literal("§eRight-click to inspect"), 100));
        }
    }

    public static final ItemEntry<CompositeItem> MAGIC_WAND =
            RegistryLibTest.REGISTRYLIB.item(
                    "magic_wand",
                    CompositeItem::new,
                    item -> {
                        item.initialProperties(() -> new Item.Properties().stacksTo(1));
                        item.properties(p -> p.fireResistant());
                        item.lang("Magic Wand");
                        item.defaultModel();
                        item.tab(CreativeModeTabs.TOOLS_AND_UTILITIES);
                        item.tag(ItemTags.DURABILITY_ENCHANTABLE);

                        // Single-line tooltip
                        item.tooltip(Component.literal("§5A powerful magical artifact"));

                        // Dynamic multi-line tooltip + independent box
                        item.tooltip((collector, stack) -> {
                            collector.node(
                                    new SubNode.Basic(
                                            Component.literal("§dMagic Wand"), 0),
                                    true, false);
                            collector.node(
                                    new SubNode.Basic(
                                            Component.literal("§7Durability: §f"
                                                    + (stack.getMaxDamage()
                                                            - stack.getDamageValue())),
                                            10));
                            collector.node(DETAIL_BOX,
                                    new SubNode.Basic(
                                            Component.literal("§bDetailed Information"), 0));
                            collector.node(DETAIL_BOX,
                                    new SubNode.Basic(
                                            Component.literal("§7Fire resistant"), 10));
                        });

                        // Attachment contributes tooltip nodes automatically
                        item.attach(new InspectAttachment());
                    });
}
```

The resulting tooltip shows:
1. **Vanilla tooltip area**: the item name, "§5A powerful magical artifact", a separator, "§dMagic Wand", "§7Durability: §f...", "§eRight-click to inspect" (from attachment).
2. **Independent box below**: "§bDetailed Information", "§7Fire resistant" — rendered in a separate dark bordered box.

---

## Extending with Custom SubNode

For content beyond plain text — icons, progress bars, colour swatches — extend `SubNode` directly:

```java
public class ProgressBarNode extends SubNode {

    private final float progress; // 0.0 – 1.0

    public ProgressBarNode(float progress, int priority) {
        super(priority);
        this.progress = progress;
    }

    @Override
    public int getHeight(Font font) {
        return 7;
    }

    @Override
    public int getWidth(Font font) {
        return 80;
    }

    @Override
    public void renderImage(
            Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        graphics.fill(x, y + 2, x + 80, y + 5, 0xFF333333);   // track
        int fillWidth = (int) (80 * progress);
        graphics.fill(x, y + 2, x + fillWidth, y + 5, 0xFF55FF55); // bar
    }
}
```

Use it like any other SubNode:

```java
item.tooltip((collector, stack) -> {
    float pct = 1.0f - (float) stack.getDamageValue() / stack.getMaxDamage();
    collector.node(new ProgressBarNode(pct, 20));
});
```

Implement `renderText()` for text content and `renderImage()` for graphical elements. Both default to no-op, so you only override what you need.

---

## Custom Box Rendering

When creating an independent-box RootNode, you can supply a custom `BoxRenderer` to control the background appearance:

```java
public static final RootNodeRef CUSTOM_BOX = TooltipRegistry.rootNode(
        "mymod:custom",
        5,
        true,
        6,    // 6px padding
        (graphics, x, y, w, h) -> {
            graphics.fill(x, y, x + w, y + h, 0xCC222222);
        });
```

The default renderer (`RootNode.DEFAULT_BOX_RENDERER`) draws a dark background (`0xF0100010`) with a 1px gradient border (white highlight at top fading to shadow at bottom), matching the vanilla tooltip style.

`BoxRenderer` is a functional interface:

```java
@FunctionalInterface
public interface BoxRenderer {
    void render(GuiGraphics graphics, int x, int y, int width, int height);
}
```

---

## Architecture and Rendering Pipeline

### End-to-End Flow

```
Registration phase                     Render phase (per-frame)
──────────────────                     ────────────────────────
ItemBuilder.tooltip(config)            RenderTooltipEvent.GatherComponents
        │                                        │
        ▼                                        ▼
TooltipRegistry.register(item, config)   TooltipRegistry.resolve(itemStack)
        │                                        │
        ▼                                        ├─ Invoke all TooltipConfigs
(stored in pendingEntries)                       │   config.configure(collector, stack)
                                                 │
                                                 ├─ Group SubNodes by RootNodeRef
                                                 │
                                                 ├─ Sort within each group by SubNode.priority
                                                 │
                                                 ├─ Insert SeparatorNodes per preferences
                                                 │
                                                 ├─ separateBox=false → inline nodes
                                                 │   separateBox=true  → independent box
                                                 │
                                                 ▼
                                      RegistryLibTooltipComponent
                                                 │
                                                 ▼
                                      RegistryLibClientTooltip
                                        ├─ renderText()  → inline text + box bg & text
                                        └─ renderImage() → inline images + box images
```

### How It Hooks into Minecraft

Two NeoForge events are registered in `Client.java`:

1. **`RegisterClientTooltipComponentFactoriesEvent`** — maps `RegistryLibTooltipComponent` to `RegistryLibClientTooltip`, telling Minecraft how to render the custom component.
2. **`RenderTooltipEvent.GatherComponents`** — fires every time a tooltip is about to render. Calls `TooltipRegistry.resolve(itemStack)` and injects the result into the vanilla component list via `Either.right(component)`.

### Inline Nodes

Inline nodes (default RootNode, `separateBox=false`) are rendered inside the vanilla tooltip frame. Their `getWidth()` / `getHeight()` contribute to the vanilla tooltip's size calculation.

### Independent Boxes

Independent-box nodes (`separateBox=true`) render **below** the vanilla tooltip:

- Offset: vanilla tooltip bottom edge + 3px border + 2px gap
- Each box draws its background via `BoxRenderer`, then renders SubNodes inside with configured padding
- Multiple boxes stack vertically with a 2px gap between them
- Boxes are sorted by their RootNode's `priority` (ascending)

### Render Order

1. `renderText()` is called first — inline text, then independent box backgrounds + text
2. `renderImage()` is called second — inline images, then independent box images

This ensures box backgrounds are drawn before any overlaid content.

### SeparatorNode

A system-managed node automatically inserted between SubNodes. Renders a full-width 1px semi-transparent white line (`0x40FFFFFF`) with 3px padding above and below (7px total). **You never create this manually.**

---

## API Reference

### `TooltipRegistry`

The global registry managing RootNodes and per-item tooltip callbacks.

| Method | Description |
| --- | --- |
| `defaultRootRef()` | Returns the built-in default `RootNodeRef` (inline, no separate box). |
| `rootNode(id, priority, separateBox)` | Creates a custom `RootNode` with default padding (4px) and default box renderer. Returns a `RootNodeRef`. |
| `rootNode(id, priority, separateBox, padding, boxRenderer)` | Fully customised variant with explicit padding and box renderer. |
| `registerRootNode(ref, rootNode)` | Registers a pre-built `RootNode` instance. |
| `register(ItemLike, TooltipConfig)` | Manually registers a tooltip callback for an item (usually handled by `ItemBuilder.tooltip()`). |

### `TooltipNodeCollector`

Collects `SubNode` entries grouped by `RootNodeRef`. Passed to your `TooltipConfig` callback.

| Method | Description |
| --- | --- |
| `node(SubNode)` | Default root, no separators. |
| `node(SubNode, separatorAbove, separatorBelow)` | Default root, with separator control. |
| `node(RootNodeRef, SubNode)` | Specific root, no separators. |
| `node(RootNodeRef, SubNode, separatorAbove, separatorBelow)` | Specific root, with separator control. |

### `TooltipNodeCollector.TooltipConfig`

Functional interface for tooltip configuration callbacks.

```java
@FunctionalInterface
public interface TooltipConfig {
    void configure(TooltipNodeCollector collector, ItemStack stack);
}
```

### `SubNode`

Abstract base class for tooltip leaf nodes.

| Member | Description |
| --- | --- |
| `SubNode(int priority)` | Constructor. Lower priority = higher position. |
| `getHeight(Font)` | Node height in pixels (abstract). |
| `getWidth(Font)` | Node width in pixels (abstract). |
| `renderText(GuiGraphics, Font, x, y)` | Renders text content. Default no-op. |
| `renderImage(Font, x, y, width, height, GuiGraphics)` | Renders graphical content. Default no-op. |

### `SubNode.Basic`

Built-in text node wrapping a `Component`.

| Constructor | Description |
| --- | --- |
| `Basic(Component text)` | Priority defaults to 0. |
| `Basic(Component text, int priority)` | Explicit priority. |

### `RootNode`

Box container defining how a group of SubNodes is rendered.

| Parameter | Default | Description |
| --- | --- | --- |
| `id` | — | Unique string identifier. |
| `priority` | — | Sort order among independent boxes. |
| `separateBox` | — | `true` = independent box; `false` = inline. |
| `padding` | `4` | Inner padding in pixels. |
| `boxRenderer` | `DEFAULT_BOX_RENDERER` | Custom box background renderer. |

### `RootNodeRef`

Lightweight handle referencing a `RootNode` by string ID. Store as `static final` and pass to `collector.node(ref, ...)`.

### `CompositeItemAttachment.collectTooltipNodes(T, ItemStack, TooltipNodeCollector)`

Override this in your attachment to contribute tooltip nodes. The system automatically detects the override via bitmask introspection (`COLLECT_TOOLTIP = 1 << 6`) and registers a unified config.
