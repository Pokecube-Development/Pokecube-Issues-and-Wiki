package pokecube.core.client.render.mobs.overlays;

import java.awt.Color;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.api.maths.vecmath.Vector3f;

public class Evolution
{
    public static void render(final IPokemob pokemob, final MatrixStack mat, final IRenderTypeBuffer iRenderTypeBuffer,
            final float partialTick)
    {
        if (pokemob.isEvolving())
            Evolution.renderEffect(pokemob, mat, partialTick, PokecubeCore
                    .getConfig().evolutionTicks, true);
    }

    public static void renderEffect(final IPokemob pokemob, final MatrixStack mat,
            final float partialTick,
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
        final float time = 40 * (ticks + partialTick) / duration;
        final float f = time / 200f;
        float f1 = 0.0F;
        if (f > 0.8F) f1 = (f - 0.8F) / 0.2F;
        final Random random = new Random(432L);

        mat.push();

        if (scaleMob)
        {
            final float mobScale = pokemob.getSize();
            final Vector3f dims = entry.getModelSize();
            scale = 0.1f * Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            mat.translate(0.0F, dims.y * pokemob.getSize() / 2, 0.0F);
        }
        for (int i = 0; i < (f + f * f) / 2.0F * 100.0F; ++i)
        {
            final float f2 = (random.nextFloat() * 20.0F + 5.0F + f1 * 10.0F) * scale;
            final float f3 = (random.nextFloat() * 2.0F + 1.0F + f1 * 2.0F) * scale;
            // FIXME evolution animations
            // bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
            // bufferbuilder.pos(0.0D, 0.0D, 0.0D).color(col1.getRed(),
            // col1.getGreen(), col1.getBlue(), (int) (255.0F
            // * (1.0F - f1))).endVertex();
            // bufferbuilder.pos(-0.866D * f3, f2, -0.5F *
            // f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
            // .endVertex();
            // bufferbuilder.pos(0.866D * f3, f2, -0.5F *
            // f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
            // .endVertex();
            // bufferbuilder.pos(0.0D, f2, 1.0F * f3).color(col2.getRed(),
            // col2.getGreen(), col2.getBlue(), 0).endVertex();
            // bufferbuilder.pos(-0.866D * f3, f2, -0.5F *
            // f3).color(col2.getRed(), col2.getGreen(), col2.getBlue(), 0)
            // .endVertex();
        }

        mat.pop();

    }
}
