package pokecube.mobs.abilities.eventwatchers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Explode;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class Damp extends Ability
{
    IPokemob mob;

    int range = 16;

    @SubscribeEvent
    public void denyBoom(final ExplosionEvent.Start boom)
    {
        if (!this.mob.getEntity().isAlive()) this.destroy();
        else
        {
            final Vector3 boomLoc = new Vector3().set(boom.getExplosion().getPosition());
            if (boomLoc.distToEntity(this.mob.getEntity()) < this.range) boom.setCanceled(true);
        }
    }

    @Override
    public void destroy()
    {
        if (ThutCore.proxy.isClientSide()) return;
        MinecraftForge.EVENT_BUS.unregister(this);
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
                    MinecraftForge.EVENT_BUS.register(this);
                    this.mob = IPokemob.class.cast(args[i]);
                }
                if (args[i] instanceof Integer) this.range = (int) args[i];
            }
        return this;
    }

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (move.getMove() instanceof Move_Explode)
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
