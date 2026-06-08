package com.luxglitch.schematichighlight.proxy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

import com.luxglitch.schematichighlight.client.HighlightConfig;
import com.luxglitch.schematichighlight.client.HighlightManager;
import com.luxglitch.schematichighlight.client.input.InputHandler;
import com.luxglitch.schematichighlight.client.render.HighlightRenderer;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        HighlightConfig.init(event.getSuggestedConfigurationFile());

        for (KeyBinding keyBinding : InputHandler.KEY_BINDINGS) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Keyboard input (open GUI / clear) is delivered on the FML bus.
        FMLCommonHandler.instance()
            .bus()
            .register(InputHandler.INSTANCE);
        // Periodic pruning of already-placed highlighted cells runs on the client tick.
        FMLCommonHandler.instance()
            .bus()
            .register(HighlightManager.INSTANCE);
        // The world-space highlight rendering hooks RenderWorldLastEvent on the Forge bus.
        MinecraftForge.EVENT_BUS.register(HighlightRenderer.INSTANCE);
    }
}
