package pokecube.core.effects.presets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import pokecube.core.effects.MoveAnimationBase;

import java.awt.*;

public class EvolutionRays extends MoveAnimationBase
{
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
    public static final RenderType EFFECT = RenderType.create("pokemob:evo_effect", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder().setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .setTransparencyState(EvolutionRays.TRANSP).createCompositeState(false));


    private static void white_points(final VertexConsumer builder, final Matrix4f posmat, final int alpha,
            final Color col)
    {
        builder.addVertex(posmat, 0.0F, 0.0F, 0.0F).setColor(col.getRed(), col.getGreen(), col.getBlue(), alpha);
        builder.addVertex(posmat, 0.0F, 0.0F, 0.0F).setColor(col.getRed(), col.getGreen(), col.getBlue(), alpha);
    }

    private static void transp_point_a(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dxz, final Color col)
    {
        builder.addVertex(posmat, -EvolutionRays.sqrt3_2 * dxz, dy, -0.5F * dxz)
                .setColor(col.getRed(), col.getGreen(), col.getBlue(), 0);
    }

    private static void transp_point_b(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dxz, final Color col)
    {
        builder.addVertex(posmat, EvolutionRays.sqrt3_2 * dxz, dy, -0.5F * dxz)
                .setColor(col.getRed(), col.getGreen(), col.getBlue(), 0);
    }

    private static void transp_point_c(final VertexConsumer builder, final Matrix4f posmat, final float dy,
            final float dz, final Color col)
    {
        builder.addVertex(posmat, 0.0F, dy, dz).setColor(col.getRed(), col.getGreen(), col.getBlue(), 0);
    }


}
