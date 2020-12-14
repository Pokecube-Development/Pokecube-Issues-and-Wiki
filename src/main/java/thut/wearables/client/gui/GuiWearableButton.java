package thut.wearables.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class GuiWearableButton extends Button
{

    public GuiWearableButton(final int xIn, final int yIn, final int widthIn, final int heightIn,
            final ITextComponent nameIn, final IPressable onPress)
    {
        super(xIn, yIn, widthIn, heightIn, nameIn, onPress);
    }

    @Override
    protected int getYImage(final boolean hovored)
    {
        return hovored ? 237 : 247;
    }

    @Override
    public void renderButton(final MatrixStack mat, final int p_renderButton_1_, final int p_renderButton_2_,
            final float p_renderButton_3_)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(GuiWearables.background);
        final int i = this.getYImage(this.isHovered());
        this.blit(mat, this.x, this.y, 0, i, this.width, this.height);
    }
}