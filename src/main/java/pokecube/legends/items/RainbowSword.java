package pokecube.legends.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.items.tools.ToolSword;

public class RainbowSword extends ToolSword
{
    public RainbowSword(final IItemTier material, final int bonusDamage, final int attackSpeed, final ItemGroup group)
    {
        super(attackSpeed, bonusDamage, material, group);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(final ItemStack itemstack)
    {
        return true;
    }
}
