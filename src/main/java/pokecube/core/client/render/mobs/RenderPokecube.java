package pokecube.core.client.render.mobs;

import java.util.HashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import pokecube.api.items.IPokecube;
import pokecube.core.PokecubeItems;
import pokecube.core.client.render.mobs.RenderPokecube.ModelPokecube;
import pokecube.core.entity.pokecubes.EntityPokecube;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.Tracker;
import thut.lib.AxisAngles;

public class RenderPokecube extends LivingEntityRenderer<EntityPokecube, ModelPokecube>
{
    public static class ModelPokecube extends EntityModel<EntityPokecube>
    {

        public ModelPokecube()
        {}

        EntityPokecube cube;
        float ageInTicks;
        MultiBufferSource buffer;

        @Override
        public void setupAnim(final EntityPokecube entityIn, final float limbSwing, final float limbSwingAmount,
                final float ageInTicks, final float netHeadYaw, final float headPitch)
        {
            this.cube = entityIn;
            this.ageInTicks = ageInTicks;
        }

        @Override
        public void renderToBuffer(final PoseStack mat, final VertexConsumer bufferIn, final int packedLightIn,
                final int packedOverlayIn, final float red, final float green, final float blue, final float alpha)
        {
            mat.pushPose();
            final float scale = 1.f;
            mat.scale(scale, scale, scale);

            if (PokecubeManager.getTilt(this.cube.getItem()) > 0)
            {
                final float rotateY = Mth.cos(Mth.abs((float) (Math.PI * this.ageInTicks) / 12));
                final float sx = 0.0f;
                final float sy = 1.25f;
                final float sz = 0f;
                mat.translate(sx, sy, sz);
                mat.mulPose(AxisAngles.ZP.rotation(rotateY));
                mat.translate(-sx, -sy, -sz);
            }
            mat.translate(0, 1.5, 0);
            mat.mulPose(AxisAngles.ZP.rotationDegrees(180));

            ItemStack renderStack = this.cube.getItem();
            if (renderStack == null || !(renderStack.getItem() instanceof IPokecube))
                renderStack = PokecubeItems.POKECUBE_CUBES;

            final Minecraft mc = Minecraft.getInstance();

            if (this.buffer == null) this.buffer = mc.renderBuffers().bufferSource();
            final BakedModel ibakedmodel = mc.getItemRenderer().getModel(renderStack, this.cube.level, this.cube, 0);
            mc.getItemRenderer().render(renderStack, ItemTransforms.TransformType.GROUND, false, mat, this.buffer,
                    packedLightIn, OverlayTexture.NO_OVERLAY, ibakedmodel);

            mat.popPose();
        }
    }

    public static HashMap<ResourceLocation, EntityRenderer<EntityPokecube>> pokecubeRenderers = new HashMap<>();

    public RenderPokecube(final EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new ModelPokecube(), 0);
    }

    @Override
    protected boolean shouldShowName(final EntityPokecube entity)
    {
        return false;
    }

    @Override
    public void render(final EntityPokecube entity, final float entityYaw, final float partialTicks,
            final PoseStack matrixStackIn, final MultiBufferSource bufferIn, final int packedLightIn)
    {
        final long time = entity.reset;
        final long world = Tracker.instance().getTick();
        if (time > world) return;
        this.getModel().buffer = bufferIn;
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
    public ResourceLocation getTextureLocation(final EntityPokecube entity)
    {
        return new ResourceLocation(PokecubeMod.ID, "textures/item/pokecubefront.png");
    }

}
