package thut.api.entity.multipart;

import java.lang.reflect.Field;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.entity.PartEntity;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import thut.core.common.ThutCore;
import thut.core.common.network.PartInteract;

public abstract class GenericPartEntity<E extends Entity> extends PartEntity<E>
{
    public final Vector4f r = new Vector4f();
    public Vector3f ride_point = null;

    public float width;
    public float height;
    public float requiredShift = 0;

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

    public abstract void update(Matrix4f transform);

    public void applyPos(Vec3 dr)
    {
        this.setPos(r.x, r.y - requiredShift, r.z);
        this.xOld = this.getX() + dr.x;
        this.yOld = this.getY() + dr.y;
        this.zOld = this.getZ() + dr.z;
    }

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
    public boolean is(final Entity entity)
    {
        if (entity == this.getParent()) return true;
        if (entity instanceof PartEntity<?> p && p.getParent() == this.getParent()) return true;
        return this == entity;
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
    public void push(final Entity entity)
    {
        if (entity == this.getParent()) return;
        if (entity instanceof PartEntity<?> p && p.getParent() == this.getParent()) return;
        super.push(entity);
    }

    @Override
    public boolean canCollideWith(final Entity entity)
    {
        if (entity == this.getParent()) return false;
        if (entity instanceof PartEntity<?> p && p.getParent() == this.getParent()) return false;
        return super.canCollideWith(entity);
    }

    @Override
    public void refreshDimensions()
    {
        EntityDimensions entitydimensions = this.dimensions;
        Pose pose = this.getPose();
        EntityDimensions entitydimensions1 = this.getDimensions(pose);
        this.dimensions = entitydimensions1;
        this.reapplyPosition();
        boolean flag = (double)entitydimensions1.width() <= 4.0 && (double)entitydimensions1.height() <= 4.0;
        if (!this.level.isClientSide
                && !this.firstTick
                && !this.noPhysics
                && flag
                && (entitydimensions1.width() > entitydimensions.width() || entitydimensions1.height() > entitydimensions.height())
                ) { // had a player check here
            this.fudgePositionAfterSizeChange(entitydimensions);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn)
    {
        return EntityDimensions.fixed(width, height);
    }

    @Override
    public boolean onGround()
    {
        return this.getParent().onGround();
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
