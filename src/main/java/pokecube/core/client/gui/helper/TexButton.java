package pokecube.core.client.gui.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.utils.Resources;

public class TexButton extends Button
{
    private static class Tooltip implements TooltipArea.OnTooltipB
    {
        @Override
        public void onTooltip(final Button button, final GuiGraphics graphics, final int x, final int y)
        {
            final Minecraft minecraft = Minecraft.getInstance();
            final Font fontrenderer = minecraft.font;
            final int j = button.getFGColor();
            graphics.drawCenteredString(fontrenderer, button.getMessage(), button.getX() + button.getWidth()
                    / 2, button.getY() + (button.getHeight() - 8) / 2, j | Mth.ceil(255.0F) << 24);
        }
    }

    public static final TooltipArea.OnTooltipB NAMEONHOVER = new Tooltip();

    public static class ShiftedTooltip implements TooltipArea.OnTooltipB
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
        public void onTooltip(final Button button, final GuiGraphics graphics, final int x, final int y)
        {
            final Minecraft minecraft = Minecraft.getInstance();
            final Font fontrenderer = minecraft.font;
            final int j = button.getFGColor();
            if (this.shadowed) graphics.drawCenteredString(fontrenderer, button.getMessage(), button.getX()
                    + this.dx, button.getY() + this.dy, j | this.alpha << 24);
            else
            {
                final String msg = button.getMessage().getString();
                final float dx = fontrenderer.width(msg) / 2f;
                // TODO: Fix this
                graphics.drawString(fontrenderer, msg, (int) (button.getX() + this.dx - dx), button.getY() + this.dy, j | this.alpha << 24);
            }
        }
    }

    public static interface IntFunc
    {
        IntFunc DEFAULT = w -> 200 - w / 2;

        int apply(int in);
    }

    public interface ImgRender
    {
        default void render(final TexButton button, final GuiGraphics graphics, final int mouseX, final int mouseY,
                final float partialTicks)
        {
            //@formatter:off
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            // TODO: Fix this
            final int i = button.getTextureY();
            graphics.blit(Resources.SLOT_ICON_CUBE, button.getX(), button.getY(),
                    button.uOffset, button.vOffset + i * button.vSize,
                    button.width / 2, button.height);
            graphics.blit(Resources.SLOT_ICON_CUBE,  button.getX() + button.width / 2, button.getY(),
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
        public void render(final TexButton button, final GuiGraphics graphics, final int mouseX, final int mouseY,
                final float partialTicks)
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            final int i = button.getTextureY();

            // TODO: Check this
            graphics.blit(new ResourceLocation(""), button.getX(), button.getY(), this.u, this.v + i * this.h, this.w, this.h);
        }
    }

    public ResourceLocation texture = AbstractWidget.WIDGETS_LOCATION;

    boolean renderName = true;

    int uOffset = 0;
    int vOffset = 46;
    int vSize   = 20;

    IntFunc uEnd = IntFunc.DEFAULT;

    ImgRender render = new ImgRender()
    {};
    protected final Button.OnPress onPress;
    protected final Button.CreateNarration createNarration;

    public static Button.Builder builder(Component component, Button.OnPress onPress) {
        return new Button.Builder(component, onPress);
    }

    public TexButton(final int x, final int y, final int width, final int height, final Component title,
            final OnPress pressedAction, Button.CreateNarration narration)
    {
        super(x, y, width, height, title, pressedAction, narration);
        this.onPress = pressedAction;
        this.createNarration = narration;
    }

    public TexButton(Builder builder)
    {
        this(builder.x, builder.y, builder.width, builder.height, builder.name, builder.onPress, builder.createNarration);
        setTooltip(builder.tooltip);
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

    public void renderBg(PoseStack stack, Minecraft minecraft, int mouseX, int mouseY) {
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final Font fontrenderer = minecraft.font;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);

        this.render.render(this, graphics, mouseX, mouseY, partialTicks);
        // TODO: Check this
        this.renderBg(graphics.pose(), minecraft, mouseX, mouseY);
        final int j = this.getFGColor();
        if (this.renderName)
        {
            final String msg = this.getMessage().getString();
            final float dx = fontrenderer.width(msg) / 2f;

            graphics.drawString(fontrenderer, msg, (int) (this.getX() + this.getWidth() / 2 - dx), this.getY() + (this.getHeight() - 8) / 2, j | 255 << 24);
        }
        // TODO: Fix this
        // if (this.isHoveredOrFocused()) this.renderBg(graphics.pose(), minecraft, mouseX, mouseY);
    }

    public void onPress() {
        this.onPress.onPress(this);
    }

    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> {
            return super.createNarrationMessage();
        });
    }

    public static final TexButton.CreateNarration DEFAULT_NARRATION = (supplier) -> {
        return supplier.get();
    };

    public void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        public final Component name;
        public final Button.OnPress onPress;
        @Nullable
        public net.minecraft.client.gui.components.Tooltip tooltip;
        public TooltipArea.OnTooltipB onTooltip;
        public int x;
        public int y;
        public int width = 150;
        public int height = 20;
        boolean renderName = true;
        public TexButton.CreateNarration createNarration;
        public ResourceLocation texture = AbstractWidget.WIDGETS_LOCATION;
        ImgRender render = new ImgRender()
        {};

        public Builder(Component name, Button.OnPress onPress) {
            this.createNarration = TexButton.DEFAULT_NARRATION;
            this.name = name;
            this.onPress = onPress;
        }

        public TexButton.Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public TexButton.Builder width(int width) {
            this.width = width;
            return this;
        }

        public TexButton.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public TexButton.Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public TexButton.Builder tooltip(@Nullable net.minecraft.client.gui.components.Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public TexButton.Builder onTooltip(@Nullable TooltipArea.OnTooltipB onTooltip) {
            this.onTooltip = onTooltip;
            return this;
        }

        public TexButton.Builder createNarration(TexButton.CreateNarration narration) {
            this.createNarration = narration;
            return this;
        }

        public TexButton.Builder setTex(final ResourceLocation texture)
        {
            this.texture = texture;
            return this;
        }

        public TexButton.Builder setRender(final ImgRender render)
        {
            this.render = render;
            return this;
        }

        public TexButton.Builder noName()
        {
            this.renderName = false;
            return this;
        }

        public TexButton build() {
            return this.build(TexButton::new);
        }

        public TexButton build(Function<TexButton.Builder, TexButton> builder) {
            return builder.apply(this);
        }
    }

//    @OnlyIn(Dist.CLIENT)
//    public interface OnPress {
//        void onPress(TexButton var1);
//    }

//    @OnlyIn(Dist.CLIENT)
//    public interface CreateNarration {
//        MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
//    }
}
