package pokecube.nbtedit.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiCharacterButton extends Button
{
    public static final int WIDTH = 14, HEIGHT = 14;

    private final byte id;

    public GuiCharacterButton(final byte id, final int x, final int y, final OnPress onPress, CreateNarration narration)
    {
        super(x, y, GuiCharacterButton.WIDTH, GuiCharacterButton.HEIGHT, Component.literal(""), onPress, narration);
        this.id = id;
    }

    public byte getId()
    {
        return this.id;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mx, final int my, final float m)
    {
        if (this.isHoveredOrFocused()) graphics.fill(this.getX(), this.getY(), this.getX() + GuiCharacterButton.WIDTH,
                this.getY() + GuiCharacterButton.HEIGHT, 0x80ffffff);

        if (this.active) RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        else RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);

        graphics.blit(GuiNBTNode.WIDGET_TEXTURE, this.getX(), this.getY(), this.id * GuiCharacterButton.WIDTH, 27,
                GuiCharacterButton.WIDTH, GuiCharacterButton.HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
