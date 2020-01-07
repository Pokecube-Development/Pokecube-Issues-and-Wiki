package pokecube.adventures.client.gui.items;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.pc.PCInventory;

public class Bag<T extends BagContainer> extends ContainerScreen<T>
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
        this.xSize = 175;
        this.ySize = 229;
        this.page = container.getPageNb();
        this.boxName = container.getPage();
    }

    @Override
    public boolean keyPressed(int keyCode, int b, int c)
    {
        if (textFieldSelectedBox.isFocused() && keyCode == GLFW.GLFW_KEY_ENTER)
        {
            final String entry = this.textFieldSelectedBox.getText();
            String box = this.textFieldBoxName.getText();
            int number = 1;

            try
            {
                number = Integer.parseInt(entry);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }

            number = Math.max(1, Math.min(number, PCInventory.PAGECOUNT));
            this.container.gotoInventoryPage(number);

            if (this.textFieldBoxName.enableBackgroundDrawing && box != this.boxName)
            {
                if (this.textFieldBoxName.enableBackgroundDrawing)
                {
                    box = this.textFieldBoxName.getText();
                    if (box != this.boxName) this.container.changeName(box);
                }
                this.textFieldBoxName.enableBackgroundDrawing = !this.textFieldBoxName.enableBackgroundDrawing;
            }
            return true;
        }
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float f, final int i, final int j)
    {
        GL11.glColor4f(1f, 1f, 1f, 1f);

        this.minecraft.getTextureManager().bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/gui/pcgui.png"));
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int par1, final int par2)
    {

    }

    @Override
    public void init()
    {
        super.init();
        final int xOffset = 0;
        final int yOffset = -11;
        final String next = I18n.format("block.pc.next");
        this.addButton(new Button(this.width / 2 - xOffset - 44, this.height / 2 - yOffset - 121, 10, 10, next, b ->
        {
            this.container.updateInventoryPages((byte) 1, this.minecraft.player.inventory);
            this.textFieldSelectedBox.setText(this.container.getPageNb());
        }));
        final String prev = I18n.format("block.pc.previous");
        this.addButton(new Button(this.width / 2 - xOffset - 81, this.height / 2 - yOffset - 121, 10, 10, prev, b ->
        {
            this.container.updateInventoryPages((byte) -1, this.minecraft.player.inventory);
            this.textFieldSelectedBox.setText(this.container.getPageNb());
        }));
        this.textFieldSelectedBox = new TextFieldWidget(this.font, this.width / 2 - xOffset - 70, this.height / 2
                - yOffset - 121, 25, 10, this.page);

        final String rename = I18n.format("block.pc.rename");
        this.addButton(new Button(this.width / 2 - xOffset + 30, this.height / 2 - yOffset - 0, 50, 10, rename, b ->
        {
            if (this.textFieldBoxName.enableBackgroundDrawing)
            {
                final String box = this.textFieldBoxName.getText();
                if (box != this.boxName) this.container.changeName(box);
            }
            this.textFieldBoxName.enableBackgroundDrawing = !this.textFieldBoxName.enableBackgroundDrawing;
        }));

        this.textFieldBoxName = new TextFieldWidget(this.font, this.width / 2 - xOffset - 80, this.height / 2 - yOffset
                + 0, 100, 10, this.boxName);
        this.textFieldBoxName.enableBackgroundDrawing = false;
        this.textFieldSearch = new TextFieldWidget(this.font, this.width / 2 - xOffset - 10, this.height / 2 - yOffset
                - 121, 90, 10, "");

        this.addButton(this.textFieldSelectedBox);
        this.addButton(this.textFieldBoxName);
        this.addButton(this.textFieldSearch);

        this.textFieldSelectedBox.text = this.page;
        this.textFieldBoxName.text = this.boxName;
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat
     * events
     */
    @Override
    public void onClose()
    {
        if (this.minecraft.player != null) this.container.onContainerClosed(this.minecraft.player);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float f)
    {
        this.renderBackground();

        if (this.textFieldSearch == null || this.textFieldSearch.getText() == null)
        {
            // PokecubeCore.LOGGER.error("error with search box?");
            return;
        }

        final float zLevel = 1000;
        for (int i = 0; i < 54; i++)
            if (!this.textFieldSearch.getText().isEmpty())
            {
                final ItemStack stack = this.container.inv.getStackInSlot(i + 54 * this.container.inv.getPage());
                final int x = i % 9 * 18 + this.width / 2 - 80;
                final int y = i / 9 * 18 + this.height / 2 - 96;

                final String name = stack == null ? "" : stack.getDisplayName().getFormattedText();
                if (name.isEmpty() || !name.toLowerCase(java.util.Locale.ENGLISH).contains(this.textFieldSearch
                        .getText()))
                {
                    GL11.glPushMatrix();
                    GL11.glTranslated(0, 0, zLevel);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glColor4f(0, 0, 0, 1);
                    this.minecraft.getTextureManager().bindTexture(new ResourceLocation(PokecubeMod.ID,
                            "textures/hologram.png"));
                    this.blit(x, y, 0, 0, 16, 16);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glTranslated(0, 0, -zLevel);
                    GL11.glPopMatrix();
                }
                else
                {
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glColor4f(0, 1, 0, 1);
                    this.minecraft.getTextureManager().bindTexture(new ResourceLocation(PokecubeMod.ID,
                            "textures/hologram.png"));
                    this.blit(x, y, 0, 0, 16, 16);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                }
            }
        super.render(mouseX, mouseY, f);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

}