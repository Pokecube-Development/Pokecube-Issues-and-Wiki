package pokecube.gimmicks.mega.conditions;

import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.conditions.HasHeldItem;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.Tools;
import pokecube.gimmicks.mega.MegaCapability;

public class HeldItem extends HasHeldItem implements MegaCondition
{
    @Override
    public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
    {
        if (_tag != null && mobIn.getHeldItem().is(_tag)) return true;
        if (!this._value.isEmpty())
        {
            boolean rightStack = Tools.isSameStack(this._value, mobIn.getHeldItem(), true);
            if (!rightStack) rightStack = MegaCapability.matches(mobIn.getHeldItem(), entryTo);
            return rightStack;
        }
        // Check for backwards compat with old mega items
        if(_tag.location().getNamespace().contains("pokecube")&&_tag.location().getPath().contains("mega"))
        {
            return MegaCapability.getForStack(mobIn.getHeldItem())==entryTo;
        }
        return false;
    }
}
