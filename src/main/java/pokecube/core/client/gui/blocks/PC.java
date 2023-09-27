package pokecube.core.client.gui.blocks;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pc.PCSlot;
import pokecube.core.network.packets.PacketPC;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class PC<T extends PCContainer> extends AbstractContainerScreen<T>
{

    String page;

    EditBox textFieldSelectedBox;
    EditBox textFieldBoxName;
    EditBox textFieldSearch;

    MutableComponent autoOn = TComponent.translatable("block.pc.autoon");
    MutableComponent autoOff = TComponent.translatable("block.pc.autooff");

    private String boxName = "1";

    boolean bound = false;
    boolean release = false;

    public PC(final T container, final Inventory ivplay, final Component name)
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
        if (this.textFieldBoxName.isFocused())
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) this.textFieldBoxName.setFocused(false);
            else if (keyCode == GLFW.GLFW_KEY_ENTER && this.textFieldBoxName.isFocused()) return true;
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void renderBg(final PoseStack mat, final float f, final int i, final int j)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeMod.ID, "textures/gui/pcgui.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        this.blit(mat, x, y, 0, 0, this.imageWidth + 1, this.imageHeight + 1);
    }

    @Override
    protected void renderLabels(final PoseStack mat, final int par1, final int par2)
    {}

    @Override
    public void init()
    {
        super.init();
        final int xOffset = 0;
        final int yOffset = -11;
        final Component next = TComponent.translatable("block.pc.next");
        this.addRenderableWidget(
                new Button(this.width / 2 - xOffset - 44, this.height / 2 - yOffset - 121, 10, 10, next, b ->
                {
                    this.menu.updateInventoryPages((byte) 1, this.minecraft.player.getInventory());
                    this.textFieldSelectedBox.setValue(this.menu.getPageNb());
                    this.textFieldBoxName.setValue(this.menu.getPage());
                }));
        final Component prev = TComponent.translatable("block.pc.previous");
        this.addRenderableWidget(
                new Button(this.width / 2 - xOffset - 81, this.height / 2 - yOffset - 121, 10, 10, prev, b ->
                {
                    this.menu.updateInventoryPages((byte) -1, this.minecraft.player.getInventory());
                    this.textFieldSelectedBox.setValue(this.menu.getPageNb());
                    this.textFieldBoxName.setValue(this.menu.getPage());
                }));
        this.textFieldSelectedBox = new EditBox(this.font, this.width / 2 - xOffset - 70,
                this.height / 2 - yOffset - 121, 25, 10, TComponent.literal(this.page));

        if (!this.bound)
        {
            final Component auto = this.menu.inv.isAutoToPC() ? autoOn : autoOff;
            this.addRenderableWidget(
                    new Button(this.width / 2 - xOffset + 30, this.height / 2 - yOffset + 10, 50, 10, auto, button ->
                    {
                        this.menu.toggleAuto();
                        var _auto = this.menu.inv.isAutoToPC() ? autoOn : autoOff;
                        button.setMessage(_auto);
                    }));
        }
        if (!this.bound)
        {
            final Component rename = TComponent.translatable("block.pc.rename");
            this.addRenderableWidget(
                    new Button(this.width / 2 - xOffset + 30, this.height / 2 - yOffset - 0, 50, 10, rename, b ->
                    {
                        final String box = this.textFieldBoxName.getValue();
                        if (!box.equals(this.boxName)) this.menu.changeName(box);
                        this.boxName = box;
                    }));
        }
        if (this.menu.pcPos != null)
        {
            if (!this.bound) this.addRenderableWidget(new Button(this.width / 2 - xOffset - 137,
                    this.height / 2 - yOffset - 85, 50, 20, TComponent.translatable("block.pc.option.private"), b ->
                    {
                        // TODO bind.
                        // this.container.pcTile.toggleBound();
                        this.minecraft.player.closeContainer();
                    }));
            else
            {
                this.addRenderableWidget(new Button(this.width / 2 - xOffset - 137, this.height / 2 - yOffset - 125, 50,
                        20, TComponent.translatable("block.pc.option.public"), b ->
                        {
                            // TODO bind.
                            // this.container.pcTile.toggleBound();
                            this.minecraft.player.closeContainer();
                        }));
                this.addRenderableWidget(new Button(this.width / 2 - xOffset - 137, this.height / 2 - yOffset - 105, 50,
                        20, TComponent.translatable("block.pc.option.bind"), b ->
                        {
                            // TODO bind.
                            // this.container.pcTile.setBoundOwner(this.minecraft.player);
                            this.minecraft.player.closeContainer();
                        }));
            }
        }
        else this.addRenderableWidget(new Button(this.width / 2 - xOffset - 137, this.height / 2 - yOffset - 125, 0, 0,
                TComponent.literal(""), b ->
                {
                    // TODO bind.
                    // this.container.pcTile.toggleBound();
                    this.minecraft.player.closeContainer();
                }));
        if (!this.bound)
        {
            this.addRenderableWidget(new Button(this.width / 2 - xOffset - 81, this.height / 2 - yOffset + 10, 50, 10,
                    TComponent.translatable("block.pc.option.release"), b ->
                    {
                        this.release = !this.release;
                        if (!this.release && this.menu.release)
                        {
                            this.menu.toRelease = new boolean[54];
                            for (int i = 0; i < 54; i++)
                            {
                                final int index = i;
                                final PCSlot slot = (PCSlot) this.menu.slots.get(index);
                                slot.release = this.release;
                            }
                        }
                        else for (int i = 0; i < 54; i++)
                {
                    final int index = i;
                    final PCSlot slot = (PCSlot) this.menu.slots.get(index);
                    slot.release = this.release;
                }
                        this.menu.release = this.release;
                        final PacketPC packet = new PacketPC(PacketPC.RELEASE, this.minecraft.player.getUUID());
                        packet.data.putBoolean("T", true);
                        packet.data.putBoolean("R", this.release);
                        PokecubeCore.packets.sendToServer(packet);
                        this.checkReleaseButton();
                    }));
            this.addRenderableWidget(new Button(this.width / 2 - xOffset - 31, this.height / 2 - yOffset + 10, 50, 10,
                    TComponent.translatable("block.pc.option.confirm"), b ->
                    {
                        this.release = !this.release;
                        this.menu.setRelease(this.release, this.minecraft.player.getUUID());
                        this.checkReleaseButton();
                        final PacketPC packet = new PacketPC(PacketPC.RELEASE, this.minecraft.player.getUUID());
                        packet.data.putBoolean("T", false);
                        packet.data.putBoolean("R", this.release);
                        packet.data.putInt("page", this.menu.inv.getPage());
                        for (int i = 0; i < 54; i++)
                            if (this.menu.toRelease[i]) packet.data.putBoolean("val" + i, true);
                        PokecubeCore.packets.sendToServer(packet);
                        this.menu.toRelease = new boolean[54];
                        for (int i = 0; i < 54; i++)
                        {
                            final int index = i;
                            final PCSlot slot = (PCSlot) this.menu.slots.get(index);
                            slot.release = this.release;
                        }
                    }));
            this.checkReleaseButton();
        }

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

    private void checkReleaseButton()
    {
        AbstractWidget button = (AbstractWidget) this.renderables.get(6);
        button.visible = this.release;
        button.active = this.release;
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
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float f)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, f);
        for (int i = 0; i < 54; i++)
        {
            final int x = i % 9 * 18 + this.width / 2 - 80;
            final int y = i / 9 * 18 + this.height / 2 - 97;
            if (!this.textFieldSearch.getValue().isEmpty())
            {
                final ItemStack stack = this.menu.inv.getItem(i + 54 * this.menu.inv.getPage());
                if (stack.isEmpty()) continue;
                final String name = stack == null ? "" : stack.getHoverName().getString();
                if (name.isEmpty() || !ThutCore.trim(name).contains(ThutCore.trim(this.textFieldSearch.getValue())))
                {
                    final int slotColor = 0x55FF0000;
                    GuiComponent.fill(mat, x, y, x + 16, y + 16, slotColor);
                }
                else
                {
                    final int slotColor = 0x5500FF00;
                    GuiComponent.fill(mat, x, y, x + 16, y + 16, slotColor);
                }
            }
            if (this.menu.toRelease[i])
            {
                final int slotColor = 0x55FF0000;
                GuiComponent.fill(mat, x, y, x + 16, y + 16, slotColor);
            }
        }
        this.renderTooltip(mat, mouseX, mouseY);
    }

}