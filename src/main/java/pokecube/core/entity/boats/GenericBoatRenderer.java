package pokecube.core.entity.boats;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import pokecube.core.PokecubeCore;

public class GenericBoatRenderer extends EntityRenderer<GenericBoat>
{
    private final Map<GenericBoat.Type, Pair<ResourceLocation, BoatModel>> boatResources;

    public GenericBoatRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.8F;
        this.boatResources = new HashMap<>(Stream.of(GenericBoat.Type.values()).collect(ImmutableMap.toImmutableMap((type) -> type,
                (type) -> Pair.of(PokecubeCore.resourceLocation("textures/entity/boat/" + type.getName() + ".png"),
                        new BoatModel(context.bakeLayer(createBoatModelName(type)))))));
    }

    public static ModelLayerLocation createBoatModelName(GenericBoat.Type type)
    {
        return resourceLocation("boat/" + type.getName(), "main");
    }

    public static ModelLayerLocation resourceLocation(String path, String id)
    {
        return new ModelLayerLocation(PokecubeCore.resourceLocation(path), id);
    }

    @Override
    public void render(GenericBoat boat, float entityYaw, float partialTicks, PoseStack matricStack, MultiBufferSource source, int i)
    {
        matricStack.pushPose();
        matricStack.translate(0.0D, 0.375D, 0.0D);
        matricStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityYaw));
        float f = (float)boat.getHurtTime() - partialTicks;
        float f1 = boat.getDamage() - partialTicks;
        if (f1 < 0.0F)
        {
            f1 = 0.0F;
        }

        if (f > 0.0F)
        {
            matricStack.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * (float)boat.getHurtDir()));
        }

        float f2 = boat.getBubbleAngle(partialTicks);
        if (!Mth.equal(f2, 0.0F))
        {
            matricStack.mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 1.0F), boat.getBubbleAngle(partialTicks), true));
        }

        Pair<ResourceLocation, BoatModel> pair = this.boatResources.get(boat.getGenericBoatType());
        ResourceLocation resourcelocation = pair.getFirst();
        BoatModel boatmodel = pair.getSecond();
        matricStack.scale(-1.0F, -1.0F, 1.0F);
        matricStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
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
    public @NotNull ResourceLocation getTextureLocation(GenericBoat boat)
    {
        return this.boatResources.get(boat.getGenericBoatType()).getFirst();
    }
}
