package pokecube.api.data.pokedex.conditions;

import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * This class matches a traded pokemob<br>
 * <br>
 * Matcher key: "traded" <br>
 */
@Condition(name="traded")
public class IsTraded extends PokemobCondition
{
    @Override
    public boolean matches(IPokemob mobIn)
    {
        return mobIn.traded();
    }
    
    @Override
    public Component makeDescription()
    {
        return Component.translatable("pokemob.description.evolve.traded");
    }
}