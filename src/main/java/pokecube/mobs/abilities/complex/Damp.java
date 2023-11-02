package pokecube.mobs.abilities.complex;

import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.database.tags.Tags;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AbilityProvider(name = "damp", singleton = false)
public class Damp extends Ability
{
    IPokemob mob;

    int range = 16;

    @SubscribeEvent
    public void denyBoom(final ExplosionEvent.Start boom)
    {
        if (!this.mob.getEntity().isAlive()) this.destroy(this.mob);
        else
        {
            final Vector3 boomLoc = new Vector3().set(boom.getExplosion().getPosition());
            if (boomLoc.distToEntity(this.mob.getEntity()) < this.range) boom.setCanceled(true);
        }
    }

    @Override
    public void destroy(IPokemob mob)
    {
        if (ThutCore.proxy.isClientSide()) return;
        ThutCore.FORGE_BUS.unregister(this);
    }

    @Override
    public Ability init(final Object... args)
    {
        if (ThutCore.proxy.isClientSide()) return this;
        for (int i = 0; i < 2; i++)
            if (args != null && args.length > i)
            {
                if (IPokemob.class.isInstance(args[i]))
                {
                    ThutCore.FORGE_BUS.register(this);
                    this.mob = IPokemob.class.cast(args[i]);
                }
                if (args[i] instanceof Integer) this.range = (int) args[i];
            }
        return this;
    }

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (Tags.MOVE.isIn("damp-affected", move.getName()))
        {
            move.failed = true;
            move.canceled = true;
        }
    }

    @Override
    public void onUpdate(final IPokemob mob)
    {
        this.mob = mob;
    }
}
