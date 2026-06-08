package com.luxglitch.schematichighlight.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.luxglitch.schematichighlight.client.HighlightConfig;
import com.luxglitch.schematichighlight.client.HighlightManager;
import com.luxglitch.schematichighlight.client.MaterialEntry;
import com.luxglitch.schematichighlight.client.MaterialScanner;

import cpw.mods.fml.client.config.GuiSlider;

/**
 * A clickable materials list. Clicking a row highlights that material's remaining blocks in the world;
 * "Highlight All Remaining" highlights everything still missing. Closing the screen leaves the
 * highlights active.
 */
public class GuiMaterialHighlight extends GuiScreen implements GuiSlider.ISlider {

    private static final int BTN_ALL = 0;
    private static final int BTN_CLEAR = 1;
    private static final int BTN_RESCAN = 2;
    private static final int BTN_DONE = 3;
    private static final int BTN_BEACON = 4;
    private static final int BTN_DIST = 5;

    private final GuiScreen parentScreen;
    private List<MaterialEntry> entries = new ArrayList<>();
    private GuiMaterialHighlightSlot slot;
    private GuiSlider distanceSlider;
    private String title = "";

    public GuiMaterialHighlight(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        rescan();
    }

    private void rescan() {
        final SchematicWorld schematic = ClientProxy.schematic;
        this.entries = MaterialScanner
            .scan(schematic, this.mc != null ? this.mc.theWorld : null, this.mc != null ? this.mc.thePlayer : null);
        // Incomplete materials first (most-missing on top), then alphabetical.
        Collections.sort(this.entries, (a, b) -> {
            int byMissing = Integer.compare(b.missingCount(), a.missingCount());
            if (byMissing != 0) {
                return byMissing;
            }
            return a.displayName()
                .compareToIgnoreCase(b.displayName());
        });
    }

    @Override
    public void initGui() {
        this.title = I18n.format("schematichighlight.gui.title");

        // mc/theWorld/thePlayer are available now; rescan if the constructor ran before they were set.
        if (this.entries.isEmpty()) {
            rescan();
        }

        final int y = this.height - 28;
        final int startX = this.width / 2 - 233;

        this.buttonList.clear();
        this.buttonList
            .add(new GuiButton(BTN_ALL, startX, y, 120, 20, I18n.format("schematichighlight.gui.highlightall")));
        this.buttonList.add(new GuiButton(BTN_BEACON, startX + 124, y, 130, 20, beaconButtonLabel()));
        this.buttonList
            .add(new GuiButton(BTN_CLEAR, startX + 258, y, 60, 20, I18n.format("schematichighlight.gui.clear")));
        this.buttonList
            .add(new GuiButton(BTN_RESCAN, startX + 322, y, 70, 20, I18n.format("schematichighlight.gui.rescan")));
        this.buttonList
            .add(new GuiButton(BTN_DONE, startX + 396, y, 70, 20, I18n.format("schematichighlight.gui.done")));

        this.distanceSlider = new GuiSlider(
            BTN_DIST,
            this.width / 2 - 200,
            this.height - 52,
            180,
            20,
            I18n.format("schematichighlight.gui.markerrange") + ": ",
            "",
            HighlightConfig.MIN_DISTANCE,
            HighlightConfig.MAX_DISTANCE,
            HighlightConfig.getMarkerDistance(),
            false,
            true,
            this);
        this.buttonList.add(this.distanceSlider);

        this.slot = new GuiMaterialHighlightSlot(this);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        switch (button.id) {
            case BTN_ALL:
                HighlightManager.INSTANCE.highlightAll(this.entries, ClientProxy.schematic);
                break;
            case BTN_CLEAR:
                HighlightManager.INSTANCE.clear();
                break;
            case BTN_RESCAN:
                rescan();
                this.slot = new GuiMaterialHighlightSlot(this);
                break;
            case BTN_BEACON:
                HighlightManager.INSTANCE.toggleBeaconMode();
                button.displayString = beaconButtonLabel();
                break;
            case BTN_DONE:
                this.mc.displayGuiScreen(this.parentScreen);
                break;
            default:
                break;
        }
    }

    private static String beaconButtonLabel() {
        return I18n.format(
            HighlightManager.INSTANCE.isBeaconMode() ? "schematichighlight.gui.beacon_on"
                : "schematichighlight.gui.beacon_off");
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if (slider.id == BTN_DIST) {
            // Apply live so the highlights update as you drag; persisted to disk on close.
            HighlightConfig.setMarkerDistance(slider.getValueInt());
        }
    }

    @Override
    public void onGuiClosed() {
        HighlightConfig.save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        if (this.slot != null) {
            this.slot.drawScreen(mouseX, mouseY, partialTicks);
        }

        drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 8, 0xFFFFFF);

        if (this.entries.isEmpty()) {
            drawCenteredString(
                this.fontRendererObj,
                I18n.format("schematichighlight.gui.noschematic"),
                this.width / 2,
                this.height / 2,
                0xFF5555);
        } else {
            int materials = 0;
            int blocks = 0;
            for (MaterialEntry entry : this.entries) {
                if (entry.missingCount() > 0) {
                    materials++;
                    blocks += entry.missingCount();
                }
            }
            drawCenteredString(
                this.fontRendererObj,
                I18n.format("schematichighlight.gui.summary", materials, blocks),
                this.width / 2,
                22,
                0xA0A0A0);
        }

        if (HighlightConfig.getMarkerDistance() >= HighlightConfig.WARN_DISTANCE) {
            drawString(
                this.fontRendererObj,
                I18n.format("schematichighlight.gui.distancewarn"),
                this.width / 2 - 6,
                this.height - 46,
                0xFFAA00);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ---- accessors used by the slot (same package) ----

    List<MaterialEntry> getEntries() {
        return this.entries;
    }

    void onElementSelected(int index) {
        if (index < 0 || index >= this.entries.size()) {
            return;
        }
        HighlightManager.INSTANCE.highlightSingle(this.entries.get(index), ClientProxy.schematic);
    }

    void renderStackTooltip(ItemStack stack, int x, int y) {
        renderToolTip(stack, x, y);
    }
}
