package com.luxglitch.schematichighlight.client.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.luxglitch.schematichighlight.client.HighlightManager;
import com.luxglitch.schematichighlight.client.gui.GuiMaterialHighlight;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

public class InputHandler {

    public static final InputHandler INSTANCE = new InputHandler();

    private static final String CATEGORY = "schematichighlight.key.category";

    public static final KeyBinding KEY_OPEN = new KeyBinding("schematichighlight.key.open", Keyboard.KEY_N, CATEGORY);
    public static final KeyBinding KEY_CLEAR = new KeyBinding("schematichighlight.key.clear", Keyboard.KEY_B, CATEGORY);
    public static final KeyBinding KEY_BEACON = new KeyBinding(
        "schematichighlight.key.beacon",
        Keyboard.KEY_NONE,
        CATEGORY);

    public static final KeyBinding[] KEY_BINDINGS = { KEY_OPEN, KEY_CLEAR, KEY_BEACON };

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private InputHandler() {}

    @SubscribeEvent
    public void onKeyInput(InputEvent event) {
        // Only react to keybinds when no screen is open (matches Schematica's own behaviour).
        if (this.minecraft.currentScreen != null) {
            return;
        }

        if (KEY_OPEN.isPressed()) {
            this.minecraft.displayGuiScreen(new GuiMaterialHighlight(null));
        }

        if (KEY_CLEAR.isPressed()) {
            HighlightManager.INSTANCE.clear();
        }

        if (KEY_BEACON.isPressed()) {
            HighlightManager.INSTANCE.toggleBeaconMode();
        }
    }
}
