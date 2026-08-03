package pokecube.api.events;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class RegisterCubeRenderer extends Event implements ICancellableEvent
{
    public final EntityRendererProvider.Context renderManager;

    public RegisterCubeRenderer(EntityRendererProvider.Context renderManager) {this.renderManager = renderManager;}
}
