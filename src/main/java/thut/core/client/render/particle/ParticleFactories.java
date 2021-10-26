package thut.core.client.render.particle;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
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
    public static class RenderType implements IParticleRenderType
    {
        @Override
        public void begin(final BufferBuilder builder, final TextureManager textures)
        {
            textures.bind(ParticleBase.TEXTUREMAP);
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        }

        @Override
        public void end(final Tessellator tes)
        {
            tes.end();
        }

    }

    public static class ThutParticle extends Particle
    {
        final ParticleBase particle;
        final World        world;

        protected ThutParticle(final World worldIn, final ParticleBase particleIn)
        {
            super((ClientWorld) worldIn, particleIn.position.x, particleIn.position.y, particleIn.position.z);
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
        public IParticleRenderType getRenderType()
        {
            return ParticleFactories.TYPE;
        }

        @Override
        public boolean isAlive()
        {
            return this.particle.getDuration() >= 0;
        }

        @Override
        public void render(final IVertexBuilder buffer, final ActiveRenderInfo renderInfo,
                final float partialTicks)
        {
            final Vector3d vec3d = renderInfo.getPosition();
            final float x = (float) (MathHelper.lerp(partialTicks, this.xo, this.x) - vec3d.x);
            final float y = (float) (MathHelper.lerp(partialTicks, this.yo, this.y) - vec3d.y);
            final float z = (float) (MathHelper.lerp(partialTicks, this.zo, this.z) - vec3d.z);
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

    public static final IParticleFactory<ParticleBase> GENERICFACTORY = (type, world, x, y, z, vx, vy, vz) ->
    {
        type = ThutParticles.clone(type);
        type.setVelocity(Vector3.getNewVector().set(vx, vy, vz));
        type.setPosition(Vector3.getNewVector().set(x, y, z));
        return new ThutParticle(world, type);
    };
}
