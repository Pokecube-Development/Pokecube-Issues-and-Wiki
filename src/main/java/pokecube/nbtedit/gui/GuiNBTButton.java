package pokecube.nbtedit.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import pokecube.nbtedit.NBTStringHelper;
import thut.lib.TComponent;

public class GuiNBTButton extends Button
{

    public static final int WIDTH = 9, HEIGHT = 9;

    private final Minecraft mc = Minecraft.getInstance();

    private final byte id;

    private long hoverTime;

    public GuiNBTButton(final byte id, final int x, final int y, final OnPress onPress, CreateNarration narration)
    {
        super(x, y, GuiNBTButton.WIDTH, GuiNBTButton.HEIGHT, TComponent.translatable(""), onPress, narration);
        this.id = id;
    }

    public void drawToolTip(final GuiGraphics graphics, final int mx, final int my)
    {
        if (!(this.hoverTime != -1 && System.currentTimeMillis() - this.hoverTime > 300)) return;
        final String s = NBTStringHelper.getButtonName(this.id);
        final int width = this.mc.font.width(s);
        graphics.fill( mx + 4, my + 7, mx + 5 + width, my + 17, 0xff000000);
        // TODO: Fix this
        // this.mc.font.draw(graphics, s, mx + 5, my + 8, 0xffffff);
    }

    public byte getId()
    {
        return this.id;
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int mx, final int my, final float tick)
    {
        // check if the mouse is over the button
        if (this.isHoveredOrFocused())
        {
            // Draw a background
            graphics.fill(this.getX(), this.getY(), this.getX() + GuiNBTButton.WIDTH, this.getY() + GuiNBTButton.HEIGHT, 0x80ffffff);
            if (this.hoverTime == -1) this.hoverTime = System.currentTimeMillis();
        }
        else this.hoverTime = -1;

        // Draw the texture
        if (this.visible)
        {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GuiNBTNode.WIDGET_TEXTURE);
            // TODO: Check this
            graphics.blit(GuiNBTNode.WIDGET_TEXTURE, this.getX(), this.getY(), (this.id - 1) * 9, 18, GuiNBTButton.WIDTH, GuiNBTButton.HEIGHT);
        }
    }
}
