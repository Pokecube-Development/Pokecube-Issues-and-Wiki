package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
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

    EditBox textFieldBoxName;
    EditBox textFieldSearch;
    EditBox textFieldSelectedBox;
    Button autoButton;
    Button confirmButton;
    Button nextButton;
    Button prevButton;
    Button releaseButton;
    Button renameButton;
    Button searchButton;

    MutableComponent autoOn = TComponent.translatable("block.pc.autoon");
    MutableComponent autoOff = TComponent.translatable("block.pc.autooff");

    private String boxName = "1";

    boolean bound = false;
    boolean release = false;

    public PC(final T container, final Inventory ivplay, final Component name)
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
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeMod.ID, "textures/gui/pc_gui.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        graphics.blit(new ResourceLocation(PokecubeMod.ID, "textures/gui/pc_gui.png"),
                x, y, 0, 0, this.imageWidth + 1, this.imageHeight + 1);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int par1, final int par2)
    {
        String text = this.menu.getPage();
        graphics.drawString(this.font, text, 8, 6, 0xFFFFFF, false);
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
                x + 117, y + 6, 40, 10, TComponent.translatable("block.pc.rename.narrate"));
        this.textFieldBoxName.setTooltip(Tooltip.create(Component.translatable("block.pc.rename.tooltip")));
        this.textFieldBoxName.setTextColor(0xFFFFFF);
        this.textFieldBoxName.setBordered(false);
        this.textFieldBoxName.maxLength = 17;
        this.addRenderableWidget(this.textFieldBoxName);

        if (!this.bound)
        {
            final Component rename = TComponent.translatable("block.pc.rename");
            this.renameButton = this.addRenderableWidget(new Button.Builder(rename, (b) -> {
                final String box = this.textFieldBoxName.getValue();
                if (!box.equals(this.boxName)) this.menu.changeName(box);
                this.boxName = box;
            }).bounds(x + 157, y + 4, 12, 12)
                    .tooltip(Tooltip.create(Component.translatable("block.pc.rename.tooltip")))
                    .createNarration(supplier -> TComponent.translatable("block.pc.rename.narrate")).build());
            this.renameButton.setAlpha(0);
        }

        final Component prev = TComponent.translatable("block.pc.previous");
        this.prevButton = this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            this.menu.updateInventoryPages((byte) -1, this.minecraft.player.getInventory());
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
        }).bounds(x + 7, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.pc.previous.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.previous.narrate")).build());
        this.prevButton.setAlpha(0);

        this.textFieldSelectedBox = new EditBox(this.font,
                x + 21, y + 128, 21, 10, TComponent.translatable("block.pc.page.tooltip.narrate"));
        this.textFieldSelectedBox.setTooltip(Tooltip.create(Component.translatable("block.pc.page.tooltip")));
        this.textFieldSelectedBox.setTextColor(0xFFFFFF);
        this.textFieldSelectedBox.setBordered(false);
        this.addRenderableWidget(this.textFieldSelectedBox);

        final Component next = TComponent.translatable("block.pc.next");
        this.nextButton = this.addRenderableWidget(new Button.Builder(next, (b) -> {
            this.menu.updateInventoryPages((byte) 1, this.minecraft.player.getInventory());
            this.textFieldSelectedBox.setValue(this.menu.getPageNb());
        }).bounds(x + 44, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.pc.next.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.next.narrate")).build());
        this.nextButton.setAlpha(0);

        this.textFieldSelectedBox.value = this.page;
        this.textFieldBoxName.value = "";


//        if (this.menu.pcPos != null)
//        {
//            if (!this.bound)
//                this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.private"), (b) -> {
//                    // TODO bind.
//                    // this.container.pcTile.toggleBound();
//                    this.minecraft.player.closeContainer();
//                }).bounds(this.width / 2 - x - 137, this.height / 2 - y - 85, 50, 20).build());
//            else
//            {
//                this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.public"), (b) -> {
//                    // TODO bind.
//                    // this.container.pcTile.toggleBound();
//                    this.minecraft.player.closeContainer();
//                }).bounds(this.width / 2 - x - 137, this.height / 2 - y - 125, 50, 20).build());
//
//                this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.bind"), (b) -> {
//                    // TODO bind.
//                    // this.container.pcTile.setBoundOwner(this.minecraft.player);
//                    this.minecraft.player.closeContainer();
//                }).bounds(this.width / 2 - x - 137, this.height / 2 - y - 105, 50, 20).build());
//            }
//        }
//        else this.addRenderableWidget(new Button.Builder(TComponent.literal(""), (b) -> {
//            // TODO bind.
//            // this.container.pcTile.toggleBound();
//            this.minecraft.player.closeContainer();
//            }).bounds(this.width / 2 - x - 137, this.height / 2 - y - 125, 0, 0).build());
//
        if (!this.bound)
        {
            this.releaseButton = this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.release"), (b) -> {
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
            }).bounds(x + 73, y + 127, 10, 10)
                    .tooltip(Tooltip.create(Component.translatable("block.pc.option.release.tooltip")))
                    .createNarration(supplier -> TComponent.translatable("block.pc.option.release.narrate")).build());
            this.releaseButton.setAlpha(0);

            this.confirmButton = this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.confirm"), (b) -> {
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
            }).bounds(x + 61, y + 127, 10, 10)
                    .tooltip(Tooltip.create(Component.translatable("block.pc.option.confirm.tooltip")))
                    .createNarration(supplier -> TComponent.translatable("block.pc.option.confirm.narrate")).build());
            this.confirmButton.setAlpha(0);

            this.checkReleaseButton();
        }

        if (!this.bound)
        {
            final Component auto = this.menu.inv.autoToPC ? TComponent.translatable("block.pc.auto_on")
                    : TComponent.translatable("block.pc.auto_off");

            // TODO: Causes the / by 0 crash
            /*this.renderables.add(*/this.autoButton = this.addRenderableWidget(new Button.Builder(auto, (b) -> {
            this.menu.toggleAuto();
        }).bounds(x + 85, y + 127, 10, 10)
                .tooltip(Tooltip.create(this.menu.inv.autoToPC ? TComponent.translatable("block.pc.auto_on.tooltip")
                        : TComponent.translatable("block.pc.auto_off.tooltip")))
                .createNarration(supplier -> this.menu.inv.autoToPC ? TComponent.translatable("block.pc.auto_on.narrate")
                        : TComponent.translatable("block.pc.auto_off.narrate")).build());
            this.autoButton.setAlpha(0);
        }

        this.textFieldSearch = new EditBox(this.font,
                x + 99, y + 128, 58, 10, TComponent.translatable("block.pc.search.narrate"));
        this.textFieldSearch.setTooltip(Tooltip.create(Component.translatable("block.pc.search.tooltip")));
        this.textFieldSearch.setTextColor(0xFFFFFF);
        this.textFieldSearch.setBordered(false);
        this.addRenderableWidget(this.textFieldSearch);

        final Component search = TComponent.translatable("block.pc.search");
        this.searchButton = this.addRenderableWidget(new Button.Builder(search, (b) -> {
        }).bounds(x + 157, y + 127, 12, 12)
                .tooltip(Tooltip.create(Component.translatable("block.pc.search.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.pc.search.narrate")).build());
        this.searchButton.setAlpha(0);
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
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float f)
    {
        this.renderBackground(graphics);
        // TODO: Fix / by zero crash
        super.render(graphics, mouseX, mouseY, f);

        for (int i = 0; i < 54; i++)
        {
            final int x = i % 9 * 18 + this.width / 2 - 80;
            final int y = i / 9 * 18 + this.height / 2 - 102;
            if (!this.textFieldSearch.getValue().isEmpty())
            {
                final ItemStack stack = this.menu.inv.getItem(i + 54 * this.menu.inv.getPage());
                if (stack.isEmpty()) continue;
                final String name = stack == null ? "" : stack.getHoverName().getString();
                if (name.isEmpty() || !ThutCore.trim(name).contains(ThutCore.trim(this.textFieldSearch.getValue())))
                {
                    final int slotColor = 0x75FF0000;
                    // TODO: Check this
                    graphics.fill(x, y, x + 16, y + 16, slotColor);
                }
                else
                {
                    final int slotColor = 0x5500FF00;
                    // TODO: Check this
                    graphics.fill(x, y, x + 16, y + 16, slotColor);
                }
            }
            if (this.menu.toRelease[i])
            {
                final int slotColor = 0x55FF0000;
                // TODO: Check this
                graphics.fill(x, y, x + 16, y + 16, slotColor);
            }
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

}