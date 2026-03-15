---
title: Home
nav_order: 1
---

# RegistryLib

RegistryLib is a NeoForge library mod built to reduce registry boilerplate and make common content
setup feel much more consistent. Instead of scattering item registration, block setup, fluid
definitions, tooltip logic, and data-related glue code across multiple classes, you can keep that
work inside a clean fluent builder workflow.

The RegistryLibTest module shows the everyday use cases that matter most for real mod development.
It covers fast item and block registration, automatic item bindings, custom loot behavior, grouped
configuration for content families, and tooltip composition that stays readable even when your
content starts getting more complex.

RegistryLib also gives fluid content a much cleaner setup path. The examples include tinted fluids,
vanilla-style texture reuse, linked fluid blocks, and bucket item customization, which makes it a
strong fit for tech mods, material-heavy mods, and projects that need to add a lot of structured
content without drowning in setup code.

For larger projects, the library goes beyond basic registration. RegistryLibTest demonstrates
CompositeItem attachments, BlockEntity binding, client renderer integration, and structured tooltip
nodes, so the API remains useful even after your mod grows past simple items and decorative blocks.

If you want a registry workflow that scales from quick prototypes to full content mods without
turning into maintenance-heavy boilerplate, RegistryLib is designed for that job. It is a practical
builder-based toolkit for NeoForge developers who want cleaner registration code and more time to
focus on gameplay.

## Guides

| Development and Maintenance |
| --- |
| [Development and Maintenance](development-and-maintenance) |

| Items | Blocks |
| --- | --- |
| [Register Items](register-items) | [Register Blocks](register-blocks) |

| Block Entities and Renderers | Fluids and Buckets |
| --- | --- |
| [Register Block Entities and Renderers](register-block-entities-and-renderers) | [Register Fluids and Buckets](register-fluids-and-buckets) |

| Advancements |
| --- |
| [Register Advancements](register-advancements) |

| Systems |
| --- |
| [Tooltip System](tooltip-system) |

## Repository

- [GitHub Repository](https://github.com/GregTech-Odyssey/RegistryLib)
