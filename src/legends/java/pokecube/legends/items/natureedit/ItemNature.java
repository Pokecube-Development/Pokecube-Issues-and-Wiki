package pokecube.legends.items.natureedit;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.Nature;

public class ItemNature extends Item
{
    public static boolean isNature(final ItemStack stackIn)
    {
        return stackIn.getItem() instanceof ItemNature;
    }

    public final Nature type;

    public ItemNature(final Nature type)
    {
        super(new Item.Properties());
        this.type = type;
    }
}
