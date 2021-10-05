package thut.core.client.render.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.api.particle.ParticleBase;
import thut.api.particle.ParticleNoGravity;
import thut.api.particle.ThutParticles;

@OnlyIn(value = Dist.CLIENT)
public class ParticleFactories
{
    public static class RenderType implements ParticleRenderType
    {
        @Override
        public void begin(final BufferBuilder builder, final TextureManager textures)
        {
            textures.bindForSetup(ParticleBase.TEXTUREMAP);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }

        @Override
        public void end(final Tesselator tes)
        {
            tes.end();
        }

    }

    public static class ThutParticle extends Particle
    {
        final ParticleBase particle;
        final Level        world;

        protected ThutParticle(final Level worldIn, final ParticleBase particleIn)
        {
            super((ClientLevel) worldIn, particleIn.position.x, particleIn.position.y, particleIn.position.z);
            this.particle = particleIn;
            this.world = worldIn;
            if (this.particle instanceof ParticleNoGravity) this.gravity = 0;
            this.xd = this.particle.velocity.x;
            this.yd = this.particle.velocity.z;
            this.zd = this.particle.velocity.y;
            this.setLifetime(this.particle.lifetime);
        }

        @Override
        public int getLifetime()
        {
            return this.particle.lifetime;
        }

        @Override
        public ParticleRenderType getRenderType()
        {
            return ParticleFactories.TYPE;
        }

        @Override
        public boolean isAlive()
        {
            return this.particle.getDuration() >= 0;
        }

        @Override
        public void render(final VertexConsumer buffer, final Camera renderInfo, final float partialTicks)
        {
            final Vec3 vec3d = renderInfo.getPosition();
            final float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - vec3d.x);
            final float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - vec3d.y);
            final float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - vec3d.z);
            final Vector3f source = new Vector3f(x, y, z);
            this.particle.renderParticle(buffer, renderInfo, partialTicks, source);
        }

        @Override
        public void tick()
        {
            this.age = 0;
            super.tick();
            this.particle.setDuration(this.particle.getDuration() - 1);
            this.particle.setLastTick(this.world.getGameTime());
            if (this.particle.getDuration() < 0)
            {
                this.particle.kill();
                this.remove();
            }
        }

    }

    public static final RenderType TYPE = new RenderType();

    public static final ParticleProvider<ParticleBase> GENERICFACTORY = (type, world, x, y, z, vx, vy, vz) ->
    {
        type = ThutParticles.clone(type);
        type.setVelocity(Vector3.getNewVector().set(vx, vy, vz));
        type.setPosition(Vector3.getNewVector().set(x, y, z));
        return new ThutParticle(world, type);
    };
}
