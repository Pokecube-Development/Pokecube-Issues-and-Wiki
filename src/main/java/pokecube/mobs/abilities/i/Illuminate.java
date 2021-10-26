package pokecube.mobs.abilities.i;

import net.minecraft.server.level.ServerLevel;
import pokecube.core.PokecubeCore;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class Illuminate extends Ability
{
    int range = 4;

    @Override
    public Ability init(final Object... args)
    {
        if (args == null) return this;
        for (final Object arg : args)
            if (arg instanceof Integer)
            {
                this.range = (int) arg;
                return this;
            }
        return this;
    }

    @Override
    public void onUpdate(final IPokemob mob)
    {
        if (!(mob.getEntity().getCommandSenderWorld() instanceof ServerLevel)) return;
        if (mob.getEntity().tickCount % 20 == 0)
        {
            final ServerLevel world = (ServerLevel) mob.getEntity().getCommandSenderWorld();
            PokecubeCore.spawner.doSpawnForPoint(Vector3.getNewVector().set(mob.getEntity()), world, 0, this.range);
        }
    }
}
