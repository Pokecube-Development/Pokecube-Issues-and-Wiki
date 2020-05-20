package thut.crafts.client;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.ResourceLocation;
import thut.crafts.entity.EntityTest;

public class TestMobRender extends MobRenderer<EntityTest, EntityModel<EntityTest>>
{
    public TestMobRender(final EntityRendererManager manager)
    {
        super(manager, null, 0);
    }

    @Override
    public ResourceLocation getEntityTexture(final EntityTest entity)
    {
        return new ResourceLocation("derp");
    }

}
