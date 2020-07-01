package thut.api.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.brigadier.StringReader;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.Vector4;
import thut.api.maths.vecmath.Vector3f;

public class ParticleOrientable extends ParticleBase
{
    public Vector4 orientation;

    public ParticleOrientable(final int x, final int y)
    {
        super(x, y);
        this.billboard = false;
    }

    @Override
    protected ParticleBase read(final StringReader reader)
    {
        super.read(reader);
        return this;
    }

    @Override
    public ParticleBase read(final PacketBuffer buffer)
    {
        super.read(buffer);
        this.orientation = new Vector4(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        return this;
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void renderParticle(final IVertexBuilder buffer, final ActiveRenderInfo renderInfo, final float partialTicks,
            final Vector3f offset)
    {
        Quaternion quaternion;
        quaternion = new Quaternion(renderInfo.getRotation());
        quaternion.multiply(this.orientation.toMCQ());
        this.render(buffer, quaternion, offset);
    }

    public ParticleOrientable setOrientation(final Vector4 orientation)
    {
        this.orientation = orientation;
        return this;
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        super.write(buffer);
        buffer.writeFloat(this.orientation.x);
        buffer.writeFloat(this.orientation.y);
        buffer.writeFloat(this.orientation.z);
        buffer.writeFloat(this.orientation.w);
    }

}
