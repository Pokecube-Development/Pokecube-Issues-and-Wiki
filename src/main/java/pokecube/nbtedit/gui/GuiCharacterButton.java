package pokecube.nbtedit.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiCharacterButton extends Button
{
    public static final int WIDTH = 14, HEIGHT = 14;

    private final byte id;

    public GuiCharacterButton(final byte id, final int x, final int y, final IPressable onPress)
    {
        super(x, y, GuiCharacterButton.WIDTH, GuiCharacterButton.HEIGHT, new TranslationTextComponent(""), onPress);
        this.id = id;
    }

    public byte getId()
    {
        return this.id;
    }

    @Override
    public void render(final MatrixStack mat, final int mx, final int my, final float m)
    {
        Minecraft.getInstance().getTextureManager().bindTexture(GuiNBTNode.WIDGET_TEXTURE);
        if (this.isHovered()) AbstractGui.fill(mat, this.x, this.y, this.x + GuiCharacterButton.WIDTH, this.y
                + GuiCharacterButton.HEIGHT, 0x80ffffff);

        if (this.active) GL11.glColor4f(1, 1, 1, 1);
        else GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);

        AbstractGui.blit(mat, this.x, this.y, this.id * GuiCharacterButton.WIDTH, 27, GuiCharacterButton.WIDTH,
                GuiCharacterButton.HEIGHT, my, my, my, my);
    }
}
