package thut.bling.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import thut.bling.ThutBling;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class SmallBag<T extends ChestMenu> extends AbstractContainerScreen<T>
{
    public static ResourceLocation BAG_GUI = ResourceLocation.fromNamespaceAndPath(ThutBling.MODID, "textures/gui/bag.png");
    public static ResourceLocation WIDGETS_GUI = ResourceLocation.fromNamespaceAndPath(ThutBling.MODID, "textures/gui/widgets.png");
    public static ResourceLocation BAG_GUI_BROWN = ResourceLocation.fromNamespaceAndPath(ThutBling.MODID, "textures/gui/bag_brown.png");
    public static ResourceLocation WIDGETS_GUI_FANCY = ResourceLocation.fromNamespaceAndPath(ThutBling.MODID, "textures/gui/widgets_fancy.png");

    String  page;
    EditBox renamePageBox;
    Button renameButton;

    public SmallBag(final T container, final Inventory ivplay, final Component name)
    {
        super(container, ivplay, name);
        this.imageWidth = 176;
        this.imageHeight = 172;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int par1, final int par2)
    {
        if (ThutCore.getConfig().fancyGUI) graphics.drawString(this.font, this.getTitle().getString(), 8, 6, 0x330001, false);
        else graphics.drawString(this.font, this.getTitle().getString(), 8, 6, 4210752, false);

        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float f, final int i, final int j)
    {
        ResourceLocation WIDGETS_DEFAULT_OR_FANCY = ThutCore.getConfig().fancyGUI ? WIDGETS_GUI_FANCY : WIDGETS_GUI;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BAG_GUI_BROWN);
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        // Blit format: Texture location, gui x pos, gui y position, texture x pos, texture y pos, texture x size, texture y size
        if (ThutCore.getConfig().fancyGUI) graphics.blit(BAG_GUI_BROWN, x, y, 0, 0, this.imageWidth, this.imageHeight);
        else  graphics.blit(BAG_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (this.renameButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 5, 75, 15, 10, 10);
        } else {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 5, 75, 0, 10, 10);
        }

        if (this.renamePageBox.visible)
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 115, y + 5, 45, 60, 43, 10);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 86;

        // Elements placed in order of selection when pressing tab
        this.renamePageBox = new EditBox(this.font,
                x + 117, y + 6, 40, 10, TComponent.translatable("block.bag.rename.narrate"));
        this.renamePageBox.setTooltip(Tooltip.create(Component.translatable("block.bag.rename.tooltip")));
        this.renamePageBox.setBordered(false);
        this.renamePageBox.setVisible(false);
        this.renamePageBox.setMaxLength(24);
        this.addRenderableWidget(this.renamePageBox);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat
     * events
     */
    @Override
    public void removed()
    {
        if (this.minecraft.player != null) this.menu.removed(this.minecraft.player);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float f)
    {
        this.renderBackground(graphics, mouseY, mouseY, f);
        super.render(graphics, mouseX, mouseY, f);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}