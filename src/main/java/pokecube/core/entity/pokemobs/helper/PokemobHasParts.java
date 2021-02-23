package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import thut.api.maths.vecmath.Matrix3f;
import thut.api.maths.vecmath.Vector3f;

public abstract class PokemobHasParts extends PokemobCombat
{
    private PokemobPart[] parts;

    float colWidth  = 0;
    float colHeight = 0;

    float last_size = 0;

    final Matrix3f rot = new Matrix3f();
    final Vector3f r   = new Vector3f();

    public PokemobHasParts(final EntityType<? extends ShoulderRidingEntity> type, final World worldIn)
    {
        super(type, worldIn);
        this.initSizes(1);
    }

    protected void initSizes(final float size)
    {
        if (size == this.last_size) return;
        this.last_size = size;
        final float maxH = (float) PokecubeCore.getConfig().largeMobForSplit;
        final float maxW = (float) PokecubeCore.getConfig().largeMobForSplit;
        final float width = this.pokemobCap.getPokedexEntry().width * size;
        final float length = this.pokemobCap.getPokedexEntry().length * size;
        final float height = this.pokemobCap.getPokedexEntry().height * size;
        if (height > maxH || width > maxW || length > maxW)
        {
            final int nx = MathHelper.ceil(width / maxW);
            final int ny = MathHelper.ceil(length / maxW);
            final int nz = MathHelper.ceil(height / maxH);

            final float dx = width / nx;
            final float dy = length / ny;
            final float dz = height / nz;

            this.parts = new PokemobPart[nx * ny * nz];
            final float dw = Math.max(width / nx, length / ny);
            final float dh = height / nz;
            int i = 0;

            for (int y = 0; y < ny; y++)
                for (int x = 0; x < nx; x++)
                    for (int z = 0; z < nz; z++)
                        this.parts[i++] = new PokemobPart(this, dw, dh, x * dx - nx * dx / 2f, y * dy, z * dz - nz * dz
                                / 2f);

            this.colWidth = Math.min(1, maxW);
            this.colHeight = Math.min(1, maxH);
            this.ignoreFrustumCheck = true;
        }
        else
        {
            this.parts = new PokemobPart[0];
            this.ignoreFrustumCheck = false;
        }
        // This needs the larger bounding box regardless of parts, so that the
        // lookup finds the parts at all for things like projectile impact
        // calculations.
        this.size = EntitySize.fixed(width, height);
        this.recalculateSize();
    }

    @Override
    public boolean isMultipartEntity()
    {
        return this.parts.length > 0;
    }

    @Override
    public PokemobPart[] getParts()
    {
        // This only does something complex if the parts have changed, otherwise
        // it just ensures their locations are synced to us.
        this.updatePartsPos();
        return this.parts;
    }

    @Override
    public boolean canBePushed()
    {
        return !this.isMultipartEntity() && super.canBePushed();
    }

    @Override
    protected void collideWithNearbyEntities()
    {
        if (!this.isMultipartEntity()) super.collideWithNearbyEntities();
    }

    @Override
    public void applyEntityCollision(final Entity entityIn)
    {
        if (entityIn.isEntityEqual(this)) return;
        if (this.isMultipartEntity())
        {
            for (final PokemobPart part : this.getParts())
                if (part.getBoundingBox().intersects(entityIn.getBoundingBox())) part.applyEntityCollision(entityIn);
        }
        else super.applyEntityCollision(entityIn);
    }

    /**
     * Returns true if Entity argument is equal to this Entity
     */
    @Override
    public boolean isEntityEqual(final Entity entityIn)
    {
        return this == entityIn || entityIn instanceof PokemobPart && ((PokemobPart) entityIn).base == this;
    }

    @Override
    public void livingTick()
    {
        this.updatePartsPos();
        super.livingTick();
    }

    protected void updatePartsPos()
    {
        float size = this.pokemobCap.getSize();
        if (this.pokemobCap.getCombatState(CombatStates.DYNAMAX)) size = 5 / this.pokemobCap.getPokedexEntry().height;
        this.initSizes(size);
        if (this.parts.length > 0)
        {
            final Vector3d v = this.getPositionVec();
            this.r.set((float) v.getX(), (float) v.getY(), (float) v.getZ());
            final Vector3d dr = new Vector3d(this.r.x - this.lastTickPosX, this.r.y - this.lastTickPosY, this.r.z
                    - this.lastTickPosZ);
            this.rot.rotY((float) Math.toRadians(180 - this.rotationYaw));
            for (final PokemobPart p : this.parts)
                p.update(this.rot, this.r, dr);
        }
    }

    @Override
    public void move(final MoverType typeIn, final Vector3d pos)
    {
        if (this.parts.length == 0)
        {
            super.move(typeIn, pos);
            return;
        }
        final EntitySize backup = this.size;
        this.size = new EntitySize(this.colWidth, this.colHeight, true);
        this.recalculateSize();
        super.move(typeIn, pos);
        this.size = backup;
        this.recalculateSize();
    }
}
