package pokecube.core.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;
import pokecube.api.moves.MoveEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.packets.PacketTMs;
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class TMs<T extends TMContainer> extends AbstractContainerScreen<T>
{
    public static ResourceLocation TM_GUI = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "tm_machine.png");
    public static ResourceLocation TM_DARK_GUI = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "tm_machine_dark.png");
    public static ResourceLocation WIDGETS_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/widgets/pc_widgets.png");

    public static ResourceLocation WIDGETS_DARK_GUI = new ResourceLocation(PokecubeMod.ID, "textures/gui/widgets/pc_widgets_dark.png");

    private EditBox searchBar;
    Button applyButton;
    Button darkModeButton;
    Button lightModeButton;
    Button movesSelection;
    Button nextButton;
    Button prevButton;
    int index = 0;

    public TMs(final T container, final Inventory playerInventory, final Component name)
    {
        super(container, playerInventory, name);
        this.imageWidth = 176;
        this.imageHeight = 176;
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

        if (this.searchBar.isFocused())
        {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
            {
                // TODO search the moves list and go to the one here.
            }
            this.searchBar.setCanLoseFocus(true);
            return true;
        }
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int x, final int y)
    {
        if (this.lightModeButton.visible) graphics.drawString(this.font, TComponent.translatable("block.pokecube.tm_machine"),
                8, 6, 0xB2AFD6, false);
        else graphics.drawString(this.font, TComponent.translatable("block.pokecube.tm_machine"),
                8, 6, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY)
    {
        ResourceLocation WIDGETS_DARK_OR_LIGHT_GUI = this.darkModeButton.visible ? WIDGETS_GUI : WIDGETS_DARK_GUI;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TMs.TM_GUI);
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        //  Blit format: Texture location, gui x pos, gui y position, texture x pos, texture y pos, texture x size, texture y size
        if (this.darkModeButton.visible)
            graphics.blit(TM_GUI, x, y, 0, 0, this.imageWidth + 1, this.imageHeight + 1);
        else if (this.lightModeButton.visible)
            graphics.blit(TM_DARK_GUI, x, y, 0, 0, this.imageWidth + 1, this.imageHeight + 1);

        graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 58, y + 16, 0, 90, 112, 19);

        if (this.darkModeButton.isHoveredOrFocused() && this.darkModeButton.visible)
        {
            graphics.blit(WIDGETS_DARK_GUI, x - 17, y + 1, 240, 0, 15, 13);
        } else if (this.darkModeButton.visible) {
            graphics.blit(WIDGETS_DARK_GUI, x - 16, y + 1, 240, 20, 14, 13);
        }

        if (this.lightModeButton.isHoveredOrFocused() && this.lightModeButton.visible)
        {
            graphics.blit(WIDGETS_GUI, x - 17, y + 1, 240, 0, 15, 13);
        } else if (this.lightModeButton.visible) {
            graphics.blit(WIDGETS_GUI, x - 16, y + 1, 240, 20, 14, 13);
        }

        if (this.prevButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 57, y + 35, 75, 15, 12, 12);
        } else {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 58, y + 36, 75, 0, 11, 11);
        }

        if (this.searchBar.visible)
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 70, y + 36, 75, 30, 88, 11);

        if (this.nextButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 158, y + 35, 90, 15, 12, 12);
        } else {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 159, y + 36, 90, 0, 11, 11);
        }

        if (this.applyButton.isHoveredOrFocused())
        {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 104, y + 47, 0, 190, 20, 20);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 104, y + 47, 25, 165, 20, 20);
        } else {
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 105, y + 48, 0, 165, 19, 19);
            graphics.blit(WIDGETS_DARK_OR_LIGHT_GUI, x + 105, y + 48, 25, 165, 19, 19);
        }
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.width / 2 - 88;
        final int y = this.height / 2 - 88;

        final String[] moves = this.menu.moves;
        final String s = moves.length > 0 ? moves[this.index % moves.length] : "";

        // Elements placed in order of selection when pressing tab
        final Component darkMode = TComponent.literal("");
        this.darkModeButton = this.addRenderableWidget(new Button.Builder(darkMode, (b) -> {
            this.darkModeButton.visible = false;
            this.lightModeButton.visible = true;
        }).bounds(x - 16, y + 1, 14, 13)
                .tooltip(Tooltip.create(Component.translatable("block.pc.dark_mode.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.dark_mode.narrate")).build());
        this.darkModeButton.setAlpha(0);

        final Component lightMode = TComponent.literal("");
        this.lightModeButton = this.addRenderableWidget(new Button.Builder(lightMode, (b) -> {
            this.lightModeButton.visible = false;
            this.darkModeButton.visible = true;
        }).bounds(x - 16, y + 1, 14, 13)
                .tooltip(Tooltip.create(Component.translatable("block.pc.light_mode.tooltip")))
                .createNarration(supplier -> Component.translatable("block.pc.light_mode.narrate")).build());
        this.lightModeButton.visible = false;
        this.lightModeButton.setAlpha(0);

        this.movesSelection = this.addRenderableWidget(new Button.Builder(Component.literal(""), (b) -> {})
                .bounds( x + 58, y + 16, 111, 18)
                .tooltip(Tooltip.create(Component.translatable("block.tm_machine.moves_selection.tooltip")))
                .createNarration(supplier -> TComponent.translatable("block.tm_machine.moves_selection.narrate" + MovesUtils.getMoveName(s, null))).build());
        this.movesSelection.setAlpha(0);
        this.movesSelection.active = false;

        final Component prev = TComponent.translatable("block.tm_machine.previous");
        this.prevButton = this.addRenderableWidget(new Button.Builder(prev, (b) -> {
            this.index--;
            if (this.index < 0 && moves.length > 0) this.index = moves.length - 1;
            else if (this.index < 0) this.index = 0;
        }).bounds(x + 58, y + 36, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.tm_machine.previous.tooltip")))
                .createNarration(supplier -> Component.translatable("block.tm_machine.previous.narrate")).build());
        this.prevButton.setAlpha(0);

        this.searchBar = this.addRenderableWidget(new EditBox(this.font,
                x + 72, y + 38, 86, 10,
                TComponent.translatable("block.tm_machine.search_bar.narrate")));
        this.searchBar.setTooltip(Tooltip.create(Component.translatable("block.tm_machine.search_bar.tooltip")));
        if (this.lightModeButton.visible) {
            this.searchBar.setTextColor(0xB2AFD6);
        } else this.searchBar.setTextColor(0xFFFFFF);
        this.searchBar.setBordered(false);

        final Component next = TComponent.translatable("block.tm_machine.next");
        this.nextButton = this.addRenderableWidget(new Button.Builder(next, (b) -> {
            this.index++;
            if (this.index > moves.length - 1) this.index = 0;
        }).bounds(x + 159, y + 36, 10, 10)
                .tooltip(Tooltip.create(Component.translatable("block.tm_machine.next.tooltip")))
                .createNarration(supplier -> Component.translatable("block.tm_machine.next.narrate")).build());
        this.nextButton.setAlpha(0);

        final Component apply = TComponent.translatable("block.tm_machine.apply").withStyle(ChatFormatting.WHITE);
        this.applyButton = this.addRenderableWidget(new Button.Builder(apply, (b) -> {
            final PacketTMs packet = new PacketTMs();
            packet.data.putInt("m", this.index);
            PokecubeCore.packets.sendToServer(packet);
        }).bounds(x + 105, y + 48, 19, 19)
                .tooltip(Tooltip.create(Component.translatable("block.tm_machine.apply.tooltip")))
                .createNarration(supplier -> Component.translatable("block.tm_machine.apply.narrate")).build());
        this.applyButton.setAlpha(0);
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        final String[] moves = this.menu.moves;
        final String s = moves.length > 0 ? moves[this.index % moves.length] : "";
        final MoveEntry move = MovesUtils.getMove(s);
        if (move != null)
        {
            final int yOffset = this.height / 2 - 88;
            final int xOffset = this.width / 2 - 88;
            String append = MovesUtils.getMoveName(s, null).getString().length() >= 15 ? "".concat("...") : "";

            graphics.drawString(this.font, MovesUtils.getMoveName(s, null).getString(15) + append, xOffset + 61,
                    yOffset + 22, move.getType(null).colour);
            graphics.drawString(this.font, "" + move.getPWR(), xOffset + 166 - this.font.width("" + move.getPWR()),
                    yOffset + 22, 0xFFFFFF);
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

}
