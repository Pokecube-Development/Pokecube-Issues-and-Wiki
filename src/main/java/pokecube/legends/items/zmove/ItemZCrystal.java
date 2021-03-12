package pokecube.legends.items.zmove;

import java.util.Locale;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.utils.PokeType;
import pokecube.legends.Reference;

public class ItemZCrystal extends Item
{
    public static boolean isZCrystal(final ItemStack stackIn)
    {
        return stackIn != null && stackIn.getItem() instanceof ItemZCrystal;
    }

    public final PokeType type;

    public ItemZCrystal(final PokeType type)
    {
        super(new Item.Properties().tab(PokecubeItems.POKECUBEITEMS));
        final String name = type.name.equals("???") ? "unknown" : type.name;
        this.setRegistryName(Reference.ID, "z_" + name.toLowerCase(Locale.ROOT));
        this.type = type;
    }
}
