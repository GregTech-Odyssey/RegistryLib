---
title: 5-Minute Quickstart
parent: Getting Started
nav_order: 1
permalink: /quickstart/
---

# 5-Minute Quickstart

This page has one goal: show you the shortest path to a working basic registration chain in RegistryLib.

## When This Page Applies / Prerequisites

- You have already added RegistryLib as a dependency to your project.
- You have a usable `RegistryCore` instance, for example `RegistryLibTest.REGISTRYLIB`.
- You only want to get a minimal Item working first rather than understanding every system at once.

## Minimal Example

```java
public static final ItemEntry<Item> COPPER_COIN = RegistryLibTest.REGISTRYLIB
        .item("copper_coin", Item::new)
        .lang("Copper Coin")
        .register();
```

This chain does three things at once: it declares the registry name, provides the factory method, and generates the English display name.

## How You Should Verify It

1. Make sure the field is declared in a registration class that is actually loaded.
2. Run your normal project build or client startup flow.
3. In game or in generated resources, confirm that `copper_coin` exists and that its display name is `Copper Coin`.

{: .important }
> Defining the field alone is not enough. If the containing class never participates in initialization, the registration will not take effect. In most cases this is not a RegistryLib problem but a class-loading path that was never triggered.

## Where to Go Next

- Continue with [Registering Items]({{ '/register-items/' | relative_url }}) to add models, tooltips, recipes, and attachments.
- If you are blocked while adding the dependency, go back to [Development and Maintenance]({{ '/development-and-maintenance/' | relative_url }}).
- If you are already seeing problems such as “why was nothing generated?” or “why is nothing visible?”, go straight to [Troubleshooting]({{ '/troubleshooting/' | relative_url }}).
