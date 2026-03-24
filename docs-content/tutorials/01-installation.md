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
- A GitHub account (for package access)

## Step 1: Create a Read-Only GitHub Token

RegistryLib is published through GitHub Packages. To resolve the dependency, you need a GitHub Personal Access Token.

1. Go to **GitHub �?Settings �?Developer settings �?Personal access tokens �?Tokens (classic)**.
2. Click **Generate new token (classic)**.
3. Select only the **`read:packages`** scope.
4. Generate and copy the token.

## Step 2: Expose Credentials to Gradle

On Windows, the simplest local setup is:

```cmd
setx GITHUB_ACTOR your-github-username
setx GITHUB_TOKEN your-token
```

Restart the terminal after setting these values.

:::warning
`setx` stores user-level environment variables in plaintext for your local account. That is acceptable for local development on a private machine, but avoid it on shared or production systems.
:::

## Step 3: Add the Package Repository

Add the following to your `settings.gradle`:

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

## Step 4: Add the Dependency

In your `build.gradle`:

```groovy
dependencies {
        implementation 'com.gto:registrylib:1.0.0'
}
```

:::note
Replace the version with the target release you are using, and keep your Minecraft, mappings, and NeoForge versions aligned with that release.
:::

## Step 5: Verify the Local Tooling Path

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
