package thut.api.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.EventHooks;
import thut.api.entity.event.CopySetEvent;
import thut.api.entity.event.CopyUpdateEvent;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;
import thut.mixin.accessors.WalkAniAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICopyMob extends INBTSerializable<CompoundTag>
{
    ResourceLocation getCopiedID();

    LivingEntity getCopiedMob();

    CompoundTag getCopiedNBT();

    void setCopiedID(ResourceLocation id);

    void setCopiedMob(LivingEntity mob);

    void setCopiedNBT(CompoundTag tag);

    void setFullTick(boolean fullTick);

    /**
     * @return whether we run living.tick(), if false we run living.baseTick() instead
     */
    boolean isFullTick();

    @Override
    default void deserializeNBT(HolderLookup.Provider provider, final CompoundTag nbt)
    {
        var oldId = this.getCopiedID();
        var oldMob = this.getCopiedMob();
        if (nbt.contains("id")) this.setCopiedID(ResourceLocation.parse(nbt.getString("id")));
        else this.setCopiedID(null);
        var tag = nbt.getCompound("tag");
        this.setCopiedNBT(nbt.getCompound("tag"));
        if (oldId != null && oldId.equals(this.getCopiedID()) && oldMob != null) oldMob.load(tag);
    }

    default boolean recreateMob(Level level)
    {
        if (this.getCopiedMob() == null || this.getCopiedMob().level() != level)
        {
            this.setCopiedMob((LivingEntity) EntityType.loadEntityRecursive(getCopiedNBT(), level, e -> e));
            return true;
        }
        return false;
    }

    @Override
    default CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        final CompoundTag nbt = new CompoundTag();
        if (this.getCopiedID() != null) nbt.putString("id", this.getCopiedID().toString());
        CompoundTag tag = this.getCopiedNBT();
        if (this.getCopiedMob() != null)
        {
            var mob = this.getCopiedMob();
            CompoundTag ret = new CompoundTag();
            String id = mob.getEncodeId();
            if (id != null) ret.putString("id", id);
            tag.merge(mob.saveWithoutId(ret));
        }
        if (!tag.isEmpty())
        {
            nbt.put("tag", tag);
        }
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
            var tag = getCopiedNBT();
            tag.putString("id", getCopiedID().toString());
            final Entity entity = EntityType.loadEntityRecursive(tag, level, e -> e);
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
                this.setCopiedMob(mob);
            }
            else
            {
                this.setCopiedID(null);
                this.setCopiedNBT(new CompoundTag());
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
            if (isFullTick())
            {
                EventHooks.fireEntityTickPre(living);
                living.tick();
                EventHooks.fireEntityTickPost(living);
            }
            else living.baseTick();
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
        ICopyMob.copyPositions(to, from);

        WalkAniAccessor toWalk = (WalkAniAccessor) to.walkAnimation;
        WalkAniAccessor fromWalk = (WalkAniAccessor) from.walkAnimation;

        toWalk.copyCap$setPosition(fromWalk.copyCap$position());
        toWalk.copyCap$setSpeedOld(fromWalk.copyCap$speedOld());
        toWalk.copyCap$setSpeed(fromWalk.copyCap$speed());

        to.setOnGround(from.onGround());
        // TODO more variable syncing, used to do fluids, etc
    }
}
