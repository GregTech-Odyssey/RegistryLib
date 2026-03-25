---
sidebar_position: 1
title: Installation & Setup
description: Set up RegistryLib in your NeoForge project in 3 minutes.
---

# Installation & Setup

This page walks you through dependency setup and local validation so you can start registering content with RegistryLib as quickly as possible.

## Prerequisites

- A working NeoForge mod project (Minecraft 1.21.x)
- Gradle 8+ with a Java 21 toolchain

## Step 1: Add the Maven Repository

RegistryLib is published on the Gtodyssey Maven repository. No authentication is required to download.

Add the following to your `settings.gradle`:

```groovy
dependencyResolutionManagement {
        repositories {
                maven {
                        name = 'Gtodyssey'
                        url = uri('https://maven.gtodyssey.com/releases')
                }
        }
}
```

Or, if you prefer to configure repositories in `build.gradle`:

```groovy
repositories {
        maven {
                url = 'https://maven.gtodyssey.com/releases'
        }
}
```

## Step 2: Add the Dependency

In your `build.gradle`:

```groovy
dependencies {
        implementation 'com.gto:registrylib:1.0.0'
}
```

:::note
Replace the version with the target release you are using, and keep your Minecraft, mappings, and NeoForge versions aligned with that release.
:::

## Step 3: Verify the Local Tooling Path

| Command | Purpose |
| --- | --- |
| `./gradlew build` | Validate that the project resolves and builds |
| `./gradlew runClient` | Start the client and verify registrations in game |
| `./gradlew runServer` | Check dedicated-server safety |
| `./gradlew runData` | Confirm datagen output |

Run `./gradlew build` first. If it succeeds, your dependency setup is correct.

## Create `REGISTRYLIB` First

Before you can register an Item, Block, or Fluid, you need one shared registry entry point. This is a static field that all your registration calls will reference.

```java
public static final String MOD_ID = "examplemod";
public static final ModRegistryCore REGISTRYLIB = ModRegistryCore.create(MOD_ID);
```

:::tip
The test mod uses `ModRegistryCore` instead of the base `RegistryCore` because its custom Builder layer adds `langCn(...)`. If you do not need that custom Builder behavior, the plain equivalent is:

```java
public static final RegistryCore REGISTRYLIB = RegistryCore.create(MOD_ID);
```
:::

## What's Next

Your environment is ready. Head to [Your First Item](/tutorials/first-item) to register your first piece of content with RegistryLib.
