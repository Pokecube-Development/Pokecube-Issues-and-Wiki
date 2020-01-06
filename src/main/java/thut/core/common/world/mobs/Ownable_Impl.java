package thut.core.common.world.mobs;

import java.util.UUID;

import thut.api.world.mobs.Mob;
import thut.api.world.mobs.Ownable;

public class Ownable_Impl extends Mob_Impl implements Ownable
{
    UUID owner_id;
    Mob  owner_mob;

    @Override
    public Mob getOwner()
    {
        if (this.owner_id == null) return null;
        else if (this.owner_mob == null) this.owner_mob = this.world().getMob(this.owner_id);
        return this.owner_mob;
    }

    @Override
    public UUID getOwnerId()
    {
        return this.owner_id;
    }

    @Override
    public void setOwnerId(UUID owner)
    {
        this.owner_id = owner;
        this.owner_mob = this.world().getMob(owner);
    }

}
