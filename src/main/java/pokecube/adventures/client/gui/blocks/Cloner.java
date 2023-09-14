package pokecube.adventures.client.gui.blocks;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.cloner.ClonerContainer;
import pokecube.core.PokecubeCore;
import thut.lib.TComponent;

public class Cloner extends AbstractContainerScreen<ClonerContainer>
{

    public Cloner(final ClonerContainer screenContainer, final Inventory inv, final Component titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeAdv.MODID, "textures/gui/cloner.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        graphics.blit(new ResourceLocation(PokecubeAdv.MODID, "textures/gui/cloner.png"), x, y, 0, 0, this.imageWidth, this.imageHeight);

        // TODO: Fix - Draw the progress bar.
        graphics.blit(new ResourceLocation(PokecubeAdv.MODID, "textures/gui/cloner.png"), x, y, 0, 0, this.imageWidth, this.imageHeight);
        final int i = this.menu.tile.progress;
        final int j = this.menu.tile.total;
        final int l1 = j != 0 && i != 0 ? i * 24 / j : 0;
        // TODO: correct texture?
        graphics.blit(new ResourceLocation(PokecubeAdv.MODID, "textures/gui/cloner.png"), x + 89, y + 34, 176, 0, l1 + 1, 16);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY)
    {
        graphics.drawString(this.font, this.getTitle().getString(), 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle.getString(),
                8, this.imageHeight - 96 + 2, 4210752, false);

        final Component warning0 = TComponent.translatable("gui.pokecube_adventures.cloner.warning_0");
        final Component warning1 = TComponent.translatable("gui.pokecube_adventures.cloner.warning_1");

        final float s = 0.5f;
        final int dx = 172 - this.font.width(warning0.getString());
        final int dx1 = (int) (((dx + 41) / s) - this.font.width(warning1.getString()));
        final int dy = 6;

        graphics.drawString(this.font, warning0.getString(), dx, dy, 0xbf1e0b, false);
        graphics.pose().pushPose();
        graphics.pose().scale(s, s, s);
        graphics.drawString(this.font, warning1.getString(), dx1, (int) ((dy + 10) / s), 0xbf1e0b, false);
        graphics.pose().popPose();
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
