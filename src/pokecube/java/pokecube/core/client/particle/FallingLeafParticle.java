package pokecube.core.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class FallingLeafParticle extends TextureSheetParticle {
    private static final float ACCELERATION_SCALE = 0.0025F;
    private static final int INITIAL_LIFETIME = 300;
    private static final int CURVE_ENDPOINT_TIME = 300;
    private static final float WIND_BIG = 2.0F;
    private float rotSpeed;
    private final float particleRandom;
    private final float spinAcceleration;

    public FallingLeafParticle(ClientLevel clientWorld, double x, double y, double z, SpriteSet spriteSet)
    {
        super(clientWorld, x, y, z);
        this.setSprite(spriteSet.get(this.random.nextInt(12), 12));
        this.rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0D : 30.0D);
        this.particleRandom = this.random.nextFloat();
        this.spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0D : 5.0D);
        this.lifetime = INITIAL_LIFETIME;
        this.gravity = 7.5E-4F;
        float f = this.random.nextBoolean() ? 0.05F : 0.075F;
        this.quadSize = f;
        this.setSize(f, f);
        this.friction = 1.0F;
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0)
        {
            this.remove();
        }

        if (!this.removed)
        {
            float f = (float)(CURVE_ENDPOINT_TIME - this.lifetime);
            float f1 = Math.min(f / INITIAL_LIFETIME, 1.0F);
            double d0 = Math.cos(Math.toRadians(this.particleRandom * 60.0F)) * WIND_BIG * Math.pow(f1, 1.25D);
            double d1 = Math.sin(Math.toRadians(this.particleRandom * 60.0F)) * WIND_BIG * Math.pow(f1, 1.25D);
            this.xd += d0 * (double)ACCELERATION_SCALE;
            this.zd += d1 * (double)ACCELERATION_SCALE;
            this.yd -= this.gravity;
            this.rotSpeed += this.spinAcceleration / 20.0F;
            this.oRoll = this.roll;
            this.roll += this.rotSpeed / 20.0F;
            this.move(this.xd, this.yd, this.zd);
            if (this.onGround || this.lifetime < 299 && (this.xd == 0.0D || this.zd == 0.0D))
            {
                this.remove();
            }

            if (!this.removed)
            {
                this.xd *= this.friction;
                this.yd *= this.friction;
                this.zd *= this.friction;
            }
        }
    }
}