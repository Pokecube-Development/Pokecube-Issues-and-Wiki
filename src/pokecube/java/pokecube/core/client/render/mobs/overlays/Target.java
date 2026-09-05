package pokecube.core.client.render.mobs.overlays;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import pokecube.core.client.gui.components.TargetInfo;
import thut.lib.AxisAngles;

import java.util.function.Function;

public class Target
{
    public static final Function<ResourceLocation, RenderType> TARGET_ICON_TYPE_NODEPTH = Util.memoize(texture -> {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setDepthTestState(RenderType.NO_DEPTH_TEST)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setCullState(RenderType.NO_CULL).setLightmapState(RenderType.LIGHTMAP).createCompositeState(true);
        return RenderType.create("target_info_icon", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
                rendertype$compositestate);
    });

    public static void renderTargetArrow(final LivingEntity entity, PoseStack mat, final MultiBufferSource buf,
            final float partialTick, final Entity viewPoint, final int br)
    {
        if (entity != TargetInfo.lastViewedTarget) return;

        Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
        if (vec3 == null || viewPoint == null) return;

        float scale = .02f;
        mat.pushPose();
        var renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        mat.translate(vec3.x, vec3.y, vec3.z);
        mat.mulPose(renderManager.cameraOrientation());
        mat.mulPose(AxisAngles.ZN.rotationDegrees(90));
        mat.scale(scale, scale, scale);

        int a = 196;

        float size = 8*viewPoint.distanceTo(entity);
        float x1 = -size - 12 + 2 * Mth.sin((partialTick + entity.tickCount) / 10);
        float x2 = x1 + size;
        float y1 = -size / 2;
        float y2 = y1 + size;
        var pos = mat.last().pose();
        var buffer = Utils.makeBuilder(TARGET_ICON_TYPE_NODEPTH.apply(
                ResourceLocation.parse("pokecube:textures/gui/sprites/icons/target_icon.png")), buf);
        blit(buffer, pos, x1, y1, x2, y2, 0, 255, 255, 255, a);
        mat.popPose();
    }

    private static void blit(final VertexConsumer buffer, final Matrix4f pos, final float x1, final float y1,
            final float x2, final float y2, final float z, final int r, final int g, final int b, final int a)
    {
        var o = OverlayTexture.NO_OVERLAY;
        int brightness = LightTexture.FULL_BRIGHT;
        float nx = 0, ny = 0, nz = 1;
        buffer.addVertex(pos, x1, y1, z).setNormal(nx, ny, nz).setUv(0, 0).setColor(r, g, b, a).setOverlay(o)
                .setLight(brightness);
        buffer.addVertex(pos, x1, y2, z).setNormal(nx, ny, nz).setUv(1, 0).setColor(r, g, b, a).setOverlay(o)
                .setLight(brightness);
        buffer.addVertex(pos, x2, y2, z).setNormal(nx, ny, nz).setUv(1, 1).setColor(r, g, b, a).setOverlay(o)
                .setLight(brightness);
        buffer.addVertex(pos, x2, y1, z).setNormal(nx, ny, nz).setUv(0, 1).setColor(r, g, b, a).setOverlay(o)
                .setLight(brightness);
    }
}
