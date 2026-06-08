package com.luxglitch.schematichighlight;

import com.luxglitch.schematichighlight.proxy.CommonProxy;
import com.luxglitch.schematichighlight.reference.Reference;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Client-side add-on for Schematica. Adds a clickable materials list that highlights every remaining
 * (un-placed) block of the selected material in the world, visible through walls.
 *
 * <p>
 * This mod never edits Schematica; it depends on Schematica's public classes
 * ({@code ClientProxy.schematic}, {@code SchematicWorld}, block/metadata accessors) and renders on top
 * of them.
 */
@Mod(
    modid = Reference.MODID,
    name = Reference.NAME,
    version = Reference.VERSION,
    dependencies = Reference.DEPENDENCIES,
    acceptableRemoteVersions = "*")
public class SchematicHighlight {

    @Mod.Instance(Reference.MODID)
    public static SchematicHighlight instance;

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
