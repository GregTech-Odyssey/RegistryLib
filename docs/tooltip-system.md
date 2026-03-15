---
title: Tooltip System
nav_order: 9
permalink: /tooltip-system/
---

# Tooltip System

RegistryLib extends vanilla Minecraft tooltips with a two-level model:

- `SubNode` is the content you want to show.
- `RootNode` is the container that decides where that content is rendered.

That extra structure gives you four things vanilla tooltips do not have:

- ordering by priority
- automatic separator lines
- extra tooltip boxes below the vanilla box
- custom-rendered content such as bars, icons, or swatches

If you only need one sentence under the item name, use `item.tooltip(Component)`. If you need layout, ordering, or custom visuals, use tooltip nodes.

For general item registration, also see [Register Items]({{ '/register-items/' | relative_url }}).

---

## Quick Start

If you are new to the system, start here.

```java
public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
        "mymod:detail_box", 10, true);

item.tooltip((collector, stack) -> {
    collector.node(
            new SubNode.Basic(Component.literal("§dMagic Wand"), 0),
            true, false);
    collector.node(
            new SubNode.Basic(
                    Component.literal("§7Durability: §f"
                            + (stack.getMaxDamage() - stack.getDamageValue())),
                    10));

    collector.node(
            DETAIL_BOX,
            new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
    collector.node(
            DETAIL_BOX,
            new SubNode.Basic(Component.literal("§7Fire resistant"), 10));
});
```

What this does:

1. Adds two inline lines to the vanilla tooltip area.
2. Inserts a separator above the first custom inline line.
3. Creates a second tooltip box below the vanilla one for extra details.
4. Sorts all custom lines by priority within their own container.

Use this mental shortcut:

- same box as vanilla -> default root
- different box below vanilla -> custom `RootNodeRef`
- text line -> `SubNode.Basic`
- custom visual element -> extend `SubNode`

{: .note }
> Examples use `Component.literal(...)` for brevity. In a real mod, prefer `Component.translatable(...)` when the text should be localizable.

---

## Choose the Right Approach

| Goal | API | Use it when |
| --- | --- | --- |
| Add one static line | `item.tooltip(Component)` | You just want a short description. |
| Add dynamic lines | `item.tooltip((collector, stack) -> ...)` | Content depends on `ItemStack`, durability, NBT, mode, or attachments. |
| Add a visual break from vanilla lines | `collector.node(node, true, false)` | You want your custom section to read like a separate block. |
| Show extra information in another box | `TooltipRegistry.rootNode(..., true)` | Secondary details should not crowd the main tooltip. |
| Add tooltip logic from an attachment | `collectTooltipNodes(...)` | Tooltip content belongs to a reusable `CompositeItemAttachment`. |
| Render icons or bars | custom `SubNode` | Text is not enough. |

---

## Core Mental Model

Vanilla Minecraft treats a tooltip as a flat list of lines inside one dark box. RegistryLib does not replace that system. It injects additional tooltip content during NeoForge's `RenderTooltipEvent.GatherComponents`, so your content appears alongside the vanilla tooltip.

Think of the RegistryLib model like this:

- `SubNode`: one renderable unit, such as a text line or progress bar
- `RootNode`: one tooltip region that holds subnodes
- `RootNodeRef`: the handle you keep in registration code
- `TooltipNodeCollector`: the object you write nodes into during tooltip construction

### `SubNode`

`SubNode` is the smallest renderable part of the tooltip. The built-in `SubNode.Basic` wraps a `Component`.

Each subnode has a priority:

```java
new SubNode.Basic(Component.literal("§dTitle"), 0);
new SubNode.Basic(Component.literal("§7Details"), 10);
```

Lower priority values render higher within the same root.

### `RootNode`

`RootNode` decides where a group of subnodes is rendered.

| Mode | `separateBox` | Result |
| --- | --- | --- |
| Inline | `false` | Content is appended inside the vanilla tooltip frame. |
| Independent box | `true` | Content is drawn in its own box below the vanilla tooltip. |

RegistryLib always provides one built-in default root node with `separateBox=false`. If you call `collector.node(subNode)`, that node goes there.

### `RootNodeRef`

You usually do not construct `RootNode` directly in item registration code. Instead, create and reuse a `RootNodeRef`:

```java
public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
        "mymod:detail_box", 10, true);
```

Declare it once, usually as `static final`, and reuse it everywhere that should write into that box.

### Separators

Separators are inserted automatically by the registry. You only express the intent:

```java
collector.node(subNode, true, false);
```

This means:

- `separatorAbove=true`: add a separator before this node when appropriate
- `separatorBelow=true`: add a separator after this node if another node follows

