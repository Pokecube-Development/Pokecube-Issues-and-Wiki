package pokecube.adventures.client.gui.trainer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
import pokecube.core.client.Resources;

public class Trainer extends ContainerScreen<ContainerTrainer>
{
    public final static ResourceLocation TRAINER_GUI = new ResourceLocation(PokecubeAdv.MODID,
            Resources.TEXTURE_GUI_FOLDER + "trainergui.png");

    public Trainer(final ContainerTrainer screenContainer, final PlayerInventory inv, final ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(final MatrixStack matrixStack, final float partialTicks, final int x,
            final int y)
    {
        // bind texture
        this.minecraft.getTextureManager().bind(Trainer.TRAINER_GUI);
        final int j2 = (this.width - this.imageWidth) / 2;
        final int k2 = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, j2, k2, 0, 0, this.imageWidth, this.imageHeight);
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
