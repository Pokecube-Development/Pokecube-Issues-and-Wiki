package thut.wearables.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public class GuiWearableButton extends Button
{
    public CreativeModeInventoryScreen gui = null;

    public GuiWearableButton(final int xIn, final int yIn, final int widthIn, final int heightIn,
            final Component nameIn, final OnPress onPress)
    {
        super(xIn, yIn, widthIn, heightIn, nameIn, onPress);
    }

    @Override
    protected int getYImage(final boolean hovored)
    {
        return hovored ? 237 : 247;
    }

    @Override
    public void render(PoseStack stack, int p_93658_, int p_93659_, float p_93660_)
    {
        if (gui != null)
        {
            this.visible = this.active = gui.getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId();
        }
        super.render(stack, p_93658_, p_93659_, p_93660_);
    }

    @Override
    public void renderButton(final PoseStack mat, final int p_renderButton_1_, final int p_renderButton_2_,
            final float p_renderButton_3_)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GuiWearables.background);
        final int i = this.getYImage(this.isHoveredOrFocused());
        this.blit(mat, this.x, this.y, 0, i, this.width, this.height);
    }
}