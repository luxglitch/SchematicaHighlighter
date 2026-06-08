package com.luxglitch.schematichighlight.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Server/common side proxy. This add-on is purely client-side, so the common proxy is a no-op and is
 * what loads on a dedicated server.
 */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {}

    public void init(FMLInitializationEvent event) {}
}
