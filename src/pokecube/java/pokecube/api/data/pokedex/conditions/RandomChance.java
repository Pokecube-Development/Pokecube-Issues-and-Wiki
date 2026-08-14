package pokecube.api.data.pokedex.conditions;

import java.util.Random;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * This class matches a pokemob with a random chance. Note that this is
 * deterministic for a single pokemob, but probabilistic over many<br>
 * <br>
 * Matcher key: "chance" <br>
 * Json keys: <br>
 * "chance" - double, probability of the match succeeding
 */
@Condition(name = "chance")
public class RandomChance extends PokemobCondition
{
    public double chance;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return new Random(mobIn.getRNGValue()).nextDouble() < chance;
    }

    @Override
    public void init(HolderLookup.Provider registries)
    {
        if (chance > 1) chance /= 100;
    }

    @Override
    public Component makeDescription()
    {
        final String var = (int) (100 * this.chance) + "%";
        return Component.translatableEscape("pokemob.description.evolve.chance", var);
    }
}