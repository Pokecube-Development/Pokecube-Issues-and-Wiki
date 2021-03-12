package pokecube.adventures.client.gui.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.extractor.ExtractorContainer;

public class Extractor extends ContainerScreen<ExtractorContainer>
{

    public Extractor(final ExtractorContainer screenContainer, final PlayerInventory inv, final ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(final MatrixStack mat, final float partialTicks, final int mouseX,
            final int mouseY)
    {
        this.minecraft.getTextureManager().bind(new ResourceLocation(PokecubeAdv.MODID,
                "textures/gui/extractor.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        this.blit(mat, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Draw the progress bar.
        this.blit(mat, x, y, 0, 0, this.imageWidth, this.imageHeight);
        final int i = this.menu.tile.progress;
        final int j = this.menu.tile.total;
        final int l1 = j != 0 && i != 0 ? i * 24 / j : 0;
        this.blit(mat, x + 78, y + 34, 176, 0, l1 + 1, 16);
    }

    @Override
    protected void renderLabels(final MatrixStack mat, final int mouseX, final int mouseY)
    {
        this.font.draw(mat, this.getTitle().getString(), 8, 6, 4210752);
        this.font.draw(mat, this.inventory.getName().getString(), 8, this.imageHeight - 96 + 2, 4210752);

        final ITextComponent warning0 = new TranslationTextComponent("gui.pokecube_adventures.cloner.warning_0");
        final ITextComponent warning1 = new TranslationTextComponent("gui.pokecube_adventures.cloner.warning_1");

        final int dx = 109;
        final int dy = 6;

        this.font.draw(mat, warning0.getString(), dx, dy, 4210752);
        mat.pushPose();
        final float s = 0.5f;
        mat.scale(s, s, s);
        this.font.draw(mat, warning1.getString(), dx / s, (dy + 10) / s, 4210752);
        mat.popPose();
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        this.renderTooltip(mat, mouseX, mouseY);
    }
}
