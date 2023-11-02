package thut.api;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface IOwnable
{

    /**
     * Gets the owner as an LivingEntity, may be null if not in world, or if no
     * owner.
     */
    @Nullable
    LivingEntity getOwner();

    /**
     * Gets the owner as an LivingEntity, may be null if not in world, or if no
     * owner, this method will look up the owner as an entity in the world.
     */
    @Nullable
    default LivingEntity getOwner(final ServerLevel world)
    {
        return this.getOwner(world, this.getOwner());
    }

    default LivingEntity getOwner(final ServerLevel world, final LivingEntity _default)
    {
        if (_default == null && this.getOwnerId() != null)
        {
            final Entity mob = world.getEntity(this.getOwnerId());
            if (mob instanceof LivingEntity living)
            {
                this.setOwner(living);
                return living;
            }
        }
        return _default;
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
