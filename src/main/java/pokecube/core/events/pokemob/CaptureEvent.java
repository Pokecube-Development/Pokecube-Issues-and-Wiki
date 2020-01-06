package pokecube.core.events.pokemob;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.PokecubeManager;

@Cancelable
/** These events are all fired on the PokecubeCore.POKEMOB_BUS */
public class CaptureEvent extends Event
{
    @Cancelable
    /**
     * Post is sent after the capture is completed, canceling it will prevent
     * the capture from being recorded, and will destroy the cube and the
     * mob.
     */
    public static class Post extends CaptureEvent
    {
        public Post(EntityPokecubeBase pokecube)
        {
            super(pokecube);
        }

    }

    @Cancelable
    /**
     * Pre is sent before the capture is completed, canceling it will prevent
     * capture.
     */
    public static class Pre extends CaptureEvent
    {
        public Pre(IPokemob hit, EntityPokecubeBase entityPokecubeBase)
        {
            super(hit, entityPokecubeBase);
        }

    }

    public final ItemStack filledCube;

    public final Entity pokecube;

    public final IPokemob caught;

    protected CaptureEvent(EntityPokecubeBase pokecube)
    {
        this.pokecube = pokecube;
        if (pokecube != null)
        {
            this.filledCube = pokecube.getItem();
            this.caught = PokecubeManager.itemToPokemob(pokecube.getItem(), pokecube.getEntityWorld());
        }
        else
        {
            this.filledCube = null;
            this.caught = null;
        }
    }

    protected CaptureEvent(IPokemob hit, EntityPokecubeBase pokecube)
    {
        this.pokecube = pokecube;
        this.caught = hit;
        this.filledCube = pokecube.getItem();
    }

}
