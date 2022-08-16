package pokecube.api.events.pokemobs;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;

public class TradeEvent extends Event
{
    public final ItemStack cube;
    public final IPokemob  mob;

    public TradeEvent(Level world, ItemStack pokecube)
    {
        this.cube = pokecube;
        this.mob = PokecubeManager.itemToPokemob(pokecube, world);
    }

}
