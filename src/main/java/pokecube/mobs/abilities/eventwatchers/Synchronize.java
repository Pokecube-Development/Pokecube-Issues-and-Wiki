package pokecube.mobs.abilities.eventwatchers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class Synchronize extends Ability
{
    Vector3  location = Vector3.getNewVector();
    IPokemob pokemob;
    int      range    = 16;

    @Override
    public void destroy()
    {
        if (ThutCore.proxy.isClientSide()) return;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void editNature(SpawnEvent.Post event)
    {
        if (!this.pokemob.getEntity().isAlive()) this.destroy();
        else if (event.location().distToSq(this.location) < this.range * this.range && Math.random() > 0.5) event.pokemob
                .setNature(this.pokemob.getNature());
    }

    @Override
    public Ability init(Object... args)
    {
        if (ThutCore.proxy.isClientSide()) return this;
        for (int i = 0; i < 1; i++)
            if (args != null && args.length > i)
            {
                if (IPokemob.class.isInstance(args[i]))
                {
                    MinecraftForge.EVENT_BUS.register(this);
                    this.location = Vector3.getNewVector().set(args[i]);
                    this.pokemob = IPokemob.class.cast(args[i]);
                }
                if (args[i] instanceof Integer) this.range = (int) args[i];
            }
        return this;
    }

    @Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && move.statusChange != IMoveConstants.STATUS_NON && mob
                .getStatus() == IMoveConstants.STATUS_NON) if (move.statusChange != IMoveConstants.STATUS_FRZ
                        && move.statusChange != IMoveConstants.STATUS_SLP) MovesUtils.setStatus(move.attacker
                                .getEntity(), move.statusChange);
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        this.location.set(mob.getEntity());
        this.pokemob = mob;
    }
}
