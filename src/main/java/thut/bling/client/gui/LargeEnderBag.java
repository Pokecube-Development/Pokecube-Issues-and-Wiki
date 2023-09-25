package thut.bling.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thut.bling.ThutBling;
import thut.bling.bag.large.LargeContainer;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class LargeEnderBag<T extends LargeContainer> extends AbstractContainerScreen<T>
{
    public static ResourceLocation BAG_GUI = new ResourceLocation(ThutBling.MODID, "textures/gui/large_ender_bag.png");
    public static ResourceLocation WIDGETS_GUI = new ResourceLocation(ThutBling.MODID, "textures/gui/widgets.png");
    public static ResourceLocation BAG_GUI_GREEN = new ResourceLocation(ThutBling.MODID, "textures/gui/large_ender_bag_green.png");
    public static ResourceLocation WIDGETS_GUI_FANCY = new ResourceLocation(ThutBling.MODID, "textures/gui/widgets_fancy.png");

    String  page;
    EditBox renamePageBox;
    EditBox searchBar;
    EditBox selectedPageBox;
    Button nextButton;
    Button prevButton;
    Button renameButton;
    Button searchButton;

    private String boxName = "1";
    boolean        release = false;

    public LargeEnderBag(final T container, final Inventory ivplay, final Component name)
    {
        super(container, ivplay, name);
        this.imageWidth = 176;
        this.imageHeight = 240;
        this.page = container.getPageNb();
        this.boxName = container.getPage();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (this.searchBar.isFocused() && (keyCode == GLFW.GLFW_KEY_ESCAPE
                || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            this.searchBar.setFocused(false);
            return false;
        }

        if (this.searchBar.isFocused() && keyCode == GLFW.GLFW_KEY_E)
        {
            this.searchBar.setFocused(true);
            return true;
        }

        if (this.renamePageBox.isFocused() && (keyCode == GLFW.GLFW_KEY_ESCAPE
                || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
            {
                final String box = this.renamePageBox.getValue();
                if (!box.equals(this.boxName) && this.renamePageBox.isVisible() && !this.renamePageBox.getValue().equals(""))
                {
                    this.menu.changeName(box);
                    this.boxName = box;
                }

                if (this.renamePageBox.visible) {
                    this.renamePageBox.setVisible(false);
                }
            }
            this.renamePageBox.setFocused(false);
            return false;
        }

        if (this.renamePageBox.isFocused() && keyCode == GLFW.GLFW_KEY_E)
        {
            this.renamePageBox.setFocused(true);
            return true;
        }

        if (this.selectedPageBox.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            final String entry = this.selectedPageBox.getValue();
            int number = 1;
            try
            {
                number = Integer.parseInt(entry);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
            number = Math.max(1, Math.min(number, this.menu.inv.boxCount()));
            this.menu.gotoInventoryPage(number);
            return true;
        }
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int par1, final int par2)
    {
        String text = this.menu.getPage();
        if (this.renamePageBox.visible && text.length() > 17 && ThutCore.getConfig().fancyGUI)
            graphics.drawString(this.font, "", 8, 6, 0x263631, false);
        else if (ThutCore.getConfig().fancyGUI) graphics.drawString(this.font, text, 8, 6, 0x263631, false);
        else graphics.drawString(this.font, text, 8, 6, 4210752, false);

        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float f, final int i, final int j)
    {
        ResourceLocation WIDGETS_DEFAULT_OR_FANCY = ThutCore.getConfig().fancyGUI ? WIDGETS_GUI_FANCY : WIDGETS_GUI;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BAG_GUI_GREEN);
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        //  Blit format: Texture location, gui x pos, gui y position, texture x pos, texture y pos, texture x size, texture y size
        if (ThutCore.getConfig().fancyGUI) graphics.blit(BAG_GUI_GREEN, x, y, 0, 0, this.imageWidth, this.imageHeight);
        else  graphics.blit(BAG_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (this.renameButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 5, 30, 15, 10, 10);
        } else {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 5, 30, 0, 10, 10);
        }

        if (this.renamePageBox.visible)
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 115, y + 5, 0, 60, 43, 10);

        if (this.prevButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 7, y + 127, 45, 15, 10, 10);
        } else {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 7, y + 127, 45, 0, 10, 10);
        }

        if (this.selectedPageBox.visible)
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 18, y + 127, 0, 75, 25, 10);

        if (this.nextButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 44, y + 127, 60, 15, 10, 10);
        } else {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 44, y + 127, 60, 0, 10, 10);
        }

        if (this.searchButton.isHoveredOrFocused())
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 127, 15, 15, 10, 10);
        else graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 127, 15, 0, 10, 10);

        if (this.searchBar.visible)
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 79, y + 127, 0, 30, 79, 10);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 120;

        // Elements placed in order of selection when pressing tab
        this.renamePageBox = new EditBox(this.font,
                x + 117, y + 6, 40, 10, TComponent.translatable("block.bag.rename.narrate"));
        this.renamePageBox.setTooltip(Tooltip.create(Component.translatable("block.bag.rename.tooltip")));
        this.renamePageBox.setBordered(false);
        this.renamePageBox.setVisible(false);
        this.renamePageBox.maxLength = 24;
        this.addRenderableWidget(this.renamePageBox);

        final Component rename = TComponent.translatable("block.bag.rename");
        this.renameButton = this.addRenderableWidget(new Button.Builder(rename, (b) -> {
            final String box = this.renamePageBox.getValue();
            if (!box.equals(this.boxName) && this.renamePageBox.isVisible() && !this.renamePageBox.getValue().equals(""))
            {
                this.menu.changeName(box);
                this.boxName = box;
            }

            this.renamePageBox.setVisible(!this.renamePageBox.visible);
        }).bounds(x + 159, y + 5, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.bag.rename.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.bag.rename.narrate")).build());
        this.renameButton.setAlpha(0);

        final Component prev = TComponent.translatable("block.bag.previous");
        this.prevButton = this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            this.menu.updateInventoryPages((byte) -1, this.minecraft.player.getInventory());
            this.selectedPageBox.setValue(this.menu.getPageNb());
        }).bounds(x + 7, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.bag.previous.tooltip")))
                .createNarration(supplier -> Component.translatable("block.bag.previous.narrate")).build());
        this.prevButton.setAlpha(0);

        this.selectedPageBox = new EditBox(this.font,
                x + 20, y + 128, 22, 10, TComponent.literal(this.page));
        this.selectedPageBox.setTooltip(Tooltip.create(Component.translatable("block.bag.page.tooltip")));
        this.selectedPageBox.setBordered(false);
        this.addRenderableWidget(this.selectedPageBox);

        final Component next = TComponent.translatable("block.bag.next");
        this.nextButton = this.addRenderableWidget(new Button.Builder(next, (b) -> {
            this.menu.updateInventoryPages((byte) 1, this.minecraft.player.getInventory());
            this.selectedPageBox.setValue(this.menu.getPageNb());
        }).bounds(x + 44, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.bag.next.tooltip")))
                .createNarration(supplier -> Component.translatable("block.bag.next.narrate")).build());
        this.nextButton.setAlpha(0);

        this.searchBar = new EditBox(this.font,
                x + 81, y + 128, 77, 10, TComponent.translatable("block.bag.search.narrate"));
        this.searchBar.setTooltip(Tooltip.create(Component.translatable("block.bag.search.tooltip")));

        this.searchBar.setBordered(false);
        this.searchBar.setVisible(false);
        this.addRenderableWidget(this.searchBar);

        final Component search = TComponent.translatable("block.bag.search");
        this.searchButton = this.addRenderableWidget(new Button.Builder(search, (b) -> {
            this.searchBar.setVisible(!this.searchBar.visible);
        }).bounds(x + 159, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.bag.search.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.bag.search.narrate")).build());
        this.searchButton.visible = true;
        this.searchButton.setAlpha(0);

        this.selectedPageBox.value = this.page;
        this.renamePageBox.value = "";
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
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, f);
        for (int i = 0; i < 54; i++)
            if (!this.searchBar.getValue().isEmpty() && this.searchBar.visible)
            {
                final ItemStack stack = this.menu.inv.getItem(i + 54 * this.menu.inv.getPage());
                if (stack.isEmpty()) continue;
                final int x = i % 9 * 18 + this.width / 2 - 80;
                final int y = i / 9 * 18 + this.height / 2 - 102;
                final String name = stack == null ? "" : stack.getHoverName().getString();
                if (name.isEmpty() || !ThutCore.trim(name).contains(ThutCore.trim(this.searchBar.getValue())))
                {
                    final int slotColor = 0x75FF0000;
                    graphics.fill(x, y, x + 16, y + 16, slotColor);
                }
                else
                {
                    final int slotColor = 0x7500FF00;
                    graphics.fill(x, y, x + 16, y + 16, slotColor);
                }
            }
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}