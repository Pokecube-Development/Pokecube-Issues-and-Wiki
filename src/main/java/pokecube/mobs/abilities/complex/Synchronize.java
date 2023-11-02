package pokecube.mobs.abilities.complex;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AbilityProvider(name = "synchronize", singleton = false)
public class Synchronize extends Ability
{
    Vector3 location = new Vector3();
    IPokemob pokemob;
    int range = 16;

    @Override
    public void destroy(IPokemob mob)
    {
        if (ThutCore.proxy.isClientSide()) return;
        ThutCore.FORGE_BUS.unregister(this);
    }

    @SubscribeEvent
    public void editNature(SpawnEvent.Post event)
    {
        if (!this.pokemob.getEntity().isAlive()) this.destroy(this.pokemob);
        else if (event.location().distToSq(this.location) < this.range * this.range && Math.random() > 0.5)
            event.pokemob.setNature(this.pokemob.getNature());
    }

    @Override
    public Ability init(Object... args)
    {
        if (ThutCore.proxy.isClientSide()) return this;
        for (int i = 0; i < 1; i++) if (args != null && args.length > i)
        {
            if (IPokemob.class.isInstance(args[i]))
            {
                ThutCore.FORGE_BUS.register(this);
                this.location = new Vector3().set(args[i]);
                this.pokemob = IPokemob.class.cast(args[i]);
            }
            if (args[i] instanceof Integer) this.range = (int) args[i];
        }
        return this;
    }

    @Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {}

    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        if (areWeUser(mob, move)) return;
        if (move.status_effects != IMoveConstants.STATUS_NON && mob.getStatus() == IMoveConstants.STATUS_NON)
            if (move.status_effects != IMoveConstants.STATUS_FRZ && move.status_effects != IMoveConstants.STATUS_SLP)
                MovesUtils.setStatus(mob, move.getUser().getEntity(), move.status_effects);
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        this.location.set(mob.getEntity());
        this.pokemob = mob;
    }
}
