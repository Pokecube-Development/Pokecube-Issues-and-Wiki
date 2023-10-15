package pokecube.api.data.pokedex.conditions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.maths.Vector3;

public class AtLocation implements PokemobCondition
{
    public SpawnRule location;

    private SpawnBiomeMatcher _matcher;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        if (_matcher == null)
        {
            _matcher = SpawnBiomeMatcher.get(location);
        }
        if (mobIn.getEntity().level instanceof ServerLevel world)
        {
            final LivingEntity entity = mobIn.getEntity();
            final Vector3 loc = new Vector3().set(entity);
            final SpawnCheck check = new SpawnCheck(loc, world);
            return _matcher.matches(check);
        }
        return false;
    }
}