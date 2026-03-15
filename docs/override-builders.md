---
title: Override Builders
parent: Advanced Topics
nav_order: 1
permalink: /override-builders/
---

# Override Builders

This page explains how to turn project-specific syntax sugar or default rules into your own Builder types by overriding the Builder factory hooks in `RegistryCore`.

## When to Use It

- You keep repeating the same project-level rules across multiple registration chains.
- Group can only solve shared defaults, but your requirement is closer to “add new methods” or “change the default chaining experience”.
- You are willing to maintain a project-specific Builder layer for a more native call style.

## Quick Example

```java
public class ModBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

    public ModBlockBuilder<T, P> langCn(String name) {
        lang(ModRegistryCore.LANG_ZH_CN, name);
        return this;
    }
}
```

This shows the most common goal: wrapping an extra locale into Builder syntax sugar that feels native inside the project.

## Core Concepts

### Three Factory Hooks

The public registration entry points of `RegistryCore` eventually pass through three overridable factory methods:

- `newBlockBuilder(...)`
- `newItemBuilder(...)`
- `newFluidBuilder(...)`

After you override them, you can replace the default Builders with your own subclasses.

### Recommended Implementation Order

1. Implement the language-side or other datagen-side support types first.
2. Create `ModRegistryCore` and override the Builder factory methods.
3. Create `ModBlockBuilder`, `ModItemBuilder`, and `ModFluidBuilder`.
4. Switch the project entry point from `RegistryCore.create(...)` to `ModRegistryCore.create(...)`.

{: .important }
> The single-argument shorthand entry points usually return the base Builder type. If you want your custom Builder subtype at compile time, you typically need an overload that keeps the explicit `parent` type.

## Common Combinations

- Multilingual syntax sugar such as `langCn(...)` and `langTw(...)`.
- Project-level fixed tags or tooltip rules, such as machine-family Blocks automatically receiving a shared label or hint.
- Project-level default models or default tabs when the rule is genuinely stable across modules.

## Boundaries and Pitfalls

- Custom Builders are not meant to replace Group. Group is good at shared defaults, while custom Builders are good at new syntax sugar and custom compile-time return types.
- When creating a custom `create()` factory, verify that you did not drop important default behavior from the base Builder path.
- This kind of extension raises the abstraction level inside the project, so it is only worth doing when the rule is stable and heavily repeated.

## Related Links

- [Advanced Topics]({{ '/advanced-topics/' | relative_url }})
- [Lang System]({{ '/lang-system/' | relative_url }})
- [API Reference]({{ '/api-reference/' | relative_url }})
