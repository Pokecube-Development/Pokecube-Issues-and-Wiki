package pokecube.core.client.gui.helper;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class TexButton extends Button
{
    public static final ITooltip NAMEONHOVOR = (button, matrixStack, mouseX, mouseY) ->
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final FontRenderer fontrenderer = minecraft.fontRenderer;
        final int j = button.getFGColor();
        // TODO decide if we want alpha as well?
        AbstractGui.drawCenteredString(matrixStack, fontrenderer, button.getMessage(), button.x + button.getWidth() / 2,
                button.y + (button.getHeightRealms() - 8) / 2, j | MathHelper.ceil(255.0F) << 24);
    };

    public static interface IntFunc
    {
        IntFunc DEFAULT = w -> 200 - w / 2;

        int apply(int in);
    }

    public ResourceLocation texture = Widget.WIDGETS_LOCATION;

    boolean renderName = true;

    int uOffset = 0;
    int vOffset = 46;
    int vSize   = 20;

    IntFunc uEnd = IntFunc.DEFAULT;

    public TexButton(final int x, final int y, final int width, final int height, final ITextComponent title,
            final IPressable pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
    }

    public TexButton(final int x, final int y, final int width, final int height, final ITextComponent title,
            final IPressable pressedAction, final ITooltip onTooltip)
    {
        super(x, y, width, height, title, pressedAction, onTooltip);
    }

    public TexButton setTex(final ResourceLocation texture)
    {
        this.texture = texture;
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
    public void renderButton(final MatrixStack matrixStack, final int mouseX, final int mouseY,
            final float partialTicks)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final FontRenderer fontrenderer = minecraft.fontRenderer;
        minecraft.getTextureManager().bindTexture(this.texture);
        final int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        //@formatter:off
        this.blit(matrixStack,
                this.x, this.y,
                this.uOffset, this.vOffset + i * this.vSize,
                this.width / 2, this.height);
        this.blit(matrixStack, this.x + this.width / 2, this.y,
                this.uEnd.apply(this.width), this.vOffset + i * this.vSize,
                this.width / 2, this.height);
        //@formatter:on
        this.renderBg(matrixStack, minecraft, mouseX, mouseY);
        final int j = this.getFGColor();
        if (this.renderName) AbstractGui.drawCenteredString(matrixStack, fontrenderer, this.getMessage(), this.x
                + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        if (this.isHovered()) this.renderToolTip(matrixStack, mouseX, mouseY);
    }
}