For the default inline root, `separatorAbove` on the first node is the usual way to visually split your custom section from the vanilla lines.

---

## Step by Step

### 1. Add a Single Static Line

Use this when all you want is a short description.

```java
item.tooltip(Component.literal("§5A powerful magical artifact"));
```

This is the simplest API and the right default choice for basic item flavor text.

### 2. Add Dynamic Multi-Line Content

Use the callback form when the tooltip depends on the current `ItemStack`.

```java
item.tooltip((collector, stack) -> {
    collector.node(
            new SubNode.Basic(Component.literal("§dMagic Wand"), 0),
            true, false);
    collector.node(
            new SubNode.Basic(
                    Component.literal("§7Durability: §f"
                            + (stack.getMaxDamage() - stack.getDamageValue())),
                    10));
});
```

Why this is useful:

- `stack` gives you access to durability, NBT, custom data, and state
- priorities keep the output stable even when several systems contribute nodes
- the separator makes the custom content read like a real section rather than an arbitrary extra line

### 3. Move Details into a Separate Box

When there is too much information for the main tooltip, split it into another box.

```java
public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
        "mymod:detail_box", 10, true);

item.tooltip((collector, stack) -> {
    collector.node(new SubNode.Basic(Component.literal("§dTitle"), 0), true, false);

    collector.node(
            DETAIL_BOX,
            new SubNode.Basic(Component.literal("§bDetailed Information"), 0));
    collector.node(
            DETAIL_BOX,
            new SubNode.Basic(Component.literal("§7Fire resistant"), 10));
});
```

Good candidates for separate boxes:

- secondary stats
- debug or dev-only details
- contextual usage instructions
- attachment-provided auxiliary info

### 4. Add Tooltips to Blocks

Blocks expose item tooltips through their `BlockItem`, so you configure the tooltip via `.item(item -> ...)`.

```java
block.item(item -> item
    .tooltip((collector, stack) -> {
        collector.node(
                new SubNode.Basic(Component.literal("§5Drops coins when mined")),
                true, false);
    })
);
```

All tooltip features available to items also work here, because the block item uses the same item builder pipeline.

### 5. Let Attachments Contribute Tooltip Content

If you use `CompositeItemAttachment`, the attachment can provide its own tooltip nodes by overriding `collectTooltipNodes(...)`.

```java
public class InspectAttachment extends CompositeItemAttachment<CompositeItem> {

    @Override
    public void collectTooltipNodes(
            CompositeItem item, ItemStack stack, TooltipNodeCollector collector) {
        collector.node(
                new SubNode.Basic(Component.literal("§eRight-click to inspect"), 100));
    }
}
```

Then attach it normally:

```java
item.attach(new InspectAttachment());
```

This is a good fit when tooltip content belongs to behavior that is already encapsulated in an attachment. The attachment's nodes are merged with all other tooltip sources for that item.

### 6. Render Custom Visual Content with a `SubNode`

When text is not enough, extend `SubNode` directly.

```java
public class ProgressBarNode extends SubNode {

    private final float progress;

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
        graphics.fill(x, y + 2, x + 80, y + 5, 0xFF333333);
        int fillWidth = (int) (80 * progress);
        graphics.fill(x, y + 2, x + fillWidth, y + 5, 0xFF55FF55);
    }
}
```

Use it like any other node:

```java
item.tooltip((collector, stack) -> {
    float pct = 1.0f - (float) stack.getDamageValue() / stack.getMaxDamage();
    collector.node(new ProgressBarNode(pct, 20));
});
```

Implementation guidelines:

- implement `getWidth()` and `getHeight()` accurately, because layout depends on them
- put text in `renderText(...)`
- put shapes, icons, lines, and bars in `renderImage(...)`
- override only what you need; both render methods are no-ops by default

### 7. Customize the Box Background

Independent boxes can use a custom background renderer.

```java
public static final RootNodeRef CUSTOM_BOX = TooltipRegistry.rootNode(
        "mymod:custom",
        5,
        true,
        6,
        (graphics, x, y, w, h) -> {
            graphics.fill(x, y, x + w, y + h, 0xCC222222);
        });
```

The default renderer matches the vanilla tooltip look closely: dark background, subtle bright top border, and darker bottom edge.

---

## Copy-Paste Recipes

### Recipe: Add a Basic Section Below the Vanilla Lines

```java
item.tooltip((collector, stack) -> {
    collector.node(
            new SubNode.Basic(Component.literal("§6Special Properties"), 0),
            true, false);
    collector.node(new SubNode.Basic(Component.literal("§7Fire resistant"), 10));
    collector.node(new SubNode.Basic(Component.literal("§7Unbreakable in lava"), 20));
});
```

