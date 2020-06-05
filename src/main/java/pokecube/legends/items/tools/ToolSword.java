package pokecube.legends.items.tools;

import net.minecraft.item.IItemTier;
import net.minecraft.item.SwordItem;
import pokecube.core.PokecubeItems;

public class ToolSword extends SwordItem
{
    public ToolSword(final int maxDamage, final int attackSpeed, final IItemTier material)
    {
        super(material, maxDamage, attackSpeed, new Properties().group(PokecubeItems.POKECUBEITEMS));
    }
}
