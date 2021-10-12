package pokecube.nbtedit.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import pokecube.nbtedit.NBTStringHelper;

public class GuiNBTButton extends Button
{

    public static final int WIDTH = 9, HEIGHT = 9;

    private final Minecraft mc = Minecraft.getInstance();

    private final byte id;

    private long hoverTime;

    public GuiNBTButton(final byte id, final int x, final int y, final OnPress onPress)
    {
        super(x, y, GuiNBTButton.WIDTH, GuiNBTButton.HEIGHT, new TranslatableComponent(""), onPress);
        this.id = id;
    }

    public void drawToolTip(final PoseStack mat, final int mx, final int my)
    {
        if (!(this.hoverTime != -1 && System.currentTimeMillis() - this.hoverTime > 300)) return;
        final String s = NBTStringHelper.getButtonName(this.id);
        final int width = this.mc.font.width(s);
        GuiComponent.fill(mat, mx + 4, my + 7, mx + 5 + width, my + 17, 0xff000000);
        this.mc.font.draw(mat, s, mx + 5, my + 8, 0xffffff);
    }

    public byte getId()
    {
        return this.id;
    }

    @Override
    public void renderButton(final PoseStack mat, final int mx, final int my, final float tick)
    {
        // check if the mouse is over the button
        if (this.isHovered())
        {
            // Draw a background
            GuiComponent.fill(mat, this.x, this.y, this.x + GuiNBTButton.WIDTH, this.y + GuiNBTButton.HEIGHT, 0x80ffffff);
            if (this.hoverTime == -1) this.hoverTime = System.currentTimeMillis();
        }
        else this.hoverTime = -1;

        // Draw the texture
        if (this.visible)
        {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GuiNBTNode.WIDGET_TEXTURE);
            this.blit(mat, this.x, this.y, (this.id - 1) * 9, 18, GuiNBTButton.WIDTH, GuiNBTButton.HEIGHT);
        }
    }
}
