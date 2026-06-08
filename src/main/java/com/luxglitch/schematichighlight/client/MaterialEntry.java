package com.luxglitch.schematichighlight.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

/** One material in the schematic: its item, placed/total counts, and the cells still missing. */
public final class MaterialEntry {

    public final ItemStack itemStack;
    public int placed;
    public int total;
    public final List<Cell> missing = new ArrayList<>();
    /** Highlight colour {r,g,b}, derived once from the item. */
    public final float[] color;

    public MaterialEntry(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.color = ColorUtil.colorFor(itemStack);
    }

    public String displayName() {
        try {
            return this.itemStack.getDisplayName();
        } catch (Exception e) {
            return String.valueOf(this.itemStack.getItem());
        }
    }

    public int missingCount() {
        return this.missing.size();
    }

    public boolean isComplete() {
        return this.missing.isEmpty();
    }

    /** Formatted "placed/total" with colour codes (red while incomplete, green when done). */
    public String formattedAmount() {
        char color = this.placed < this.total ? 'c' : 'a';
        return String.format("§%c%d§r/%d", color, this.placed, this.total);
    }
}
