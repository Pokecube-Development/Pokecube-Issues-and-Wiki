package pokecube.core.client.render.mobs;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import pokecube.core.PokecubeItems;
import pokecube.core.client.render.mobs.RenderPokecube.ModelPokecube;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;

public class RenderPokecube extends LivingRenderer<EntityPokecube, ModelPokecube>
{
    public static class ModelPokecube extends EntityModel<EntityPokecube>
    {

        public ModelPokecube()
        {
        }

        @Override
        public void render(final EntityPokecube entityIn, final float limbSwing, final float limbSwingAmount,
                final float ageInTicks, final float netHeadYaw, final float headPitch, float scale)
        {
            GL11.glPushMatrix();
            GL11.glTranslated(-0.0, 1.4, -0.0);
            scale = 0.25f;
            GL11.glScalef(scale, scale, scale);
            GL11.glColor4f(1, 1, 1, 1f);
            GL11.glRotated(180, 0, 0, 1);

            final EntityPokecube cube = entityIn;

            if (cube.isReleasing())
            {
                final Entity mob = cube.getReleased();
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                if (pokemob != null)
                {
                    // TODO exit cube effects.
                    // RenderPokemob.renderEffect(pokemob, f2 -
                    // cube.ticksExisted, 40,
                    // false);
                }
            }

            if (PokecubeManager.getTilt(cube.getItem()) > 0)
            {
                final float rotateY = MathHelper.cos(MathHelper.abs((float) (Math.PI * ageInTicks) / 12)) * (180F
                        / (float) Math.PI);
                GL11.glRotatef(rotateY, 0.0F, 0.0F, 1.0F);
            }
            ItemStack renderStack = cube.getItem();
            if (renderStack == null || !(renderStack.getItem() instanceof IPokecube))
                renderStack = PokecubeItems.POKECUBE_CUBES;

            Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            final IBakedModel model = Minecraft.getInstance().getItemRenderer().getModelWithOverrides(renderStack);
            Minecraft.getInstance().getItemRenderer().renderItem(renderStack, model);

            GL11.glPopMatrix();
        }
    }

    public static HashMap<ResourceLocation, EntityRenderer<EntityPokecube>> pokecubeRenderers = new HashMap<>();

    public RenderPokecube(final EntityRendererManager renderManager)
    {
        super(renderManager, new ModelPokecube(), 0);
    }

    @Override
    protected boolean canRenderName(final EntityPokecube entity)
    {
        return false;
    }

    @Override
    public void doRender(final EntityPokecube entity, final double x, final double y, final double z,
            final float entityYaw, final float partialTicks)
    {
        final long time = entity.reset;
        final long world = entity.getEntityWorld().getGameTime();
        if (time > world) return;

        final ResourceLocation num = PokecubeItems.getCubeId(entity.getItem());
        if (RenderPokecube.pokecubeRenderers.containsKey(num))
        {
            RenderPokecube.pokecubeRenderers.get(num).doRender(entity, x, y, z, entityYaw, partialTicks);
            return;
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public ResourceLocation getEntityTexture(final EntityPokecube entity)
    {
        return new ResourceLocation(PokecubeMod.ID, "textures/items/pokecubefront.png");
    }

}
