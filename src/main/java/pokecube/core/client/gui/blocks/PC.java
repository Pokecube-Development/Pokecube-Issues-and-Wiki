package pokecube.core.client.gui.blocks;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
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
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pc.PCSlot;
import pokecube.core.network.packets.PacketPC;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class PC<T extends PCContainer> extends AbstractContainerScreen<T>
{
    public static ResourceLocation PC_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/pc_gui.png");
    public static ResourceLocation WIDGETS_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/widgets/pc_widgets.png");
    public static ResourceLocation PC_LIGHT_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/pc_gui_light.png");
    public static ResourceLocation WIDGETS_LIGHT_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/widgets/pc_widgets_light.png");
    public static ResourceLocation PC_DARK_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/pc_dark_gui.png");
    public static ResourceLocation WIDGETS_DARK_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/widgets/pc_widgets_dark.png");

    String page;

    EditBox renamePageBox;
    EditBox searchBar;
    EditBox selectedPageBox;
    Button autoButton;
    Button confirmButton;
    Button darkModeButton;
    Button lightModeButton;
    Button nextButton;
    Button prevButton;
    Button releaseButton;
    Button renameButton;
    Button searchButton;

    MutableComponent autoOn = TComponent.translatable("block.pc.auto_on");
    MutableComponent autoOff = TComponent.translatable("block.pc.auto_off");

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
        if (this.renamePageBox.visible && text.length() > 17 && this.lightModeButton.visible && PokecubeCore.getConfig().fancyGUI)
            graphics.drawString(this.font, "", 8, 6, 0xB2AFD6, false);
        else if (this.lightModeButton.visible && PokecubeCore.getConfig().fancyGUI) graphics.drawString(this.font, text, 8, 6, 0xB2AFD6, false);
        else if (this.renamePageBox.visible && text.length() > 17 && PokecubeCore.getConfig().fancyGUI)
            graphics.drawString(this.font, "", 8, 6, 0xFFFFFF, false);
        else if (PokecubeCore.getConfig().fancyGUI) graphics.drawString(this.font, text, 8, 6, 0xFFFFFF, false);
        else graphics.drawString(this.font, text, 8, 6, 4210752, false);

        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float f, final int i, final int j)
    {
        ResourceLocation WIDGETS_DARK_OR_LIGHT = this.darkModeButton.visible ? WIDGETS_LIGHT_GUI : WIDGETS_DARK_GUI;
        ResourceLocation WIDGETS_DEFAULT_OR_FANCY = PokecubeCore.getConfig().fancyGUI ? WIDGETS_DARK_OR_LIGHT : WIDGETS_GUI;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PC_LIGHT_GUI);
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        //  Blit format: Texture location, gui x pos, gui y position, texture x pos, texture y pos, texture x size, texture y size
        if (this.darkModeButton.visible)
            graphics.blit(PC_LIGHT_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight);
        else if (this.lightModeButton.visible)
            graphics.blit(PC_DARK_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight);
        else graphics.blit(PC_GUI, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (this.darkModeButton.isHoveredOrFocused() && this.darkModeButton.visible)
        {
            graphics.blit(WIDGETS_DARK_GUI, x - 17, y + 1, 240, 20, 15, 13);
        } else if (this.darkModeButton.visible) {
            graphics.blit(WIDGETS_DARK_GUI, x - 16, y + 1, 240, 0, 14, 13);
        }

        if (this.lightModeButton.isHoveredOrFocused() && this.lightModeButton.visible)
        {
            graphics.blit(WIDGETS_LIGHT_GUI, x - 17, y + 1, 240, 20, 15, 13);
        } else if (this.lightModeButton.visible) {
            graphics.blit(WIDGETS_LIGHT_GUI, x - 16, y + 1, 240, 0, 14, 13);
        }

        if (this.renameButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 158, y + 4, 60, 15, 12, 12);
        } else {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 5, 60, 0, 11, 11);
        }

        if (this.renamePageBox.visible)
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 115, y + 5, 0, 60, 43, 11);

        if (this.prevButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 6, y + 126, 75, 15, 12, 12);
        } else {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 7, y + 127, 75, 0, 11, 11);
        }

        if (this.selectedPageBox.visible)
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 19, y + 127, 0, 75, 24, 11);

        if (this.nextButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 43, y + 126, 90, 15, 12, 12);
        } else {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 44, y + 127, 90, 0, 11, 11);
        }

        if (this.releaseButton.isHoveredOrFocused())
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 146, y + 126, 15, 15, 12, 12);
        else graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 147, y + 127, 15, 0, 11, 11);

        if (this.confirmButton.isHoveredOrFocused() && this.confirmButton.visible)
        {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 134, y + 126, 105, 15, 12, 12);
        } else if (this.confirmButton.visible) {
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 135, y + 127, 105, 0, 11, 11);
        }

        if (this.confirmButton.visible)
        {
            if (this.searchButton.isHoveredOrFocused())
                graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 122, y + 126, 45, 15, 12, 12);
            else graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 123, y + 127, 45, 0, 11, 11);
            this.searchButton.setPosition(x + 122, y + 126);

            if (this.searchBar.visible)
                graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 61, y + 127, 0, 45, 61, 11);
            this.searchBar.setWidth(58);
        } else {
            if (this.searchButton.isHoveredOrFocused())
                graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 134, y + 126, 45, 15, 12, 12);
            else graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 135, y + 127, 45, 0, 11, 11);
            this.searchButton.setPosition(x + 134, y + 126);

            if (this.searchBar.visible)
                graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 61, y + 127, 0, 30, 73, 11);
            this.searchBar.setWidth(70);
        }

        if (this.autoButton.isHoveredOrFocused() && this.menu.inv.isAutoToPC())
            graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 158, y + 126, 30, 15, 12, 12);
        else if (this.menu.inv.isAutoToPC()) graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 127, 30, 0, 11, 11);
        else if (this.autoButton.isHoveredOrFocused()) graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 158, y + 126, 120, 15, 12, 12);
        else graphics.blit(WIDGETS_DEFAULT_OR_FANCY, x + 159, y + 127, 120, 0, 11, 11);

    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 120;

        // Elements placed in order of selection when pressing tab
        final Component darkMode = TComponent.literal("");
        this.darkModeButton = this.addRenderableWidget(new Button.Builder(darkMode, (b) -> {
            this.darkModeButton.visible = false;
            this.lightModeButton.visible = true;
        }).bounds(x - 16, y + 1, 14, 13)
                .tooltip(Tooltip.create(Component.translatable("block.pc.dark_mode.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.dark_mode.narrate")).build());
        this.darkModeButton.visible = (!PokecubeCore.getConfig().darkMode && PokecubeCore.getConfig().fancyGUI);
        this.darkModeButton.setAlpha(0);

        final Component lightMode = TComponent.literal("");
        this.lightModeButton = this.addRenderableWidget(new Button.Builder(lightMode, (b) -> {
            this.lightModeButton.visible = false;
            this.darkModeButton.visible = true;
        }).bounds(x - 16, y + 1, 14, 13)
                .tooltip(Tooltip.create(Component.translatable("block.pc.light_mode.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.light_mode.narrate")).build());
        this.lightModeButton.visible = (PokecubeCore.getConfig().darkMode && PokecubeCore.getConfig().fancyGUI);
        this.lightModeButton.setAlpha(0);

        this.renamePageBox = new EditBox(this.font, x + 117, y + 7, 40, 10, TComponent.translatable("block.pc.rename.narrate"));
        this.renamePageBox.setTooltip(Tooltip.create(Component.translatable("block.pc.rename.tooltip")));
        if (!PokecubeCore.getConfig().fancyGUI) this.renamePageBox.setPosition(x + 117, y + 6);
        if (this.lightModeButton.visible)
            this.renamePageBox.setTextColor(0xB2AFD6);
        else this.renamePageBox.setTextColor(0xFFFFFF);
        this.renamePageBox.setBordered(false);
        this.renamePageBox.setVisible(false);
        this.renamePageBox.maxLength = 24;
        this.addRenderableWidget(this.renamePageBox);

        if (!this.bound)
        {
            final Component rename = TComponent.translatable("block.pc.rename");
            this.renameButton = this.addRenderableWidget(new Button.Builder(rename, (b) -> {
                final String box = this.renamePageBox.getValue();
                if (!box.equals(this.boxName) && this.renamePageBox.isVisible() && !this.renamePageBox.getValue().equals(""))
                {
                    this.menu.changeName(box);
                    this.boxName = box;
                }

                this.renamePageBox.setVisible(!this.renamePageBox.visible);
            }).bounds(x + 157, y + 3, 10, 10)
                    .tooltip(Tooltip.create(Component.translatable("block.pc.rename.tooltip")))
                    .createNarration(supplier -> TComponent.translatable("block.pc.rename.narrate")).build());
            this.renameButton.setAlpha(0);
        }

        final Component prev = TComponent.translatable("block.pc.previous");
        this.prevButton = this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            this.menu.updateInventoryPages((byte) -1, this.minecraft.player.getInventory());
            this.selectedPageBox.setValue(this.menu.getPageNb());
        }).bounds(x + 5, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.pc.previous.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.previous.narrate")).build());
        this.prevButton.setAlpha(0);

        this.selectedPageBox = new EditBox(this.font,
                x + 21, y + 129, 21, 10, TComponent.translatable("block.pc.page.tooltip.narrate"));
        this.selectedPageBox.setTooltip(Tooltip.create(Component.translatable("block.pc.page.tooltip")));
        if (!PokecubeCore.getConfig().fancyGUI) this.selectedPageBox.setPosition(x + 21, y + 128);
        if (this.lightModeButton.visible)
            this.selectedPageBox.setTextColor(0xB2AFD6);
        else this.selectedPageBox.setTextColor(0xFFFFFF);
        this.selectedPageBox.setBordered(false);
        this.addRenderableWidget(this.selectedPageBox);

        final Component next = TComponent.translatable("block.pc.next");
        this.nextButton = this.addRenderableWidget(new Button.Builder(next, (b) -> {
            this.menu.updateInventoryPages((byte) 1, this.minecraft.player.getInventory());
            this.selectedPageBox.setValue(this.menu.getPageNb());
        }).bounds(x + 44, y + 127, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.pc.next.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.next.narrate")).build());
        this.nextButton.setAlpha(0);

        this.selectedPageBox.value = this.page;
        this.renamePageBox.value = "";

        this.searchBar = new EditBox(this.font,
                x + 63, y + 129, 72, 10, TComponent.translatable("block.pc.search.narrate"));
        this.searchBar.setTooltip(Tooltip.create(Component.translatable("block.pc.search.tooltip")));
        if (!PokecubeCore.getConfig().fancyGUI) this.searchBar.setPosition(x + 63, y + 128);
        if (this.lightModeButton.visible)
            this.searchBar.setTextColor(0xB2AFD6);
        else this.searchBar.setTextColor(0xFFFFFF);
        this.searchBar.setBordered(false);
        this.searchBar.setVisible(false);
        this.addRenderableWidget(this.searchBar);

        final Component search = TComponent.translatable("block.pc.search");
        this.searchButton = this.addRenderableWidget(new Button.Builder(search, (b) -> {
            this.searchBar.setVisible(!this.searchBar.visible);
        }).bounds(x + 134, y + 126, 12, 12)
                .tooltip(Tooltip.create(Component.translatable("block.pc.search.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.pc.search.narrate")).build());
        this.searchButton.visible = true;
        this.searchButton.setAlpha(0);

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
            final Component auto = this.menu.inv.isAutoToPC() ? autoOn : autoOff;
            this.autoButton = this.addRenderableWidget(new Button.Builder(auto, (b) -> {
                this.menu.toggleAuto();
                var _auto = this.menu.inv.isAutoToPC() ? autoOn : autoOff;
                b.setMessage(_auto);
              }).bounds(x + 159, y + 127, 10, 10)
                .createNarration(supplier -> this.menu.inv.isAutoToPC() ? TComponent.translatable("block.pc.auto_on.narrate")
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

        this.autoButton.setTooltip(Tooltip.create(this.menu.inv.isAutoToPC() ? TComponent.translatable("block.pc.auto_on.tooltip")
                : TComponent.translatable("block.pc.auto_off.tooltip")));

        for (int i = 0; i < 54; i++)
        {
            final int x = i % 9 * 18 + this.width / 2 - 80;
            final int y = i / 9 * 18 + this.height / 2 - 102;
            if (!this.searchBar.getValue().isEmpty() && this.searchBar.visible)
            {
                final ItemStack stack = this.menu.inv.getItem(i + 54 * this.menu.inv.getPage());
                if (stack.isEmpty()) continue;
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
            if (this.menu.toRelease[i])
            {
                final int slotColor = 0x75FF0000;
                graphics.fill(x, y, x + 16, y + 16, slotColor);
            }
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

}