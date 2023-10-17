package pokecube.api.data.pokedex.conditions;

import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;
import thut.lib.TComponent;

public class AtLeastLevel implements PokemobCondition
{
    public int level;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return mobIn.getLevel() >= level;
    }

    @Override
    public Component makeDescription()
    {
        return TComponent.translatable("pokemob.description.evolve.level", this.level);
    }
}
