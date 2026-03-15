---
title: RegistryLib
nav_order: 1
permalink: /
---

# RegistryLib

RegistryLib is a fluent registration library for NeoForge. Its goal is to consolidate common content registration work from scattered boilerplate into a single builder chain that is composable, resource-generation friendly, and maintainable.

If this is your first time working with the project, start with Getting Started. If you already know what you want to register, go straight to Content Guides or API Reference.

## What You Will Find Here

| Entry Point | Best For | What You Will Get |
| --- | --- | --- |
| [Getting Started]({{ '/start-here/' | relative_url }}) | Developers using RegistryLib for the first time | A 5-minute quickstart, FAQ, troubleshooting, and a glossary |
| [Content Guides]({{ '/content-guides/' | relative_url }}) | Developers who already know they want to register Items, Blocks, Fluids, and similar content | Tutorials organized by content type |
| [Core Systems]({{ '/systems-overview/' | relative_url }}) | Developers who want to understand cross-cutting features such as Group, Tooltip, and Lang | Design guidance, composition patterns, and system boundaries |
| [API Reference]({{ '/api-reference/' | relative_url }}) | Developers who need a quick lookup for entry points, builder families, and common chains | A concise reference and responsibility map |
| [Advanced Topics]({{ '/advanced-topics/' | relative_url }}) | Developers who need custom Builders or want to understand implementation strategies | Extension and optimization guidance |

{: .note }
> The documentation is written in English, while type names, method names, and class names remain unchanged so they continue to match the source code and IDE hints directly.

## Recommended Reading Path

1. Start with [5-Minute Quickstart]({{ '/quickstart/' | relative_url }}) to get a minimal Item working.
2. Then move to [Registering Items]({{ '/register-items/' | relative_url }}), [Registering Blocks]({{ '/register-blocks/' | relative_url }}), or [Registering Fluids and Buckets]({{ '/register-fluids-and-buckets/' | relative_url }}) based on the content type you need.
3. When you begin reusing defaults or organizing more complex tooltips, continue with [Group System]({{ '/group-system/' | relative_url }}), [Tooltip System]({{ '/tooltip-system/' | relative_url }}), and [Lang System]({{ '/lang-system/' | relative_url }}).
4. If you need to customize Builders, continue with [Override Builders]({{ '/override-builders/' | relative_url }}).

## Repository and Project Information

- [Development and Maintenance]({{ '/development-and-maintenance/' | relative_url }}): dependency integration, local development, release workflow, and API conventions.
- [GitHub Repository](https://github.com/GregTech-Odyssey/RegistryLib)
