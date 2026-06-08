# Changelog

All notable changes to this project are documented here.

## [1.0.0] - 2026-06-08

First release.

### Added
- **Material highlighter GUI** (open with the `N` keybind): a scrollable, clickable list of every
  material in the loaded schematic, showing each item's icon, colour swatch, name, placed/total
  count, and how many are still missing.
- **Click-to-highlight**: click a material to highlight its remaining (un-placed) blocks in the
  world. Markers render through walls (depth test disabled) so hidden blocks are easy to find.
- **Highlight All Remaining**: highlight every missing block at once, colour-coded per material.
- **Sky Beacon locator**: toggle tall, pulsing, beacon-style columns that run past build height and
  are culled only by horizontal distance — so a block that is far away or buried at a very different
  altitude is still visible from across your base.
- **Live pruning**: highlighted markers disappear automatically as you place the blocks, with no
  full rescan and no per-frame world scanning.
- **Clear** (`B` keybind) and **Rescan** actions.
- Configurable marker render distance (default 96 blocks) and beam/beacon appearance via constants
  in `HighlightRenderer.java`.

### Notes
- Client-side only. Requires [Schematica](https://github.com/GTNewHorizons/Schematica) and
  LunatriusCore. Built for Minecraft 1.7.10 / GTNH.
