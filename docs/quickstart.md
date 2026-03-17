---
title: 5-Minute Quickstart
parent: Getting Started
nav_order: 1
permalink: /quickstart/
---

# 5-Minute Quickstart

This page has one goal: show you the shortest path from dependency setup to a working basic registration chain in RegistryLib.

## When This Page Applies / Prerequisites

- You are integrating RegistryLib into a project for the first time.
- You want one page that covers dependency setup, local validation, and the first minimal registration chain.
- You only want to get a minimal Item working first rather than understanding every system at once.

{: .note }
> The first registration example below is excerpted from `SimpleItemExample` in `RegistryLibTest`, and the current test mod passes `runData`.

## Development Setup

RegistryLib is published through GitHub Packages. For direct dependency resolution, prepare credentials first.

### Step 1: Create a Read-Only GitHub Token

Create a classic GitHub Personal Access Token with the `read:packages` scope.

### Step 2: Expose Credentials to Gradle

On Windows, the simplest local setup is:

```cmd
setx GITHUB_ACTOR your-github-username
setx GITHUB_TOKEN your-token
```

Restart the terminal after setting these values.

{: .important }
> `setx` stores user-level environment variables in plaintext for your local account. That is acceptable for local development on a private machine, but avoid it on shared or production systems.

### Step 3: Add the Package Repository

Recommended `settings.gradle`:

```groovy
dependencyResolutionManagement {
        repositories {
                maven {
                        name = 'GitHubPackages-RegistryLib'
                        url = uri('https://maven.pkg.github.com/GregTech-Odyssey/RegistryLib')
                        credentials {
                                username = System.getenv('GITHUB_ACTOR')
                                                ?: settings.providers.gradleProperty('gpr.user').orNull
                                password = System.getenv('GITHUB_TOKEN')
                                                ?: settings.providers.gradleProperty('gpr.key').orNull
                        }
                }
        }
}
```

### Step 4: Add the Dependency

`build.gradle`:

```groovy
dependencies {
        implementation 'com.gto:registrylib:1.0.0'
}
```

Replace the version with the target release you are using, and keep your Minecraft, mappings, and NeoForge versions aligned with that release.

### Step 5: Verify the Local Tooling Path

Common commands during setup:

| Command | Purpose |
| --- | --- |
| `./gradlew build` | Validate that the project resolves and builds |
| `./gradlew runClient` | Start the client and verify registrations in game |
| `./gradlew runServer` | Check dedicated-server safety |
| `./gradlew runData` | Confirm datagen output |

## Create `REGISTRYLIB` First

Before you can register an Item, Block, or Fluid, you need one shared registry entry point.

In `RegistryLibTest`, that field is created like this:

```java
public static final String MOD_ID = "registrylibtest";
public static final ModRegistryCore REGISTRYLIB = ModRegistryCore.create(MOD_ID);
```

This is why the later examples can call `RegistryLibTest.REGISTRYLIB.item(...)` directly.

{: .note }
> The test mod uses `ModRegistryCore` instead of the base `RegistryCore` because its custom Builder layer adds `langCn(...)`. If you do not need that custom Builder behavior, the plain equivalent is `RegistryCore.create(MOD_ID)`.

## Minimal Example

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .langCn("铜币")
        .lang("Copper Coin")
        .register();
```

This is the real minimal item chain used by the test mod after the shared `REGISTRYLIB` field already exists: it declares the registry name, provides the factory method, and generates both the English and Chinese display names.

## How You Should Verify It

1. Make sure the field is declared in a registration class that is actually loaded.
2. Run your normal project build or client startup flow.
3. In game or in generated resources, confirm that `copper_coin` exists and that its display name is `Copper Coin`.

{: .important }
> Defining the field alone is not enough. If the containing class never participates in initialization, the registration will not take effect. In most cases this is not a RegistryLib problem but a class-loading path that was never triggered.

## Where to Go Next

- Continue with [Registering Items]({{ '/register-items/' | relative_url }}) to add models, tooltips, recipes, and attachments.
- If dependency resolution is working but release or compatibility rules are unclear, continue with [Maintenance]({{ '/development-and-maintenance/' | relative_url }}).
- If you are already seeing problems such as “why was nothing generated?” or “why is nothing visible?”, go straight to [Troubleshooting]({{ '/troubleshooting/' | relative_url }}).
