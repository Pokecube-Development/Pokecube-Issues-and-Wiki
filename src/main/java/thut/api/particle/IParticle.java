package thut.api.particle;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.vecmath.Vec3f;

public interface IParticle
{
    int getDuration();

    void kill();

    long lastTick();

    @OnlyIn(value = Dist.CLIENT)
    public void renderParticle(final com.mojang.blaze3d.vertex.VertexConsumer buffer,
            final net.minecraft.client.Camera entityIn, final float partialTicks, Vec3f offset);

    void setColour(int colour);

    void setDuration(int duration);

    void setLastTick(long tick);

    void setLifetime(int ticks);

    void setSize(float size);
}
