package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.PartEntity;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketPartInteract;
import thut.api.maths.vecmath.Matrix3f;
import thut.api.maths.vecmath.Vector3f;

public class PokemobPart extends PartEntity<PokemobHasParts>
{
    public final PokemobHasParts base;

    public final IPokemob pokemob;

    public Vector3f r0;

    public float width;
    public float height;

    public Vector3f r;

    public final String id;

    public PokemobPart(final PokemobHasParts base, final float width, final float height, final float x, final float y,
            final float z, final String id)
    {
        super(base);

        this.width = width;
        this.height = height;

        this.id = id;

        this.dimensions = EntitySize.scalable(width, height);
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
        this.setPos(this.r.getX(), this.r.getY(), this.r.getZ());
        this.xOld = this.getX() + dr.x;
        this.yOld = this.getY() + dr.y;
        this.zOld = this.getZ() + dr.z;
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    protected void readAdditionalSaveData(final CompoundNBT compound)
    {
    }

    @Override
    protected void addAdditionalSaveData(final CompoundNBT compound)
    {
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(final DamageSource source, final float amount)
    {
        if (this.getCommandSenderWorld().isClientSide && source.getDirectEntity() instanceof PlayerEntity)
        {
            final PacketPartInteract packet = new PacketPartInteract(this.id, this.getParent(), source
                    .getDirectEntity().isShiftKeyDown());
            PokecubeCore.packets.sendToServer(packet);
        }
        return this.base.isInvulnerableTo(source) ? false : this.base.attackFromPart(this, source, amount);
    }

    /**
     * Returns true if Entity argument is equal to this Entity
     */
    @Override
    public boolean is(final Entity entityIn)
    {
        return this == entityIn || this.base == entityIn;
    }

    @Override
    public EntitySize getDimensions(final Pose poseIn)
    {
        return this.dimensions;
    }

    @Override
    public ActionResultType interactAt(final PlayerEntity player, final Vector3d vec, final Hand hand)
    {
        if (this.getCommandSenderWorld().isClientSide)
        {
            final PacketPartInteract packet = new PacketPartInteract(this.id, this.getParent(), hand, vec, player
                    .isShiftKeyDown());
            PokecubeCore.packets.sendToServer(packet);
        }
        return this.getParent().interactAt(player, vec, hand);
    }

    @Override
    public ActionResultType interact(final PlayerEntity player, final Hand hand)
    {
        if (this.getCommandSenderWorld().isClientSide)
        {
            final PacketPartInteract packet = new PacketPartInteract(this.id, this.getParent(), hand, player
                    .isShiftKeyDown());
            PokecubeCore.packets.sendToServer(packet);
        }
        return this.getParent().interact(player, hand);
    }

    @Override
    public boolean canRiderInteract()
    {
        return this.getParent().canRiderInteract();
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public void push(final Entity entityIn)
    {
        super.push(entityIn);
    }

    @Override
    public boolean canCollideWith(final Entity entity)
    {
        return super.canCollideWith(entity);
    }

    @Override
    public void refreshDimensions()
    {
        final EntitySize entitysize = this.dimensions;
        final Pose pose = this.getPose();
        final net.minecraftforge.event.entity.EntityEvent.Size sizeEvent = net.minecraftforge.event.ForgeEventFactory
                .getEntitySizeForge(this, pose, this.getDimensions(pose), this.getEyeHeight(pose, entitysize));
        final EntitySize entitysize1 = sizeEvent.getNewSize();
        this.dimensions = entitysize1;
        if (entitysize1.width < entitysize.width)
        {
            final double d0 = entitysize1.width / 2.0D;
            this.setBoundingBox(new AxisAlignedBB(this.getX() - d0, this.getY(), this.getZ() - d0, this
                    .getX() + d0, this.getY() + entitysize1.height, this.getZ() + d0));
        }
        else
        {
            final AxisAlignedBB axisalignedbb = this.getBoundingBox();
            this.setBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                    axisalignedbb.minX + entitysize1.width, axisalignedbb.minY + entitysize1.height, axisalignedbb.minZ
                            + entitysize1.width));
            if (entitysize1.width > entitysize.width && !this.firstTick && !this.level.isClientSide)
            {
                final float f = entitysize.width - entitysize1.width;
                this.move(MoverType.SELF, new Vector3d(f, 0.0D, f));
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        return this.getParent().getCapability(cap, side);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap)
    {
        return this.getParent().getCapability(cap);
    }
}
