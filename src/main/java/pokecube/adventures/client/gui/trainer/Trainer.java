package pokecube.adventures.client.gui.trainer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
import pokecube.core.client.Resources;

public class Trainer extends ContainerScreen<ContainerTrainer>
{

    public Trainer(final ContainerTrainer screenContainer, final PlayerInventory inv, final ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack matrixStack, final float partialTicks, final int x,
            final int y)
    {
        // bind texture
        this.minecraft.getTextureManager().bindTexture(Resources.GUI_HEAL_TABLE);
        final int j2 = (this.width - this.xSize) / 2;
        final int k2 = (this.height - this.ySize) / 2;
        this.blit(matrixStack, j2, k2, 0, 0, this.xSize, this.ySize);
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
