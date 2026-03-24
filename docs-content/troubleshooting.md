---
sidebar_position: 101
title: Troubleshooting
description: Common errors and how to fix them.
---

# Troubleshooting

## Build & Dependency Errors

### `Could not resolve com.gto:registrylib:x.x.x`

**Cause:** Gradle cannot reach GitHub Packages.

**Fix:**
1. Verify your `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables are set correctly.
2. Confirm the token has `read:packages` scope.
3. Ensure the Maven repository URL in `settings.gradle` is `https://maven.pkg.github.com/GregTech-Odyssey/RegistryLib`.

### `NoClassDefFoundError` or `ClassNotFoundException` for RegistryLib classes

**Cause:** RegistryLib is not on the runtime classpath.

**Fix:** Ensure you are using `implementation` (not `compileOnly`) in your `build.gradle` dependencies:
```groovy
dependencies {
    implementation 'com.gto:registrylib:1.0.0'
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

### `NullPointerException` when calling `.get()` on an Entry

**Cause:** `.get()` was called before registration is complete (before `FMLCommonSetupEvent`).

**Fix:** Use the Entry as a `Holder` or `Supplier` where possible, or defer the `.get()` call to an event handler that runs after registration.

### `.attach(...)` compilation error on plain `Item`

**Cause:** Attachments are only supported on `ComponentItem` or `IComponentItem` implementations.

**Fix:** Switch from `.item(...)` to `.componentItem(...)`.

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

**Fix:** Use lazy loading for renderer registration:
```java
.renderer(() -> MyBlockEntityRenderer::new)
```

The `Supplier` wrapping ensures the renderer class is only loaded on the client. See [Performance & Optimization](/tutorials/performance).

### Tooltip box renders at the wrong position

**Cause:** A custom `SubNode` implementation returns incorrect `getWidth()` or `getHeight()`.

**Fix:** Ensure your node's size methods return accurate pixel dimensions. See [Tooltip System](/tutorials/tooltip-system).

---

## Still stuck?

Check the [FAQ](/faq) or open an issue on [GitHub](https://github.com/GregTech-Odyssey/RegistryLib/issues).
