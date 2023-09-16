package thut.bling.client.gui;

import net.minecraft.client.gui.GuiGraphics;
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

public class Bag<T extends LargeContainer> extends AbstractContainerScreen<T>
{

    String  page;
    EditBox textFieldSelectedBox;
    EditBox textFieldBoxName;
    EditBox textFieldSearch;

    private String boxName = "1";
    boolean        release = false;

    public Bag(final T container, final Inventory ivplay, final Component name)
    {
        super(container, ivplay, name);
        this.imageWidth = 175;
        this.imageHeight = 229;
        this.page = container.getPageNb();
        this.boxName = container.getPage();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (this.textFieldSearch.isFocused() && keyCode != GLFW.GLFW_KEY_BACKSPACE) return true;
        if (this.textFieldSelectedBox.isFocused() && keyCode == GLFW.GLFW_KEY_ENTER)
        {
            final String entry = this.textFieldSelectedBox.getValue();
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
        if (this.textFieldBoxName.isFocused() && keyCode != GLFW.GLFW_KEY_BACKSPACE) return true;
        if (this.textFieldBoxName.isFocused()) if (keyCode == GLFW.GLFW_KEY_ESCAPE) this.textFieldBoxName.setFocused(
                false);
        else if (keyCode == GLFW.GLFW_KEY_ENTER && this.textFieldBoxName.isFocused()) return true;
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float f, final int i, final int j)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(ThutBling.MODID, "textures/gui/large_ender_bag.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        graphics.blit(new ResourceLocation(ThutBling.MODID, "textures/gui/large_ender_bag.png"), x, y,
                0, 0, this.imageWidth + 1, this.imageHeight + 1);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int par1, final int par2)
    {

    }

    @Override
    public void init()
    {
        super.init();
        final int xOffset = 0;
        final int yOffset = -11;

        final Component next = TComponent.translatable("block.pc.next");
        this.addRenderableWidget(new Button.Builder(next, (b) -> {
            this.menu.updateInventoryPages((byte) 1, this.minecraft.player.getInventory());
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
            this.textFieldBoxName.setValue(this.menu.getPage());
        }).bounds(this.width / 2 - xOffset - 44, this.height / 2 - yOffset - 121, 10, 10).build());

        final Component prev = TComponent.translatable("block.pc.previous");
        this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            this.menu.updateInventoryPages((byte) -1, this.minecraft.player.getInventory());
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
            this.textFieldBoxName.setValue(this.menu.getPage());
        }).bounds(this.width / 2 - xOffset - 81, this.height / 2 - yOffset - 121, 10, 10).build());

        this.textFieldSelectedBox = new EditBox(this.font, this.width / 2 - xOffset - 70, this.height / 2 - yOffset
                - 121, 25, 10, TComponent.literal(this.page));

        final Component rename = TComponent.translatable("block.pc.rename");
        this.addRenderableWidget(new Button.Builder(rename, (b) -> {
            final String box = this.textFieldBoxName.getValue();
            if (!box.equals(this.boxName)) this.menu.changeName(box);
            this.boxName = box;
        }).bounds(this.width / 2 - xOffset + 30, this.height / 2 - yOffset - 0, 50, 10).build());

        this.textFieldBoxName = new EditBox(this.font, this.width / 2 - xOffset - 80, this.height / 2 - yOffset + 0,
                100, 10, TComponent.literal(this.boxName));
        this.textFieldSearch = new EditBox(this.font, this.width / 2 - xOffset - 10, this.height / 2 - yOffset - 121,
                90, 10, TComponent.literal(""));

        this.addRenderableWidget(this.textFieldSelectedBox);
        this.addRenderableWidget(this.textFieldBoxName);
        this.addRenderableWidget(this.textFieldSearch);

        this.textFieldSelectedBox.value = this.page;
        this.textFieldBoxName.value = this.boxName;
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
            if (!this.textFieldSearch.getValue().isEmpty())
            {
                final ItemStack stack = this.menu.inv.getItem(i + 54 * this.menu.inv.getPage());
                if (stack.isEmpty()) continue;
                final int x = i % 9 * 18 + this.width / 2 - 80;
                final int y = i / 9 * 18 + this.height / 2 - 96;
                final String name = stack == null ? "" : stack.getHoverName().getString();
                if (name.isEmpty() || !ThutCore.trim(name).contains(ThutCore.trim(this.textFieldSearch.getValue())))
                {
                    final int slotColor = 0x55FF0000;
                    graphics.fill(x, y, x + 16, y + 16, slotColor);
                }
                else
                {
                    final int slotColor = 0x5500FF00;
                    graphics.fill(x, y, x + 16, y + 16, slotColor);
                }
            }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

}