package pokecube.core.client.render.mobs;

import java.util.HashMap;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.client.render.mobs.RenderPokecube.ModelPokecube;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.Tracker;

public class RenderPokecube extends LivingEntityRenderer<EntityPokecube, ModelPokecube>
{
    public static class ModelPokecube extends EntityModel<EntityPokecube>
    {

        public ModelPokecube()
        {
        }

        EntityPokecube    cube;
        float             ageInTicks;
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
                mat.mulPose(Vector3f.ZP.rotation(rotateY));
                mat.translate(-sx, -sy, -sz);
            }
            mat.translate(0, 1.5, 0);
            mat.mulPose(Vector3f.ZP.rotationDegrees(180));

            ItemStack renderStack = this.cube.getItem();
            if (renderStack == null || !(renderStack.getItem() instanceof IPokecube))
                renderStack = PokecubeItems.POKECUBE_CUBES;

            final RenderType rendertype = ItemBlockRenderTypes.getRenderType(renderStack, true);
            RenderType rendertype1;
            if (Objects.equals(rendertype, Sheets.translucentCullBlockSheet())) rendertype1 = Sheets
                    .translucentCullBlockSheet();
            else rendertype1 = rendertype;
            final MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers()
                    .bufferSource();
            final VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(irendertypebuffer$impl, rendertype1, true,
                    renderStack.hasFoil());

            final Minecraft mc = Minecraft.getInstance();
            final BakedModel ibakedmodel = mc.getItemRenderer().getModel(renderStack, this.cube.level, this.cube);
            if (this.buffer != null) mc.getItemRenderer().render(renderStack, ItemTransforms.TransformType.GROUND,
                    false, mat, this.buffer, packedLightIn, OverlayTexture.NO_OVERLAY, ibakedmodel);
            else mc.getItemRenderer().renderModelLists(ibakedmodel, renderStack, packedLightIn, packedOverlayIn, mat,
                    ivertexbuilder);

            mat.popPose();
        }
    }

    public static HashMap<ResourceLocation, EntityRenderer<EntityPokecube>> pokecubeRenderers = new HashMap<>();

    public RenderPokecube(final EntityRenderDispatcher renderManager)
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
        return new ResourceLocation(PokecubeMod.ID, "textures/items/pokecubefront.png");
    }

}
