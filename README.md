# Schematica Material Highlighter

[![Latest release](https://img.shields.io/github/v/release/luxglitch/SchematicaHighlighter?sort=semver)](https://github.com/luxglitch/SchematicaHighlighter/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/luxglitch/SchematicaHighlighter/total)](https://github.com/luxglitch/SchematicaHighlighter/releases)
[![Minecraft 1.7.10](https://img.shields.io/badge/Minecraft-1.7.10-brightgreen)](https://github.com/GTNewHorizons)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A small **client-side GTNH (Minecraft 1.7.10) Forge add-on** for
[Schematica](https://github.com/GTNewHorizons/Schematica). It does **not** modify Schematica —
it depends on Schematica's public classes and adds:

- A **clickable materials list**: open it with a keybind, click any material, and every
  **remaining (un-placed) block** of that material is highlighted in the world.
- **Through-wall highlighting**: the markers render on top of everything (depth test disabled),
  so blocks hidden behind other blocks are easy to find. Short vertical beams help you spot a buried
  block nearby.
- **Sky Beacon locator**: toggle tall, pulsing, beacon-style columns that run from the ground past
  build height. Unlike the normal markers, beacons are *not* distance-limited (culled only by
  horizontal distance) and ignore altitude — so a single block that's far away or buried deep is
  visible from across your base. Ideal for hunting down that last elusive block.
- **"Highlight All Remaining"**: highlight every missing block at once, color-coded.
- **Live updates**: as you place the highlighted blocks, their markers disappear automatically
  (cheap pruning — no full rescan needed).

## Download & install

1. Download the latest `schematichighlight-<version>.jar` from the
   [**Releases**](https://github.com/luxglitch/SchematicaHighlighter/releases/latest) page.
2. Drop it into your instance's `mods/` folder, alongside **Schematica** and **LunatriusCore**
   (both already ship with GTNH).
3. Launch the game, load a schematic in Schematica, and press **N**.

It is a client-side mod and does nothing on a dedicated server.

## How it works (no fork required)

Schematica already compares the loaded schematic against the real world to build its materials
count (`com.github.lunatrius.schematica.client.util.BlockList`). This add-on performs the same
comparison but also records the **schematic-local coordinates** of the missing cells, then renders
them. It reads the loaded schematic from `ClientProxy.schematic` (a public static field) and its
world anchor from `SchematicWorld.position` — all public API, so no reflection and no Mixins.

## Default controls

| Action | Default key |
| --- | --- |
| Open material highlighter GUI | `N` |
| Clear all highlights | `B` |
| Toggle Sky Beacon | unbound |

Rebind these under *Options → Controls → "Schematica Highlighter"*.

In the GUI:
- **Click a material row** → highlight that material's remaining blocks.
- **Highlight All Remaining** → highlight everything still missing.
- **Sky Beacon: ON/OFF** → toggle the tall locator columns for whatever is highlighted.
- **Clear** → remove highlights.
- **Rescan** → recompute from the current world state.
- **Done** → close (highlights stay active in the world).

> **Tip for finding one elusive block:** click just that material so only it is highlighted, then
> turn **Sky Beacon** on. A single bright column will mark its column from anywhere in your base —
> walk to its base and the normal box guides you to the exact spot.

## Building

This uses the standard GTNH build system (RetroFuturaGradle + GTNH conventions), identical to
Schematica itself.

Requirements: **JDK 17** on your `PATH`/`JAVA_HOME` (the build targets Java 8 bytecode via Jabel,
but Gradle itself runs on 17).

```sh
./gradlew build          # produces build/libs/<modid>-<version>.jar
./gradlew runClient      # launches a dev client with the mod (and its deps) loaded
```

The first run downloads Forge, MCP mappings, Schematica, and LunatriusCore from the GTNH Maven.

> **Dependency versions:** `dependencies.gradle` pins the Schematica and LunatriusCore `:dev`
> artifacts. Bump the Schematica version there to match the build you are targeting.

## Install (in a real instance)

Drop the built jar into `mods/` alongside Schematica and LunatriusCore. It is a client-side mod;
it does nothing on a dedicated server.
