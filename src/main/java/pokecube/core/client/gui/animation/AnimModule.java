package pokecube.core.client.gui.animation;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.components.AbstractWidget;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.client.gui.helper.ListEditBox;

public abstract class AnimModule
{
    protected final AnimationGui parent;

    protected List<AbstractWidget> ours = Lists.newArrayList();

    public boolean active = false;

    public AnimModule(AnimationGui parent)
    {
        this.parent = parent;
    }

    public void setEnabled(boolean active)
    {
        this.active = active;
        ours.forEach(widget -> {
            widget.visible = active;
            if (active && widget instanceof ListEditBox box && box.preFocusGain == null)
            {
                box.registerPreFocus(parent);
            }
        });
    }

    public <T extends AbstractWidget> T addRenderableWidget(T widget)
    {
        parent.addRenderableWidget(widget);
        this.ours.add(widget);
        return widget;
    }

    public abstract void init();

    public void onUpdated()
    {}

    public void preRender()
    {}

    public void postRender()
    {}

    public boolean isPauseScreen()
    {
        return false;
    }

    public boolean updateOnButtonPress(int code)
    {
        return false;
    }
}
