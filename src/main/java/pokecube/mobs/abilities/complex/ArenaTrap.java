package pokecube.mobs.abilities.complex;

import net.minecraft.server.level.ServerLevel;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.core.PokecubeCore;

@AbilityProvider(name = "arena-trap", singleton = false)
public class ArenaTrap extends Ability
{
    // the part that usually prevents switching is still "TODO"
    int range = 4;

    @Override
    public Ability init(final Object... args)
    {
        if (args == null) return this;
        for (final Object arg : args) if (arg instanceof Integer)
        {
            this.range = (int) arg;
            return this;
        }
        return this;
    }

    @Override
    public void onUpdate(final IPokemob mob)
    {
        if (!(mob.getEntity().getLevel() instanceof ServerLevel)) return;
        if (mob.getEntity().tickCount % 20 == 0)
        {
            SpawnContext context = new SpawnContext(mob);
            context = PokecubeCore.spawner.randomSpawnContext(context, 0, this.range);
            PokecubeCore.spawner.doSpawnForContext(context);
        }
    }
}
