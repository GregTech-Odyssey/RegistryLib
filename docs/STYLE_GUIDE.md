---
title: STYLE_GUIDE
nav_exclude: true
search_exclude: true
---

# RegistryLib Docs Style Guide

## Goal

The documentation serves four jobs at the same time: fastest onboarding, implementation guidance, quick lookup, and long-term maintenance. Tutorial pages teach, system pages explain, API Reference supports lookup, and meta documents enforce the rules.

## Information Architecture

- Homepage: project positioning and reading entry points.
- Getting Started: quickstart, FAQ, troubleshooting, and glossary.
- Content Guides: tutorials organized by registered object type.
- Core Systems: explanations organized by cross-cutting system.
- API Reference: method families, responsibility boundaries, and quick lookup without full teaching.
- Advanced Topics: extension points and implementation strategy.
- Maintenance: release, compatibility, and contribution boundaries after setup is already complete.

## Page Templates

### Tutorial Page Template

1. What this page solves
2. When this applies or prerequisites
3. Quick start
4. Full example
5. Step-by-step explanation
6. Common patterns or common pitfalls
7. Common API lookup
8. Related links

### System Page Template

1. Core purpose
2. When to use it
3. Quick example
4. Core concepts
5. Common combinations
6. Boundaries and pitfalls
7. Related links

### API Reference Template

1. Entry-point selection
2. Type and Builder mapping
3. Common chain lookup
4. Links to tutorials and system pages

## Language and Terminology

- Explanations are written in English.
- Class names, method names, type names, and annotation names stay in English exactly as they appear in code.
- When an important term first appears, prefer giving it a one-sentence explanation in the text. Complex terms should be centralized in the glossary.
- Do not alternate between multiple translated names for the same concept inside one page.

## Example Rules

- Keep one minimal example and one fuller example on each page.
- Minimal examples should usually stay within 5 to 12 lines.
- Full examples should only cover the theme of the current page instead of forcing every cross-topic feature into one block.
- Examples must remain internally consistent with concepts that already exist in the repository. Do not invent runtime results or nonexistent APIs.

## Boundary Between Tutorial API Lookup and API Reference

- A tutorial page's Common API Lookup section should list only 3 to 5 frequently used methods and explain when each is used in one sentence.
- API Reference should list Builder families, Entry types, entry-point selection, and common chains.
- Full examples, scenario-driven explanation, and step-by-step teaching should stay only on tutorial and system pages, not be duplicated in API Reference.

## Callout Rules

- Only `.note`, `.important`, and `.warning` are allowed.
- Each callout should stay within 1 to 3 lines.
- A single page should usually not exceed 3 callouts.
- If a point works as a normal paragraph, do not force it into a callout.
- `.important` is for critical constraints or frequent misuse, `.warning` is for situations that can cause failure or invalid loading, and `.note` is for supporting context.

## Internal Linking Rules

- Every page must end with a Related Links section.
- A tutorial page should link to at least one parent hub page, one neighboring tutorial page, and one related system page.
- A system page should link to at least one parent hub page and two tutorial pages that use the system.
- FAQ and Troubleshooting should link back to the relevant tutorial pages instead of duplicating their core content.

## Retention Standard for special-optimizations

Keep content only if it relates directly to RegistryLib's Builder, Entry, datagen, event, or resource lifecycle.

Remove content if it is only general Java performance teaching, such as concurrency, generics, collection, or JVM tricks that are detached from RegistryLib context.

Decision rule: if a section cannot answer “why does this affect RegistryLib's registration, generation, or runtime lifecycle?”, it should not remain on that page.

## Meta Document Rules

- AGENTS_SUMMARY.md and STYLE_GUIDE.md stay under docs/ as repository deliverables.
- Neither should appear in the main navigation by default, and neither is part of the external end-user documentation path.
