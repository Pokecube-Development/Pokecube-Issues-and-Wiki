package pokecube.nbtedit.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.nbtedit.NBTStringHelper;

public class GuiNBTButton extends Button
{

    public static final int WIDTH = 9, HEIGHT = 9;

    private final Minecraft mc = Minecraft.getInstance();

    private final byte id;

    private long hoverTime;

    public GuiNBTButton(final byte id, final int x, final int y, final IPressable onPress)
    {
        super(x, y, GuiNBTButton.WIDTH, GuiNBTButton.HEIGHT, new TranslationTextComponent(""), onPress);
        this.id = id;
    }

    public void drawToolTip(final MatrixStack mat, final int mx, final int my)
    {
        if (!(this.hoverTime != -1 && System.currentTimeMillis() - this.hoverTime > 300)) return;
        final String s = NBTStringHelper.getButtonName(this.id);
        final int width = this.mc.fontRenderer.getStringWidth(s);
        AbstractGui.fill(mat, mx + 4, my + 7, mx + 5 + width, my + 17, 0xff000000);
        this.mc.fontRenderer.drawString(mat, s, mx + 5, my + 8, 0xffffff);
    }

    public byte getId()
    {
        return this.id;
    }

    @Override
    public void renderButton(final MatrixStack mat, final int mx, final int my, final float tick)
    {
        // check if the mouse is over the button
        if (this.isHovered())
        {
            // Draw a background
            AbstractGui.fill(mat, this.x, this.y, this.x + GuiNBTButton.WIDTH, this.y + GuiNBTButton.HEIGHT, 0x80ffffff);
            if (this.hoverTime == -1) this.hoverTime = System.currentTimeMillis();
        }
        else this.hoverTime = -1;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // Draw the texture
        if (this.visible)
        {
            this.mc.getTextureManager().bindTexture(GuiNBTNode.WIDGET_TEXTURE);
            this.blit(mat, this.x, this.y, (this.id - 1) * 9, 18, GuiNBTButton.WIDTH, GuiNBTButton.HEIGHT);
        }
    }
}
