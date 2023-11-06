package pokecube.core.client.render.mobs;

import java.util.HashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.RegisterCubeRenderer;
import pokecube.api.items.IPokecube;
import pokecube.core.PokecubeItems;
import pokecube.core.client.render.mobs.RenderPokecube.ModelPokecube;
import pokecube.core.client.render.mobs.overlays.Evolution;
import pokecube.core.entity.pokecubes.EntityPokecube;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;
import thut.lib.RegHelper;

public class RenderPokecube extends LivingEntityRenderer<EntityPokecube, ModelPokecube>
{
    public static class ModelPokecube extends EntityModel<EntityPokecube>
    {
        public static Object2FloatOpenHashMap<ResourceLocation> CUBE_SHIFTS = new Object2FloatOpenHashMap<>();

        public ModelPokecube()
        {}

        ItemStack item;
        EntityPokecube cube;
        float ageInTicks;
        float shift;
        MultiBufferSource buffer;

        @Override
        public void setupAnim(final EntityPokecube entityIn, final float limbSwing, final float limbSwingAmount,
                final float ageInTicks, final float netHeadYaw, final float headPitch)
        {
            this.cube = entityIn;
            this.ageInTicks = ageInTicks;
            this.item = cube.getItem();
            this.shift = CUBE_SHIFTS.getOrDefault(RegHelper.getKey(item), 0.0625f);
        }

        @Override
        public void renderToBuffer(final PoseStack mat, final VertexConsumer bufferIn, final int packedLightIn,
                final int packedOverlayIn, final float red, final float green, final float blue, final float alpha)
        {
            mat.pushPose();
            final float scale = 1.f;
            mat.scale(scale, scale, scale);

            LivingEntity capturing = this.cube.getCapturing();
            boolean shaking = PokecubeManager.getTilt(item) > 0;

            if (shaking && capturing != null)
            {
                shaking = capturing.tickCount >= CaptureManager.CAPTURE_SHRINK_TIMER;
            }

            if (shaking)
            {
                final float rotateY = Mth.cos(Mth.abs((float) (Math.PI * this.ageInTicks) / 12));
                final float sx = 0.0f;
                final float sy = 1.501f - cube.getBbHeight() / 2;
                final float sz = 0f;
                mat.translate(sx, sy, sz);
                mat.mulPose(AxisAngles.ZP.rotation(rotateY));
                mat.translate(-sx, -sy, -sz);
            }
            mat.translate(0, 1.5 + shift, 0);
            mat.mulPose(AxisAngles.ZP.rotationDegrees(180));

            ItemStack renderStack = item;
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
        pokecubeRenderers.clear();
        ThutCore.FORGE_BUS.post(new RegisterCubeRenderer());
        new RenderFancyPokecube(renderManager);
    }

    @Override
    protected boolean shouldShowName(final EntityPokecube entity)
    {
        return false;
    }

    @Override
    public void render(final EntityPokecube entity, final float entityYaw, final float partialTicks,
            final PoseStack stack, final MultiBufferSource bufferIn, final int packedLightIn)
    {
        final long time = entity.reset;
        final long world = Tracker.instance().getTick();
        if (time > world) return;
        this.getModel().buffer = bufferIn;
        final ResourceLocation num = PokecubeItems.getCubeId(entity.getItem());
        if (RenderPokecube.pokecubeRenderers.containsKey(num))
        {
            RenderPokecube.pokecubeRenderers.get(num).render(entity, entityYaw, partialTicks, stack, bufferIn,
                    packedLightIn);
            return;
        }
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);

        LivingEntity capturing = entity.getCapturing();
        if (capturing != null)
        {
            int duration = CaptureManager.CAPTURE_SHRINK_TIMER;
            var renderer = this.entityRenderDispatcher.getRenderer(capturing);
            float dt = (duration - (capturing.tickCount + partialTicks));
            float scale = dt / duration;
            if (scale > 0)
            {
                if (capturing.getAttributes().hasAttribute(SharedAttributes.MOB_SIZE_SCALE.get()))
                {
                    capturing.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get()).setBaseValue(scale);
                }
                IPokemob pokemob = PokemobCaps.getPokemobFor(capturing);
                stack.pushPose();
                Vector3 capt = entity.capturePos;
                stack.translate(capt.x - entity.getX(), capt.y - entity.getY(), capt.z - entity.getZ());
                if (pokemob != null)
                {
                    float scaleShift = 0;
                    final PokedexEntry entry = pokemob.getPokedexEntry();
                    var dims = entry.getModelSize();
                    scaleShift = dims.y * pokemob.getSize() * scale / 2;
                    float mobScale = pokemob.getSize();
                    scale = 0.1f * Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
                    Evolution.renderEffect(pokemob, stack, bufferIn, partialTicks, (int) dt, duration, scale,
                            scaleShift, true);
                }
                renderer.render(capturing, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
                stack.popPose();
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(final EntityPokecube entity)
    {
        return new ResourceLocation(PokecubeMod.ID, "textures/item/pokecubefront.png");
    }

}
