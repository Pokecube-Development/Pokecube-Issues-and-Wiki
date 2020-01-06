package pokecube.core.client.render.mobs;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.client.models.ModelPokemobEgg;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class RenderEgg extends LivingRenderer<EntityPokemobEgg, ModelPokemobEgg>
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(PokecubeCore.MODID, "textures/egg.png");

    public RenderEgg(final EntityRendererManager manager)
    {
        super(manager, new ModelPokemobEgg(), 0.1f);
    }

    @Override
    protected boolean canRenderName(final EntityPokemobEgg entity)
    {
        return false;
    }

    @Override
    public ResourceLocation getEntityTexture(final EntityPokemobEgg entity)
    {
        return RenderEgg.TEXTURE;
    }
}
