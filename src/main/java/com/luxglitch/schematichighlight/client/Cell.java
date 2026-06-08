package com.luxglitch.schematichighlight.client;

import net.minecraft.block.Block;

/**
 * A single schematic cell that still needs a block placed.
 *
 * <p>
 * Coordinates are <b>schematic-local</b> (0..width/height/length). The expected {@link #block} and
 * {@link #meta} are captured at scan time so pruning can cheaply re-check "has this been placed yet?"
 * against the real world without re-deriving anything from the schematic (and without being affected
 * by Schematica's layer-view masking).
 */
public final class Cell {

    public final int x;
    public final int y;
    public final int z;
    public final Block block;
    public final int meta;

    public Cell(int x, int y, int z, Block block, int meta) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.meta = meta;
    }
}
