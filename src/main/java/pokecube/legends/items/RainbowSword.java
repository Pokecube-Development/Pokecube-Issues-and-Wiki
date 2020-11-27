package pokecube.legends.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.items.tools.ToolSword;

public class RainbowSword extends ToolSword
{
    public RainbowSword(final int bonusDamage, final int attackSpeed, final IItemTier material)
    {
        super(bonusDamage, attackSpeed, material);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(final ItemStack itemstack)
    {
        return true;
    }
}
