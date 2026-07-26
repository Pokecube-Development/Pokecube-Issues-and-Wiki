package pokecube.gimmicks.mega.conditions;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.conditions.HasHeldItem;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.Tools;
import pokecube.gimmicks.mega.MegaCapability;
import pokecube.gimmicks.mega.MegaEvoData;
import pokecube.gimmicks.mega.MegaEvolveHelper;
import pokecube.gimmicks.mega.MegaStoneColours;
import thut.api.item.ItemList;

import java.util.Collections;
import java.util.List;

public class HeldItem extends HasHeldItem implements MegaCondition
{
    public boolean autoConvertStones = true;

    private boolean tryConvertStone(IPokemob pokemob, PokedexEntry newEntry) {
        if(pokemob.getHappiness()<250||!autoConvertStones) return false;
        var stack = pokemob.getHeldItem();
        if (ItemList.is(MegaEvolveHelper.BLANK_MEGASTONE, stack) && !stack.has(MegaEvolveHelper.MEGA_STONE) && stack.getCount() == 1)
        {
            var stone = new MegaCapability.MegaStone();
            stone.entry = newEntry.getTrimmedName();
            stone.colours = MegaCapability.COLOUR_MAPPER.apply(pokemob.getPokedexEntry(), newEntry);
            stack.set(MegaEvolveHelper.MEGA_STONE, stone);
            final Component customName = stack.get(DataComponents.CUSTOM_NAME);
            if (customName == null || "Mega Stone".equalsIgnoreCase(customName.getString().trim()))
            {
                final String stoneName = MegaStoneColours.getName(newEntry);
                stack.set(DataComponents.CUSTOM_NAME,
                        stoneName == null ? newEntry.getTranslatedName() : Component.literal(stoneName));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
    {
        if (_tag != null && mobIn.getHeldItem().is(_tag)) return true;
        boolean rightStack = false;
        if (!this._value.isEmpty())
        {
            rightStack = Tools.isSameStack(this._value, mobIn.getHeldItem(), true);
            rightStack |= MegaCapability.matches(mobIn.getHeldItem(), entryTo);
        }
        // Check for backwards compat with old mega items
        if(_tag.location().getNamespace().contains("pokecube")&&_tag.location().getPath().contains("mega"))
        {
            rightStack |= MegaCapability.getForStack(mobIn.getHeldItem())==entryTo;
        }
        return rightStack || tryConvertStone(mobIn, entryTo);
    }
}
