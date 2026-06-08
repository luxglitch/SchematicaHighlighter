package com.luxglitch.schematichighlight.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.luxglitch.schematichighlight.reference.Reference;

/**
 * Walks the loaded schematic and compares each cell against the real world, exactly like Schematica's
 * own {@code BlockList} does, but additionally records the schematic-local coordinates of every cell
 * that is still missing so they can be highlighted.
 */
public final class MaterialScanner {

    private MaterialScanner() {}

    public static List<MaterialEntry> scan(SchematicWorld schematic, WorldClient mcWorld, EntityPlayer player) {
        final List<MaterialEntry> list = new ArrayList<>();
        if (schematic == null || mcWorld == null || player == null) {
            return list;
        }

        final Vector3i pos = schematic.position;
        final MovingObjectPosition movingObjectPosition = new MovingObjectPosition(player);

        final int height = schematic.getHeight();
        final int width = schematic.getWidth();
        final int length = schematic.getLength();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    // Respect Schematica's "single layer" view so our list matches what the user sees.
                    if (schematic.isRenderingLayer && y != schematic.renderingLayer) {
                        continue;
                    }

                    final Block block = schematic.getBlock(x, y, z);
                    if (block == Blocks.air || schematic.isAirBlock(x, y, z)) {
                        continue;
                    }

                    final int wx = pos.x + x;
                    final int wy = pos.y + y;
                    final int wz = pos.z + z;
                    final int meta = schematic.getBlockMetadata(x, y, z);

                    final Block mcBlock = mcWorld.getBlock(wx, wy, wz);
                    final boolean placed = block == mcBlock && meta == mcWorld.getBlockMetadata(wx, wy, wz);

                    ItemStack stack = null;
                    try {
                        stack = block.getPickBlock(movingObjectPosition, schematic, x, y, z, player);
                    } catch (Exception e) {
                        Reference.logger.debug("Could not get the pick block for: {}", block, e);
                    }

                    if (stack == null || stack.getItem() == null) {
                        continue;
                    }

                    final MaterialEntry entry = findOrCreate(list, stack);
                    entry.total++;
                    if (placed) {
                        entry.placed++;
                    } else {
                        entry.missing.add(new Cell(x, y, z, block, meta));
                    }
                }
            }
        }

        return list;
    }

    private static MaterialEntry findOrCreate(List<MaterialEntry> list, ItemStack stack) {
        for (MaterialEntry entry : list) {
            if (entry.itemStack.isItemEqual(stack)) {
                return entry;
            }
        }
        final MaterialEntry entry = new MaterialEntry(stack.copy());
        list.add(entry);
        return entry;
    }
}
