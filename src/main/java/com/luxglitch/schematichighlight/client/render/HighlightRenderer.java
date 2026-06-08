package com.luxglitch.schematichighlight.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import com.github.lunatrius.core.util.vector.Vector3i;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.luxglitch.schematichighlight.client.Cell;
import com.luxglitch.schematichighlight.client.HighlightManager;
import com.luxglitch.schematichighlight.client.MaterialEntry;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Draws the highlighted cells in the world at the end of the world render pass, with depth testing
 * disabled so they are visible through walls. Each cell gets a translucent box, a bright outline, and
 * (optionally) a vertical beacon-style beam so a single buried block is easy to spot.
 */
public class HighlightRenderer {

    public static final HighlightRenderer INSTANCE = new HighlightRenderer();

    /** Only draw box/outline markers within this distance of the player (squared below). */
    private static final double MAX_DISTANCE = 96.0;
    /** Hard cap on boxes drawn per frame, shared across all materials. */
    private static final int MAX_BOXES = 4000;
    /** Box inflation so the outline reads cleanly around the target block. */
    private static final double INFLATE = 0.01;

    private static final float FACE_ALPHA = 0.22f;
    private static final float LINE_ALPHA = 0.9f;

    private static final boolean DRAW_BEAMS = true;
    private static final int MAX_BEAMS = 48;
    private static final double BEAM_HEIGHT = 24.0;
    private static final double BEAM_RADIUS = 0.12;
    private static final float BEAM_ALPHA = 0.14f;

    // "Sky Beacon" locator mode: tall pulsing columns culled only by horizontal distance, so a block
    // that is far away or buried at a very different altitude can still be found.
    private static final double BEACON_MAX_DISTANCE = 512.0;
    private static final int MAX_BEACONS = 256;
    private static final double BEACON_RADIUS = 0.18;
    private static final double BEACON_BOTTOM = 0.0;
    private static final double BEACON_TOP = 300.0;
    private static final float BEACON_ALPHA_MIN = 0.18f;
    private static final float BEACON_ALPHA_MAX = 0.55f;

    private HighlightRenderer() {}

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        final HighlightManager manager = HighlightManager.INSTANCE;
        if (!manager.isActive()) {
            return;
        }

        final SchematicWorld schematic = manager.schematic();
        if (schematic == null || schematic != ClientProxy.schematic) {
            return;
        }

        final Minecraft minecraft = Minecraft.getMinecraft();
        final EntityClientPlayerMP player = minecraft.thePlayer;
        if (player == null) {
            return;
        }

