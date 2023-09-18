package thut.wearables.client.gui;

import java.awt.*;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thut.lib.TComponent;
import thut.wearables.ThutWearables;

public class GuiWearableButton extends Button
{
    public Supplier<Boolean> stillVisible = () -> true;

    public GuiWearableButton(final int xIn, final int yIn, final int widthIn, final int heightIn,
            final Component nameIn, final OnPress onPress, Screen parent)
    {
//      TODO: Fix tooltip
        super(xIn, yIn, widthIn, heightIn, nameIn, onPress, (CreateNarration) parent /*(b, pose, x, y) -> {
            Component tooltip = narration instanceof GuiWearables ? TComponent.translatable("wearables.gui.button.close")
                    : TComponent.translatable("wearables.gui.button.open");
            if (ThutWearables.config.buttonTooltip) narration.renderTooltip(pose, tooltip, x, y);
        }*/);
    }
//
//    TODO: Fix override
//    @Override
    protected int getYImage(final boolean hovered)
    {
        return hovered ? 237 : 247;
    }

    @Override
    public void render(GuiGraphics graphics, int p_93658_, int p_93659_, float p_93660_)
    {
        this.visible = this.active = stillVisible.get();
        super.render(graphics, p_93658_, p_93659_, p_93660_);
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int x, final int y, final float tick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GuiWearables.background);
        final int i = this.getYImage(this.isHoveredOrFocused());
        graphics.blit(GuiWearables.background, this.getX(), this.getY(), 0, i, this.width, this.height);
//      TODO: Fix tooltip
//        if (this.isHoveredOrFocused())
//        {
//            graphics.renderTooltip(Font, graphics, x, y);
//        }
    }
}