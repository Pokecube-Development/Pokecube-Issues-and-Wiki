package pokecube.mobs.abilities.s;

import net.minecraft.server.level.ServerLevel;
import pokecube.core.PokecubeCore;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.pokemob.SpawnEvent.SpawnContext;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.utils.PokeType;

public class Swarm extends Ability
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
            SpawnContext context = new SpawnContext(mob);
            context = PokecubeCore.spawner.randomSpawnContext(context, 0, this.range);
            PokecubeCore.spawner.doSpawnForContext(context);
        }
    }

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {

        if (!move.pre) return;
        if (mob == move.attacker && move.attackType == PokeType.getType("bug") && mob.getEntity().getHealth() < mob
                .getEntity().getMaxHealth() / 3) move.PWR *= 1.5;
    }
}
