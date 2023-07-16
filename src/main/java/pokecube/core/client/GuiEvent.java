package pokecube.core.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class GuiEvent extends Event
{

    @Cancelable
    public static class RenderMoveMessages extends GuiEvent
    {
        public RenderMoveMessages(final PoseStack mat, final ForgeIngameGui gui)
        {
            super(mat, gui);
        }

    }

    @Cancelable
    public static class RenderSelectedInfo extends GuiEvent
    {
        public RenderSelectedInfo(final PoseStack mat, final ForgeIngameGui gui)
        {
            super(mat, gui);
        }
    }

    @Cancelable
    public static class RenderTargetInfo extends GuiEvent
    {
        public RenderTargetInfo(final PoseStack mat, final ForgeIngameGui gui)
        {
            super(mat, gui);
        }
    }

    @Cancelable
    public static class RenderTeleports extends GuiEvent
    {
        public RenderTeleports(final PoseStack mat, final ForgeIngameGui gui)
        {
            super(mat, gui);
        }
    }

    private final PoseStack      mat;
    private final ForgeIngameGui gui;

    public GuiEvent(final PoseStack mat, final ForgeIngameGui gui)
    {
        this.mat = mat;
        this.gui = gui;
    }

    public ForgeIngameGui getGui()
    {
        return this.gui;
    }

    public PoseStack getMat()
    {
        return this.mat;
    }

}
