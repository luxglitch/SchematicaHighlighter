package com.luxglitch.schematichighlight.client;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/**
 * Persistent client settings, stored in {@code config/schematichighlight.cfg}.
 *
 * <p>
 * Currently just the marker render distance: how far box/outline markers are drawn. The Sky Beacon
 * is intentionally <i>not</i> limited by this.
 */
public final class HighlightConfig {

    public static final int MIN_DISTANCE = 16;
    public static final int MAX_DISTANCE = 256;
    public static final int DEFAULT_DISTANCE = 96;
    /** At or above this range, the GUI shows a "may cause lag" warning. */
    public static final int WARN_DISTANCE = 128;

    private static final String CATEGORY = "general";
    private static final String KEY = "markerRenderDistance";
    private static final String COMMENT = "Distance in blocks within which box/outline markers are drawn. Higher values can cause lag on "
        + "large builds with many missing blocks. The Sky Beacon is not affected by this setting.";

    private static Configuration config;
    private static int markerDistance = DEFAULT_DISTANCE;

    private HighlightConfig() {}

    public static void init(File file) {
        config = new Configuration(file);
        config.load();
        markerDistance = clamp(config.getInt(KEY, CATEGORY, DEFAULT_DISTANCE, MIN_DISTANCE, MAX_DISTANCE, COMMENT));
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static int getMarkerDistance() {
        return markerDistance;
    }

    /** Update the in-memory value (e.g. while dragging the slider). Does not write to disk. */
    public static void setMarkerDistance(int distance) {
        markerDistance = clamp(distance);
    }

    /** Persist the current value to disk. Call when the user is done adjusting (e.g. on GUI close). */
    public static void save() {
        if (config != null) {
            config.get(CATEGORY, KEY, DEFAULT_DISTANCE)
                .set(markerDistance);
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public static int clamp(int distance) {
        if (distance < MIN_DISTANCE) {
            return MIN_DISTANCE;
        }
        if (distance > MAX_DISTANCE) {
            return MAX_DISTANCE;
        }
        return distance;
    }
}
