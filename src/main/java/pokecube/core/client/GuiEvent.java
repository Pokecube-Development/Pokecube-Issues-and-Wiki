package pokecube.core.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class GuiEvent extends Event
{

    @Cancelable
    public static class RenderMoveMessages extends GuiEvent
    {
        final ElementType type;

        public RenderMoveMessages(final PoseStack mat, final ElementType type)
        {
            super(mat);
            this.type = type;
        }

        public ElementType getType()
        {
            return this.type;
        }

    }

    @Cancelable
    public static class RenderSelectedInfo extends GuiEvent
    {
        public RenderSelectedInfo(final PoseStack mat)
        {
            super(mat);
        }
    }

    @Cancelable
    public static class RenderTargetInfo extends GuiEvent
    {
        public RenderTargetInfo(final PoseStack mat)
        {
            super(mat);
        }
    }

    @Cancelable
    public static class RenderTeleports extends GuiEvent
    {
        public RenderTeleports(final PoseStack mat)
        {
            super(mat);
        }
    }

    public final PoseStack mat;

    public GuiEvent(final PoseStack mat)
    {
        this.mat = mat;
    }

}
