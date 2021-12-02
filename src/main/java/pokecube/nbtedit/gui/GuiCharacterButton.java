package pokecube.nbtedit.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;

public class GuiCharacterButton extends Button
{
    public static final int WIDTH = 14, HEIGHT = 14;

    private final byte id;

    public GuiCharacterButton(final byte id, final int x, final int y, final OnPress onPress)
    {
        super(x, y, GuiCharacterButton.WIDTH, GuiCharacterButton.HEIGHT, new TranslatableComponent(""), onPress);
        this.id = id;
    }

    public byte getId()
    {
        return this.id;
    }

    @Override
    public void render(final PoseStack mat, final int mx, final int my, final float m)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GuiNBTNode.WIDGET_TEXTURE);

        if (this.isHoveredOrFocused()) GuiComponent.fill(mat, this.x, this.y, this.x + GuiCharacterButton.WIDTH, this.y
                + GuiCharacterButton.HEIGHT, 0x80ffffff);

        if (this.active) RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        else RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);

        GuiComponent.blit(mat, this.x, this.y, this.id * GuiCharacterButton.WIDTH, 27, GuiCharacterButton.WIDTH,
                GuiCharacterButton.HEIGHT, my, my, my, my);
    }
}
