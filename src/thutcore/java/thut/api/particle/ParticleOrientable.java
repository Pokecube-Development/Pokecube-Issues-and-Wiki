package thut.api.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Camera;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import thut.api.maths.Vector4;

public class ParticleOrientable extends ParticleBase
{
    public Vector4 orientation;

    public ParticleOrientable(final int x, final int y)
    {
        super(x, y);
        this.billboard = false;
        this.orientation = new Vector4(0,0,0,1);
    }

    @Override
    public ParticleBase read(final FriendlyByteBuf buffer)
    {
        super.read(buffer);
        this.orientation = new Vector4(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        return this;
    }

    @Override
    public Quaternionf getQuat(Camera renderInfo, float partialTicks)
    {
        Quaternionf quaternion;
        quaternion = new Quaternionf(renderInfo.rotation());
        quaternion.mul(this.orientation.toMCQ());
        return quaternion;
    }

    public void setOrientation(final Vector4 orientation)
    {
        this.orientation = orientation;
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
