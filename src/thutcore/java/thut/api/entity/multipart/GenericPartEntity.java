package thut.api.entity.multipart;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityEvent;

import org.joml.Matrix4f;
import thut.core.common.ThutCore;
import thut.core.common.network.PartInteract;

public abstract class GenericPartEntity<E extends Entity> extends PartEntity<E>
{
    public static class BodyNode
    {
        public List<BodyPart> parts = Lists.newArrayList();

        public void onLoad()
        {
            this.parts.forEach(BodyPart::onLoad);
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
                this.__ride__ = new Vec3(Double.parseDouble(args[0]) - __pos__.x,
                        Double.parseDouble(args[1]) - __pos__.y, Double.parseDouble(args[2]) - __pos__.z);
            }
        }
    }

    public static interface Factory<T extends GenericPartEntity<E>, E extends Entity>
    {
        T create(E parent, final float width, final float height, final float x, final float y, final float z,
                final String id);
    }

    public final String id;

    protected GenericPartEntity(E parent, final String id)
    {
        super(parent);
        this.id = id;

        // Hackery to use identical attachment map
        try
        {
            Field F = AttachmentHolder.class.getDeclaredField("attachments");
            F.setAccessible(true);
            F.set(this, F.get(parent));
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            ThutCore.LOGGER.error(e);
        }
    }

    public abstract void update(Matrix4f transform, Vec3 dr);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {}

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(final DamageSource source, final float amount)
    {
        if (this.level().isClientSide && source.getDirectEntity() instanceof Player)
        {
            final PartInteract packet = new PartInteract(this.id, this.getParent(),
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
        if (this.level().isClientSide)
        {
            final PartInteract packet = new PartInteract(this.id, this.getParent(), hand, vec,
                    player.isShiftKeyDown());
            ThutCore.packets.sendToServer(packet);
        }
        return this.getParent().interactAt(player, vec, hand);
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand)
    {
        if (this.level().isClientSide)
        {
            final PartInteract packet = new PartInteract(this.id, this.getParent(), hand,
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
        final EntityDimensions entitysize = this.getDimensions(null);
        final Pose pose = this.getPose();
        
        final EntityEvent.Size sizeEvent = EventHooks
                .getEntitySizeForge(this, pose, this.getDimensions(pose));
        final EntityDimensions entitysize1 = sizeEvent.getNewSize();
        this.dimensions = entitysize1;
        if (entitysize1.width() < entitysize.width())
        {
            final double d0 = entitysize1.width() / 2.0D;
            this.setBoundingBox(new AABB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0,
                    this.getY() + entitysize1.height(), this.getZ() + d0));
        }
        else
        {
            final AABB axisalignedbb = this.getBoundingBox();
            this.setBoundingBox(new AABB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
                    axisalignedbb.minX + entitysize1.width(), axisalignedbb.minY + entitysize1.height(),
                    axisalignedbb.minZ + entitysize1.width()));
            if (entitysize1.width() > entitysize.width() && !this.firstTick && !this.level().isClientSide)
            {
                final float f = entitysize.width() - entitysize1.width();
                this.move(MoverType.SELF, new Vec3(f, 0.0D, f));
            }
        }
    }

    @Override
    public float maxUpStep()
    {
        return this.getParent().maxUpStep();
    }

    @Override
    public ItemStack getPickedResult(HitResult target)
    {
        return this.getParent().getPickedResult(target);
    }
}
