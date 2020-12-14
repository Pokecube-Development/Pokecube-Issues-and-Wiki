package pokecube.pokeplayer.events;

import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.pokeplayer.Pokeplayer;

public class Render {

    public Render() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void preRenderPlayer(RenderPlayerEvent.Pre event) {
        event.getMatrixStack().push();
        event.getMatrixStack().scale(Pokeplayer.sizePercentage, Pokeplayer.sizePercentage, Pokeplayer.sizePercentage);
    }

    @SubscribeEvent
    public void postRenderPlayer(RenderPlayerEvent.Post event) {
        event.getMatrixStack().pop();
    }
}
