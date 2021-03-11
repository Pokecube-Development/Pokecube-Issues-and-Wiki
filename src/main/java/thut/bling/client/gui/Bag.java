package thut.bling.client.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thut.bling.ThutBling;
import thut.bling.bag.large.LargeContainer;
import thut.core.common.ThutCore;

public class Bag<T extends LargeContainer> extends ContainerScreen<T>
{

    String          page;
    TextFieldWidget textFieldSelectedBox;
    TextFieldWidget textFieldBoxName;
    TextFieldWidget textFieldSearch;

    private String boxName = "1";
    boolean        release = false;

    public Bag(final T container, final PlayerInventory ivplay, final ITextComponent name)
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
    protected void renderBg(final MatrixStack mat, final float f, final int i, final int j)
    {
        GL11.glColor4f(1f, 1f, 1f, 1f);

        this.minecraft.getTextureManager().bind(new ResourceLocation(ThutBling.MODID,
                "textures/gui/large_bag.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        this.blit(mat, x, y, 0, 0, this.imageWidth + 1, this.imageHeight + 1);
    }

    @Override
    protected void renderLabels(final MatrixStack mat, final int par1, final int par2)
    {

    }

    @Override
    public void init()
    {
        super.init();
        final int xOffset = 0;
        final int yOffset = -11;
        final ITextComponent next = new TranslationTextComponent("block.pc.next");
        this.addButton(new Button(this.width / 2 - xOffset - 44, this.height / 2 - yOffset - 121, 10, 10, next, b ->
        {
            this.menu.updateInventoryPages((byte) 1, this.minecraft.player.inventory);
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
        }));
        final ITextComponent prev = new TranslationTextComponent("block.pc.previous");
        this.addButton(new Button(this.width / 2 - xOffset - 81, this.height / 2 - yOffset - 121, 10, 10, prev, b ->
        {
            this.menu.updateInventoryPages((byte) -1, this.minecraft.player.inventory);
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
        }));
        this.textFieldSelectedBox = new TextFieldWidget(this.font, this.width / 2 - xOffset - 70, this.height / 2
                - yOffset - 121, 25, 10, new StringTextComponent(this.page));

        final ITextComponent rename = new TranslationTextComponent("block.pc.rename");
        this.addButton(new Button(this.width / 2 - xOffset + 30, this.height / 2 - yOffset - 0, 50, 10, rename, b ->
        {
            final String box = this.textFieldBoxName.getValue();
            if (!box.equals(this.boxName)) this.menu.changeName(box);
            this.boxName = box;
        }));

        this.textFieldBoxName = new TextFieldWidget(this.font, this.width / 2 - xOffset - 80, this.height / 2 - yOffset
                + 0, 100, 10, new StringTextComponent(this.boxName));
        this.textFieldSearch = new TextFieldWidget(this.font, this.width / 2 - xOffset - 10, this.height / 2 - yOffset
                - 121, 90, 10, new StringTextComponent(""));

        this.addButton(this.textFieldSelectedBox);
        this.addButton(this.textFieldBoxName);
        this.addButton(this.textFieldSearch);

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
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float f)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, f);
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
                    AbstractGui.fill(mat, x, y, x + 16, y + 16, slotColor);
                }
                else
                {
                    final int slotColor = 0x5500FF00;
                    AbstractGui.fill(mat, x, y, x + 16, y + 16, slotColor);
                }
            }
        this.renderTooltip(mat,mouseX, mouseY);
    }

}