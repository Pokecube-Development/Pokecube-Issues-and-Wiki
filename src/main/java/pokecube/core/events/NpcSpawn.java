package pokecube.core.events;

import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.entity.npc.NpcMob;

@Cancelable
public class NpcSpawn extends Event
{
    private final NpcMob      trainer;
    private final BlockPos    location;
    private final IWorld      world;
    private final SpawnReason reason;

    public NpcSpawn(final NpcMob trainer, final BlockPos location, final IWorld world, final SpawnReason reason)
    {
        this.location = location;
        this.world = world;
        this.trainer = trainer;
        this.reason = reason;
    }

    public BlockPos getLocation()
    {
        return this.location;
    }

    public NpcMob getNpcMob()
    {
        return this.trainer;
    }

    public IWorld getWorld()
    {
        return this.world;
    }

    public SpawnReason getReason()
    {
        return this.reason;
    }

}
