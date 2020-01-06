package pokecube.core.client;

import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class GuiEvent extends Event
{

    @Cancelable
    public static class RenderMoveMessages extends GuiEvent
    {
        final ElementType type;

        public RenderMoveMessages(ElementType type)
        {
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

    }

    @Cancelable
    public static class RenderTargetInfo extends GuiEvent
    {

    }

    @Cancelable
    public static class RenderTeleports extends GuiEvent
    {

    }

    public GuiEvent()
    {
    }

}
