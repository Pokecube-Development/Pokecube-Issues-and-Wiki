package pokecube.core.client.render.mobs.overlays;

import java.awt.Color;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class Evolution
{
    private static final float sqrt3_2 = (float) (Math.sqrt(3.0D) / 2.0D);

    public static void render(final IPokemob pokemob, final MatrixStack mat, final IRenderTypeBuffer iRenderTypeBuffer,
            final float partialTick)
    {
        if (pokemob.isEvolving()) Evolution.renderEffect(pokemob, mat, iRenderTypeBuffer, partialTick,
                PokecubeCore.getConfig().evolutionTicks, true);
    }

    public static void renderEffect(final IPokemob pokemob, final MatrixStack mat, final IRenderTypeBuffer bufferIn,
            final float partialTick, final int duration, final boolean scaleMob)
    {
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
        if (f5 > 0.8F)
        {
            f7 = (f5 - 0.8F) / 0.2F;
        }

        IVertexBuilder ivertexbuilder2 = bufferIn.getBuffer(RenderType.lightning());
        mat.push();
        if (scaleMob)
        {
            final float mobScale = pokemob.getSize();
            final thut.api.maths.vecmath.Vector3f dims = entry.getModelSize();
            scale = 0.1f * Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            mat.translate(0.0F, dims.y * pokemob.getSize() * pokemob.getEntity().getRenderScale() / 2, 0.0F);
        }
        for (int i = 0; i < (f5 + f5 * f5) / 2.0F * 100.0F; ++i)
        {
            mat.rotate(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.rotate(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.rotate(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.rotate(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.rotate(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
            mat.rotate(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + f5 * 90.0F));
            float f3 = random.nextFloat() * 20.0F + 5.0F + f7 * 10.0F;
            float f4 = random.nextFloat() * 2.0F + 1.0F + f7 * 2.0F;

            f3 *= scale;
            f4 *= scale;

            Matrix4f matrix4f = mat.getLast().getPositionMatrix();
            int j = (int) (200 * (1.0F - f7));
            white_points(ivertexbuilder2, matrix4f, j, col1);
            transp_point_a(ivertexbuilder2, matrix4f, f3, f4, col2);
            transp_point_b(ivertexbuilder2, matrix4f, f3, f4, col2);
            white_points(ivertexbuilder2, matrix4f, j, col2);
            transp_point_b(ivertexbuilder2, matrix4f, f3, f4, col1);
            transp_point_c(ivertexbuilder2, matrix4f, f3, f4, col1);
            white_points(ivertexbuilder2, matrix4f, j, col1);
            transp_point_c(ivertexbuilder2, matrix4f, f3, f4, col2);
            transp_point_a(ivertexbuilder2, matrix4f, f3, f4, col2);
        }
        mat.pop();
    }

    private static void white_points(IVertexBuilder builder, Matrix4f posmat, int alpha, Color col)
    {
        builder.pos(posmat, 0.0F, 0.0F, 0.0F).color(col.getRed(), col.getGreen(), col.getBlue(), alpha).endVertex();
        builder.pos(posmat, 0.0F, 0.0F, 0.0F).color(col.getRed(), col.getGreen(), col.getBlue(), alpha).endVertex();
    }

    private static void transp_point_a(IVertexBuilder builder, Matrix4f posmat, float dy, float dxz, Color col)
    {
        builder.pos(posmat, -sqrt3_2 * dxz, dy, -0.5F * dxz).color(col.getRed(), col.getGreen(), col.getBlue(), 0)
                .endVertex();
    }

    private static void transp_point_b(IVertexBuilder builder, Matrix4f posmat, float dy, float dxz, Color col)
    {
        builder.pos(posmat, sqrt3_2 * dxz, dy, -0.5F * dxz).color(col.getRed(), col.getGreen(), col.getBlue(), 0)
                .endVertex();
    }

    private static void transp_point_c(IVertexBuilder builder, Matrix4f posmat, float dy, float dz, Color col)
    {
        builder.pos(posmat, 0.0F, dy, 1.0F * dz).color(col.getRed(), col.getGreen(), col.getBlue(), 0).endVertex();
    }
}
