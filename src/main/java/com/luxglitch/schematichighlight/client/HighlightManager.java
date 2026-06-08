package com.luxglitch.schematichighlight.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.client.world.SchematicWorld;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Holds the set of materials currently being highlighted and the schematic they belong to.
 *
 * <p>
 * Highlights are stored as {@link MaterialEntry} groups, each carrying the schematic-local
 * {@link Cell}s that are still missing. Every few ticks {@link #onClientTick} cheaply prunes cells
 * that have since been placed, so markers vanish as the user fills the build in — no full rescan and
 * no per-frame world scanning.
 */
public final class HighlightManager {

    public static final HighlightManager INSTANCE = new HighlightManager();

    private static final int PRUNE_INTERVAL_TICKS = 10;

    private final List<MaterialEntry> active = new ArrayList<>();
    private SchematicWorld schematic;
    private int tickCounter;
    /** When true, the renderer draws tall pulsing sky beacons (locator mode). Persists across highlights. */
    private boolean beaconMode;

    private HighlightManager() {}

    /** Highlight every still-missing material in the given list. */
    public synchronized void highlightAll(List<MaterialEntry> groups, SchematicWorld schematic) {
        this.active.clear();
        if (groups != null) {
            for (MaterialEntry entry : groups) {
                if (!entry.missing.isEmpty()) {
                    this.active.add(entry);
                }
            }
        }
        this.schematic = this.active.isEmpty() ? null : schematic;
    }

    /** Highlight a single material's remaining blocks. */
    public synchronized void highlightSingle(MaterialEntry entry, SchematicWorld schematic) {
        this.active.clear();
        if (entry != null && !entry.missing.isEmpty()) {
            this.active.add(entry);
            this.schematic = schematic;
        } else {
            this.schematic = null;
        }
    }

    public synchronized void clear() {
        this.active.clear();
        this.schematic = null;
    }

    /** True when there is something to render against the currently-loaded schematic. */
    public synchronized boolean isActive() {
        return !this.active.isEmpty() && this.schematic != null
            && this.schematic == com.github.lunatrius.schematica.proxy.ClientProxy.schematic;
    }

    public synchronized SchematicWorld schematic() {
        return this.schematic;
    }

    public synchronized List<MaterialEntry> groups() {
        return this.active;
    }

    public synchronized boolean isBeaconMode() {
        return this.beaconMode;
    }

    public synchronized boolean toggleBeaconMode() {
        this.beaconMode = !this.beaconMode;
        return this.beaconMode;
    }

    public synchronized int totalCells() {
        int total = 0;
        for (MaterialEntry entry : this.active) {
            total += entry.missing.size();
        }
        return total;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++this.tickCounter < PRUNE_INTERVAL_TICKS) {
            return;
        }
        this.tickCounter = 0;
        prune();
    }

    /** Remove cells that have since been placed; drop emptied groups; auto-clear on schematic change. */
    private synchronized void prune() {
        if (this.active.isEmpty()) {
            return;
        }

        final SchematicWorld current = com.github.lunatrius.schematica.proxy.ClientProxy.schematic;
        if (current == null || current != this.schematic) {
            this.active.clear();
            this.schematic = null;
            return;
        }

        final WorldClient world = Minecraft.getMinecraft().theWorld;
        if (world == null) {
            return;
        }

        final Vector3i pos = this.schematic.position;
        final Iterator<MaterialEntry> entryIterator = this.active.iterator();
        while (entryIterator.hasNext()) {
            final MaterialEntry entry = entryIterator.next();
            final Iterator<Cell> cellIterator = entry.missing.iterator();
            while (cellIterator.hasNext()) {
                final Cell cell = cellIterator.next();
                final int wx = pos.x + cell.x;
                final int wy = pos.y + cell.y;
                final int wz = pos.z + cell.z;
                if (world.getBlock(wx, wy, wz) == cell.block && world.getBlockMetadata(wx, wy, wz) == cell.meta) {
                    entry.placed++;
                    cellIterator.remove();
                }
            }
            if (entry.missing.isEmpty()) {
                entryIterator.remove();
            }
        }

        if (this.active.isEmpty()) {
            this.schematic = null;
        }
    }
}
