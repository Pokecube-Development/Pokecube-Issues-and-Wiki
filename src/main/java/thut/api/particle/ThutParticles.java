package thut.api.particle;

import java.util.Random;

import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.common.ThutCore;

public class ThutParticles
{
    public static final ParticleNoGravity  STRING = new ParticleNoGravity(8, 5);
    public static final ParticleNoGravity  AURORA = new ParticleNoGravity(0, 0);
    public static final ParticleNoGravity  MISC   = new ParticleNoGravity(0, 0);
    public static final ParticleNoGravity  POWDER = new ParticleNoGravity(0, 0);
    public static final ParticleOrientable LEAF   = new ParticleOrientable(2, 2);

    public static ParticleBase clone(final ParticleBase type)
    {
        ParticleBase ret = null;
        if (type == ThutParticles.AURORA) ret = new ParticleNoGravity(0, 0);
        else if (type == ThutParticles.STRING) ret = new ParticleNoGravity(8, 5);
        else if (type == ThutParticles.MISC) ret = new ParticleNoGravity(0, 0);
        else if (type == ThutParticles.POWDER) ret = new ParticleNoGravity(0, 0);
        else if (type == ThutParticles.LEAF)
        {
            ret = new ParticleOrientable(2, 2);
            ((ParticleOrientable) ret).setOrientation(((ParticleOrientable) type).orientation);
        }
        if (ret != null)
        {
            ret.setVelocity(type.velocity.copy());
            ret.setTex(type.tex.clone());
            ret.setLastTick(type.lastTick());
            ret.setAnimSpeed(type.animSpeed);
            ret.setLifetime(type.lifetime);
            ret.setSize(type.size);
            ret.setStartTime(type.initTime);
            ret.setColour(type.rgba);
            ret.name = type.name;
        }
        return ret;
    }

    public static IParticleData makeParticle(String name, final Vector3 pos, final Vector3 vel, final int... args)
    {
        if (!name.toLowerCase().equals(name)) ThutCore.LOGGER.error("Error with particle name of: " + name);

        name = name.toLowerCase();
        ParticleBase ret = null;
        if (name.equalsIgnoreCase("string"))
        {
            final ParticleNoGravity particle = ThutParticles.STRING;
            particle.setVelocity(vel);
            ret = particle;
        }
        else if (name.equalsIgnoreCase("aurora"))
        {
            final ParticleNoGravity particle = ThutParticles.AURORA;
            particle.setVelocity(vel);
            final int[][] textures = new int[2][2];
            textures[0][0] = 2;
            textures[0][1] = 4;
            textures[1][0] = 1;
            textures[1][1] = 4;
            particle.setTex(textures);
            int life = 32;
            if (args.length > 1) life = args[1];
            particle.setStartTime(ThutCore.newRandom().nextInt(100));
            particle.setAnimSpeed(1);
            particle.setLifetime(life);
            particle.setSize(0.1f);
            particle.name = "aurora";
            ret = particle;
        }
        else if (name.equalsIgnoreCase("misc"))
        {
            final ParticleNoGravity particle = ThutParticles.MISC;
            particle.setVelocity(vel);
            final int[][] textures = new int[2][2];
            textures[0][0] = 2;
            textures[0][1] = 4;
            textures[1][0] = 1;
            textures[1][1] = 4;
            particle.setTex(textures);
            int life = 32;
            if (args.length > 0) particle.setColour(args[0]);
            if (args.length > 1) life = args[1];
            particle.setLifetime(life);
            particle.setSize(0.15f);
            ret = particle;
        }
        else if (name.equalsIgnoreCase("powder"))
        {
            final ParticleNoGravity particle = ThutParticles.POWDER;
            particle.setVelocity(vel);
            final int[][] textures = new int[7][2];
            textures[0][0] = 0;
            textures[0][1] = 0;
            textures[1][0] = 1;
            textures[1][1] = 0;
            textures[2][0] = 2;
            textures[2][1] = 0;
            textures[3][0] = 3;
            textures[3][1] = 0;
            textures[4][0] = 4;
            textures[4][1] = 0;
            textures[5][0] = 5;
            textures[5][1] = 0;
            textures[6][0] = 6;
            textures[6][1] = 0;
            particle.setTex(textures);
            particle.setSize(0.125f);
            int life = 32;
            if (args.length > 0) particle.setColour(args[0]);
            if (args.length > 1) life = args[1];
            particle.setLifetime(life);
            ret = particle;
        }
        else if (name.equalsIgnoreCase("leaf"))
        {
            final ParticleOrientable particle = ThutParticles.LEAF;
            particle.setLifetime(20);
            particle.setVelocity(vel);
            particle.size = 0.25f;
            if (vel != null)
            {
                final Vector3 normal = vel.normalize().copy();
                final Vector4 v3 = new Vector4(0, 1, 0, (float) (90 - normal.toSpherical().z * 180 / Math.PI));
                final Vector4 v2 = new Vector4(1, 0, 0, (float) (90 + normal.y * 180 / Math.PI));
                particle.setOrientation(v3.addAngles(v2).toQuaternion());
            }
            ret = particle;
        }
        if (ret == null)
        {
            final ResourceLocation location = new ResourceLocation(name);
            final ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(location);
            if (type != null) return (IParticleData) type;
        }

        if (ret == null)
        {
            final ParticleNoGravity particle = ThutParticles.MISC;
            particle.setVelocity(vel);
            ret = particle;
        }
        return ret;
    }
}
