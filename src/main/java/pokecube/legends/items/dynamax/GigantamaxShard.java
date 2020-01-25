package pokecube.legends.items.dynamax;

import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.items.ItemBase;

public class GigantamaxShard extends ItemBase
{

    public GigantamaxShard(final String name, final int num)
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
