package thut.api.particle;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.vecmath.Vector3f;

public interface IParticle
{
    int getDuration();

    void kill();

    long lastTick();

    @OnlyIn(value = Dist.CLIENT)
    public void renderParticle(final com.mojang.blaze3d.vertex.IVertexBuilder buffer,
            final net.minecraft.client.renderer.ActiveRenderInfo entityIn, final float partialTicks, Vector3f offset);

    void setColour(int colour);

    void setDuration(int duration);

    void setLastTick(long tick);

    void setLifetime(int ticks);

    void setSize(float size);
}
