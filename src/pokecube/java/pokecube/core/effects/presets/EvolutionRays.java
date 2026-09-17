package pokecube.core.effects.presets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.EvolutionEffect;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.effects.VectorPositionSource;
import pokecube.api.effects.context.PokemobContext;
import pokecube.core.effects.AnimPreset;
import pokecube.core.effects.MoveAnimationBase;
import thut.api.maths.Vector3;

import java.awt.Color;
import java.util.Random;

@AnimPreset(getPreset = "evo_rays")
public class EvolutionRays extends MoveAnimationBase
{
    public static void init()
    {
        EvolutionEffect.EVO_EFFECT_FACTORY = pokemob -> {
            var level = pokemob.getEntity().level();
            var targ = new Vector3(pokemob.getEntity()).addTo(new Vector3(pokemob.getEntity().getLookAngle()));
            var animation = new EffectRecord("pokecube.pokemob.evolution", new EvolutionRays());
            return new EffectPacketInfo(animation, level,
                    EffectPacketInfo.TaggedEntityTracker.create(pokemob.getEntity(), ParticleEffects.EVO_ANCHORS),
                    new VectorPositionSource(targ.toJOML()), 1, 1).setContext(new PokemobContext(pokemob));
        };
    }

    @OnlyIn(Dist.CLIENT)
    private static final RenderStateShard.TransparencyStateShard TRANSP = new RenderStateShard.TransparencyStateShard(
            "lightning_transparency", () ->
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    private static final float sqrt3_2 = (float) (Math.sqrt(3.0D) / 2.0D);
    @OnlyIn(Dist.CLIENT)
    public static final RenderType EFFECT = RenderType.create("pokemob:evo_effect", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder().setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .setTransparencyState(EvolutionRays.TRANSP).createCompositeState(false));


    @OnlyIn(Dist.CLIENT)
    private static void white_points(final VertexConsumer builder, final Matrix4f posmat, final int alpha,
            final Color col)
    {
        builder.addVertex(posmat, 0.0F, 0.0F, 0.0F).setColor(col.getRed(), col.getGreen(), col.getBlue(), alpha);
        builder.addVertex(posmat, 0.0F, 0.0F, 0.0F).setColor(col.getRed(), col.getGreen(), col.getBlue(), alpha);
    }

    @OnlyIn(Dist.CLIENT)
    private static void transp_point_a(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dxz, final Color col)
    {
        builder.addVertex(posmat, -EvolutionRays.sqrt3_2 * dxz, dy, -0.5F * dxz)
                .setColor(col.getRed(), col.getGreen(), col.getBlue(), 0);
    }

    @OnlyIn(Dist.CLIENT)
    private static void transp_point_b(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dxz, final Color col)
    {
        builder.addVertex(posmat, EvolutionRays.sqrt3_2 * dxz, dy, -0.5F * dxz)
                .setColor(col.getRed(), col.getGreen(), col.getBlue(), 0);
    }

    @OnlyIn(Dist.CLIENT)
    private static void transp_point_c(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dz, final Color col)
    {
        builder.addVertex(posmat, 0.0F, dy, dz).setColor(col.getRed(), col.getGreen(), col.getBlue(), 0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientAnimation(PoseStack mat, MultiBufferSource buffer, EffectPacketInfo info, float partialTick,
            int packedLightIn)
    {
        EvolutionEffect.EvoContext context = EvolutionEffect.EvoContext.fromMoveInfo(info);
        if (context == null || info.endTick == 0)
        {
            info.currentTick = info.endTick;
            return;
        }

        if(!(buffer instanceof MultiBufferSource.BufferSource source)) return;

        Color col1 = context.col1();
        Color col2 = context.col2();
        float scale = context.scale().get();
        float scaleShift = scale * context.entry().getModelSize().y / 2;

        final float time = 40 * (info.currentTick + partialTick) / info.endTick;
        final float f5 = time / 200f;
        final Random random = new Random(432L);
        float f7 = 0.0F;
        if (f5 > 0.8F) f7 = (f5 - 0.8F) / 0.2F;

        var builder = source.getBuffer(EvolutionRays.EFFECT);
        mat.pushPose();
        mat.translate(0, scaleShift, 0);
        for (int i = 0; i < (f5 + f5 * f5) / 2.0F * 100.0F; ++i)
        {
            mat.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + f5 * 90.0F));
            float f3 = (random.nextFloat() * 20.0F + 5.0F + f7 * 10.0F) * scale;
            float f4 = (random.nextFloat() * 2.0F + 1.0F + f7 * 2.0F) * scale;

            final Matrix4f matrix4f = mat.last().pose();
            final int j = (int) (200 * (1.0F - f7));

            EvolutionRays.white_points(builder, matrix4f, j, col1);
            EvolutionRays.transp_point_a(builder, matrix4f, f3, f4, col2);
            EvolutionRays.transp_point_b(builder, matrix4f, f3, f4, col2);
            EvolutionRays.white_points(builder, matrix4f, j, col2);
            EvolutionRays.transp_point_b(builder, matrix4f, f3, f4, col1);
            EvolutionRays.transp_point_c(builder, matrix4f, f3, f4, col1);
            EvolutionRays.white_points(builder, matrix4f, j, col1);
            EvolutionRays.transp_point_c(builder, matrix4f, f3, f4, col2);
            EvolutionRays.transp_point_a(builder, matrix4f, f3, f4, col2);

        }
        mat.popPose();
        source.endBatch(EvolutionRays.EFFECT);
    }

    @Override
    public boolean hasComplexRender()
    {
        return true;
    }
}
