package pokecube.adventures.client.gui.blocks;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.cloner.ClonerContainer;

public class Cloner extends AbstractContainerScreen<ClonerContainer>
{

    public Cloner(final ClonerContainer screenContainer, final Inventory inv, final Component titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(final PoseStack mat, final float partialTicks, final int mouseX, final int mouseY)
    {
        this.minecraft.getTextureManager().bindForSetup(new ResourceLocation(
                PokecubeAdv.MODID,
                "textures/gui/cloner.png"));
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        this.blit(mat, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Draw the progress bar.
        this.blit(mat, x, y, 0, 0, this.imageWidth, this.imageHeight);
        final int i = this.menu.tile.progress;
        final int j = this.menu.tile.total;
        final int l1 = j != 0 && i != 0 ? i * 24 / j : 0;
        this.blit(mat, x + 89, y + 34, 176, 0, l1 + 1, 16);
    }

    @Override
    protected void renderLabels(final PoseStack mat, final int mouseX, final int mouseY)
    {
        this.font.draw(mat, this.getTitle().getString(), 8, 6, 4210752);
        this.font.draw(mat, this.playerInventoryTitle.getString(), 8, this.imageHeight - 96 + 2, 4210752);

        final Component warning0 = new TranslatableComponent("gui.pokecube_adventures.cloner.warning_0");
        final Component warning1 = new TranslatableComponent("gui.pokecube_adventures.cloner.warning_1");

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
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat,mouseX, mouseY, partialTicks);
        this.renderTooltip(mat,mouseX, mouseY);
    }
}
