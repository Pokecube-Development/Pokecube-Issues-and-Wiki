package pokecube.core.client.gui.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TexButton extends Button
{
    private static class Tooltip implements OnTooltip
    {
        @Override
        public void onTooltip(final Button button, final PoseStack matrixStack, final int mouseX, final int mouseY)
        {
            final Minecraft minecraft = Minecraft.getInstance();
            final Font fontrenderer = minecraft.font;
            final int j = button.getFGColor();
            // TODO decide if we want alpha as well?
            GuiComponent.drawCenteredString(matrixStack, fontrenderer, button.getMessage(), button.x + button.getWidth()
                    / 2, button.y + (button.getHeight() - 8) / 2, j | Mth.ceil(255.0F) << 24);
        }
    }

    public static final OnTooltip NAMEONHOVER = new Tooltip();

    public static class ShiftedTooltip implements OnTooltip
    {
        int dx;
        int dy;
        int alpha = 255;

        boolean shadowed = true;

        public ShiftedTooltip(final int dx, final int dy, final int alpha)
        {
            this.dx = dx;
            this.dy = dy;
            this.alpha = alpha;
        }

        public ShiftedTooltip(final int dx, final int dy)
        {
            this(dx, dy, 255);
        }

        public ShiftedTooltip noShadow()
        {
            this.shadowed = false;
            return this;
        }

        @Override
        public void onTooltip(final Button button, final PoseStack matrixStack, final int mouseX, final int mouseY)
        {
            final Minecraft minecraft = Minecraft.getInstance();
            final Font fontrenderer = minecraft.font;
            final int j = button.getFGColor();
            if (this.shadowed) GuiComponent.drawCenteredString(matrixStack, fontrenderer, button.getMessage(), button.x
                    + this.dx, button.y + this.dy, j | this.alpha << 24);
            else
            {
                final String msg = button.getMessage().getString();
                final float dx = fontrenderer.width(msg) / 2f;
                fontrenderer.draw(matrixStack, msg, button.x + this.dx - dx, button.y + this.dy, j | this.alpha << 24);
            }
        }
    }

    public static interface IntFunc
    {
        IntFunc DEFAULT = w -> 200 - w / 2;

        int apply(int in);
    }

    public static interface ImgRender
    {
        default void render(final TexButton button, final PoseStack matrixStack, final int mouseX, final int mouseY,
                final float partialTicks)
        {
            //@formatter:off
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            final int i = button.getYImage(button.isHovered());
            button.blit(matrixStack,
                    button.x, button.y,
                    button.uOffset, button.vOffset + i * button.vSize,
                    button.width / 2, button.height);
            button.blit(matrixStack, button.x + button.width / 2, button.y,
                    button.uEnd.apply(button.width), button.vOffset + i * button.vSize,
                    button.width / 2, button.height);
            //@formatter:on
        }
    }

    public static class UVImgRender implements ImgRender
    {
        int u;
        int v;
        int w;
        int h;

        public UVImgRender(final int u, final int v, final int w, final int h)
        {
            this.u = u;
            this.v = v;
            this.w = w;
            this.h = h;
        }

        @Override
        public void render(final TexButton button, final PoseStack matrixStack, final int mouseX, final int mouseY,
                final float partialTicks)
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            final int i = button.getYImage(button.isHovered());
            button.blit(matrixStack, button.x, button.y, this.u, this.v + i * this.h, this.w, this.h);
        }
    }

    public ResourceLocation texture = AbstractWidget.WIDGETS_LOCATION;

    boolean renderName = true;

    int uOffset = 0;
    int vOffset = 46;
    int vSize   = 20;

    IntFunc uEnd = IntFunc.DEFAULT;

    ImgRender render = new ImgRender()
    {
    };

    public TexButton(final int x, final int y, final int width, final int height, final Component title,
            final OnPress pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
    }

    public TexButton(final int x, final int y, final int width, final int height, final Component title,
            final OnPress pressedAction, final OnTooltip onTooltip)
    {
        super(x, y, width, height, title, pressedAction, onTooltip);
    }

    public TexButton setTex(final ResourceLocation texture)
    {
        this.texture = texture;
        return this;
    }

    public TexButton setRender(final ImgRender render)
    {
        this.render = render;
        return this;
    }

    public TexButton noName()
    {
        this.renderName = false;
        return this;
    }

    public TexButton sized(final int uOffset, final int vOffset, final int vSize)
    {
        return this.sized(uOffset, vOffset, vSize, this.uEnd);
    }

    public TexButton sized(final int uOffset, final int vOffset, final int vSize, final IntFunc uEnd)
    {
        this.uEnd = uEnd;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.vSize = vSize;
        return this;
    }

    @Override
    public void renderButton(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final Font fontrenderer = minecraft.font;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);

        this.render.render(this, matrixStack, mouseX, mouseY, partialTicks);
        this.renderBg(matrixStack, minecraft, mouseX, mouseY);
        final int j = this.getFGColor();
        if (this.renderName)
        {
            final String msg = this.getMessage().getString();
            final float dx = fontrenderer.width(msg) / 2f;
            fontrenderer.draw(matrixStack, msg, this.x + this.getWidth() / 2 - dx, this.y + (this.getHeight() - 8) / 2,
                    j | 255 << 24);
        }
        if (this.isHovered()) this.renderToolTip(matrixStack, mouseX, mouseY);
    }
}
