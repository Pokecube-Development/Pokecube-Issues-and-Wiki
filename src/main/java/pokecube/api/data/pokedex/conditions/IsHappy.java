package pokecube.api.data.pokedex.conditions;

import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;
import thut.lib.TComponent;

public class IsHappy implements PokemobCondition
{
    boolean above = true;
    int amount = 220;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return above ? mobIn.getHappiness() >= amount : mobIn.getHappiness() <= amount;
    }

    @Override
    public Component makeDescription()
    {
        if (above && amount > 200)
        {
            return TComponent.translatable("pokemob.description.evolve.happy");
        }
        return PokemobCondition.super.makeDescription();
    }
}
