package thut.core.client.render.particle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.maths.Vector3;
import thut.api.particle.ParticleBase;
import thut.api.particle.ParticleNoGravity;
import thut.api.particle.ThutParticles;

@OnlyIn(value = Dist.CLIENT)
public class ParticleFactories
{
    public static class RenderType implements IParticleRenderType
    {

        @Override
        public void beginRender(final BufferBuilder builder, final TextureManager textures)
        {
            // textures.bindTexture(ParticleBase.TEXTUREMAP);
            // builder.begin(GL11.GL_QUADS,
            // DefaultVertexFormats.POSITION_TEX_COLOR);
        }

        @Override
        public void finishRender(final Tessellator tes)
        {
            // tes.draw();
        }

    }

    public static class ThutParticle extends Particle
    {
        final ParticleBase particle;
        final World        world;

        protected ThutParticle(final World worldIn, final ParticleBase particleIn)
        {
            super(worldIn, particleIn.position.x, particleIn.position.y, particleIn.position.z);
            this.particle = particleIn;
            this.world = worldIn;
            if (this.particle instanceof ParticleNoGravity) this.particleGravity = 0;
            this.motionX = this.particle.velocity.x;
            this.motionY = this.particle.velocity.z;
            this.motionZ = this.particle.velocity.y;
            this.setMaxAge(this.particle.lifetime);
        }

        @Override
        public int getMaxAge()
        {
            return this.particle.lifetime;
        }

        @Override
        public IParticleRenderType getRenderType()
        {
            return IParticleRenderType.CUSTOM;
        }

        @Override
        public boolean isAlive()
        {
            return this.particle.getDuration() >= 0;
        }

        @Override
        public void renderParticle(final BufferBuilder buffer, final ActiveRenderInfo entityIn,
                final float partialTicks, final float rotationX, final float rotationZ, final float rotationYZ,
                final float rotationXY, final float rotationXZ)
        {
            GL11.glPushMatrix();
            final Vector3 source = Vector3.getNewVector();
            final float x = (float) (MathHelper.lerp(partialTicks, this.prevPosX, this.posX) - Particle.interpPosX);
            final float y = (float) (MathHelper.lerp(partialTicks, this.prevPosY, this.posY) - Particle.interpPosY);
            final float z = (float) (MathHelper.lerp(partialTicks, this.prevPosZ, this.posZ) - Particle.interpPosZ);
            source.set(x, y, z);
            GL11.glTranslated(source.x, source.y, source.z);
            this.particle.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY,
                    rotationXZ);
            GL11.glPopMatrix();
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
                this.setExpired();
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
