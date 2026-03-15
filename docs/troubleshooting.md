---
title: Troubleshooting
parent: Getting Started
nav_order: 3
permalink: /troubleshooting/
---

# Troubleshooting

This page is organized by symptom rather than by API. When something goes wrong, start from the symptom and narrow the scope from there.

## Symptom: The Registered Object Does Not Exist at All

Check these three things first:

1. Whether the containing class is actually loaded.
2. Whether the registration chain ends with `.register()`.
3. Whether you are using the correct `RegistryCore` or `Group` instance.

## Symptom: The Block Exists, but There Is No BlockItem

The usual cause is that `.simpleItem()` or `.item(...)` was omitted. If you explicitly removed the creative tab, also confirm that the problem is not only about where the item is displayed.

## Symptom: The Server Reports Errors Related to Client Classes

This most often happens when a `BlockEntity` renderer or another client-only type is referenced too early. Check whether you kept the `Supplier`-based lazy-loading pattern intact.

{: .warning }
> As soon as a client class appears on a server-side initialization path, it can trigger `ClassNotFoundException`. Do not instantiate renderers directly in shared static fields.

## Symptom: Fluids Do Not Render or Behave Correctly

First confirm the still and flow texture paths. Then confirm that you selected the correct `clientExtension(...)` overload. Grayscale textures usually pair with the three-argument overload, while textures that already contain color information usually pair with the two-argument overload.

## Symptom: Multilingual Output Does Not Go to the Expected Locale

If you are using the `ProviderType` approach, confirm that the provider returns the correct `ProviderType`. If you are using a custom Builder approach, confirm that you are actually receiving your custom Builder type rather than falling back to the base Builder.

## Still Not Finding the Answer?

- Go back to the relevant tutorial page and confirm that the minimal chain works first.
- Then review [FAQ]({{ '/faq/' | relative_url }}), [5-Minute Quickstart]({{ '/quickstart/' | relative_url }}), and [Maintenance]({{ '/development-and-maintenance/' | relative_url }}).
