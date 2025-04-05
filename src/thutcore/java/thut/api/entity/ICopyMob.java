package thut.api.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import thut.api.entity.event.CopySetEvent;
import thut.api.entity.event.CopyUpdateEvent;
import thut.core.common.ThutCore;
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
    default void deserializeNBT(HolderLookup.Provider provider, final CompoundTag nbt)
    {
        if (nbt.contains("id")) this.setCopiedID(ResourceLocation.parse(nbt.getString("id")));
        else this.setCopiedID(null);
        this.setCopiedNBT(nbt.getCompound("tag"));
    }

    default void recreateMob(Level level)
    {
        if (this.getCopiedMob() == null || this.getCopiedMob().level() != level)
        {
            this.setCopiedMob((LivingEntity) EntityType.loadEntityRecursive(getCopiedNBT(), level, e -> e));
        }
    }

    @Override
    default CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        final CompoundTag nbt = new CompoundTag();
        if (this.getCopiedID() != null) nbt.putString("id", this.getCopiedID().toString());
        if (this.getCopiedMob() != null)
        {
            var mob = this.getCopiedMob();
            CompoundTag ret = new CompoundTag();
            String id = mob.getEncodeId();
            if (id != null)
            {
                ret.putString("id", id);
            }
            this.setCopiedNBT(mob.saveWithoutId(ret));
        }
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
                    var event = new CopySetEvent(holder, mob, null);
                    ThutCore.FORGE_BUS.post(event);
                    if (event.isCanceled())
                    {
                        this.setCopiedID(RegHelper.getKey(this.getCopiedMob().getType()));
                        this.setCopiedMob(mob);
                        CompoundTag ret = new CompoundTag();
                        String id = mob.getEncodeId();
                        if (id != null)
                        {
                            ret.putString("id", id);
                        }
                        this.setCopiedNBT(mob.saveWithoutId(ret));
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
            final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(this.getCopiedID());
            final Entity entity = type.create(level);
            if (entity instanceof LivingEntity mob)
            {
                var event = new CopySetEvent(holder, null, mob);
                ThutCore.FORGE_BUS.post(event);
                if (event.isCanceled())
                {
                    this.setCopiedID(null);
                    this.setCopiedNBT(new CompoundTag());
                    return;
                }
                try
                {
                    CompoundTag ret = this.getCopiedNBT();
                    String id = this.getCopiedID().toString();
                    if (id != null)
                    {
                        ret.putString("id", id);
                    }
                    this.setCopiedNBT(mob.saveWithoutId(ret));
                    mob.load(ret);
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

            living.onAddedToLevel();
            living.baseTick();
            living.onRemovedFromLevel();

            // TODO eye height check?
            final float eye = living.getEyeHeight(holder.getPose());
            if (eye != holder.getEyeHeight(holder.getPose())) holder.refreshDimensions();

            living.setItemInHand(InteractionHand.MAIN_HAND, holder.getItemInHand(InteractionHand.MAIN_HAND));
            living.setItemInHand(InteractionHand.OFF_HAND, holder.getItemInHand(InteractionHand.OFF_HAND));

            var event = new CopyUpdateEvent(living, holder);
            ThutCore.FORGE_BUS.post(event);
            if (!event.isCanceled())
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
        to.setXRot(from.getXRot());
        to.tickCount = from.tickCount;
        to.setYRot(from.getYRot());
        to.setYHeadRot(from.getYHeadRot());
        to.xRotO = from.xRotO;
        to.yRotO = from.yRotO;
    }

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
        // TODO more variable syncing
//        to.wasTouchingWater = from.wasTouchingWater;
//        to.fluidHeight = from.fluidHeight;
//        to.fluidOnEyes.clear();
//        to.fluidOnEyes.addAll(from.fluidOnEyes);
    }
}
