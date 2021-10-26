package pokecube.legends.items.tools;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class ToolSword extends SwordItem
{
    public ToolSword(final int maxDamage, final int attackSpeed, final Tier material, final CreativeModeTab group)
    {
        super(material, maxDamage, attackSpeed, new Properties().tab(group));
    }
}
