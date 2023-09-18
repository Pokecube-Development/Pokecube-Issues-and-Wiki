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
    public static ResourceLocation PC_GUI_TEXTURE = new ResourceLocation(PokecubeMod.ID, "textures/gui/pc_gui.png");
    public static ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(PokecubeMod.ID, "textures/gui/widgets/pc_widgets.png");

    String page;

    EditBox textFieldPageName;
    EditBox textFieldSearch;
    EditBox textFieldSelectedPage;
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

        if (this.textFieldPageName.isFocused() && (keyCode == GLFW.GLFW_KEY_ESCAPE
                || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
            {
                final String box = this.textFieldPageName.getValue();
                if (!box.equals(this.boxName) && this.textFieldPageName.isVisible())
                {
                    this.menu.changeName(box);
                    this.boxName = box;
                }

                if (this.textFieldPageName.visible) {
                    this.textFieldPageName.setVisible(false);
                }
            }
            this.textFieldPageName.setFocused(false);
            return false;
        }

        if (this.textFieldPageName.isFocused() && keyCode == GLFW.GLFW_KEY_E)
        {
            this.textFieldPageName.setFocused(true);
            return true;
        }

        if (this.textFieldSelectedPage.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER))
        {
            final String entry = this.textFieldSelectedPage.getValue();
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
        RenderSystem.setShaderTexture(0, PC_GUI_TEXTURE);
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        graphics.blit(PC_GUI_TEXTURE, x, y, 0, 0, this.imageWidth + 1, this.imageHeight + 1);

        if (this.renameButton.isHovered())
        {
            graphics.blit(WIDGETS_TEXTURE, x + 158, y + 4, 45, 15, 12, 12);
        } else {
            graphics.blit(WIDGETS_TEXTURE, x + 159, y + 5, 45, 0, 11, 11);
        }

        if (this.textFieldPageName.visible)
            graphics.blit(WIDGETS_TEXTURE, x + 115, y + 5, 0, 60, 43, 11);

        if (this.prevButton.isHovered())
        {
            graphics.blit(WIDGETS_TEXTURE, x + 6, y + 126, 60, 15, 12, 12);
        } else {
            graphics.blit(WIDGETS_TEXTURE, x + 7, y + 127, 60, 0, 11, 11);
        }

        if (this.textFieldSelectedPage.visible)
            graphics.blit(WIDGETS_TEXTURE, x + 19, y + 127, 0, 75, 24, 11);

        if (this.nextButton.isHovered())
        {
            graphics.blit(WIDGETS_TEXTURE, x + 43, y + 126, 75, 15, 12, 12);
        } else {
            graphics.blit(WIDGETS_TEXTURE, x + 44, y + 127, 75, 0, 11, 11);
        }

        if (this.releaseButton.isHovered())
            graphics.blit(WIDGETS_TEXTURE, x + 146, y + 126, 0, 15, 12, 12);
        else graphics.blit(WIDGETS_TEXTURE, x + 147, y + 127, 0, 0, 11, 11);

        if (this.confirmButton.isHovered() && this.confirmButton.visible)
        {
            graphics.blit(WIDGETS_TEXTURE, x + 134, y + 126, 90, 15, 12, 12);
        } else if (this.confirmButton.visible)
        {
            graphics.blit(WIDGETS_TEXTURE, x + 135, y + 127, 90, 0, 11, 11);
        }

        if (this.confirmButton.visible)
        {
            if (this.searchButton.isHovered())
                graphics.blit(WIDGETS_TEXTURE, x + 122, y + 126, 30, 15, 12, 12);
            else graphics.blit(WIDGETS_TEXTURE, x + 123, y + 127, 30, 0, 11, 11);
            this.searchButton.setPosition(x + 122, y + 126);

            if (this.textFieldSearch.visible)
                graphics.blit(WIDGETS_TEXTURE, x + 61, y + 127, 0, 45, 61, 11);
            this.textFieldSearch.setWidth(58);
        } else {
            if (this.searchButton.isHovered())
                graphics.blit(WIDGETS_TEXTURE, x + 134, y + 126, 30, 15, 12, 12);
            else graphics.blit(WIDGETS_TEXTURE, x + 135, y + 127, 30, 0, 11, 11);
            this.searchButton.setPosition(x + 134, y + 126);

            if (this.textFieldSearch.visible)
                graphics.blit(WIDGETS_TEXTURE, x + 61, y + 127, 0, 30, 73, 11);
            this.textFieldSearch.setWidth(70);
        }

        if (this.autoButton.isHovered())
            graphics.blit(WIDGETS_TEXTURE, x + 158, y + 126, 15, 15, 12, 12);
        else graphics.blit(WIDGETS_TEXTURE, x + 159, y + 127, 15, 0, 11, 11);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int par1, final int par2)
    {
        String text = this.menu.getPage();
        if (this.textFieldPageName.visible && text.length() > 17)
            graphics.drawString(this.font, "", 8, 6, 0xFFFFFF, false);
        else graphics.drawString(this.font, text, 8, 6, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 120;

        this.textFieldPageName = new EditBox(this.font, x + 117, y + 7, 40, 10, TComponent.translatable("block.pc.rename.narrate"));
        this.textFieldPageName.setTooltip(Tooltip.create(Component.translatable("block.pc.rename.tooltip")));
        this.textFieldPageName.setTextColor(0xFFFFFF);
        this.textFieldPageName.setBordered(false);
        this.textFieldPageName.setVisible(false);
        this.textFieldPageName.maxLength = 24;
        this.addRenderableWidget(this.textFieldPageName);

        if (!this.bound)
        {
            final Component rename = TComponent.translatable("block.pc.rename");
            this.renameButton = this.addRenderableWidget(new Button.Builder(rename, (b) -> {
                final String box = this.textFieldPageName.getValue();
                if (!box.equals(this.boxName) && this.textFieldPageName.isVisible())
                {
                    this.menu.changeName(box);
                    this.boxName = box;
                }

                this.textFieldPageName.setVisible(!this.textFieldPageName.visible);
            }).bounds(x + 157, y + 3, 10, 10)
                    .tooltip(Tooltip.create(Component.translatable("block.pc.rename.tooltip")))
                    .createNarration(supplier -> TComponent.translatable("block.pc.rename.narrate")).build());
            this.renameButton.setAlpha(0);
        }

        final Component prev = TComponent.translatable("block.pc.previous");
        this.prevButton = this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            this.menu.updateInventoryPages((byte) -1, this.minecraft.player.getInventory());
            this.textFieldSelectedPage.setValue(this.menu.getPageNb());
        }).bounds(x + 5, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.pc.previous.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.previous.narrate")).build());
        this.prevButton.setAlpha(0);

        this.textFieldSelectedPage = new EditBox(this.font,
                x + 21, y + 129, 21, 10, TComponent.translatable("block.pc.page.tooltip.narrate"));
        this.textFieldSelectedPage.setTooltip(Tooltip.create(Component.translatable("block.pc.page.tooltip")));
        this.textFieldSelectedPage.setTextColor(0xFFFFFF);
        this.textFieldSelectedPage.setBordered(false);
        this.addRenderableWidget(this.textFieldSelectedPage);

        final Component next = TComponent.translatable("block.pc.next");
        this.nextButton = this.addRenderableWidget(new Button.Builder(next, (b) -> {
            this.menu.updateInventoryPages((byte) 1, this.minecraft.player.getInventory());
            this.textFieldSelectedPage.setValue(this.menu.getPageNb());
        }).bounds(x + 44, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.pc.next.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.next.narrate")).build());
        this.nextButton.setAlpha(0);

        this.textFieldSelectedPage.value = this.page;
        this.textFieldPageName.value = "";

        this.textFieldSearch = new EditBox(this.font,
                x + 63, y + 129, 72, 10, TComponent.translatable("block.pc.search.narrate"));
        this.textFieldSearch.setTooltip(Tooltip.create(Component.translatable("block.pc.search.tooltip")));
        this.textFieldSearch.setTextColor(0xFFFFFF);
        this.textFieldSearch.setBordered(false);
        this.textFieldSearch.setVisible(false);
        this.addRenderableWidget(this.textFieldSearch);

        final Component search = TComponent.translatable("block.pc.search");
        this.searchButton = this.addRenderableWidget(new Button.Builder(search, (b) -> {
            this.textFieldSearch.setVisible(!this.textFieldSearch.visible);
        }).bounds(x + 134, y + 126, 12, 12)
                .tooltip(Tooltip.create(Component.translatable("block.pc.search.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.pc.search.narrate")).build());
        this.searchButton.visible = true;
        this.searchButton.setAlpha(100);

        if (!this.bound)
        {
            this.confirmButton = this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.confirm"), (b) -> {
                this.confirmButton.visible = !this.confirmButton.visible;
                this.release = !this.release;
                this.menu.setRelease(this.release, this.minecraft.player.getUUID());
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
            }).bounds(x + 135, y + 127, 10, 10)
                    .tooltip(Tooltip.create(Component.translatable("block.pc.option.confirm.tooltip")))
                    .createNarration(supplier -> TComponent.translatable("block.pc.option.confirm.narrate")).build());
            this.confirmButton.setAlpha(0);
            this.confirmButton.visible = false;

            this.releaseButton = this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.release"), (b) -> {
                this.confirmButton.visible = !this.confirmButton.visible;
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
            }).bounds(x + 147, y + 127, 10, 10)
                    .tooltip(Tooltip.create(Component.translatable("block.pc.option.release.tooltip")))
                    .createNarration(supplier -> TComponent.translatable("block.pc.option.release.narrate")).build());
            this.releaseButton.setAlpha(0);
            this.releaseButton.setFocused(false);
        }

        if (!this.bound)
        {
            final Component auto = this.menu.inv.autoToPC ? TComponent.translatable("block.pc.auto_on")
                    : TComponent.translatable("block.pc.auto_off");

            // TODO: Causes the / by 0 crash
            /*this.renderables.add(*/this.autoButton = this.addRenderableWidget(new Button.Builder(auto, (b) -> {
            this.menu.toggleAuto();
        }).bounds(x + 159, y + 127, 10, 10)
                .tooltip(Tooltip.create(this.menu.inv.autoToPC ? TComponent.translatable("block.pc.auto_on.tooltip")
                        : TComponent.translatable("block.pc.auto_off.tooltip")))
                .createNarration(supplier -> this.menu.inv.autoToPC ? TComponent.translatable("block.pc.auto_on.narrate")
                        : TComponent.translatable("block.pc.auto_off.narrate")).build());
            this.autoButton.setAlpha(0);
        }

