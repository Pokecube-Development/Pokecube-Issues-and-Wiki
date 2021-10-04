package thut.wearables.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiWearableButton extends Button
{

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
    public void renderButton(final PoseStack mat, final int p_renderButton_1_, final int p_renderButton_2_,
            final float p_renderButton_3_)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindForSetup(GuiWearables.background);
        final int i = this.getYImage(this.isHovered());
        this.blit(mat, this.x, this.y, 0, i, this.width, this.height);
    }
}