---
sidebar_position: 7
title: Multi-Language Support
description: Add multiple locales to your registration chains.
---

# Multi-Language Support

This tutorial explains how RegistryLib writes display names and other text into language files, and how to add additional locales such as `zh_cn`.

## What You Will Learn

- Generate default English display names from registration chains.
- Add raw lang entries for non-registered text such as tooltips and creative tab titles.
- Use `lang(Map)` on builders and `langPair(...)` / `lang(key, Map)` on `RegistryCore` for multi-locale support in a single call.
- Add extra locale files with `locale(...)`, `lang(...)`, and `withLangAlias(...)`.
- Decide when custom Builder methods are worth the maintenance cost.

## Step 1 - Use Built-In English Support

Every builder provides two ways to set the English display name:

| Call | Effect |
| --- | --- |
| `.lang("Display Name")` | Writes to `en_us.json` |
| `.defaultLang()` | Infers the display name from the registry path |

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .register();
```

Use `.defaultLang()` when the registry name directly implies the correct display name, such as `copper_coin` -> "Copper Coin". Use `.lang(...)` when you have a specific wording requirement.

### Multi-Locale Builder Shortcut

All builders also accept a `Map<String, String>` that sets every locale in a single call. The `"en_us"` entry is routed through the default lang provider; all other entries are routed through `locale(...)`:

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang(Map.of(
            "en_us", "Copper Coin",
            "zh_cn", "铜币"))
    .register();
```

This is equivalent to calling `.lang("Copper Coin")` followed by `.lang(LANG_ZH_CN, "铜币")`, but avoids the chain type narrowing issue described in Step 7. The `lang(Map)` method is available on `ItemBuilder`, `BlockBuilder`, `FluidBuilder`, `EntityBuilder`, and `EnchantmentBuilder`.

## Step 2 - Add Raw Text

Some text is not tied to a registered object. Use core lang helpers for tooltips, UI labels, creative tab titles, and other global text:

```java
public static final Component API_TOOLTIP =
        REGISTRYLIB.lang("tooltip.example.api", "RegistryLib API example");
```

The helper returns `Component.translatable(key)` and schedules the lang entry during datagen.

Adding the same key with the same value more than once is idempotent. RegistryLib still throws if the same key is assigned two different values, because that usually means two registration paths disagree about the user-facing text.

### Multi-Locale Raw Text Shortcuts

For bilingual projects (English + Chinese), `langPair(...)` registers both locales in one call:

```java
public static final Component API_TOOLTIP =
        REGISTRYLIB.langPair("tooltip.example.api",
                "RegistryLib API example",   // en_us
                "RegistryLib API 示例");      // zh_cn
```

For projects that support more than two locales, pass a `Map`:

```java
public static final Component API_TOOLTIP =
        REGISTRYLIB.lang("tooltip.example.api", Map.of(
                "en_us", "RegistryLib API example",
                "zh_cn", "RegistryLib API 示例",
                "ja_jp", "RegistryLib API 例"));
```

Both methods return `Component.translatable(key)`. The `"en_us"` entry is routed through the default lang provider, and every other entry is routed through `locale(...)`.

## Step 3 - Add Extra Locales

The shortest path for registration chains is the `lang(Map)` builder method shown in Step 1. For raw text, use `langPair(...)` or `lang(key, Map)` from Step 2.

When you need explicit control over the locale provider, `locale(...)` and the per-provider `lang(...)` overloads are still available:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        REGISTRYLIB.locale("zh_cn");

REGISTRYLIB.lang(LANG_ZH_CN, "tooltip.example.api", "RegistryLib API example (zh_cn)");
```

You can also use the locale string directly:

```java
REGISTRYLIB.lang("zh_cn", "tooltip.example.api", "RegistryLib API example (zh_cn)");
```

Then use the provider in registration chains:

```java
REGISTRYLIB.item("copper_coin", Item::new)
    .lang("Copper Coin")
    .lang(LANG_ZH_CN, "Copper Coin (zh_cn)")
    .register();
```

Both `en_us.json` and `zh_cn.json` are generated from the same code path.

:::tip
For most new code, prefer `lang(Map.of(...))` on builders and `langPair(...)` or `lang(key, Map.of(...))` on `RegistryCore`. The explicit `locale(...)` / `ProviderType` path is mainly useful when you need to share the provider constant with a custom Builder subclass or with `withLangAlias(...)`.
:::

## Step 4 - Register Creative Tabs with Locales

Creative tabs can declare English and extra locale names at registration time:

```java
public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MATERIALS_TAB =
        REGISTRYLIB.creativeTab("materials", "Materials", Map.of(
                "zh_cn", "材料"));
