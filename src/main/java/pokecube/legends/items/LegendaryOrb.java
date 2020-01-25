package pokecube.legends.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LegendaryOrb extends ItemBase
{

    public LegendaryOrb(final String name, final int num)
    {
        super(name, num);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(final ItemStack itemstack)
    {
        return true;
    }

}
