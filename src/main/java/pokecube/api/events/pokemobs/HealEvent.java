package pokecube.api.events.pokemobs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.OwnableCaps;

/**
 * Called when a mob is healed via healers, revives or sent to PC. This is fired
 * on the PokecubeAPI.POKEMOB_BUS.
 */
public abstract class HealEvent extends Event
{
    /**
     * Holder for the mob, it's owner and whether this was done via a healer. if
     * fromHealer is false, then it means it was either via revive or sending to
     * PC.
     */
    public static record HealContext(@Nonnull LivingEntity mob, @Nullable LivingEntity owner, boolean fromHealer)
    {
    }

    /**
     * Fired after the mob has been healed, see
     * {@link PokecubeManager#heal(LivingEntity)} for what has been applied
     */
    public static class Post extends HealEvent
    {
        public Post(LivingEntity pokemob, boolean fromHealer)
        {
            super(pokemob, fromHealer);
        }
    }

    /**
     * Fired before the mob has been healed, see
     * {@link PokecubeManager#heal(LivingEntity)} for what has been applied
     */
    public static class Pre extends HealEvent
    {
        public Pre(LivingEntity pokemob, boolean fromHealer)
        {
            super(pokemob, fromHealer);
        }
    }

    private final HealContext context;

    public HealEvent(final LivingEntity pokemob, boolean fromHealer)
    {
        LivingEntity owner = OwnableCaps.getOwner(pokemob);
        this.context = new HealContext(pokemob, owner, fromHealer);
    }

    public HealContext getContext()
    {
        return context;
    }

}
