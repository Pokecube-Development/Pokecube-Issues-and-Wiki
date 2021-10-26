package pokecube.core.events.pokemob;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
        public Post(final EntityPokecubeBase pokecube)
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
        public final LivingEntity mob;

        public Pre(final IPokemob hit, final EntityPokecubeBase entityPokecubeBase, final LivingEntity mob)
        {
            super(hit, entityPokecubeBase);
            this.mob = mob;
        }

    }

    private ItemStack filledCube = ItemStack.EMPTY;

    public final Entity pokecube;

    private IPokemob caught;

    protected CaptureEvent(final EntityPokecubeBase pokecube)
    {
        this.pokecube = pokecube;
        if (pokecube != null) this.setFilledCube(pokecube.getItem(), true);
        else this.setFilledCube(ItemStack.EMPTY, true);
    }

    protected CaptureEvent(final IPokemob hit, final EntityPokecubeBase pokecube)
    {
        this.pokecube = pokecube;
        this.setCaught(hit);
        this.setFilledCube(pokecube.getItem(), false);
    }

    public ItemStack getFilledCube()
    {
        return this.filledCube;
    }

    public void setFilledCube(final ItemStack filledCube, final boolean replaceMob)
    {
        this.filledCube = filledCube;
        if (replaceMob) this.setCaught(PokecubeManager.itemToPokemob(filledCube, this.pokecube
                .getCommandSenderWorld()));
    }

    public IPokemob getCaught()
    {
        return this.caught;
    }

    public void setCaught(final IPokemob caught)
    {
        this.caught = caught;
    }

}
