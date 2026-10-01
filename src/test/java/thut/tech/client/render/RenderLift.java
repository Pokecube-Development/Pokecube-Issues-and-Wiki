package thut.tech.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import thut.core.common.blockentity.render.RenderBlockEntity;
import thut.tech.common.entity.EntityLift;

public class RenderLift extends RenderBlockEntity<EntityLift>
{
    public RenderLift(final Context manager)
    {
        super(manager);
    }
}
