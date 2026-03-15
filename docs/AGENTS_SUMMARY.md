---
title: AGENTS_SUMMARY
nav_exclude: true
search_exclude: true
---

# RegistryLib Docs Agent Summary

## Participants

- Agent A: tutorial-first, focused on onboarding path, example-driven teaching, and common pitfalls.
- Agent B: reference-first, focused on API boundaries, link stability, and reference completeness.
- Agent C: docs UX and Just the Docs, focused on navigation, templates, consistency, and scan-friendly presentation.

## Round 1: Shared Findings from Independent Review

All three agents agreed on the following:

1. There was no clear starting point, so new readers had to bounce across multiple pages.
2. Existing page responsibilities were mixed together, with tutorials, system explanations, and reference material lacking clear separation.
3. Mixed Chinese and English created an inconsistent reading experience.
4. Callout usage was unstable, especially around risk-heavy sections.
5. The docs were missing FAQ, troubleshooting, glossary, and API quick-reference pages.

## Round 2: Main Points of Debate

1. Whether the top-level navigation would become bloated after adding pages.
2. Whether special-optimizations should be split into multiple pages or rewritten in place.
3. Whether tutorial pages should still keep a Common API Lookup section.
4. Whether AGENTS_SUMMARY and STYLE_GUIDE should appear in the main navigation.
5. Whether the homepage should emphasize project identity or the reading path.

## Round 2: Converged Decisions

- Use parent pages to aggregate content and compress the top-level navigation into Homepage, Getting Started, Content Guides, Core Systems, API Reference, Advanced Topics, and Development and Maintenance.
- Keep Common API Lookup on tutorial pages, but limit it strictly to 3 to 5 methods so it does not duplicate API Reference.
- Keep the special-optimizations filename, but rewrite it as a RegistryLib-specific optimization strategy page.
- Keep AGENTS_SUMMARY and STYLE_GUIDE under docs/, but keep them out of the main navigation.
- Make the homepage primarily about the reading path while preserving a concise project overview.

## Final Information Architecture

```text
Homepage
Getting Started
  quickstart
  faq
  troubleshooting
  glossary
Content Guides
  register-items
  register-blocks
  register-block-entities-and-renderers
  register-fluids-and-buckets
  register-advancements
Core Systems
  group-system
  tooltip-system
  lang-system
API Reference
Advanced Topics
  override-builders
  special-optimizations
Development and Maintenance
```

## Round 3: Sign-Off

### Agent A

I agree with the final plan.

Reason: the learning path is complete, the templates are stable, and the division between onboarding, FAQ, and Troubleshooting supports a clean teaching flow from 0 to 1.

### Agent B

I agree with the final plan.

Reason: the core URLs remain stable, new pages cover missing responsibilities, and the boundary between API Reference and tutorials is clear without breaking existing navigation assumptions.

### Agent C

I agree with the final plan.

Reason: the top-level navigation is more focused, parent-page aggregation is sensible, and the callout and template rules are concrete enough to maintain. Both scanability and long-term maintenance improve under Just the Docs.

## Shared Conclusion

All three agents ultimately agreed to:

- fully rewrite the Markdown pages under docs/
- add Getting Started, system parent pages, API Reference, and necessary support pages
- unify the technical writing style and layout rules in English
- rebuild the documentation around the path “get it working first, extend it next, look it up when needed” without inventing API facts
