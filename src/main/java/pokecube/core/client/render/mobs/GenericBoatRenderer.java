package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.entity.boats.GenericBoat.BoatType;
import thut.lib.AxisAngles;
import thut.lib.RegHelper;

public class GenericBoatRenderer extends EntityRenderer<GenericBoat>
{
    private final Object2ObjectOpenHashMap<String, Pair<ResourceLocation, BoatModel>> boatResources = new Object2ObjectOpenHashMap<>();

    public GenericBoatRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.8F;
        GenericBoat.getTypes().forEach(type -> {
            String modid = RegHelper.getKey(type.item().get()).getNamespace();
            boatResources.put(type.name(),
                    Pair.of(new ResourceLocation(modid, "textures/entity/boat/" + type.name() + ".png"),
                            new BoatModel(context.bakeLayer(createBoatModelName(modid, type)))));
        });

    }

    public static ModelLayerLocation createBoatModelName(String modid, BoatType type)
    {
        return new ModelLayerLocation(new ResourceLocation(modid, "boat/" + type.name()), "main");
    }

    @Override
    public void render(GenericBoat boat, float entityYaw, float partialTicks, PoseStack matricStack,
            MultiBufferSource source, int i)
    {
        matricStack.pushPose();
        matricStack.translate(0.0D, 0.375D, 0.0D);
        matricStack.mulPose(AxisAngles.YP.rotationDegrees(180.0F - entityYaw));
        float f = boat.getHurtTime() - partialTicks;
        float f1 = boat.getDamage() - partialTicks;
        if (f1 < 0.0F)
        {
            f1 = 0.0F;
        }

        if (f > 0.0F)
        {
            matricStack.mulPose(AxisAngles.XP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * boat.getHurtDir()));
        }

        float f2 = boat.getBubbleAngle(partialTicks);
        if (!Mth.equal(f2, 0.0F))
        {
            matricStack
                    .mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 1.0F), boat.getBubbleAngle(partialTicks), true));
        }

        Pair<ResourceLocation, BoatModel> pair = this.boatResources.get(boat.getGenericBoatType().name());
        ResourceLocation resourcelocation = pair.getFirst();
        BoatModel boatmodel = pair.getSecond();
        matricStack.scale(-1.0F, -1.0F, 1.0F);
        matricStack.mulPose(AxisAngles.YP.rotationDegrees(90.0F));
        boatmodel.setupAnim(boat, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = source.getBuffer(boatmodel.renderType(resourcelocation));
        boatmodel.renderToBuffer(matricStack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        if (!boat.isUnderWater())
        {
            VertexConsumer vertexconsumer1 = source.getBuffer(RenderType.waterMask());
            boatmodel.waterPatch().render(matricStack, vertexconsumer1, i, OverlayTexture.NO_OVERLAY);
        }
        matricStack.popPose();
        super.render(boat, entityYaw, partialTicks, matricStack, source, i);
    }

    @Override
    public ResourceLocation getTextureLocation(GenericBoat boat)
    {
        return this.boatResources.get(boat.getGenericBoatType()).getFirst();
    }
}
