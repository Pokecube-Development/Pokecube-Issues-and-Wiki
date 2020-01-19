package thut.api;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;

public interface IOwnable
{

    /**
     * Gets the owner as an LivingEntity, may be null if not in world, or
     * if no owner.
     */
    @Nullable
    LivingEntity getOwner();

    /**
     * Gets the owner as an LivingEntity, may be null if not in world, or
     * if no owner, this method will look up the owner as an entity in the
     * world.
     */
    @Nullable
    default LivingEntity getOwner(final ServerWorld world)
    {
        final LivingEntity owner = this.getOwner();
        if (owner == null && this.getOwnerId() != null)
        {
            final Entity mob = world.getEntityByUuid(this.getOwnerId());
            if (mob instanceof LivingEntity)
            {
                this.setOwner((LivingEntity) mob);
                return (LivingEntity) mob;
            }
        }
        return owner;
    }

    @Nullable
    /** Gets the UUID of the owner, might be null */
    UUID getOwnerId();

    /** @return Is our owner a player. */
    boolean isPlayerOwned();

    /** sets owner by specific entity. */
    void setOwner(LivingEntity e);

    /** sets owner by UUID */
    void setOwner(UUID id);
}