```

This writes the `itemGroup.<modid>.materials` key for `en_us` and each supplied locale. If a project also adds the same key through `lang(...)`, matching values are ignored as duplicates.

Use the overload with a builder callback when you need to customize icon or tab behavior:

```java
REGISTRYLIB.creativeTab("materials", "Materials", Map.of("zh_cn", "材料"),
        builder -> builder.icon(() -> COPPER_INGOT.asStack()));
```

## Step 5 - Reuse Existing Custom Providers

If your project already has a custom provider class, register it directly and let the provider override `getProviderType()` to return the same constant:

```java
public static final ProviderType<RegistryLibLangProvider> LANG_ZH_CN =
        ProviderType.registerClientProvider(
                "lang_zh_cn",
                () -> c -> new ZhCnLangProvider(c.parent(), c.output()));
```

Then alias the locale helper to that existing provider:

```java
protected ModRegistryCore(String modid) {
    super(modid);
    withLangAlias("zh_cn", LANG_ZH_CN);
}
```

This prevents `locale("zh_cn")` from creating a second provider for the same output file.

## Step 6 - Choose Your Extension Approach

| Approach | Best for | Advantage | Limitation |
| --- | --- | --- | --- |
| `lang(Map.of(...))` on builders | Inline multi-locale names in chains | Single call, no chain type narrowing | Requires a current RegistryLib version |
| `langPair(key, enUs, zhCn)` on core | Bilingual raw text (en + zh) | Shortest call for the common two-locale case | Only covers `en_us` and `zh_cn` |
| `lang(key, Map.of(...))` on core | Raw text in three or more locales | Single call for any number of locales | Requires a current RegistryLib version |
| `locale(...)` / `ProviderType` | Explicit provider routing | Works with the base Builder API; shareable with custom Builders | The call form is slightly longer |
| Custom Builder methods | Heavily repeated project-specific syntax | Natural call sites such as `.langCn(...)` | Requires maintaining custom Builder types |

For most projects, start with the `lang(Map)` builder method and the `langPair(...)` / `lang(key, Map)` core helpers. Use `locale(...)` and `withLangAlias(...)` when you need to share a provider constant across custom Builders. Move to custom Builder methods only when a project-specific call style is repeated enough to justify the extra type work.

## Step 7 - Understand Chain Type Narrowing

:::important
If you use a custom Builder approach, methods such as `.langCn(...)` usually need to be called while the chain is still returning your custom Builder type. Once the chain falls back to the base Builder type, those methods disappear at compile time.
:::

For example:

```java
MY_CORE.item("gem", MyItem::new)   // returns MyItemBuilder
    .langCn("Gem (zh_cn)")         // custom method, still MyItemBuilder
    .lang("Gem")                   // base method, may return base builder
    .register();
```

Place custom Builder methods before any call that returns the base type, unless you have overridden that fluent method to preserve your subtype.

## Common Patterns

- For bilingual projects, use `lang(Map.of("en_us", "...", "zh_cn", "..."))` on builders and `langPair(key, enUs, zhCn)` for raw text. These cover the majority of call sites without any locale/provider setup.
- For raw text that needs more than two locales, use `lang(key, Map.of(...))` on `RegistryCore`.
- Define a locale/provider constant with `locale(...)` only when you need it for custom Builders or `withLangAlias(...)`.
- Use `withLangAlias(...)` when a custom provider already exists for a locale.
- Combine lang entries with [Recipes and Tags](/tutorials/recipes-tags) datagen in the same registration chain.

## Boundaries and Pitfalls

:::warning
- `.defaultLang()` is appropriate when the registry name can directly imply the display name. It is not appropriate when you already have a specific wording requirement.
- If you hand-write a custom lang provider, make sure it routes back to its own provider type. Otherwise callbacks may run against the wrong locale.
- Do not mix default English naming and project-specific language sugar as if they were the same responsibility.
:::

## Next Steps

- [Custom Builder](/tutorials/custom-builder) - Build your own Builder subclass with language helpers.
- [Recipes and Tags](/tutorials/recipes-tags) - Combine datagen with your registration chains.
- [API Reference](/reference/api-overview) - Full API surface for lang providers and helper methods.
