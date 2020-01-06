package pokecube.core.items.vitamins;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.Item;
import pokecube.core.interfaces.PokecubeMod;

public class ItemVitamin extends Item
{
    public static List<String> vitamins = Lists.newArrayList();

    public final String type;

    public ItemVitamin(Properties properties, String type)
    {
        super(properties);
        this.type = type;
        this.setRegistryName(PokecubeMod.ID, "vitamin_" + type);
    }

}
