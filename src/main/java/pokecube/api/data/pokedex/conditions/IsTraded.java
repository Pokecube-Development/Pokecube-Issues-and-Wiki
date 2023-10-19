package pokecube.api.data.pokedex.conditions;

import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;
import thut.lib.TComponent;

public class IsTraded implements PokemobCondition
{
    @Override
    public boolean matches(IPokemob mobIn)
    {
        return mobIn.traded();
    }
    
    @Override
    public Component makeDescription()
    {
        return TComponent.translatable("pokemob.description.evolve.traded");
    }
}