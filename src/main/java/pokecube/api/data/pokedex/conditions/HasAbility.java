package pokecube.api.data.pokedex.conditions;

import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * This class matches a pokemob with the specified ability<br>
 * <br>
 * Matcher key: "ability" <br>
 * Json keys: <br>
 * "ability" - string, name of the ability to match
 */
@Condition(name = "ability")
public class HasAbility implements PokemobCondition
{
    public String ability;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return AbilityManager.hasAbility(this.ability, mobIn);
    }
}