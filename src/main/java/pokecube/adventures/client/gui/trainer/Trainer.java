package pokecube.adventures.client.gui.trainer;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
import pokecube.core.utils.Resources;

public class Trainer extends AbstractContainerScreen<ContainerTrainer>
{
    public final static ResourceLocation TRAINER_GUI = new ResourceLocation(PokecubeAdv.MODID,
            Resources.TEXTURE_GUI_FOLDER + "trainergui.png");

    public Trainer(final ContainerTrainer screenContainer, final Inventory inv, final Component titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int x,
            final int y)
    {
        // bind texture
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Trainer.TRAINER_GUI);

        final int j2 = (this.width - this.imageWidth) / 2;
        final int k2 = (this.height - this.imageHeight) / 2;
        graphics.blit(Trainer.TRAINER_GUI, j2, k2, 0, 0, this.imageWidth, this.imageHeight);
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
