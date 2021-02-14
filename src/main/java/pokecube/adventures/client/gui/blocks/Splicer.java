package pokecube.adventures.client.gui.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.splicer.SplicerContainer;

public class Splicer extends ContainerScreen<SplicerContainer>
{

    public Splicer(final SplicerContainer screenContainer, final PlayerInventory inv, final ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack mat, final float partialTicks, final int mouseX, final int mouseY)
    {
        this.minecraft.getTextureManager().bindTexture(new ResourceLocation(PokecubeAdv.MODID,
                "textures/gui/splicer.png"));
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.blit(mat, x, y, 0, 0, this.xSize, this.ySize);

        // Draw the progress bar.
        this.blit(mat, x, y, 0, 0, this.xSize, this.ySize);
        final int i = this.container.tile.progress;
        final int j = this.container.tile.total;
        final int l1 = j != 0 && i != 0 ? i * 24 / j : 0;
        this.blit(mat, x + 78, y + 34, 176, 0, l1 + 1, 16);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final MatrixStack mat, final int mouseX, final int mouseY)
    {
        this.font.drawString(mat, this.getTitle().getString(), 8, 6, 4210752);
        this.font.drawString(mat, this.playerInventory.getName().getString(), 8, this.ySize - 96 + 2, 4210752);

        final ITextComponent warning0 = new TranslationTextComponent("gui.pokecube_adventures.cloner.warning_0");
        final ITextComponent warning1 = new TranslationTextComponent("gui.pokecube_adventures.cloner.warning_1");

        final int dx = 110;
        final int dy = 60;

        this.font.drawString(mat, warning0.getString(), dx, dy, 4210752);
        mat.push();
        final float s = 0.5f;
        mat.scale(s, s, s);
        this.font.drawString(mat, warning1.getString(), dx / s, (dy + 10) / s, 4210752);
        mat.pop();
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(mat, mouseX, mouseY);
    }
}
