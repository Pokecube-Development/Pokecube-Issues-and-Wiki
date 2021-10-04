package thut.api.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Quaternion;
import com.mojang.serialization.Codec;

import net.minecraft.client.Camera;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.core.common.ThutCore;

public class ParticleBase extends ParticleType<ParticleBase> implements IParticle, IAnimatedParticle, ParticleOptions
{
    @SuppressWarnings("deprecation")
    private static final ParticleOptions.Deserializer<ParticleBase> DESERIALIZER = new ParticleOptions.Deserializer<ParticleBase>()
    {
        @Override
        public ParticleBase fromCommand(
                final ParticleType<ParticleBase> particleTypeIn,
                final StringReader reader)
                throws CommandSyntaxException
        {
            return ((ParticleBase) particleTypeIn)
                    .read(reader);
        }

        @Override
        public ParticleBase fromNetwork(
                final ParticleType<ParticleBase> particleTypeIn,
                final FriendlyByteBuf buffer)
        {
            return ((ParticleBase) particleTypeIn)
                    .read(buffer);
        }
    };

    public static ResourceLocation TEXTUREMAP = new ResourceLocation(ThutCore.MODID, "textures/particles.png");

    private final Codec<ParticleBase> codec = Codec.unit(this);

    public int     duration  = 10;
    public int     lifetime  = 10;
    public int     initTime  = 0;
    public long    lastTick  = 0;
    public int     animSpeed = 2;
    public float   size      = 1;
    public int     rgba      = 0xFFFFFFFF;
    public boolean billboard = true;
    public String  name      = "";
    public Vector3 velocity  = Vector3.empty;
    public Vector3 position  = Vector3.empty;
    public int[][] tex       = new int[1][2];

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
    public String writeToString()
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

    protected ParticleBase read(final StringReader reader)
    {// TODO finish this?
        return this;
    }

    protected void render(final VertexConsumer buffer, final Quaternion quaternion, final Vector3f offset)
    {
        final com.mojang.math.Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F).toMC();
        vector3f1.transform(quaternion);
        final com.mojang.math.Vector3f[] verts = new com.mojang.math.Vector3f[] {
                new com.mojang.math.Vector3f(-1.0F, -1.0F, 0.0F),
                new com.mojang.math.Vector3f(-1.0F, 1.0F, 0.0F),
                new com.mojang.math.Vector3f(1.0F, 1.0F, 0.0F),
                new com.mojang.math.Vector3f(1.0F, -1.0F, 0.0F) };
        final float f4 = this.size;

        for (int i = 0; i < 4; ++i)
        {
            final com.mojang.math.Vector3f vector3f = verts[i];
            vector3f.transform(quaternion);
            vector3f.mul(f4);
            vector3f.add(offset.x, offset.y, offset.z);
        }
        this.setColour();

        final float a = (this.rgba >> 24 & 255) / 255f;
        final float r = (this.rgba >> 16 & 255) / 255f;
        final float g = (this.rgba >> 8 & 255) / 255f;
        final float b = (this.rgba & 255) / 255f;
        // DOLATER add a configuration for the particle lightmap
        final int j = 15 << 20 | 15 << 4;

        final int num = this.getDuration() / this.animSpeed % this.tex.length;
        final int u = this.tex[num][0], v = this.tex[num][1];
        final float u1 = u * 1f / 16f, v1 = v * 1f / 16f;
        final float u2 = (u + 1) * 1f / 16f, v2 = (v + 1) * 1f / 16f;

        buffer.vertex(verts[0].x(), verts[0].y(), verts[0].z()).color(r, g, b, a).uv(u1, v2).uv2(j)
                .endVertex();
        buffer.vertex(verts[1].x(), verts[1].y(), verts[1].z()).color(r, g, b, a).uv(u2, v2).uv2(j)
                .endVertex();
        buffer.vertex(verts[2].x(), verts[2].y(), verts[2].z()).color(r, g, b, a).uv(u2, v1).uv2(j)
                .endVertex();
        buffer.vertex(verts[3].x(), verts[3].y(), verts[3].z()).color(r, g, b, a).uv(u1, v1).uv2(j)
                .endVertex();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void renderParticle(final VertexConsumer buffer, final Camera renderInfo, final float partialTicks,
            final Vector3f offset)
    {
        Quaternion quaternion;
        quaternion = renderInfo.rotation();
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

    @Override
    public Codec<ParticleBase> codec()
    {
        return this.codec;
    }
}
