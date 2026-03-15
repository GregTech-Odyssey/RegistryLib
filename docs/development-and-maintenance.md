---
title: Development and Maintenance
nav_order: 7
permalink: /development-and-maintenance/
---

# Development and Maintenance

This page is for developers who are integrating RegistryLib into a project, maintaining it, or preparing a release. It does not explain how to use a single Builder. It explains how to keep the library stable inside a real project.

## Dependency Integration

Follow the existing dependency pattern from the repository README or build scripts, and keep your NeoForge and Minecraft versions aligned with the version you are targeting.

{: .important }
> If your branch, mappings, or NeoForge version does not match the current repository assumptions, fix version alignment before investigating API usage. Many “missing method” problems are version mismatches rather than documentation mistakes.

## Local Development Guidance

- Default to this work order: get a minimal registration chain working first, then layer more complex systems on top.
- Keep one minimal working example for each major content type so regressions are easier to isolate.
- When you introduce project-specific abstractions, evaluate whether they belong in Group or in a custom Builder rather than continuing to duplicate chained calls.

## Documentation and API Evolution

- Tutorial pages answer how to do something.
- System pages answer why it is organized that way.
- API Reference answers where to start looking.
- If you change a public workflow or shared term, update the documentation at the same time rather than waiting for the next major release.

## Release and Maintenance Concerns

- Avoid breaking renames in the public API unless there is a clear migration story.
- When adding Builder capabilities, first check whether the new behavior overlaps with the existing Group, Lang, or Tooltip systems.
- For every client-only type, verify that it still stays behind a lazy-loading path.

## Related Links

- [API Reference]({{ '/api-reference/' | relative_url }})
- [Advanced Topics]({{ '/advanced-topics/' | relative_url }})