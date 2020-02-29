package pokecube.legends.items.tools;

import net.minecraft.item.IItemTier;
import net.minecraft.item.SwordItem;
import pokecube.core.PokecubeItems;
import pokecube.legends.Reference;
import pokecube.legends.init.ItemInit;

public class ToolSword extends SwordItem
{
    public ToolSword(final String name, final int maxDamage, final int attackSpeed, final IItemTier material)
    {
        super(material, maxDamage, attackSpeed, new Properties().group(PokecubeItems.POKECUBEITEMS));
        this.setRegistryName(Reference.ID, name);
        ItemInit.ITEMS.add(this);
    }
}
