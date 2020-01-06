package pokecube.adventures.events;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.adventures.capabilities.utils.TypeTrainer;

@Cancelable
public class TrainerSpawnEvent extends Event
{
    private final Entity      trainer;
    private final TypeTrainer type;
    private final BlockPos    location;
    private final World       world;

    public TrainerSpawnEvent(final TypeTrainer type, final Entity trainer, final BlockPos location, final World world)
    {
        this.type = type;
        this.location = location;
        this.world = world;
        this.trainer = trainer;
    }

    public BlockPos getLocation()
    {
        return this.location;
    }

    public Entity getTrainer()
    {
        return this.trainer;
    }

    public TypeTrainer getType()
    {
        return this.type;
    }

    public World getWorld()
    {
        return this.world;
    }

}
