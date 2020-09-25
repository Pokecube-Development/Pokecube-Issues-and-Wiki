package pokecube.core.client.gui.blocks;

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
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pc.PCSlot;
import pokecube.core.network.packets.PacketPC;
import thut.core.common.ThutCore;

public class PC<T extends PCContainer> extends ContainerScreen<T>
{

    String page;

    TextFieldWidget textFieldSelectedBox;
    TextFieldWidget textFieldBoxName;
    TextFieldWidget textFieldSearch;

    String autoOn  = new TranslationTextComponent("block.pc.autoon");
    String autoOff = new TranslationTextComponent("block.pc.autooff");

    private String boxName = "1";

    boolean bound   = false;
    boolean release = false;

    public PC(final T container, final PlayerInventory ivplay, final ITextComponent name)
    {
        super(container, ivplay, name);
        this.xSize = 175;
        this.ySize = 229;
        this.page = container.getPageNb();
        this.boxName = container.getPage();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (this.textFieldSearch.isFocused() && keyCode != GLFW.GLFW_KEY_BACKSPACE) return true;
        if (this.textFieldSelectedBox.isFocused() && keyCode == GLFW.GLFW_KEY_ENTER)
        {
            final String entry = this.textFieldSelectedBox.getText();
            int number = 1;
            try
            {
                number = Integer.parseInt(entry);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
            number = Math.max(1, Math.min(number, this.container.inv.boxCount()));
            this.container.gotoInventoryPage(number);
            return true;
        }
        if (this.textFieldBoxName.isFocused() && keyCode != GLFW.GLFW_KEY_BACKSPACE) return true;
        if (this.textFieldBoxName.isFocused()) if (keyCode == GLFW.GLFW_KEY_ESCAPE) this.textFieldBoxName.setFocused(
                false);
        else if (keyCode == GLFW.GLFW_KEY_ENTER && this.textFieldBoxName.isFocused()) return true;
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack mat, final float f, final int i, final int j)
    {
        GL11.glColor4f(1f, 1f, 1f, 1f);

        this.minecraft.getTextureManager().bindTexture(new ResourceLocation(PokecubeMod.ID, "textures/gui/pcgui.png"));
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize + 1, this.ySize + 1);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final MatrixStack mat, final int par1, final int par2)
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
            this.container.updateInventoryPages((byte) 1, this.minecraft.player.inventory);
            this.textFieldSelectedBox.setText(this.container.getPageNb());
            this.textFieldBoxName.setText(this.container.getPage());
        }));
        final ITextComponent prev = new TranslationTextComponent("block.pc.previous");
        this.addButton(new Button(this.width / 2 - xOffset - 81, this.height / 2 - yOffset - 121, 10, 10, prev, b ->
        {
            this.container.updateInventoryPages((byte) -1, this.minecraft.player.inventory);
            this.textFieldSelectedBox.setText(this.container.getPageNb());
            this.textFieldBoxName.setText(this.container.getPage());
        }));
        this.textFieldSelectedBox = new TextFieldWidget(this.font, this.width / 2 - xOffset - 70, this.height / 2
                - yOffset - 121, 25, 10, this.page);

        if (!this.bound)
        {
            final ITextComponent auto = this.container.inv.autoToPC ? new TranslationTextComponent(
                    "block.pc.autoon")
                    : new TranslationTextComponent("block.pc.autooff");
            this.buttons.add(new Button(this.width / 2 - xOffset + 30, this.height / 2 - yOffset + 10, 50, 10, auto,
                    b -> this.container.toggleAuto()));
        }
        if (!this.bound)
        {
            final ITextComponent rename = new TranslationTextComponent("block.pc.rename");
            this.addButton(new Button(this.width / 2 - xOffset + 30, this.height / 2 - yOffset - 0, 50, 10, rename, b ->
            {
                final String box = this.textFieldBoxName.getText();
                if (!box.equals(this.boxName)) this.container.changeName(box);
                this.boxName = box;
            }));
        }
        if (this.container.pcPos != null)
        {
            if (!this.bound) this.addButton(new Button(this.width / 2 - xOffset - 137, this.height / 2 - yOffset - 85,
                    50, 20, new TranslationTextComponent("block.pc.option.private"), b ->
                    {
                        // TODO bind.
                        // this.container.pcTile.toggleBound();
                        this.minecraft.player.closeScreen();
                    }));
            else
            {
                this.addButton(new Button(this.width / 2 - xOffset - 137, this.height / 2 - yOffset - 125, 50, 20,
                        new TranslationTextComponent("block.pc.option.public"), b ->
                        {
                            // TODO bind.
                            // this.container.pcTile.toggleBound();
                            this.minecraft.player.closeScreen();
                        }));
                        this.addButton(new Button(this.width / 2 - xOffset - 137, this.height / 2 - yOffset - 105, 50,
                                20, new TranslationTextComponent("block.pc.option.bind"), b ->
                        {
                            // TODO bind.
                            // this.container.pcTile.setBoundOwner(this.minecraft.player);
                            this.minecraft.player.closeScreen();
                        }));
            }
        }
        else this.addButton(new Button(this.width / 2 - xOffset - 137, this.height / 2 - yOffset - 125, 0, 0, "", b ->
        {
            // TODO bind.
            // this.container.pcTile.toggleBound();
            this.minecraft.player.closeScreen();
        }));
        if (!this.bound)
        {
            this.addButton(new Button(this.width / 2 - xOffset - 81, this.height / 2 - yOffset + 10, 50, 10,
                    new TranslationTextComponent("block.pc.option.release"), b ->
                    {
                        this.release = !this.release;
                        if (!this.release && this.container.release)
                        {
                            this.container.toRelease = new boolean[54];
                            for (int i = 0; i < 54; i++)
                            {
                                final int index = i;
                                final PCSlot slot = (PCSlot) this.container.inventorySlots.get(index);
                                slot.release = this.release;
                            }
                        }
                        else for (int i = 0; i < 54; i++)
                {
                    final int index = i;
                    final PCSlot slot = (PCSlot) this.container.inventorySlots.get(index);
                    slot.release = this.release;
                }
                        this.container.release = this.release;
                        final PacketPC packet = new PacketPC(PacketPC.RELEASE, this.minecraft.player.getUniqueID());
                        packet.data.putBoolean("T", true);
                        packet.data.putBoolean("R", this.release);
                        PokecubeCore.packets.sendToServer(packet);
                        if (this.release)
                        {
                            this.buttons.get(6).active = this.release;
                            this.buttons.get(6).visible = this.release;
                        }
                        else
                        {
                            this.buttons.get(6).active = this.release;
                            this.buttons.get(6).visible = this.release;
                        }
                    }));
                    this.addButton(new Button(this.width / 2 - xOffset - 31, this.height / 2 - yOffset + 10, 50, 10,
                            new TranslationTextComponent("block.pc.option.confirm"), b ->
                    {
                        this.release = !this.release;
                        this.container.setRelease(this.release, this.minecraft.player.getUniqueID());
                        if (this.release)
                        {
                            this.buttons.get(6).active = this.release;
                            this.buttons.get(6).visible = this.release;
                        }
                        else
                        {
                            this.buttons.get(6).active = this.release;
                            this.buttons.get(6).visible = this.release;
                        }
                        final PacketPC packet = new PacketPC(PacketPC.RELEASE, this.minecraft.player.getUniqueID());
                        packet.data.putBoolean("T", false);
                        packet.data.putBoolean("R", this.release);
                        packet.data.putInt("page", this.container.inv.getPage());
                        for (int i = 0; i < 54; i++)
                            if (this.container.toRelease[i])
                            {
                                packet.data.putBoolean("val" + i, true);
                                System.out.println("release: " + i);
                            }
                        PokecubeCore.packets.sendToServer(packet);
                        this.container.toRelease = new boolean[54];
                        for (int i = 0; i < 54; i++)
                        {
                            final int index = i;
                            final PCSlot slot = (PCSlot) this.container.inventorySlots.get(index);
                            slot.release = this.release;
                        }
                    }));
            this.buttons.get(6).visible = false;
            this.buttons.get(6).active = false;
        }

        this.textFieldBoxName = new TextFieldWidget(this.font, this.width / 2 - xOffset - 80, this.height / 2 - yOffset
                + 0, 100, 10, this.boxName);
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
        super.render(mouseX, mouseY, f);
        for (int i = 0; i < 54; i++)
        {
            final int x = i % 9 * 18 + this.width / 2 - 79;
            final int y = i / 9 * 18 + this.height / 2 - 96;
            if (!this.textFieldSearch.getText().isEmpty())
            {
                final ItemStack stack = this.container.inv.getStackInSlot(i + 54 * this.container.inv.getPage());
                if (stack.isEmpty()) continue;
                final String name = stack == null ? "" : stack.getDisplayName().getString();
                if (name.isEmpty() || !ThutCore.trim(name).contains(ThutCore.trim(this.textFieldSearch.getText())))
                {
                    final int slotColor = 0x55FF0000;
                    AbstractGui.fill(x, y, x + 16, y + 16, slotColor);
                }
                else
                {
                    final int slotColor = 0x5500FF00;
                    AbstractGui.fill(x, y, x + 16, y + 16, slotColor);
                }
            }
            if (this.container.toRelease[i])
            {
                final int slotColor = 0x55FF0000;
                AbstractGui.fill(x, y, x + 16, y + 16, slotColor);
            }
        }
        this.renderHoveredToolTip(mouseX, mouseY);
    }

}