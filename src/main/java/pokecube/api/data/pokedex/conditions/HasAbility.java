package pokecube.api.data.pokedex.conditions;

import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;

@Condition(name="ability")
public class HasAbility implements PokemobCondition
{
    public String ability;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return AbilityManager.hasAbility(this.ability, mobIn);
    }
}