package pokecube.core.client.render.mobs.overlays;

import java.awt.Color;
import java.util.Random;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class Evolution
{
    public static void render(final IPokemob pokemob, final Vec3d pos, final float partialTick)
    {
        if (pokemob.isEvolving()) Evolution.renderEffect(pokemob, pos, partialTick, PokecubeCore
                .getConfig().evolutionTicks, true);
    }

    public static void renderEffect(final IPokemob pokemob, final Vec3d pos, final float partialTick,
            final int duration, final boolean scaleMob)
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
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderHelper.disableStandardItemLighting();
        final float time = 40 * (ticks + partialTick) / duration;
        final float f = time / 200f;
        float f1 = 0.0F;
        if (f > 0.8F) f1 = (f - 0.8F) / 0.2F;
        final Random random = new Random(432L);
        GlStateManager.disableTexture();
        GlStateManager.shadeModel(7425);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlphaTest();
        GlStateManager.enableCull();
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();

        final double x = pos.x, y = pos.y, z = pos.z;
        GlStateManager.translated(x, y, z);

        if (scaleMob)
        {
            final float mobScale = pokemob.getSize();
            final Vector3f dims = entry.getModelSize();
            scale = 0.1f * Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            GL11.glTranslatef(0.0F, dims.y * pokemob.getSize() / 2, 0.0F);
        }
        for (int i = 0; i < (f + f * f) / 2.0F * 100.0F; ++i)
        {
            GlStateManager.rotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(random.nextFloat() * 360.0F + f * 90.0F, 0.0F, 0.0F, 1.0F);
            final float f2 = (random.nextFloat() * 20.0F + 5.0F + f1 * 10.0F) * scale;
            final float f3 = (random.nextFloat() * 2.0F + 1.0F + f1 * 2.0F) * scale;
            bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(0.0D, 0.0D, 0.0D).color(col1.getRed(), col1.getGreen(), col1.getBlue(), (int) (255.0F
                    * (1.0F - f1))).endVertex();
            bufferbuilder.pos(-0.866D * f3, f2, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                    .endVertex();
            bufferbuilder.pos(0.866D * f3, f2, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                    .endVertex();
            bufferbuilder.pos(0.0D, f2, 1.0F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0).endVertex();
            bufferbuilder.pos(-0.866D * f3, f2, -0.5F * f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
                    .endVertex();
            tessellator.draw();
        }

        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(7424);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture();
        GlStateManager.enableAlphaTest();
        RenderHelper.enableStandardItemLighting();
    }
}
