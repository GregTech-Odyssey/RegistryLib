---
sidebar_position: 101
title: Troubleshooting
description: Common errors and how to fix them.
---

# Troubleshooting

## Build & Dependency Errors

### `Could not resolve com.gto:registrylib:x.x.x`

**Cause:** Gradle cannot reach the Maven repository.

**Fix:**
1. Ensure your `settings.gradle` or `build.gradle` includes the Gtodyssey Maven repository.
2. Verify the URL is `https://maven.gtodyssey.com/releases`.
3. Check your network connectivity to `maven.gtodyssey.com`.

### `NoClassDefFoundError` or `ClassNotFoundException` for RegistryLib classes

**Cause:** RegistryLib is not on the runtime classpath.

**Fix:** Ensure you are using `implementation` (not `compileOnly`) in your `build.gradle` dependencies:
```groovy
dependencies {
    implementation 'com.gto:registrylib:7.0.8'
}
```

---

## Registration Errors

### Entry exists in code but does not appear in-game

**Cause:** The class containing the `static final` entry field was never loaded.

**Fix:** Reference the class from your `@Mod` constructor or another class that is guaranteed to load:
```java
@Mod("mymod")
public class MyMod {
    public MyMod(IEventBus bus) {
        MyItems.init(); // force class loading
    }
}
```

### `IllegalStateException: Registry entry '...' has not been bound yet`

**Cause:** `.get()` was called on an entry before registration is complete. Entry types now throw `IllegalStateException` with a descriptive message instead of a raw `NullPointerException`.

**Fix:**
1. Use the entry as a `Holder` or `Supplier` where possible —these resolve lazily.
2. Defer the `.get()` call to an event handler that runs after registration (e.g. `FMLCommonSetupEvent`).
3. If you need to check whether an entry is available without throwing, use `isBound()` or `getOptional()`:

```java
if (MY_ITEM.isBound()) {
    Item item = MY_ITEM.get();  // safe — registration is complete
}

// Or:
MY_ITEM.getOptional().ifPresent(item -> {
    // use item
});
```

`isBound()` returns `true` once the entry has been populated by the registry event. `getOptional()` returns `Optional.empty()` instead of throwing if the entry is not yet bound.

### `.attach(...)` compilation error on plain `Item`

**Cause:** Attachments are only supported on `ComponentItem` or `IComponentItem` implementations.

**Fix:** Switch from `.item(...)` to `.componentItem(...)`.

### `IllegalStateException: Builder already registered: <name>`

**Cause:** `.register()` or `.build()` was called more than once on the same builder instance. Every builder in RegistryLib is single-use —this guard exists on `AbstractBuilder`, `AbstractStateBuilder`, `EnchantmentBuilder`, `RecipeTypeBuilder`, `CropBuilder`, and `WorldgenFeatureBuilder`.

**Fix:** Ensure each builder chain calls `.register()` exactly once. If you need two entries with similar configuration, create two separate chains:

```java
// Wrong —reuses the same builder
var builder = REGISTRYLIB.item("a", Item::new).lang("A");
builder.register();
builder.register();  // throws IllegalStateException

// Correct —two independent chains
REGISTRYLIB.item("a", Item::new).lang("A").register();
REGISTRYLIB.item("b", Item::new).lang("B").register();
```

---

## Datagen Errors

### `runData` produces no output files

**Cause:** Registration chains were not executed before datagen runs.

**Fix:** Ensure your entry-holding classes are loaded during mod construction, before the `GatherDataEvent` fires.

### Generated lang file is missing some entries

**Cause:** `.lang(...)` or `.defaultLang()` was not called on those chains.

**Fix:** Add `.lang("Display Name")` to every registration chain that needs a display name.

---

## Runtime Errors

### `IllegalStateException: Renderer class loaded on server`

**Cause:** A `BlockEntityRenderer` or client-only class was referenced eagerly on the dedicated server.

**Fix:** Use the two-level (supplier-of-supplier) lazy form for renderer registration:
```java
.renderer(() -> () -> MyBlockEntityRenderer::new)
```

Two lambda levels are required, not one. The JVM resolves a lambda's *instantiated return type* when the lambda is **created** (linked), not when its body runs — so a single `() -> MyBlockEntityRenderer::new` (whose return type is the client-only `BlockEntityRendererProvider`) would force that class to load on the dedicated server even though the body never executes. The outer `Supplier` returns another `Supplier` (a non-client type), so the client renderer type only appears inside the inner lambda, which is linked exclusively on the client. See [Performance & Optimization](/tutorials/performance).

### Tooltip box renders at the wrong position

**Cause:** A custom `SubNode` implementation returns incorrect `getWidth()` or `getHeight()`.

**Fix:** Ensure your node's size methods return accurate pixel dimensions. See [Tooltip System](/tutorials/tooltip-system).

---

## Still stuck?

Check the [FAQ](/faq) or open an issue on [GitHub](https://github.com/GregTech-Odyssey/RegistryLib/issues).