        final float partialTicks = event.partialTicks;
        final double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        final double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        final double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        final Vector3i pos = schematic.position;
        final double maxSq = MAX_DISTANCE * MAX_DISTANCE;

        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_LINE_BIT
                | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2.0f);

        // Translate world space into the camera-relative space used during world rendering.
        GL11.glTranslated(-px, -py, -pz);

        final Tessellator tessellator = Tessellator.instance;
        final boolean beacon = manager.isBeaconMode();
        final double beaconMaxSq = BEACON_MAX_DISTANCE * BEACON_MAX_DISTANCE;
        final float beaconAlpha = BEACON_ALPHA_MIN + (BEACON_ALPHA_MAX - BEACON_ALPHA_MIN) * pulse();
        int budget = MAX_BOXES;
        int beamsDrawn = 0;
        int beaconsDrawn = 0;

        for (MaterialEntry entry : manager.groups()) {
            // Gather the visible subset once, then reuse it for faces, outlines and beams.
            final List<Cell> visible = new ArrayList<>();
            for (Cell cell : entry.missing) {
                if (budget <= 0) {
                    break;
                }
                final double cx = pos.x + cell.x + 0.5;
                final double cy = pos.y + cell.y + 0.5;
                final double cz = pos.z + cell.z + 0.5;
                final double dx = cx - px;
                final double dy = cy - py;
                final double dz = cz - pz;
                if (dx * dx + dy * dy + dz * dz > maxSq) {
                    continue;
                }
                visible.add(cell);
                budget--;
            }

            if (visible.isEmpty()) {
                continue;
            }

            final float r = entry.color[0];
            final float g = entry.color[1];
            final float b = entry.color[2];

            // Translucent faces.
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_F(r, g, b, FACE_ALPHA);
            for (Cell cell : visible) {
                addCubeFaces(tessellator, pos.x + cell.x, pos.y + cell.y, pos.z + cell.z);
            }
            tessellator.draw();

            // Bright outlines.
            tessellator.startDrawing(GL11.GL_LINES);
            tessellator.setColorRGBA_F(r, g, b, LINE_ALPHA);
            for (Cell cell : visible) {
                addCubeEdges(tessellator, pos.x + cell.x, pos.y + cell.y, pos.z + cell.z);
            }
            tessellator.draw();

            // Short vertical beams over nearby blocks (skipped in beacon mode, which draws tall ones).
            if (!beacon && DRAW_BEAMS && beamsDrawn < MAX_BEAMS) {
                tessellator.startDrawingQuads();
                tessellator.setColorRGBA_F(r, g, b, BEAM_ALPHA);
                for (Cell cell : visible) {
                    if (beamsDrawn >= MAX_BEAMS) {
                        break;
                    }
                    addBeam(tessellator, pos.x + cell.x, pos.y + cell.y, pos.z + cell.z);
                    beamsDrawn++;
                }
                tessellator.draw();
            }

            // Sky Beacon: tall pulsing columns over every remaining block of this material, regardless
            // of how far or how high/low it is. Culled by horizontal distance only so altitude can't
            // hide it. Iterates all missing cells (not just the box-distance subset).
            if (beacon && beaconsDrawn < MAX_BEACONS) {
                tessellator.startDrawingQuads();
                tessellator.setColorRGBA_F(r, g, b, beaconAlpha);
                for (Cell cell : entry.missing) {
                    if (beaconsDrawn >= MAX_BEACONS) {
                        break;
                    }
                    final double bcx = pos.x + cell.x + 0.5;
                    final double bcz = pos.z + cell.z + 0.5;
                    final double hdx = bcx - px;
                    final double hdz = bcz - pz;
                    if (hdx * hdx + hdz * hdz > beaconMaxSq) {
                        continue;
                    }
                    addColumn(tessellator, bcx, bcz, BEACON_BOTTOM, BEACON_TOP, BEACON_RADIUS);
                    beaconsDrawn++;
                }
                tessellator.draw();
            }
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void addCubeFaces(Tessellator t, int bx, int by, int bz) {
        final double x0 = bx - INFLATE;
        final double y0 = by - INFLATE;
        final double z0 = bz - INFLATE;
        final double x1 = bx + 1 + INFLATE;
        final double y1 = by + 1 + INFLATE;
        final double z1 = bz + 1 + INFLATE;

        // Down
        t.addVertex(x0, y0, z0);
        t.addVertex(x1, y0, z0);
        t.addVertex(x1, y0, z1);
        t.addVertex(x0, y0, z1);
        // Up
        t.addVertex(x0, y1, z1);
        t.addVertex(x1, y1, z1);
        t.addVertex(x1, y1, z0);
        t.addVertex(x0, y1, z0);
        // North (-Z)
        t.addVertex(x0, y0, z0);
        t.addVertex(x0, y1, z0);
        t.addVertex(x1, y1, z0);
        t.addVertex(x1, y0, z0);
        // South (+Z)
        t.addVertex(x0, y0, z1);
        t.addVertex(x1, y0, z1);
        t.addVertex(x1, y1, z1);
        t.addVertex(x0, y1, z1);
        // West (-X)
        t.addVertex(x0, y0, z0);
        t.addVertex(x0, y0, z1);
        t.addVertex(x0, y1, z1);
        t.addVertex(x0, y1, z0);
        // East (+X)
        t.addVertex(x1, y0, z0);
        t.addVertex(x1, y1, z0);
        t.addVertex(x1, y1, z1);
        t.addVertex(x1, y0, z1);
    }

    private static void addCubeEdges(Tessellator t, int bx, int by, int bz) {
        final double x0 = bx - INFLATE;
        final double y0 = by - INFLATE;
        final double z0 = bz - INFLATE;
        final double x1 = bx + 1 + INFLATE;
        final double y1 = by + 1 + INFLATE;
        final double z1 = bz + 1 + INFLATE;

        // Bottom rectangle
        edge(t, x0, y0, z0, x1, y0, z0);
        edge(t, x1, y0, z0, x1, y0, z1);
        edge(t, x1, y0, z1, x0, y0, z1);
        edge(t, x0, y0, z1, x0, y0, z0);
        // Top rectangle
        edge(t, x0, y1, z0, x1, y1, z0);
        edge(t, x1, y1, z0, x1, y1, z1);
        edge(t, x1, y1, z1, x0, y1, z1);
        edge(t, x0, y1, z1, x0, y1, z0);
        // Vertical pillars
        edge(t, x0, y0, z0, x0, y1, z0);
        edge(t, x1, y0, z0, x1, y1, z0);
        edge(t, x1, y0, z1, x1, y1, z1);
        edge(t, x0, y0, z1, x0, y1, z1);
    }

    private static void edge(Tessellator t, double ax, double ay, double az, double bx, double by, double bz) {
        t.addVertex(ax, ay, az);
        t.addVertex(bx, by, bz);
    }

    private static void addBeam(Tessellator t, int bx, int by, int bz) {
        addColumn(t, bx + 0.5, bz + 0.5, by, by + 1 + BEAM_HEIGHT, BEAM_RADIUS);
    }

    /** Four sides of a thin vertical column (no caps) from y0 to y1, centred on (cx, cz). */
    private static void addColumn(Tessellator t, double cx, double cz, double y0, double y1, double radius) {
        final double x0 = cx - radius;
        final double x1 = cx + radius;
        final double z0 = cz - radius;
        final double z1 = cz + radius;

        t.addVertex(x0, y0, z0);
        t.addVertex(x0, y1, z0);
        t.addVertex(x1, y1, z0);
        t.addVertex(x1, y0, z0);

        t.addVertex(x1, y0, z1);
        t.addVertex(x1, y1, z1);
        t.addVertex(x0, y1, z1);
        t.addVertex(x0, y0, z1);

        t.addVertex(x0, y0, z0);
        t.addVertex(x0, y0, z1);
        t.addVertex(x0, y1, z1);
        t.addVertex(x0, y1, z0);

        t.addVertex(x1, y0, z0);
        t.addVertex(x1, y1, z0);
        t.addVertex(x1, y1, z1);
        t.addVertex(x1, y0, z1);
    }

    /** 0..1 triangle-ish pulse (~1.4s period) used to make the beacon shimmer. */
    private static float pulse() {
        final double phase = (System.currentTimeMillis() % 1400L) / 1400.0 * Math.PI * 2.0;
        return (float) (0.5 * (1.0 + Math.sin(phase)));
    }
}
