package com.luxglitch.schematichighlight.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.github.lunatrius.schematica.client.gui.GuiHelper;
import com.luxglitch.schematichighlight.client.ColorUtil;
import com.luxglitch.schematichighlight.client.MaterialEntry;

/** Scrollable list backing {@link GuiMaterialHighlight}; one row per material. */
class GuiMaterialHighlightSlot extends GuiSlot {

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final GuiMaterialHighlight gui;

    protected int selectedIndex = -1;

    GuiMaterialHighlightSlot(GuiMaterialHighlight gui) {
        super(Minecraft.getMinecraft(), gui.width, gui.height, 34, gui.height - 34, 24);
        this.gui = gui;
    }

    @Override
    protected int getSize() {
        return this.gui.getEntries()
            .size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
        this.selectedIndex = index;
        this.gui.onElementSelected(index);
    }

    @Override
    protected boolean isSelected(int index) {
        return index == this.selectedIndex;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {}

    @Override
    protected void drawSlot(int index, int x, int y, int slotHeight, Tessellator tessellator, int mouseX, int mouseY) {
        final MaterialEntry entry = this.gui.getEntries()
            .get(index);
        final ItemStack itemStack = entry.itemStack;

        final String name = entry.displayName();
        final String amount = entry.formattedAmount();
        final String status = entry.isComplete()
            ? EnumChatFormatting.GREEN + I18n.format("schematichighlight.gui.complete")
            : EnumChatFormatting.RED + I18n.format("schematichighlight.gui.missing", entry.missingCount());

        GuiHelper.drawItemStack(this.minecraft.renderEngine, this.minecraft.fontRenderer, x, y, itemStack);

        // Colour swatch matching this material's in-world highlight colour.
        Gui.drawRect(x + 20, y + 3, x + 30, y + 13, ColorUtil.toArgb(entry.color, 0xFF));

        final FontRenderer fontRenderer = this.minecraft.fontRenderer;
        fontRenderer.drawStringWithShadow(name, x + 34, y + 2, 0xFFFFFF);
        fontRenderer.drawStringWithShadow(amount, x + 215 - fontRenderer.getStringWidth(amount), y + 2, 0xFFFFFF);
        fontRenderer.drawStringWithShadow(status, x + 215 - fontRenderer.getStringWidth(status), y + 12, 0xFFFFFF);

        if (mouseX > x && mouseY > y && mouseX <= x + 18 && mouseY <= y + 18) {
            this.gui.renderStackTooltip(itemStack, mouseX, mouseY);
        }
    }
}
