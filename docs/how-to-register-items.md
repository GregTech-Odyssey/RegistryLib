---
title: How to Register Items
nav_order: 2
---

# How to Register Items

RegistryLib keeps item registration compact and readable. In the simplest case, you provide an id,
an item factory, and a builder consumer. From there you can add language entries, tooltip nodes,
and more advanced behavior without splitting the setup across multiple classes.

The RegistryLibTest example starts with a plain item and then expands into richer cases like custom
display names, multi-line tooltips, and CompositeItem attachments. That makes the item API a good
entry point if you want to learn the builder style first.

## Basic Item

```java
public static final ItemEntry<Item> TEST_ITEM = RegistryLibTest.REGISTRYLIB.item(
        "test_item",
        Item::new,
        item -> {
            item.tooltip((collector, stack) -> {
                collector.node(new SubNode.Basic(Component.literal("A simple test item")));
            });
        });
```

## Item With Lang And Tooltip

```java
public static final ItemEntry<Item> MAGIC_DUST = RegistryLibTest.REGISTRYLIB.item(
        "magic_dust",
        Item::new,
        item -> {
            item.lang("Magic Dust")
                    .tooltip((collector, stack) -> {
                        collector.node(new SubNode.Basic(Component.literal("Magical Dust"), 0), true, false);
                        collector.node(
                                new SubNode.Basic(
                                        Component.literal("Amount: " + stack.getCount()),
                                        10));
                    });
        });
```

## When To Use It

Use this pattern when you want item registration, tooltip content, and item-specific configuration
to stay together in one place. It is especially useful for mods with many materials, components, or
tool items that would otherwise repeat the same registry setup over and over.