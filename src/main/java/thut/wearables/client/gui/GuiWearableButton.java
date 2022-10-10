package thut.wearables.client.gui;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import thut.lib.TComponent;
import thut.wearables.ThutWearables;

public class GuiWearableButton extends Button
{
    public Supplier<Boolean> stillVisible = () -> true;

    public GuiWearableButton(final int xIn, final int yIn, final int widthIn, final int heightIn,
            final Component nameIn, final OnPress onPress, Screen parent)
    {
        super(xIn, yIn, widthIn, heightIn, nameIn, onPress, (b, pose, x, y) -> {
            Component tooltip = parent instanceof GuiWearables ? TComponent.translatable("wearables.gui.button.close")
                    : TComponent.translatable("wearables.gui.button.open");
            if (ThutWearables.config.buttonTooltip) parent.renderTooltip(pose, tooltip, x, y);
        });
    }

    @Override
    protected int getYImage(final boolean hovored)
    {
        return hovored ? 237 : 247;
    }

    @Override
    public void render(PoseStack stack, int p_93658_, int p_93659_, float p_93660_)
    {
        this.visible = this.active = stillVisible.get();
        super.render(stack, p_93658_, p_93659_, p_93660_);
    }

    @Override
    public void renderButton(final PoseStack mat, final int x, final int y, final float tick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GuiWearables.background);
        final int i = this.getYImage(this.isHoveredOrFocused());
        this.blit(mat, this.x, this.y, 0, i, this.width, this.height);
        if (this.isHoveredOrFocused())
        {
            this.renderToolTip(mat, x, y);
        }
    }
}