---
title: Registering Advancements
parent: Content Guides
nav_order: 5
permalink: /register-advancements/
---

# Registering Advancements

## What This Page Solves

When you want Advancement definitions to follow the same organizational style as the rest of your RegistryLib-based content, the relevant chains help keep naming, trigger structure, and output in one place. This page focuses on integration and maintainable structure rather than on reteaching Minecraft's entire Advancement system.

## When This Applies

- You are already using RegistryLib for other content types.
- You want Advancement datagen and naming conventions to stay aligned with the rest of the project.
- You need to know where the entry points live in this documentation, not read a full Minecraft Advancement tutorial.

## Basic Approach

1. Decide which progression line or content flow the Advancement belongs to.
2. Keep naming, display text, and generation paths consistent with the rest of the project.
3. Keep RegistryLib focused on declaration and organization, and keep complex trigger logic in readable construction code.

## Design Suggestions

### Keep Hierarchy Explicit

If a group of Advancements belongs to the same progression, do not scatter them across unrelated classes. Organizing by functional area is easier to maintain.

### Do Not Force Complex Conditions into One Unreadable Chain

RegistryLib is good at organizing the entry point, naming, and output location. If the trigger condition itself is complex, extract it into named variables or helper methods.

{: .note }
> This page is not intended to replace Minecraft or NeoForge documentation for the Advancement mechanism itself. It explains how that content should be hosted inside a RegistryLib-based project structure.

## When to Continue Elsewhere

- If you care about language generation and multi-locale output, continue with [Lang System]({{ '/lang-system/' | relative_url }}).
- If you care about project organization and datagen entry points, continue with [Development and Maintenance]({{ '/development-and-maintenance/' | relative_url }}).
