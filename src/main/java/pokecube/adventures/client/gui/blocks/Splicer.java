package pokecube.adventures.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.splicer.SplicerContainer;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class Splicer extends AbstractContainerScreen<SplicerContainer>
{
    public static ResourceLocation WIDGETS_GUI = new ResourceLocation(PokecubeMod.ID, Resources.TEXTURE_GUI_FOLDER + "widgets/pc_widgets.png");
    Button warningButton;

    public Splicer(final SplicerContainer screenContainer, final Inventory inv, final Component titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeAdv.MODID, "textures/gui/splicer.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        graphics.blit(new ResourceLocation(PokecubeAdv.MODID, "textures/gui/splicer.png"), x, y, 0, 0, this.imageWidth, this.imageHeight);

        // TODO: Fix - Draw the progress bar.
        graphics.blit(new ResourceLocation(PokecubeAdv.MODID, "textures/gui/splicer.png"), x, y, 0, 0, this.imageWidth, this.imageHeight);
        final int i = this.menu.tile.progress;
        final int j = this.menu.tile.total;
        final int l1 = j != 0 && i != 0 ? i * 24 / j : 0;
        graphics.blit(new ResourceLocation(PokecubeAdv.MODID, "textures/gui/splicer.png"), x + 79, y + 35, 176, 0, l1 + 1, 16);

        // Warning Button
        graphics.blit(WIDGETS_GUI, x + 155, y + 4, 50, 190, 17, 17);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY)
    {
        graphics.drawString(this.font, this.getTitle().getString(), 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(), 8,
                this.imageHeight - 94 + 2, 4210752, false);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;

        final Component warning0 = Component.translatable("gui.pokecube_adventures.cloner.warning_0");
        final Component warning1 = Component.translatable("gui.pokecube_adventures.cloner.warning_1");
        this.warningButton = this.addRenderableWidget(new Button.Builder(Component.literal(""), (b) -> {})
                .bounds(x + 155, y + 4, 17, 17)
                .tooltip(Tooltip.create(warning0, warning1))
                .createNarration(supplier -> Component.translatable("gui.pokecube_adventures.cloner.warning_0.narrate")).build());
        this.warningButton.active = false;
        this.warningButton.setAlpha(0);
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
