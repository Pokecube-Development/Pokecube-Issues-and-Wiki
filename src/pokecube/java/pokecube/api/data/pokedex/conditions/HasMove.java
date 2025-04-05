package pokecube.api.data.pokedex.conditions;

import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.Tools;
import pokecube.core.moves.MovesUtils;
import thut.lib.TComponent;

/**
 * This class matches a pokemob with the specified move<br>
 * <br>
 * Matcher key: "move" <br>
 * Json keys: <br>
 * "move" - string, name of the move to match
 */
@Condition(name = "move")
public class HasMove implements PokemobCondition
{
    public String move;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return Tools.hasMove(this.move, mobIn);
    }

    @Override
    public Component makeDescription()
    {
        return TComponent.translatable("pokemob.description.evolve.move", MovesUtils.getMoveName(this.move, null));
    }
}