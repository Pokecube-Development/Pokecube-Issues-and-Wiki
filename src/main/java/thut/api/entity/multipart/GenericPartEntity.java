package thut.api.entity.multipart;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.PartEntity;
import thut.api.maths.vecmath.Mat3f;
import thut.api.maths.vecmath.Vec3f;
import thut.core.common.ThutCore;
import thut.core.common.network.PacketPartInteract;

public abstract class GenericPartEntity<E extends Entity> extends PartEntity<E>
{
    public static class BodyNode
    {
        public List<BodyPart> parts = Lists.newArrayList();

        public void onLoad()
        {
            this.parts.forEach(p -> p.onLoad());
        }
    }

    public static class BodyPart
    {
        public String name;
        public String offset;
        public String size;

        public String ride;

        public Vec3 __pos__;
        public Vec3 __size__;
        public Vec3 __ride__;

        public void onLoad()
        {
            String[] args = this.offset.split(",");
            this.__pos__ = new Vec3(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]));
            args = this.size.split(",");
            this.__size__ = new Vec3(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]));
            if (this.ride != null)
            {
                args = this.ride.split(",");
                this.__ride__ = new Vec3(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                        Double.parseDouble(args[2]));
            }
        }
    }

    public static interface Factory<T extends GenericPartEntity<E>, E extends Entity>
    {
        T create(E parent, final float width, final float height, final float x, final float y, final float z,
                final String id);
    }

    public Vec3f r0;

    public float width;
    public float height;

    public Vec3f r;

    public final String id;

    public GenericPartEntity(E parent, final float width, final float height, final float x, final float y,
            final float z, final String id)
    {
        super(parent);

        this.id = id;

        this.width = width;
        this.height = height;

        this.dimensions = EntityDimensions.scalable(width, height);

        this.r0 = new Vec3f(x + width / 2, y, z + width / 2);
        this.r = new Vec3f(x, y, z);
    }

    public void update(final Mat3f rot, final Vec3f r, final Vec3 dr)
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
    {}

    @Override
    protected void readAdditionalSaveData(final CompoundTag compound)
    {}

    @Override
    protected void addAdditionalSaveData(final CompoundTag compound)
    {}

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(final DamageSource source, final float amount)
    {
        if (this.getLevel().isClientSide && source.getDirectEntity() instanceof Player)
        {
            final PacketPartInteract packet = new PacketPartInteract(this.id, this.getParent(),
                    source.getDirectEntity().isShiftKeyDown());
            ThutCore.packets.sendToServer(packet);
        }
        return this.getParent().hurt(source, amount);
    }

    /**
     * Returns true if Entity argument is equal to this Entity
     */
    @Override
    public boolean is(final Entity entityIn)
    {
        return this == entityIn || this.getParent() == entityIn;
    }

    @Override
    public EntityDimensions getDimensions(final Pose poseIn)
    {
        return this.dimensions;
    }

    @Override
    public InteractionResult interactAt(final Player player, final Vec3 vec, final InteractionHand hand)
    {
        if (this.getLevel().isClientSide)
        {
            final PacketPartInteract packet = new PacketPartInteract(this.id, this.getParent(), hand, vec,
                    player.isShiftKeyDown());
            ThutCore.packets.sendToServer(packet);
        }
        return this.getParent().interactAt(player, vec, hand);
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand)
    {
        if (this.getLevel().isClientSide)
        {
            final PacketPartInteract packet = new PacketPartInteract(this.id, this.getParent(), hand,
                    player.isShiftKeyDown());
            ThutCore.packets.sendToServer(packet);
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
        final EntityDimensions entitysize = this.dimensions;
        final Pose pose = this.getPose();
        final net.minecraftforge.event.entity.EntityEvent.Size sizeEvent = net.minecraftforge.event.ForgeEventFactory
                .getEntitySizeForge(this, pose, this.getDimensions(pose), this.getEyeHeight(pose, entitysize));
        final EntityDimensions entitysize1 = sizeEvent.getNewSize();
        this.dimensions = entitysize1;
        if (entitysize1.width < entitysize.width)
        {
            final double d0 = entitysize1.width / 2.0D;
            this.setBoundingBox(new AABB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0,
                    this.getY() + entitysize1.height, this.getZ() + d0));
        }
        else
        {
            final AABB axisalignedbb = this.getBoundingBox();
            this.setBoundingBox(new AABB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                    axisalignedbb.minX + entitysize1.width, axisalignedbb.minY + entitysize1.height,
                    axisalignedbb.minZ + entitysize1.width));
            if (entitysize1.width > entitysize.width && !this.firstTick && !this.level.isClientSide)
            {
                final float f = entitysize.width - entitysize1.width;
                this.move(MoverType.SELF, new Vec3(f, 0.0D, f));
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        // This can be null if this is called early enough
        if (this.getParent() == null) return super.getCapability(cap, side);
        return this.getParent().getCapability(cap, side);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap)
    {
        // This can be null if this is called early enough
        if (this.getParent() == null) return super.getCapability(cap);
        return this.getParent().getCapability(cap);
    }
}
