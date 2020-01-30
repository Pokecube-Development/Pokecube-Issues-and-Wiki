package pokecube.core.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.entity.npc.NpcMob;

@Cancelable
public class NpcSpawn extends Event
{
    private final NpcMob   trainer;
    private final BlockPos location;
    private final IWorld   world;

    public NpcSpawn(final NpcMob trainer, final BlockPos location, final IWorld world)
    {
        this.location = location;
        this.world = world;
        this.trainer = trainer;
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

}
