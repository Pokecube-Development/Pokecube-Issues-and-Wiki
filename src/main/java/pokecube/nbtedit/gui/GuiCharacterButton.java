package pokecube.nbtedit.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;

public class GuiCharacterButton extends Button
{
    public static final int WIDTH = 14, HEIGHT = 14;

    private final byte id;

    public GuiCharacterButton(final byte id, final int x, final int y, final IPressable onPress)
    {
        super(x, y, GuiCharacterButton.WIDTH, GuiCharacterButton.HEIGHT, "", onPress);
        this.id = id;
    }

    public byte getId()
    {
        return this.id;
    }

    @Override
    public void render(final int mx, final int my, final float m)
    {
        Minecraft.getInstance().getTextureManager().bindTexture(GuiNBTNode.WIDGET_TEXTURE);
        if (this.isHovered()) AbstractGui.fill(this.x, this.y, this.x + GuiCharacterButton.WIDTH, this.y
                + GuiCharacterButton.HEIGHT, 0x80ffffff);

        if (this.active) RenderSystem.color4f(1, 1, 1, 1);
        else RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);

        this.blit(this.x, this.y, this.id * GuiCharacterButton.WIDTH, 27, GuiCharacterButton.WIDTH,
                GuiCharacterButton.HEIGHT);
    }
}
