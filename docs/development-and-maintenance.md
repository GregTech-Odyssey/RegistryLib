---
title: Maintenance
nav_order: 7
permalink: /development-and-maintenance/
---

# Maintenance

This page is for maintainers and release owners. Dependency integration and local setup now live in the 5-Minute Quickstart. This page keeps the repository-level rules that remain important after a project is already building.

## What Belongs Here

- Public API maintenance rules
- version updates and release publishing
- repository-wide conventions that affect long-term compatibility

{: .important }
> If you are still trying to add RegistryLib to a project or get your first run working, go back to [5-Minute Quickstart]({{ '/quickstart/' | relative_url }}). Maintenance starts after the integration path is already healthy.

## API Design Conventions

Builder methods are annotated with `@StandardAPI` and `@SyntaxSugar`.

- `@StandardAPI` marks the core API contract.
- `@SyntaxSugar` marks a convenience layer that delegates to a `@StandardAPI` path.

When reviewing or evolving the API, treat `@StandardAPI` methods as the compatibility baseline. `@SyntaxSugar` methods are still useful public surface, but they should not redefine the contract that the rest of the library depends on.

## Maintenance Rules for Public Surface

- Avoid breaking renames unless there is a clear migration story.
- Before adding a new Builder capability, check whether the feature really belongs in Group, Lang, Tooltip, or a custom Builder instead of creating overlapping entry points.
- For every client-only type, verify that it still stays behind a lazy-loading path.
- If you change a public workflow or shared term, update the matching tutorial, system page, and API Reference entry in the same change.

## Updating the Published Version

The release workflow is driven from `mod_version` in `gradle.properties`.

1. Update `mod_version` to the version you intend to publish.
2. Commit and push the change.
3. Open GitHub and run the Actions workflow that publishes the package.

The workflow will build the project, publish the package, and create the corresponding tagged release. If the tag already exists, the automation increments the generated build suffix.

{: .important }
> Version bumps are not just cosmetic. The published Maven coordinate and release artifact are derived from the repository version state, so forgetting to update `mod_version` creates incorrect release metadata.

## Release Checklist

1. Confirm the branch is on the intended Minecraft and NeoForge baseline.
2. Confirm public API changes still respect the `@StandardAPI` and `@SyntaxSugar` boundary.
3. Update `mod_version` in `gradle.properties`.
4. Run a clean local build before triggering the release workflow.
5. Verify that the published package and generated GitHub Release use the expected version.

## Documentation Responsibility

- Tutorial pages explain how to use the API.
- System pages explain why the systems are structured the way they are.
- API Reference is the lookup surface.
- Maintenance pages record repository-level rules that affect releases, compatibility, and long-term upkeep.

## Related Links

- [5-Minute Quickstart]({{ '/quickstart/' | relative_url }})
- [API Reference]({{ '/api-reference/' | relative_url }})
- [Advanced Topics]({{ '/advanced-topics/' | relative_url }})