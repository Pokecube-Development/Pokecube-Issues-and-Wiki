package thut.core.client.render.particle;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IParticle
{
    int getDuration();

    void kill();

    long lastTick();

    @OnlyIn(value = Dist.CLIENT)
    public void renderParticle(final BufferBuilder buffer, final ActiveRenderInfo entityIn, final float partialTicks,
            final float rotationX, final float rotationZ, final float rotationYZ, final float rotationXY,
            final float rotationXZ);

    void setColour(int colour);

    void setDuration(int duration);

    void setLastTick(long tick);

    void setLifetime(int ticks);

    void setSize(float size);
}
