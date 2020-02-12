package thut.core.client.render.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.item.DyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.core.common.ThutCore;

public class ParticleBase extends ParticleType<ParticleBase> implements IParticle, IAnimatedParticle, IParticleData
{
    private static final IParticleData.IDeserializer<ParticleBase> DESERIALIZER = new IParticleData.IDeserializer<ParticleBase>()
    {
        @Override
        public ParticleBase deserialize(
                final ParticleType<ParticleBase> particleTypeIn,
                final StringReader reader)
                        throws CommandSyntaxException
        {
            return ((ParticleBase) particleTypeIn)
                    .read(reader);
        }

        @Override
        public ParticleBase read(
                final ParticleType<ParticleBase> particleTypeIn,
                final PacketBuffer buffer)
        {
            return ((ParticleBase) particleTypeIn)
                    .read(buffer);
        }
    };
    public static ResourceLocation                                 TEXTUREMAP   = new ResourceLocation(ThutCore.MODID,
            "textures/particles.png");

    int                                                            duration     = 10;
    int                                                            lifetime     = 10;
    int                                                            initTime     = 0;
    long                                                           lastTick     = 0;
    int                                                            animSpeed    = 2;
    float                                                          size         = 1;
    int                                                            rgba         = 0xFFFFFFFF;
    boolean                                                        billboard    = true;
    String                                                         name         = "";
    Vector3                                                        velocity     = Vector3.empty;
    Vector3                                                        position     = Vector3.empty;
    int[][]                                                        tex          = new int[1][2];

    public ParticleBase(final int x, final int y)
    {
        super(true, ParticleBase.DESERIALIZER);
        this.tex[0][0] = x;
        this.tex[0][1] = y;
    }

    @Override
    public int getDuration()
    {
        return this.duration;
    }

    @Override
    public String getParameters()
    {
        // TODO jsonify ourselves maybe?
        return ForgeRegistries.PARTICLE_TYPES.getKey(this).toString();
    }

    @Override
    public ParticleType<?> getType()
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

    public ParticleBase read(final PacketBuffer buffer)
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

    protected ParticleBase read(final StringReader reader)
    {// TODO finish this?
        return this;
    }

    protected void render(final IVertexBuilder buffer, final Quaternion quaternion, final Vector3f offset)
    {
        final net.minecraft.client.renderer.Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F).toMC();
        vector3f1.transform(quaternion);
        final net.minecraft.client.renderer.Vector3f[] avector3f = new net.minecraft.client.renderer.Vector3f[] {
                new net.minecraft.client.renderer.Vector3f(-1.0F, -1.0F, 0.0F),
                new net.minecraft.client.renderer.Vector3f(-1.0F, 1.0F, 0.0F),
                new net.minecraft.client.renderer.Vector3f(1.0F, 1.0F, 0.0F),
                new net.minecraft.client.renderer.Vector3f(1.0F, -1.0F, 0.0F) };
        final float f4 = this.size;

        for (int i = 0; i < 4; ++i)
        {
            final net.minecraft.client.renderer.Vector3f vector3f = avector3f[i];
            vector3f.transform(quaternion);
            vector3f.mul(f4);
            vector3f.add(offset.x, offset.y, offset.z);
        }
        this.setColour();

        final float alpha = (this.rgba >> 24 & 255) / 255f;
        final float red = (this.rgba >> 16 & 255) / 255f;
        final float green = (this.rgba >> 8 & 255) / 255f;
        final float blue = (this.rgba & 255) / 255f;

        final int j = 0;// TODO include way to get this!

        final int num = this.getDuration() / this.animSpeed % this.tex.length;
        final int u = this.tex[num][0], v = this.tex[num][1];
        final float u1 = u * 1f / 16f, v1 = v * 1f / 16f;
        final float u2 = (u + 1) * 1f / 16f, v2 = (v + 1) * 1f / 16f;

        buffer.pos(avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ())
        .color(red, green, blue, alpha).tex(u2, v2).lightmap(j).endVertex();
        buffer.pos(avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ())
        .color(red, green, blue, alpha).tex(u1, v1).lightmap(j).endVertex();
        buffer.pos(avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ())
        .color(red, green, blue, alpha).tex(u1, v1).lightmap(j).endVertex();
        buffer.pos(avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ())
        .color(red, green, blue, alpha).tex(u2, v2).lightmap(j).endVertex();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void renderParticle(final IVertexBuilder buffer, final ActiveRenderInfo renderInfo, final float partialTicks,
            final Vector3f offset)
    {
        Quaternion quaternion;
        quaternion = renderInfo.getRotation();
        this.render(buffer, quaternion, offset);
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
            this.rgba += DyeColor.byId(num).textColor;
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

    @Override
    public void write(final PacketBuffer buffer)
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
}