//        TODO: Causes / by 0 crash
//        if (this.menu.pcPos != null)
//        {
//            if (!this.bound)
//                this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.private"), (b) -> {
//                    // TODO bind.
//                    // this.container.pcTile.toggleBound();
//                    this.minecraft.player.closeContainer();
//                }).bounds(x + 137, y + 85, 50, 20).build());
//            else
//            {
//                this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.public"), (b) -> {
//                    // TODO bind.
//                    // this.container.pcTile.toggleBound();
//                    this.minecraft.player.closeContainer();
//                }).bounds(x + 137, y + 125, 50, 20).build());
//
//                this.addRenderableWidget(new Button.Builder(TComponent.translatable("block.pc.option.bind"), (b) -> {
//                    // TODO bind.
//                    // this.container.pcTile.setBoundOwner(this.minecraft.player);
//                    this.minecraft.player.closeContainer();
//                }).bounds(x + 137, y + 105, 50, 20).build());
//            }
//        }
//        else this.addRenderableWidget(new Button.Builder(TComponent.literal(""), (b) -> {
//            // TODO bind.
//            // this.container.pcTile.toggleBound();
//            this.minecraft.player.closeContainer();
//        }).bounds(x + 137, y + 125, 1, 1).build());
    }

    // TODO: Fix check, had to be disabled
    // Causes search button to disable when the
    // release button was clicked for some reason
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
        super.render(graphics, mouseX, mouseY, f);

        for (int i = 0; i < 54; i++)
        {
            final int x = i % 9 * 18 + this.width / 2 - 80;
            final int y = i / 9 * 18 + this.height / 2 - 102;
            if (!this.textFieldSearch.getValue().isEmpty() && this.textFieldSearch.visible)
            {
                final ItemStack stack = this.menu.inv.getItem(i + 54 * this.menu.inv.getPage());
                if (stack.isEmpty()) continue;
                final String name = stack == null ? "" : stack.getHoverName().getString();
                if (name.isEmpty() || !ThutCore.trim(name).contains(ThutCore.trim(this.textFieldSearch.getValue())))
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
            if (this.menu.toRelease[i])
            {
                final int slotColor = 0x75FF0000;
                graphics.fill(x, y, x + 16, y + 16, slotColor);
            }
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

}