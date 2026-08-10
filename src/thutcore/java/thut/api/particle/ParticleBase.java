package thut.api.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class ParticleBase extends ParticleType<ParticleBase> implements IParticle, IAnimatedParticle, ParticleOptions
{
    private static class Codec implements StreamCodec<RegistryFriendlyByteBuf, ParticleBase>
    {

        @Override
        public ParticleBase decode(RegistryFriendlyByteBuf buffer)
        {
            return new ParticleBase(0, 0).read(buffer);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ParticleBase value)
        {
            value.writeToNetwork(buffer);
        }

    }

    public static ResourceLocation TEXTUREMAP = ResourceLocation.fromNamespaceAndPath(ThutCore.MODID,
            "textures/particles.png");

    public int duration = 10;
    public int lifetime = 10;
    public int initTime = 0;
    public long lastTick = 0;
    public int animSpeed = 2;
    public float size = 1;
    public int rgba = 0xFFFFFFFF;
    public boolean billboard = true;
    public String name = "";
    public Vector3 velocity = Vector3.empty;
    public Vector3 position = Vector3.empty;
    public int[][] tex = new int[1][2];

    public ParticleBase(final int x, final int y)
    {
        super(true);
        this.tex[0][0] = x;
        this.tex[0][1] = y;
    }

    @Override
    public int getDuration()
    {
        return this.duration;
    }

    @Override
    public ParticleBase getType()
    {
        return this;
    }

    @Override
    public void kill()
    {
        // TODO terminate rendering?
    }

    @Override
    public long lastTick()
    {
        return this.lastTick;
    }

    public ParticleBase read(final FriendlyByteBuf buffer)
    {
        this.duration = buffer.readInt();
        this.lifetime = buffer.readInt();
        this.initTime = buffer.readInt();
        // LastTick would be here, but not needed.
        this.animSpeed = buffer.readInt();
        this.size = buffer.readFloat();
        this.rgba = buffer.readInt();
        this.billboard = buffer.readBoolean();
        this.velocity = Vector3.readFromBuff(buffer);
        this.position = Vector3.readFromBuff(buffer);
        this.tex = new int[buffer.readInt()][];
        for (int i = 0; i < this.tex.length; i++)
            this.tex[i] = buffer.readVarIntArray();
        return this;
    }

    protected float rCol = 1.0F;
    protected float gCol = 1.0F;
    protected float bCol = 1.0F;
    protected float alpha = 1.0F;

    protected void renderRotatedQuad(VertexConsumer buffer, Vector3f source, Quaternionf quaternion, float partialTicks)
    {
        this.renderRotatedQuad(buffer, quaternion, source.x, source.y, source.z, partialTicks);
    }

    protected void renderRotatedQuad(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z,
            float partialTicks)
    {
        final int num = this.getDuration() / this.animSpeed % this.tex.length;
        final int u = this.tex[num][0], v = this.tex[num][1];
        final float u0 = u * 1f / 16f, v0 = v * 1f / 16f;
        final float u1 = (u + 1) * 1f / 16f, v1 = (v + 1) * 1f / 16f;
        float f = this.size;
        int i = this.getLightColor(partialTicks);
        this.renderVertex(buffer, quaternion, x, y, z, 1.0F, -1.0F, f, u1, v1, i);
        this.renderVertex(buffer, quaternion, x, y, z, 1.0F, 1.0F, f, u1, v0, i);
        this.renderVertex(buffer, quaternion, x, y, z, -1.0F, 1.0F, f, u0, v0, i);
        this.renderVertex(buffer, quaternion, x, y, z, -1.0F, -1.0F, f, u0, v1, i);
    }

    private void renderVertex(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float xOffset,
            float yOffset, float quadSize, float u, float v, int packedLight)
    {
        Vector3f vector3f = new Vector3f(xOffset, yOffset, 0.0F).rotate(quaternion).mul(quadSize).add(x, y, z);
        buffer.addVertex(vector3f.x(), vector3f.y(), vector3f.z()).setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(packedLight);
    }

    public Quaternionf getQuat(Camera renderInfo, float partialTicks)
    {
        var quaternion = new Quaternionf();
        var mode = this.billboard
                ? SingleQuadParticle.FacingCameraMode.LOOKAT_Y
                : SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ;
        mode.setRotation(quaternion, renderInfo, partialTicks);
        return quaternion;
    }

    protected int getLightColor(float partialTick)
    {
        // TODO add a configuration for the particle lightmap, vanilla has the particles aware of their location and level
        return 15 << 20 | 15 << 4;
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void renderParticle(final VertexConsumer buffer, final Camera renderInfo, final float partialTicks,
            final Vector3f offset)
    {
        Quaternionf quaternion = getQuat(renderInfo, partialTicks);

        final Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        quaternion.transform(vector3f1);
        final Vector3f[] verts = new Vector3f[] { //@formatter:off
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };//@formatter:on
        final float f4 = this.size;

        for (int i = 0; i < 4; ++i)
        {
            final Vector3f vector3f = verts[i];
            quaternion.transform(vector3f);
            vector3f.mul(f4);
            vector3f.add(offset.x, offset.y, offset.z);
        }
        this.setColour();

        alpha = (this.rgba >> 24 & 255) / 255f;
        rCol = (this.rgba >> 16 & 255) / 255f;
        gCol = (this.rgba >> 8 & 255) / 255f;
        bCol = (this.rgba & 255) / 255f;

        this.renderRotatedQuad(buffer, offset, quaternion, partialTicks);
    }

    @Override
    public void setAnimSpeed(final int speed)
    {
        this.animSpeed = Math.max(speed, 5);
    }

    void setColour()
    {
        if (this.name.equalsIgnoreCase("aurora"))
        {
            this.rgba = 0xFF000000;
            final int num = (this.getDuration() + this.initTime) / this.animSpeed % 16;
            this.rgba += DyeColor.byId(num).getTextColor();
        }
    }

    @Override
    public void setColour(final int colour)
    {
        this.rgba = colour;
    }

    @Override
    public void setDuration(final int duration)
    {
        this.duration = duration;
    }

    @Override
    public void setLastTick(final long tick)
    {
        this.lastTick = tick;
    }

    @Override
    public void setLifetime(final int ticks)
    {
        this.duration = this.lifetime = ticks;
    }

    public void setPosition(final Vector3 v)
    {
        this.position = v;
    }

    @Override
    public void setSize(final float size)
    {
        this.size = size;
    }

    @Override
    public void setStartTime(final int start)
    {
        this.initTime = start;
    }

    @Override
    public void setTex(final int[][] textures)
    {
        this.tex = textures;
    }

    public void setVelocity(Vector3 v)
    {
        if (v == null) v = Vector3.empty;
        this.velocity = v;
    }

    public void writeToNetwork(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.duration);
        buffer.writeInt(this.lifetime);
        buffer.writeInt(this.initTime);
        // LastTick would be here, but not needed.
        buffer.writeInt(this.animSpeed);
        buffer.writeFloat(this.size);
        buffer.writeInt(this.rgba);
        buffer.writeBoolean(this.billboard);
        this.velocity.writeToBuff(buffer);
        this.position.writeToBuff(buffer);
        buffer.writeInt(this.tex.length);
        for (final int[] element : this.tex)
            buffer.writeVarIntArray(element);
    }

    private final MapCodec<ParticleBase> codec = MapCodec.unit(this::getType);
    private static final Codec CODEC = new Codec();

    @Override
    public MapCodec<ParticleBase> codec()
    {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ParticleBase> streamCodec()
    {
        return CODEC;
    }
}