Use this for the most common “append a mini section” case.

### Recipe: Keep Main Tooltip Short, Put Details Elsewhere

```java
public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
        "mymod:details", 10, true);

item.tooltip((collector, stack) -> {
    collector.node(
            new SubNode.Basic(Component.literal("§aPortable Generator"), 0),
            true, false);

    collector.node(DETAIL_BOX,
            new SubNode.Basic(Component.literal("§7Output: §f80 FE/t"), 0));
    collector.node(DETAIL_BOX,
            new SubNode.Basic(Component.literal("§7Buffer: §f100000 FE"), 10));
});
```

Use this when the item should stay readable at a glance.

### Recipe: Tooltip from an Attachment

```java
public class ChargeAttachment extends CompositeItemAttachment<CompositeItem> {
    @Override
    public void collectTooltipNodes(
            CompositeItem item, ItemStack stack, TooltipNodeCollector collector) {
        collector.node(new SubNode.Basic(Component.literal("§bCharge module installed"), 50));
    }
}
```

Use this when tooltip content should travel with the attachment rather than the item registration itself.

---

## Full Example

This example combines static text, dynamic lines, a separate detail box, and attachment-contributed content.

```java
public class FullItemExample {

    public static final RootNodeRef DETAIL_BOX = TooltipRegistry.rootNode(
            "mymod:detail_box", 10, true);

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
            RegistryLibTest.REGISTRYLIB
                    .item("magic_wand", CompositeItem::new)
                    .initialProperties(() -> new Item.Properties().stacksTo(1))
                    .properties(p -> p.fireResistant())
                    .lang("Magic Wand")
                    .defaultModel()
                    .tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
                    .tag(ItemTags.DURABILITY_ENCHANTABLE)
                    .tooltip(Component.literal("§5A powerful magical artifact"))
                    .tooltip((collector, stack) -> {
                        collector.node(
                                new SubNode.Basic(Component.literal("§dMagic Wand"), 0),
                                true, false);
                        collector.node(
                                new SubNode.Basic(
                                        Component.literal("§7Durability: §f"
                                                + (stack.getMaxDamage()
                                                        - stack.getDamageValue())),
                                        10));
                        collector.node(
                                DETAIL_BOX,
                                new SubNode.Basic(
                                        Component.literal("§bDetailed Information"), 0));
                        collector.node(
                                DETAIL_BOX,
                                new SubNode.Basic(Component.literal("§7Fire resistant"), 10));
                    })
                    .attach(new InspectAttachment())
                    .register();
}
```

Resulting layout:

1. Vanilla tooltip area shows the item name, the static tooltip line, a separator, the inline custom lines, and the attachment line.
2. A second bordered box below shows the extra detail lines.

---

## How Ordering and Layout Work

Understanding these rules will prevent most confusion.

### Ordering

- nodes are grouped by `RootNodeRef`
- nodes are sorted by `SubNode.priority` inside each root
- lower priority values render first
- separate boxes are sorted by `RootNode.priority`

### Separators

- separators are inserted by the registry, not by user code
- a separator appears between two nodes if the previous node requested `separatorBelow` or the next node requested `separatorAbove`
- for the default root, `separatorAbove` on the first node is how you create a visual break from vanilla content

### Inline vs Separate Box

- inline nodes contribute to the vanilla tooltip's width and height
- separate boxes render below the vanilla tooltip
- separate boxes use their own padding and background renderer
- multiple separate boxes stack vertically with a small gap

### Render Order

Rendering happens in two passes:

1. `renderText(...)` draws inline text, then separate box backgrounds, then separate box text.
2. `renderImage(...)` draws inline graphics, then separate box graphics.

This ordering ensures backgrounds are already in place before custom imagery is drawn on top.

---

## Troubleshooting

### My tooltip does not appear

Check these first:

- you registered the tooltip on the item or block item, not only on the block itself
- the item actually reaches the tooltip callback for the hovered stack
- your collector receives at least one node
- your custom nodes report non-zero width and height when they should be visible

### My nodes are in the wrong order

Priority is ascending. `0` renders above `10`. If content from multiple places is mixing badly, assign a clear priority convention such as `0-49` for title and summary, `50-99` for item internals, and `100+` for attachments.

### My separator is missing

Remember that separators are conditional. The most common pattern is:

```java
collector.node(new SubNode.Basic(Component.literal("§6Details"), 0), true, false);
```

That requests a separator above the first inline node, which creates a break from the vanilla lines.

### My separate box is not separate

Make sure the root node was created with `separateBox=true`:

