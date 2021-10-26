package pokecube.legends.items.tools;

import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SwordItem;

public class ToolSword extends SwordItem
{
    public ToolSword(final int maxDamage, final int attackSpeed, final IItemTier material, final ItemGroup group)
    {
        super(material, maxDamage, attackSpeed, new Properties().tab(group));
    }
}
