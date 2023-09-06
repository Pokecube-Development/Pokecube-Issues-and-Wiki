package pokecube.core.client.gui.helper;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TooltipArea extends AbstractWidget
{
    public interface OnTooltip
    {
        // TODO: Check this
        void onTooltip(TooltipArea area, GuiGraphics graphics, int x, int y);

        default void narrateTooltip(Consumer<Component> consumer)
        {}
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnTooltipB {
        void onTooltip(Button button, GuiGraphics graphics, int x, int y);

        default void narrateTooltip(Consumer<Component> p_168842_) {
        }
    }
    protected static final TooltipArea.CreateNarration DEFAULT_NARRATION = (p_253298_) -> {
        return p_253298_.get();
    };

    protected CreateNarration createNarration;

    public static TooltipArea.Builder builder(Component component, DoTooltip doTooltip, OnTooltip onTooltip) {
        return new TooltipArea.Builder(component, doTooltip, onTooltip);
    }

    public interface DoTooltip
    {
        boolean doTooltip(int mx, int my);
    }

    private DoTooltip doTooltip;
    private OnTooltip onTooltip;

    private boolean autoShow = true;

    public TooltipArea(int x, int y, int w, int h, Component name, DoTooltip doTooltip, OnTooltip onTooltip, TooltipArea.CreateNarration narration)
    {
        super(x, y, w, h, name);
        this.doTooltip = doTooltip;
        this.onTooltip = onTooltip;
        this.createNarration = narration;
    }

    public TooltipArea(AbstractWidget mask, Component name, DoTooltip doTooltip, OnTooltip onTooltip)
    {
        super(mask.getX(), mask.getY(), mask.getWidth(), mask.getHeight(), name);
        this.doTooltip = doTooltip;
        this.onTooltip = onTooltip;
    }

    public TooltipArea(Builder builder)
    {
        this(builder.x, builder.y, builder.width, builder.height, builder.name, builder.doTooltip, builder.onTooltip, builder.createNarration);
        this.setTooltip(builder.tooltip);
    }

    /**
     * Call this to disable automatic tooltip rendering. This is useful if you
     * have slots that overlap the tooltip area, to prevent their highlights
     * appearing. If you call this, you must manually call renderToolTip!
     * 
     * @return this
     */
    public TooltipArea noAuto()
    {
        autoShow = false;
        return this;
    }

    public boolean autoRenders()
    {
        return autoShow;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narration)
    {}

    @Override
    protected boolean clicked(double x, double y)
    {
        // We don't do anything when clicked.
        return false;
    }

    @Override
    protected boolean isValidClickButton(int button)
    {
        // No valid click buttons for us.
        return false;
    }

    // TODO: Check this
    @Override
    public void render(final GuiGraphics graphics, final int mx, final int my, final float tick)
    {
        if (autoShow) this.renderWidget(graphics, mx, my, tick);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int x, int y, final float tick)
    {
        if (this.isHoveredOrFocused() && doTooltip.doTooltip(x, y)) onTooltip.onTooltip(this, graphics, x, y);
    }

    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> {
            return super.createNarrationMessage();
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private DoTooltip doTooltip;
        private OnTooltip onTooltip;
        public final Component name;
        @Nullable
        public Tooltip tooltip;
        public int x;
        public int y;
        public int width = 150;
        public int height = 20;
        public CreateNarration createNarration;

        public Builder(Component name, DoTooltip doTooltip, OnTooltip onTooltip) {
            this.createNarration = TooltipArea.DEFAULT_NARRATION;
            this.name = name;
            this.doTooltip = doTooltip;
            this.onTooltip = onTooltip;
        }

        public TooltipArea.Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public TooltipArea.Builder width(int width) {
            this.width = width;
            return this;
        }

        public TooltipArea.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public TooltipArea.Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public TooltipArea.Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public TooltipArea.Builder createNarration(TooltipArea.CreateNarration narration) {
            this.createNarration = narration;
            return this;
        }

        public TooltipArea build() {
            return this.build(TooltipArea::new);
        }

        public TooltipArea build(Function<Builder, TooltipArea> builder) {
            return (TooltipArea)builder.apply(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
    }
}