```java
TooltipRegistry.rootNode("mymod:detail_box", 10, true)
```

### My custom node renders but layout looks wrong

Usually the problem is one of these:

- `getWidth()` is too small, so content gets clipped or overlaps visually
- `getHeight()` is too small, so the next node renders too early
- drawing code assumes a fixed width that does not match the reported width

### I have too much tooltip logic in one place

Move reusable behavior into a `CompositeItemAttachment` and let it contribute via `collectTooltipNodes(...)`.

---

## Best Practices

- Start with `item.tooltip(Component)` unless you actually need node-level control.
- Use one inline section for the most important information and move secondary details into a separate box.
- Reuse `RootNodeRef` values instead of creating them ad hoc inside tooltip callbacks.
- Prefer clear priority ranges over arbitrary numbers.
- Prefer `Component.translatable(...)` for user-facing text in production mods.
- Keep custom `SubNode` dimensions honest; layout quality depends on them.

---

## Source Reference

If you want to understand or debug the implementation, these are the main entry points in the source tree:

- `src/main/java/com/gto/registrylib/tooltip/TooltipRegistry.java`
- `src/main/java/com/gto/registrylib/tooltip/TooltipNodeCollector.java`
- `src/main/java/com/gto/registrylib/tooltip/SubNode.java`
- `src/main/java/com/gto/registrylib/tooltip/RootNode.java`
- `src/main/java/com/gto/registrylib/client/Client.java`
- `src/main/java/com/gto/registrylib/client/RegistryLibClientTooltip.java`
- `src/main/java/com/gto/registrylibtest/item/FullItemExample.java`

Use them in this order:

1. `FullItemExample` to see intended usage.
2. `TooltipRegistry` to understand grouping, sorting, and separator insertion.
3. `RegistryLibClientTooltip` to understand final rendering behavior.

---

## API Reference

### `TooltipRegistry`

Global registry for root nodes and per-item tooltip callbacks.

| Method | Description |
| --- | --- |
| `defaultRootRef()` | Returns the built-in inline root node reference. |
| `rootNode(id, priority, separateBox)` | Creates a root node with default padding and the default box renderer. |
| `rootNode(id, priority, separateBox, padding, boxRenderer)` | Creates a root node with explicit padding and custom box rendering. |
| `registerRootNode(ref, rootNode)` | Registers a pre-built root node instance manually. |
| `register(ItemLike, TooltipConfig)` | Registers a tooltip callback for an item. Usually called indirectly by `ItemBuilder.tooltip(...)`. |

### `TooltipNodeCollector`

Receives nodes during tooltip assembly.

| Method | Description |
| --- | --- |
| `node(SubNode)` | Adds a node to the default inline root. |
| `node(SubNode, separatorAbove, separatorBelow)` | Same as above, but requests separators. |
| `node(RootNodeRef, SubNode)` | Adds a node to a specific root. |
| `node(RootNodeRef, SubNode, separatorAbove, separatorBelow)` | Adds a node to a specific root with separator preferences. |

### `TooltipNodeCollector.TooltipConfig`

```java
@FunctionalInterface
public interface TooltipConfig {
    void configure(TooltipNodeCollector collector, ItemStack stack);
}
```

### `SubNode`

Base class for tooltip leaf content.

| Member | Description |
| --- | --- |
| `SubNode(int priority)` | Constructor. Lower value means earlier rendering inside the same root. |
| `getHeight(Font)` | Returns this node's height in pixels. |
| `getWidth(Font)` | Returns this node's width in pixels. |
| `renderText(GuiGraphics, Font, x, y)` | Draws text content. Default no-op. |
| `renderImage(Font, x, y, width, height, GuiGraphics)` | Draws graphics. Default no-op. |

### `SubNode.Basic`

Built-in text node for `Component` content.

| Constructor | Description |
| --- | --- |
| `Basic(Component text)` | Creates a text node with default priority `0`. |
| `Basic(Component text, int priority)` | Creates a text node with explicit priority. |

### `RootNode`

Defines how a group of subnodes is rendered.

| Property | Meaning |
| --- | --- |
| `id` | Unique string identifier. |
| `priority` | Order among separate boxes. |
| `separateBox` | Whether the root renders in an independent box. |
| `padding` | Inner padding used for separate-box rendering. |
| `boxRenderer` | Background renderer for separate boxes. |

### `RootNodeRef`

Lightweight handle for a root node. Keep it as a reusable constant and pass it to `collector.node(ref, ...)`.

### `CompositeItemAttachment.collectTooltipNodes(...)`

Override this in attachments when the attachment should contribute tooltip nodes. RegistryLib detects the override automatically and merges the result with the item's other tooltip sources.
