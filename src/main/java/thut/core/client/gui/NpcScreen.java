package thut.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import thut.api.inventory.npc.NpcContainer;

public class NpcScreen extends AbstractContainerScreen<NpcContainer>
{
    public NpcScreen(final NpcContainer screenContainer, final Inventory inv, final Component titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(final PoseStack matrixStack, final float partialTicks, final int x, final int y)
    {
        // bind texture
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, menu.getTexture());

        final int j2 = (this.width - this.imageWidth) / 2;
        final int k2 = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, j2, k2, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    /** Draws the screen and all the components in it. */
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.renderBackground(mat);
        super.render(mat, mouseX, mouseY, partialTicks);
        this.renderTooltip(mat, mouseX, mouseY);
    }

}