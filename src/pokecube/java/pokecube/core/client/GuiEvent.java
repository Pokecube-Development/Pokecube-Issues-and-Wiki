package pokecube.core.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class GuiEvent extends Event
{

    public static class RenderMoveMessages extends GuiEvent implements ICancellableEvent
    {
        public RenderMoveMessages(final GuiGraphics mat, final DeltaTracker deltas)
        {
            super(mat, deltas);
        }

    }

    public static class RenderSelectedInfo extends GuiEvent implements ICancellableEvent
    {
        public RenderSelectedInfo(final GuiGraphics mat, final DeltaTracker deltas)
        {
            super(mat, deltas);
        }
    }

    public static class RenderTargetInfo extends GuiEvent implements ICancellableEvent
    {
        public RenderTargetInfo(final GuiGraphics mat, final DeltaTracker deltas)
        {
            super(mat, deltas);
        }
    }

    public static class RenderTeleports extends GuiEvent implements ICancellableEvent
    {
        public RenderTeleports(final GuiGraphics mat, final DeltaTracker deltas)
        {
            super(mat, deltas);
        }
    }

    private final PoseStack mat;
    private final DeltaTracker deltas;
    private final GuiGraphics grap;
    private final float tick;

    public GuiEvent(final GuiGraphics grap, final DeltaTracker deltas)
    {
        this.grap = grap;
        this.deltas = deltas;
        this.mat = grap.pose();
        this.tick = deltas.getGameTimeDeltaPartialTick(true);
    }

    public DeltaTracker getGui()
    {
        return this.deltas;
    }

    public PoseStack getMat()
    {
        return this.mat;
    }

    public GuiGraphics getGraphics()
    {
        return grap;
    }

    public float getTick()
    {
        return tick;
    }

}
