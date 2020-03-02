package pokecube.core.moves.animations.presets;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "throw")
public class ThrowParticle extends MoveAnimationBase
{

    float width = 1;

    public ThrowParticle()
    {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientAnimation(final MatrixStack mat, final IRenderTypeBuffer buffer, final MovePacketInfo info,
            final float partialTick)
    {
        final Vector3 source = info.source;
        final Vector3 target = info.target;
        final ResourceLocation texture = new ResourceLocation("pokecube", "textures/blank.png");
        Minecraft.getInstance().textureManager.bindTexture(texture);
        final double dist = source.distanceTo(target);
        final Vector3 temp = Vector3.getNewVector().set(source).subtractFrom(target);

        double factor = (info.currentTick + partialTick) / (double) this.getDuration();
        factor = Math.min(1, factor);
        temp.norm();
        temp.scalarMultBy(-dist * factor);
        final Vector3 temp2 = temp.copy();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder tez = tessellator.getBuffer();

        mat.push();

        this.initColour(info.currentTick * 300, partialTick, info.move);
        final float alpha = (this.rgba >> 24 & 255) / 255f;
        final float red = (this.rgba >> 16 & 255) / 255f;
        final float green = (this.rgba >> 8 & 255) / 255f;
        final float blue = (this.rgba & 255) / 255f;

        final long hash = (long) (temp.x * 1000000l + temp.z * 1000000000000l);
        final Random rand = new Random(hash);
        factor = this.width * 0.2;
        tez.begin(6, DefaultVertexFormats.POSITION_COLOR);
        Matrix4f pos = mat.getLast().getPositionMatrix();

        float x1, x2, y1, y2, z1, z2;

        for (int i = 0; i < 500; i++)
        {
            temp.set(rand.nextGaussian() * factor, rand.nextGaussian() * factor, rand.nextGaussian() * factor);
            temp.scalarMult(0.010);
            temp.addTo(temp2);
            final double size = 0.01;
            x1 = (float) (temp.x - size);
            y1 = (float) (temp.y - size);
            z1 = (float) (temp.z - size);
            x2 = (float) temp.x;
            y2 = (float) (temp.y + size);
            z2 = (float) temp.z;

            tez.pos(pos, x2, y2, z2).color(red, green, blue, alpha).endVertex();
            tez.pos(pos, x1, y1, z1).color(red, green, blue, alpha).endVertex();
            tez.pos(pos, x1, y2, z1).color(red, green, blue, alpha).endVertex();
            tez.pos(pos, x2, y1, z2).color(red, green, blue, alpha).endVertex();
        }
        tessellator.draw();

        mat.pop();
    }

    @Override
    public int getDuration()
    {
        return this.duration;
    }

    @Override
    public IMoveAnimation init(final String preset)
    {
        this.particle = preset;
        this.rgba = 0xFFFFFFFF;
        final String[] args = preset.split(":");
        for (int i = 1; i < args.length; i++)
        {
            final String ident = args[i].substring(0, 1);
            final String val = args[i].substring(1);
            if (ident.equals("w")) this.width = Float.parseFloat(val);
            else if (ident.equals("c")) this.initRGBA(val);
        }
        return this;
    }
}
