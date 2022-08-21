package pokecube.legends.items.zmove;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeItems;

public class ItemZCrystal extends Item
{
    public static boolean isZCrystal(final ItemStack stackIn)
    {
        return stackIn != null && stackIn.getItem() instanceof ItemZCrystal;
    }

    public final PokeType type;

    public ItemZCrystal(final PokeType type)
    {
        super(new Item.Properties().tab(PokecubeItems.TAB_ITEMS));
        this.type = type;
    }
}
