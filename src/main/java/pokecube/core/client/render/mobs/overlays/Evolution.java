package pokecube.core.client.render.mobs.overlays;

import java.awt.Color;
import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderType;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class Evolution
{
    private static final TransparencyStateShard TRANSP = new RenderStateShard.TransparencyStateShard(
            "lightning_transparency", () ->
            {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    private static final float sqrt3_2 = (float) (Math.sqrt(3.0D) / 2.0D);
    // FIXME decide on shader
    private static final RenderType EFFECT = RenderType.create("pokemob:evo_effect", DefaultVertexFormat.POSITION_COLOR,
            Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder().setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .setTransparencyState(Evolution.TRANSP).createCompositeState(false));

    public static void render(final IPokemob pokemob, final PoseStack mat, final MultiBufferSource iRenderTypeBuffer,
            final float partialTick)
    {
        if (pokemob.isEvolving()) Evolution.renderEffect(pokemob, mat, iRenderTypeBuffer, partialTick,
                PokecubeCore.getConfig().evolutionTicks, true);
    }

    public static void renderEffect(final IPokemob pokemob, final PoseStack mat, final MultiBufferSource bufferIn,
            final float partialTick, final int duration, final boolean scaleMob)
    {
        if (!pokemob.getEntity().isAddedToWorld()) return;
        int ticks = pokemob.getEvolutionTicks();
        final PokedexEntry entry = pokemob.getPokedexEntry();
        final int color1 = entry.getType1().colour;
        int color2 = entry.getType2().colour;
        if (entry.getType2() == PokeType.unknown) color2 = color1;
        final Color col1 = new Color(color1);
        final Color col2 = new Color(color2);
        ticks = ticks - 50;
        ticks = duration - ticks;

        float scale = 0.25f;
        final float time = 40 * (ticks + partialTick) / duration;
        final float f5 = time / 200f;
        final Random random = new Random(432L);
        float f7 = 0.0F;
        if (f5 > 0.8F) f7 = (f5 - 0.8F) / 0.2F;

        final VertexConsumer ivertexbuilder2 = Utils.makeBuilder(Evolution.EFFECT, bufferIn);
        mat.pushPose();
        if (scaleMob)
        {
            final float mobScale = pokemob.getSize();
            final thut.api.maths.vecmath.Vec3f dims = entry.getModelSize();
            scale = 0.1f * Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            mat.translate(0.0F, dims.y * pokemob.getSize() * pokemob.getEntity().getScale() / 2, 0.0F);
        }
        for (int i = 0; i < (f5 + f5 * f5) / 2.0F * 100.0F; ++i)
        {
            mat.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + f5 * 90.0F));
            float f3 = random.nextFloat() * 20.0F + 5.0F + f7 * 10.0F;
            float f4 = random.nextFloat() * 2.0F + 1.0F + f7 * 2.0F;

            f3 *= scale;
            f4 *= scale;

            final Matrix4f matrix4f = mat.last().pose();
            final int j = (int) (200 * (1.0F - f7));

            Evolution.white_points(ivertexbuilder2, matrix4f, j, col1);
            Evolution.transp_point_a(ivertexbuilder2, matrix4f, f3, f4, col2);
            Evolution.transp_point_b(ivertexbuilder2, matrix4f, f3, f4, col2);
            Evolution.white_points(ivertexbuilder2, matrix4f, j, col2);
            Evolution.transp_point_b(ivertexbuilder2, matrix4f, f3, f4, col1);
            Evolution.transp_point_c(ivertexbuilder2, matrix4f, f3, f4, col1);
            Evolution.white_points(ivertexbuilder2, matrix4f, j, col1);
            Evolution.transp_point_c(ivertexbuilder2, matrix4f, f3, f4, col2);
            Evolution.transp_point_a(ivertexbuilder2, matrix4f, f3, f4, col2);

        }
        mat.popPose();
    }

    private static void white_points(final VertexConsumer builder, final Matrix4f posmat, final int alpha,
            final Color col)
    {
        if (builder instanceof BufferBuilder)
        {
            final BufferBuilder buf = (BufferBuilder) builder;
            if (buf.getVertexFormat().getVertexSize() != 16) return;
        }
        builder.vertex(posmat, 0.0F, 0.0F, 0.0F).color(col.getRed(), col.getGreen(), col.getBlue(), alpha).endVertex();
        builder.vertex(posmat, 0.0F, 0.0F, 0.0F).color(col.getRed(), col.getGreen(), col.getBlue(), alpha).endVertex();
    }

    private static void transp_point_a(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dxz, final Color col)
    {
        if (builder instanceof BufferBuilder)
        {
            final BufferBuilder buf = (BufferBuilder) builder;
            if (buf.getVertexFormat().getVertexSize() != 16) return;
        }
        builder.vertex(posmat, -Evolution.sqrt3_2 * dxz, dy, -0.5F * dxz)
                .color(col.getRed(), col.getGreen(), col.getBlue(), 0).endVertex();
    }

    private static void transp_point_b(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dxz, final Color col)
    {
        if (builder instanceof BufferBuilder)
        {
            final BufferBuilder buf = (BufferBuilder) builder;
            if (buf.getVertexFormat().getVertexSize() != 16) return;
        }
        builder.vertex(posmat, Evolution.sqrt3_2 * dxz, dy, -0.5F * dxz)
                .color(col.getRed(), col.getGreen(), col.getBlue(), 0).endVertex();
    }

    private static void transp_point_c(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dz, final Color col)
    {
        if (builder instanceof BufferBuilder)
        {
            final BufferBuilder buf = (BufferBuilder) builder;
            if (buf.getVertexFormat().getVertexSize() != 16) return;
        }
        builder.vertex(posmat, 0.0F, dy, 1.0F * dz).color(col.getRed(), col.getGreen(), col.getBlue(), 0).endVertex();
    }
}
