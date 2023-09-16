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

    String  page;
    EditBox textFieldSelectedBox;
    EditBox textFieldBoxName;
    EditBox textFieldSearch;
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
        if (this.textFieldSearch.isFocused() && (keyCode == GLFW.GLFW_KEY_ESCAPE
                || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            this.textFieldSearch.setFocused(false);
            return false;
        }

        if (this.textFieldSearch.isFocused() && keyCode == GLFW.GLFW_KEY_E)
        {
            this.textFieldSearch.setFocused(true);
            return true;
        }

        if (this.textFieldBoxName.isFocused() && (keyCode == GLFW.GLFW_KEY_ESCAPE
                || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
            {
                final String box = this.textFieldBoxName.getValue();
                if (!box.equals(this.boxName)) this.menu.changeName(box);
                this.boxName = box;
            }
            this.textFieldBoxName.setFocused(false);
            return false;
        }

        if (this.textFieldBoxName.isFocused() && keyCode == GLFW.GLFW_KEY_E)
        {
            this.textFieldBoxName.setFocused(true);
            return true;
        }

        if (this.textFieldSelectedBox.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
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
        String text = this.menu.getPage();
        graphics.drawString(this.font, text, 8, 6, 0x263631, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 120;

        this.textFieldBoxName = new EditBox(this.font,
                x + 117, y + 6, 40, 10, TComponent.translatable("block.bag.rename.narrate"));
        this.textFieldBoxName.setTooltip(Tooltip.create(Component.translatable("block.bag.rename.tooltip")));
        this.textFieldBoxName.setBordered(false);
        this.textFieldBoxName.maxLength = 17;
        this.addRenderableWidget(this.textFieldBoxName);

        final Component rename = TComponent.translatable("block.bag.rename");
        this.addRenderableWidget(new Button.Builder(rename, (b) -> {
            final String box = this.textFieldBoxName.getValue();
            if (!box.equals(this.boxName)) this.menu.changeName(box);
            this.boxName = box;
        }).bounds(x + 157, y + 4, 12, 12)
                .tooltip(Tooltip.create(Component.translatable("block.bag.rename.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.bag.rename.narrate")).build());

        final Component prev = TComponent.translatable("block.bag.previous");
        this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            this.menu.updateInventoryPages((byte) -1, this.minecraft.player.getInventory());
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
        }).bounds(x + 7, y + 127, 10, 10)
                .createNarration(supplier -> Component.translatable("block.bag.previous.narrate")).build());

        this.textFieldSelectedBox = new EditBox(this.font,
                x + 20, y + 128, 22, 10, TComponent.literal(this.page));
        this.textFieldSelectedBox.setTooltip(Tooltip.create(Component.translatable("block.bag.page.tooltip")));
        this.textFieldSelectedBox.setBordered(false);
        this.addRenderableWidget(this.textFieldSelectedBox);

        final Component next = TComponent.translatable("block.bag.next");
        this.addRenderableWidget(new Button.Builder(next, (b) -> {
            this.menu.updateInventoryPages((byte) 1, this.minecraft.player.getInventory());
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
        }).bounds(x + 44, y + 127, 10, 10)
                .createNarration(supplier -> Component.translatable("block.bag.next.narrate")).build());

        this.textFieldSearch = new EditBox(this.font,
                x + 99, y + 128, 58, 10, TComponent.translatable("block.bag.search.narrate"));
        this.textFieldSearch.setTooltip(Tooltip.create(Component.translatable("block.bag.search.tooltip")));
        this.textFieldSearch.setBordered(false);
        this.addRenderableWidget(this.textFieldSearch);

        final Component search = TComponent.translatable("block.bag.search");
        this.searchButton = this.addRenderableWidget(new Button.Builder(search, (b) -> {
        }).bounds(x + 157, y + 126, 12, 12)
                .tooltip(Tooltip.create(Component.translatable("block.bag.search.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.bag.search.narrate")).build());

        this.textFieldSelectedBox.value = this.page;
        this.textFieldBoxName.value = "";
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
            if (!this.textFieldSearch.getValue().isEmpty() && this.textFieldSearch != null)
            {
                final ItemStack stack = this.menu.inv.getItem(i + 54 * this.menu.inv.getPage());
                if (stack.isEmpty()) continue;
                final int x = i % 9 * 18 + this.width / 2 - 80;
                final int y = i / 9 * 18 + this.height / 2 - 102;
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