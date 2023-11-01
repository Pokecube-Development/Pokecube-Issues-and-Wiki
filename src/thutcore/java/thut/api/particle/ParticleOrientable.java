package thut.api.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Quaternion;

import net.minecraft.client.Camera;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.Vector4;
import thut.api.maths.vecmath.Vec3f;

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
        try
        {
            this.orientation = new Vector4(reader.readFloat(), reader.readFloat(), reader.readFloat(), reader
                    .readFloat());
        }
        catch (final CommandSyntaxException e)
        {
            this.orientation = new Vector4();
//            ThutCore.LOGGER.error(e);
        }
        return this;
    }

    @Override
    public ParticleBase read(final FriendlyByteBuf buffer)
    {
        super.read(buffer);
        this.orientation = new Vector4(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        return this;
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void renderParticle(final VertexConsumer buffer, final Camera renderInfo, final float partialTicks,
            final Vec3f offset)
    {
        Quaternion quaternion;
        quaternion = new Quaternion(renderInfo.rotation());
        quaternion.mul(this.orientation.toMCQ());
        this.render(buffer, quaternion, offset);
    }

    public ParticleOrientable setOrientation(final Vector4 orientation)
    {
        this.orientation = orientation;
        return this;
    }

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer)
    {
        super.writeToNetwork(buffer);
        buffer.writeFloat(this.orientation.x);
        buffer.writeFloat(this.orientation.y);
        buffer.writeFloat(this.orientation.z);
        buffer.writeFloat(this.orientation.w);
    }

}
