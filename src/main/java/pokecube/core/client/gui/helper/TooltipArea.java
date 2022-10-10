package pokecube.core.client.gui.helper;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TooltipArea extends AbstractWidget
{
    public interface OnTooltip
    {
        void onTooltip(TooltipArea area, PoseStack stack, int x, int y);

        default void narrateTooltip(Consumer<Component> p_168842_)
        {}
    }

    public interface DoTooltip
    {
        boolean doTooltip(int mx, int my);
    }

    private final DoTooltip doTooltip;
    private final OnTooltip onTooltip;

    public TooltipArea(int x, int y, int w, int h, Component name, DoTooltip doTooltip, OnTooltip onTooltip)
    {
        super(x, y, w, h, name);
        this.doTooltip = doTooltip;
        this.onTooltip = onTooltip;
    }

    @Override
    public void updateNarration(NarrationElementOutput naration)
    {
    }
    
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

    @Override
    public void renderButton(final PoseStack mat, final int mx, final int my, final float tick)
    {
        if (this.isHoveredOrFocused())
        {
            this.renderToolTip(mat, mx, my);
        }
    }

    @Override
    public void renderToolTip(PoseStack stack, int x, int y)
    {
        if (doTooltip.doTooltip(x, y)) onTooltip.onTooltip(this, stack, x, y);
    }
}
