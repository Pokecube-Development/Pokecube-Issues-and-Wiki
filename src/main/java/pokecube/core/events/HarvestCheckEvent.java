package pokecube.core.events;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event.HasResult;
import pokecube.core.interfaces.IPokemob;

/**
 * This event is fired on the PokecubeCore.POKEMOB_BUS.
 * It is called when a pokemob is deciding if it wants to harvest a block from a
 * location, in the GatherTask.
 * <br>
 * Result effects are as follows:
 * <br>
 * Result.DEFAULT : Will check the tags in GatherTask
 * Result.ALLOW : Block will be harvested
 * Result.DENY : Block will not be harvested
 */
@HasResult
public class HarvestCheckEvent extends LivingEvent
{
    public final IPokemob   pokemob;
    public final BlockState state;
    public final BlockPos   pos;

    public HarvestCheckEvent(final IPokemob pokemob, final BlockState state, final BlockPos pos)
    {
        super(pokemob.getEntity());
        this.pokemob = pokemob;
        this.state = state;
        this.pos = pos;
    }

}
