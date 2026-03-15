---
title: Glossary
parent: Getting Started
nav_order: 4
permalink: /glossary/
---

# Glossary

## RegistryCore

The main entry point in RegistryLib. It creates registration chains such as `item(...)`, `block(...)`, `fluid(...)`, and `blockEntity(...)`.

## Builder

A chained configuration object such as `ItemBuilder`, `BlockBuilder`, or `FluidBuilder`. It accumulates configuration and submits it during `.register()`.

## Entry

A wrapper type around the registered result, such as `ItemEntry`, `BlockEntry`, or `FluidEntry`. It is the most common way to reference a registered object later in code. Many Entry types also expose convenience helpers such as `asStack()`, `asResource()`, `getDefaultState()`, or fluid-family accessors.

## Group

A set of shared defaults wrapped around `RegistryCore`. It is appropriate when a batch of entries should inherit the same lang prefix, creative tab, or property modifiers.

## datagen

The data generation pipeline, including language files, models, recipes, loot tables, and Advancements.

## ComponentItem / attachment

`ComponentItem` is an Item type that supports reusable `ItemAttachment` components. An attachment encapsulates extended behavior such as right-click logic, tooltip contributions, or tick behavior. The higher-level registration entry point for this workflow is `componentItem(...)`.

## RootNode / SubNode

Two core concepts in the Tooltip System. `RootNode` decides where content is rendered, and `SubNode` represents the actual rendered text or custom visual element.
