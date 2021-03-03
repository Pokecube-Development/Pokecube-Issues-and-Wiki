package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.entity.PartEntity;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.vecmath.Matrix3f;
import thut.api.maths.vecmath.Vector3f;

public class PokemobPart extends PartEntity<PokemobHasParts>
{
    public final PokemobHasParts base;

    public final IPokemob pokemob;

    public final Vector3f r0;

    public final float width;
    public final float height;

    public Vector3f r;

    public PokemobPart(final PokemobHasParts base, final float width, final float height, final float x, final float y,
            final float z)
    {
        super(base);

        this.width = width;
        this.height = height;

        this.size = EntitySize.flexible(width, height);
        this.pokemob = base.pokemobCap;
        this.base = base;
        this.r0 = new Vector3f(x + width / 2, y, z + width / 2);
        this.r = new Vector3f(x, y, z);
    }

    public void update(final Matrix3f rot, final Vector3f r, final Vector3d dr)
    {
        this.r.set(this.r0.getX(), this.r0.getY(), this.r0.getZ());
        rot.transform(this.r);
        this.r.add(r);
        this.setPosition(this.r.getX(), this.r.getY(), this.r.getZ());
        this.lastTickPosX = this.getPosX() + dr.x;
        this.lastTickPosY = this.getPosY() + dr.y;
        this.lastTickPosZ = this.getPosZ() + dr.z;
    }

    @Override
    protected void registerData()
    {
    }

    @Override
    protected void readAdditional(final CompoundNBT compound)
    {
    }

    @Override
    protected void writeAdditional(final CompoundNBT compound)
    {
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(final DamageSource source, final float amount)
    {
        return this.isInvulnerableTo(source) ? false : this.base.attackFromPart(this, source, amount);
    }

    /**
     * Returns true if Entity argument is equal to this Entity
     */
    @Override
    public boolean isEntityEqual(final Entity entityIn)
    {
        return this == entityIn || this.base == entityIn;
    }

    @Override
    public EntitySize getSize(final Pose poseIn)
    {
        return this.size;
    }

    @Override
    public ActionResultType applyPlayerInteraction(final PlayerEntity player, final Vector3d vec, final Hand hand)
    {
        return this.getParent().applyPlayerInteraction(player, vec, hand);
    }

    @Override
    public ActionResultType processInitialInteract(final PlayerEntity player, final Hand hand)
    {
        return this.getParent().processInitialInteract(player, hand);
    }

    @Override
    public boolean canRiderInteract()
    {
        return this.getParent().canRiderInteract();
    }

    @Override
    public boolean canBeCollidedWith()
    {
        // TODO Auto-generated method stub
        return super.canBeCollidedWith();
    }

    @Override
    public void applyEntityCollision(final Entity entityIn)
    {
        // TODO Auto-generated method stub
        super.applyEntityCollision(entityIn);
    }

    @Override
    public boolean canCollide(final Entity entity)
    {
        // TODO Auto-generated method stub
        return super.canCollide(entity);
    }

    @Override
    public void recalculateSize()
    {
        final EntitySize entitysize = this.size;
        final Pose pose = this.getPose();
        final net.minecraftforge.event.entity.EntityEvent.Size sizeEvent = net.minecraftforge.event.ForgeEventFactory
                .getEntitySizeForge(this, pose, this.getSize(pose), this.getEyeHeight(pose, entitysize));
        final EntitySize entitysize1 = sizeEvent.getNewSize();
        this.size = entitysize1;
        if (entitysize1.width < entitysize.width)
        {
            final double d0 = entitysize1.width / 2.0D;
            this.setBoundingBox(new AxisAlignedBB(this.getPosX() - d0, this.getPosY(), this.getPosZ() - d0, this
                    .getPosX() + d0, this.getPosY() + entitysize1.height, this.getPosZ() + d0));
        }
        else
        {
            final AxisAlignedBB axisalignedbb = this.getBoundingBox();
            this.setBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                    axisalignedbb.minX + entitysize1.width, axisalignedbb.minY + entitysize1.height, axisalignedbb.minZ
                            + entitysize1.width));
            if (entitysize1.width > entitysize.width && !this.firstUpdate && !this.world.isRemote)
            {
                final float f = entitysize.width - entitysize1.width;
                this.move(MoverType.SELF, new Vector3d(f, 0.0D, f));
            }
        }
    }
}
