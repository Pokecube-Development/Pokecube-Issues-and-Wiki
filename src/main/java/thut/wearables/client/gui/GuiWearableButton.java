package thut.wearables.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

public class GuiWearableButton extends Button
{

    public GuiWearableButton(final int xIn, final int yIn, final int widthIn, final int heightIn, final String nameIn,
            final IPressable onPress)
    {
        super(xIn, yIn, widthIn, heightIn, nameIn, onPress);
    }

    @Override
    protected int getYImage(final boolean hovored)
    {
        return hovored ? 237 : 247;
    }

    @Override
    public void renderButton(final int p_renderButton_1_, final int p_renderButton_2_, final float p_renderButton_3_)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(GuiWearables.background);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        final int i = this.getYImage(this.isHovered());
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.blit(this.x, this.y, 0, i, this.width, this.height);
    }
}