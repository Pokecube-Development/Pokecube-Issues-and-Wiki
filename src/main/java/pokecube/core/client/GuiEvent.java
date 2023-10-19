package pokecube.core.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class GuiEvent extends Event
{

    @Cancelable
    public static class RenderMoveMessages extends GuiEvent
    {
        public RenderMoveMessages(final GuiGraphics mat, final ForgeGui gui)
        {
            super(mat, gui);
        }

    }

    @Cancelable
    public static class RenderSelectedInfo extends GuiEvent
    {
        public RenderSelectedInfo(final GuiGraphics mat, final ForgeGui gui)
        {
            super(mat, gui);
        }
    }

    @Cancelable
    public static class RenderTargetInfo extends GuiEvent
    {
        public RenderTargetInfo(final GuiGraphics mat, final ForgeGui gui)
        {
            super(mat, gui);
        }
    }

    @Cancelable
    public static class RenderTeleports extends GuiEvent
    {
        public RenderTeleports(final GuiGraphics mat, final ForgeGui gui)
        {
            super(mat, gui);
        }
    }

    private final PoseStack mat;
    private final ForgeGui gui;
    private final GuiGraphics grap;

    public GuiEvent(final GuiGraphics grap, final ForgeGui gui)
    {
        this.grap = grap;
        this.gui = gui;
        this.mat = grap.pose();
    }

    public ForgeGui getGui()
    {
        return this.gui;
    }

    public PoseStack getMat()
    {
        return this.mat;
    }

    public GuiGraphics getGraphics()
    {
        return grap;
    }

}
