package pokecube.core.client.render.mobs;

import java.util.HashMap;
import java.util.Objects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

        EntityPokecube cube;
        float          ageInTicks;

        @Override
        public void setRotationAngles(final EntityPokecube entityIn, final float limbSwing, final float limbSwingAmount,
                final float ageInTicks, final float netHeadYaw, final float headPitch)
        {
            this.cube = entityIn;
            this.ageInTicks = ageInTicks;
        }

        @Override
        public void render(final MatrixStack mat, final IVertexBuilder bufferIn, final int packedLightIn,
                final int packedOverlayIn, final float red, final float green, final float blue, final float alpha)
        {
            // TODO Auto-generated method stub
            mat.push();
            mat.translate(0.125, 1.5, -0.125);
            final float scale = 0.25f;
            mat.scale(scale, scale, scale);
            mat.rotate(Vector3f.ZP.rotationDegrees(180));

            if (this.cube.isReleasing())
            {
                final Entity mob = this.cube.getReleased();
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                if (pokemob != null)
                {
                    // TODO exit cube effects.
                    // RenderPokemob.renderEffect(pokemob, 100 -
                    // cube.ticksExisted, 40, false);
                }
            }

            if (PokecubeManager.getTilt(this.cube.getItem()) > 0)
            {
                final float rotateY = MathHelper.cos(MathHelper.abs((float) (Math.PI * this.ageInTicks) / 12));
                mat.translate(.5, 0.5, 0);
                mat.rotate(Vector3f.ZP.rotation(rotateY));
                mat.translate(-.5, -0.5, 0);
            }
            ItemStack renderStack = this.cube.getItem();
            if (renderStack == null || !(renderStack.getItem() instanceof IPokecube))
                renderStack = PokecubeItems.POKECUBE_CUBES;

            final RenderType rendertype = RenderTypeLookup.getRenderType(renderStack);
            RenderType rendertype1;
            if (Objects.equals(rendertype, Atlases.getTranslucentBlockType())) rendertype1 = Atlases
                    .getTranslucentCullBlockType();
            else rendertype1 = rendertype;
            final IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers()
                    .getBufferSource();
            final IVertexBuilder ivertexbuilder = ItemRenderer.getBuffer(irendertypebuffer$impl, rendertype1, true,
                    renderStack.hasEffect());

            final Minecraft mc = Minecraft.getInstance();
            final IBakedModel ibakedmodel = mc.getItemRenderer().getItemModelWithOverrides(renderStack, this.cube.world,
                    (LivingEntity) null);
            mc.getItemRenderer().renderModel(ibakedmodel, renderStack, packedLightIn, packedOverlayIn, mat,
                    ivertexbuilder);

            mat.pop();
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
    public void render(final EntityPokecube entity, final float entityYaw, final float partialTicks,
            final MatrixStack matrixStackIn, final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        final long time = entity.reset;
        final long world = entity.getEntityWorld().getGameTime();
        if (time > world) return;

        final ResourceLocation num = PokecubeItems.getCubeId(entity.getItem());
        if (RenderPokecube.pokecubeRenderers.containsKey(num))
        {
            RenderPokecube.pokecubeRenderers.get(num).render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn,
                    packedLightIn);
            return;
        }
        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(final EntityPokecube entity)
    {
        return new ResourceLocation(PokecubeMod.ID, "textures/items/pokecubefront.png");
    }

}
