package com.luxglitch.schematichighlight.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Derives a stable, distinct highlight colour for each material. */
public final class ColorUtil {

    private ColorUtil() {}

    /** Returns {r, g, b} floats in [0,1] for the given stack, deterministic per item+damage. */
    public static float[] colorFor(ItemStack stack) {
        int hash = 0;
        if (stack != null && stack.getItem() != null) {
            hash = Item.getIdFromItem(stack.getItem()) * 31 + stack.getItemDamage();
        }
        // Scramble so adjacent item ids get well-separated hues.
        long scrambled = (hash * 2654435761L) & 0xFFFFFFFFL;
        float hue = (scrambled % 360L) / 360.0f;
        return hsvToRgb(hue, 0.85f, 1.0f);
    }

    /** Packs {r,g,b} floats plus an alpha into an ARGB int (for GUI swatches). */
    public static int toArgb(float[] rgb, int alpha) {
        int r = clamp255((int) (rgb[0] * 255.0f));
        int g = clamp255((int) (rgb[1] * 255.0f));
        int b = clamp255((int) (rgb[2] * 255.0f));
        return ((alpha & 0xFF) << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        float r;
        float g;
        float b;
        int i = (int) (h * 6.0f);
        float f = h * 6.0f - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float t = v * (1.0f - (1.0f - f) * s);
        switch (i % 6) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            default:
                r = v;
                g = p;
                b = q;
                break;
        }
        return new float[] { r, g, b };
    }
}
