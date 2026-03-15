---
title: FAQ
parent: Getting Started
nav_order: 2
permalink: /faq/
---

# FAQ

This page collects the most common “why did nothing happen?” questions and prioritizes the shortest path to an answer.

## Registration Chains

### Why does my entry not appear?

First check whether the registration class is actually loaded. Then confirm that the chain ends with `.register()`.

### Why can `.attach(...)` not be used on a normal `Item`?

Because attachments are an extension point for `CompositeItem`. A normal `Item` does not have the corresponding attachment lifecycle.

### Why did I register a Block, but there is no matching BlockItem in game?

Because Block and BlockItem are two separate registered objects. You need to call `.simpleItem()` or `.item(...)` explicitly.

## Datagen and Language

### Why did `.lang(...)` not appear in the language file I expected?

First confirm that you are using RegistryLib's datagen pipeline. Then confirm that the relevant page's `ProviderType` setup or default language configuration is correct.

### How should I choose between `.defaultLang()` and `.lang(...)`?

Use `.defaultLang()` when the registry name is enough to derive the display name. Use `.lang(...)` when you need a specific display string.

## Structure and Design

### When should I use Group?

Use Group when multiple entries share a lang prefix, a creative tab, or default Block or Item property modifiers. For a single isolated entry, going directly through `RegistryCore` is usually enough.

### When should I define a custom Builder?

Look at [Override Builders]({{ '/override-builders/' | relative_url }}) when you repeatedly need the same project-specific syntax sugar or default rules, and those rules cannot be expressed cleanly with only Group or ordinary chained calls.

## Continue Reading

- [Troubleshooting]({{ '/troubleshooting/' | relative_url }})
- [Glossary]({{ '/glossary/' | relative_url }})
- [API Reference]({{ '/api-reference/' | relative_url }})
