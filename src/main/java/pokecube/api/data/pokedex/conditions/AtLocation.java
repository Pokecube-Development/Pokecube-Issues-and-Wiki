package pokecube.api.data.pokedex.conditions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.maths.Vector3;


/**
 * This class matches the location the pokemob is at<br>
 * <br>
 * Matcher key: "location" <br>
 * Json keys: <br>
 * "location" - Json Object of spawn rule format
 */
@Condition(name = "location")
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

        final LivingEntity entity = mobIn.getEntity();
        final Vector3 loc = new Vector3().set(entity);
        final SpawnCheck check = new SpawnCheck(loc, mobIn.getEntity().level);
        return _matcher.matches(check);
    }

    @Override
    public Component makeDescription()
    {
        return location.makeDescription();
    }
}