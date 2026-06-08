package com.luxglitch.schematichighlight.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Reference {

    public static final String MODID = "schematichighlight";
    public static final String NAME = "Schematica Material Highlighter";
    public static final String VERSION = "1.0.0";

    /** Both Schematica and LunatriusCore must load before us; we use their public classes directly. */
    public static final String DEPENDENCIES = "required-after:Schematica;required-after:LunatriusCore;";

    public static final String PROXY_COMMON = "com.luxglitch.schematichighlight.proxy.CommonProxy";
    public static final String PROXY_CLIENT = "com.luxglitch.schematichighlight.proxy.ClientProxy";

    public static final Logger logger = LogManager.getLogger(MODID);

    private Reference() {}
}
