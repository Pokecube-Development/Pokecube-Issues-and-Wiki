package pokecube.legends.client.render.block;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import pokecube.legends.tileentity.RaidSpawn;

public class Raid extends BlockEntityRenderer<RaidSpawn>
{
    public static final ResourceLocation TEXTURE_BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");

    public Raid(final BlockEntityRenderDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(final RaidSpawn tileEntityIn, final float partialTicks, final PoseStack matrixStackIn,
            final MultiBufferSource bufferIn, final int combinedLightIn, final int combinedOverlayIn)
    {
        final long i = tileEntityIn.getLevel().getGameTime();
        final List<RaidSpawn.BeamSegment> list = tileEntityIn.getBeamSegments();
        int j = 0;

        for (int k = 0; k < list.size(); ++k)
        {
            final RaidSpawn.BeamSegment RaidSpawn$beamsegment = list.get(k);
            Raid.renderBeamSegment(matrixStackIn, bufferIn, partialTicks, i, j, k == list.size() - 1 ? 1024
                    : RaidSpawn$beamsegment.getHeight(), RaidSpawn$beamsegment.getColors());
            j += RaidSpawn$beamsegment.getHeight();
        }

    }

    private static void renderBeamSegment(final PoseStack matrixStackIn, final MultiBufferSource bufferIn,
            final float partialTicks, final long totalWorldTime, final int yOffset, final int height,
            final float[] colors)
    {
        Raid.renderBeamSegment(matrixStackIn, bufferIn, Raid.TEXTURE_BEACON_BEAM, partialTicks, 1.0F, totalWorldTime,
                yOffset, height, colors, 0.2F, 0.25F);
    }

    public static void renderBeamSegment(final PoseStack matrixStackIn, final MultiBufferSource bufferIn,
            final ResourceLocation textureLocation, final float partialTicks, final float textureScale,
            final long totalWorldTime, final int yOffset, final int height, final float[] colors,
            final float beamRadius, final float glowRadius)
    {
        final int i = yOffset + height;
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5D, 0.0D, 0.5D);
        final float f = Math.floorMod(totalWorldTime, 40L) + partialTicks;
        final float f1 = height < 0 ? f : -f;
        final float f2 = Mth.frac(f1 * 0.2F - Mth.floor(f1 * 0.1F));
        final float f3 = colors[0];
        final float f4 = colors[1];
        final float f5 = colors[2];
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f6 = 0.0F;
        float f8 = 0.0F;
        float f9 = -beamRadius;
        final float f12 = -beamRadius;
        float f15 = -1.0F + f2;
        float f16 = height * textureScale * (0.5F / beamRadius) + f15;
        Raid.renderPart(matrixStackIn, bufferIn.getBuffer(RenderType.beaconBeam(textureLocation, false)), f3, f4, f5,
                1.0F, yOffset, i, 0.0F, beamRadius, beamRadius, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
        matrixStackIn.popPose();
        f6 = -glowRadius;
        final float f7 = -glowRadius;
        f8 = -glowRadius;
        f9 = -glowRadius;
        f15 = -1.0F + f2;
        f16 = height * textureScale + f15;
        Raid.renderPart(matrixStackIn, bufferIn.getBuffer(RenderType.beaconBeam(textureLocation, true)), f3, f4, f5,
                0.125F, yOffset, i, f6, f7, glowRadius, f8, f9, glowRadius, glowRadius, glowRadius, 0.0F, 1.0F, f16,
                f15);
        matrixStackIn.popPose();
    }

    private static void renderPart(final PoseStack matrixStackIn, final VertexConsumer bufferIn, final float red,
            final float green, final float blue, final float alpha, final int yMin, final int yMax,
            final float p_228840_8_, final float p_228840_9_, final float p_228840_10_, final float p_228840_11_,
            final float p_228840_12_, final float p_228840_13_, final float p_228840_14_, final float p_228840_15_,
            final float u1, final float u2, final float v1, final float v2)
    {
        final PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        final Matrix4f matrix4f = matrixstack$entry.pose();
        final Matrix3f matrix3f = matrixstack$entry.normal();
        Raid.addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_8_, p_228840_9_,
                p_228840_10_, p_228840_11_, u1, u2, v1, v2);
        Raid.addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_14_, p_228840_15_,
                p_228840_12_, p_228840_13_, u1, u2, v1, v2);
        Raid.addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_10_, p_228840_11_,
                p_228840_14_, p_228840_15_, u1, u2, v1, v2);
        Raid.addQuad(matrix4f, matrix3f, bufferIn, red, green, blue, alpha, yMin, yMax, p_228840_12_, p_228840_13_,
                p_228840_8_, p_228840_9_, u1, u2, v1, v2);
    }

    private static void addQuad(final Matrix4f matrixPos, final Matrix3f matrixNormal, final VertexConsumer bufferIn,
            final float red, final float green, final float blue, final float alpha, final int yMin, final int yMax,
            final float x1, final float z1, final float x2, final float z2, final float u1, final float u2,
            final float v1, final float v2)
    {
        Raid.addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMax, x1, z1, u2, v1);
        Raid.addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMin, x1, z1, u2, v2);
        Raid.addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMin, x2, z2, u1, v2);
        Raid.addVertex(matrixPos, matrixNormal, bufferIn, red, green, blue, alpha, yMax, x2, z2, u1, v1);
    }

    private static void addVertex(final Matrix4f matrixPos, final Matrix3f matrixNormal, final VertexConsumer bufferIn,
            final float red, final float green, final float blue, final float alpha, final int y, final float x,
            final float z, final float texU, final float texV)
    {
        bufferIn.vertex(matrixPos, x, y, z).color(red, green, blue, alpha).uv(texU, texV).overlayCoords(
                OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(final RaidSpawn te)
    {
        return true;
    }
}
