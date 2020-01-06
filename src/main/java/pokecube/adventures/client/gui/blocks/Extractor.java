package pokecube.adventures.client.gui.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.extractor.ExtractorContainer;

public class Extractor extends ContainerScreen<ExtractorContainer>
{

    public Extractor(final ExtractorContainer screenContainer, final PlayerInventory inv, final ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GL11.glPushMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
        this.minecraft.getTextureManager().bindTexture(new ResourceLocation(PokecubeAdv.ID,
                "textures/gui/extractor.png"));
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);

        // Draw the progress bar.
        this.blit(x, y, 0, 0, this.xSize, this.ySize);
        final int i = this.container.tile.progress.get();
        final int j = this.container.tile.total.get();
        final int l1 = j != 0 && i != 0 ? i * 24 / j : 0;
        this.blit(x + 78, y + 34, 176, 0, l1 + 1, 16);

        GL11.glPopMatrix();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        this.font.drawString(this.getTitle().getFormattedText(), 8, 6, 4210752);
        this.font.drawString(this.playerInventory.getName().getFormattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
