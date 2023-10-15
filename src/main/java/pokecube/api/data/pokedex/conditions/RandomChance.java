package pokecube.api.data.pokedex.conditions;

import java.util.Random;

import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;
import thut.lib.TComponent;

public class RandomChance implements PokemobCondition
{
    public double chance;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return new Random(mobIn.getRNGValue()).nextDouble() > chance;
    }

    @Override
    public void init()
    {
        if (chance > 1) chance /= 100;
    }

    @Override
    public Component makeDescription()
    {
        final String var = (int) (100 * this.chance) + "%";
        return TComponent.translatable("pokemob.description.evolve.chance", var);
    }
}