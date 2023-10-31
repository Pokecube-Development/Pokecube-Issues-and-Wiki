package thut.api.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import thut.api.entity.event.CopySetEvent;
import thut.api.entity.event.CopyUpdateEvent;
import thut.lib.RegHelper;
import thut.mixin.accessors.WalkAniAccessor;

public interface ICopyMob extends INBTSerializable<CompoundTag>
{
    ResourceLocation getCopiedID();

    LivingEntity getCopiedMob();

    CompoundTag getCopiedNBT();

    void setCopiedID(ResourceLocation id);

    void setCopiedMob(LivingEntity mob);

    void setCopiedNBT(CompoundTag tag);

    @Override
    default void deserializeNBT(final CompoundTag nbt)
    {
        if (nbt.contains("id")) this.setCopiedID(new ResourceLocation(nbt.getString("id")));
        else this.setCopiedID(null);
        this.setCopiedNBT(nbt.getCompound("tag"));
    }

    @Override
    default CompoundTag serializeNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        if (this.getCopiedID() != null) nbt.putString("id", this.getCopiedID().toString());
        if (this.getCopiedMob() != null) nbt.put("tag", this.getCopiedMob().serializeNBT());
        else if (!this.getCopiedNBT().isEmpty()) nbt.put("tag", this.getCopiedNBT());
        return nbt;
    }

    default void baseInit(final @Nonnull Level level, final @Nullable LivingEntity holder)
    {
        if (this.getCopiedID() == null)
        {
            if (this.getCopiedMob() != null)
            {
                if (holder != null)
                {
                    final LivingEntity mob = this.getCopiedMob();
                    if (MinecraftForge.EVENT_BUS.post(new CopySetEvent(holder, mob, null)))
                    {
                        this.setCopiedID(RegHelper.getKey(this.getCopiedMob().getType()));
                        this.setCopiedMob(mob);
                        this.setCopiedNBT(mob.serializeNBT());
                        return;
                    }
                }
                this.setCopiedMob(null);
                this.setCopiedNBT(new CompoundTag());
            }
            return;
        }
        if (this.getCopiedMob() == null || !this.getCopiedID().equals(RegHelper.getKey(this.getCopiedMob().getType())))
        {
            final EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(this.getCopiedID());
            final Entity entity = type.create(level);
            if (entity instanceof LivingEntity mob)
            {
                if (MinecraftForge.EVENT_BUS.post(new CopySetEvent(holder, null, mob)))
                {
                    this.setCopiedID(null);
                    this.setCopiedNBT(new CompoundTag());
                    return;
                }
                try
                {
                    System.out.println(this.getCopiedNBT());
                    mob.deserializeNBT(this.getCopiedNBT());
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
                this.setCopiedMob(mob);
            }
            else
            {
                this.setCopiedID(null);
                this.setCopiedNBT(new CompoundTag());
                return;
            }
        }
    }

    default void onBaseTick(final @Nonnull Level level, final @Nullable LivingEntity holder)
    {
        baseInit(level, holder);
        final LivingEntity living = this.getCopiedMob();
        if (living != null && holder != null)
        {
            living.setId(-(holder.getId() + 100));
            living.noPhysics = true;
            living.level = holder.level;

            ICopyMob.copyEntityTransforms(living, holder);
            ICopyMob.copyPositions(living, holder);

            living.onAddedToWorld();
            living.baseTick();
            living.onRemovedFromWorld();

            final float eye = living.getEyeHeight(holder.getPose(), holder.getDimensions(holder.getPose()));
            if (eye != holder.getEyeHeight(holder.getPose(), holder.getDimensions(holder.getPose())))
                holder.refreshDimensions();

            living.setItemInHand(InteractionHand.MAIN_HAND, holder.getItemInHand(InteractionHand.MAIN_HAND));
            living.setItemInHand(InteractionHand.OFF_HAND, holder.getItemInHand(InteractionHand.OFF_HAND));

            if (!MinecraftForge.EVENT_BUS.post(new CopyUpdateEvent(living, holder)))
            {
                living.setHealth(holder.getHealth());
                living.setAirSupply(holder.getAirSupply());
            }
        }
    }

    public static void copyPositions(final Entity to, final Entity from)
    {
        to.xOld = from.xOld;
        to.yOld = from.yOld;
        to.zOld = from.zOld;

        to.xo = from.xo;
        to.yo = from.yo;
        to.zo = from.zo;

        to.setPos(from.getX(), from.getY(), from.getZ());
        to.setDeltaMovement(from.getDeltaMovement());
    }

    public static void copyRotations(final Entity to, final Entity from)
    {
        to.xRot = from.xRot;
        to.tickCount = from.tickCount;
        to.yRot = from.yRot;
        to.setYHeadRot(from.getYHeadRot());
        to.xRotO = from.xRotO;
        to.yRotO = from.yRotO;
    }

    @SuppressWarnings("deprecation")
    public static void copyEntityTransforms(final LivingEntity to, final LivingEntity from)
    {
        ICopyMob.copyRotations(to, from);

        to.yHeadRotO = from.yHeadRotO;
        to.yBodyRotO = from.yBodyRotO;
        to.yBodyRot = from.yBodyRot;

        WalkAniAccessor toWalk = (WalkAniAccessor) to.walkAnimation;
        WalkAniAccessor fromWalk = (WalkAniAccessor) from.walkAnimation;
        
        toWalk.copyCap$setPosition(fromWalk.copyCap$position());
        toWalk.copyCap$setSpeedOld(fromWalk.copyCap$speedOld());
        toWalk.copyCap$setSpeed(fromWalk.copyCap$speed());

        to.setOnGround(from.onGround());
        to.wasTouchingWater = from.wasTouchingWater;
        to.fluidHeight = from.fluidHeight;
        to.fluidOnEyes.clear();
        to.fluidOnEyes.addAll(from.fluidOnEyes);
    }
}