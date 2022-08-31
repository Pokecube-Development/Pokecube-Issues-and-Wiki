package pokecube.core.items.megastuff;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeItems;
import thut.lib.RegHelper;

public class ItemMegawearable extends Item implements DyeableLeatherItem
{
    private static Map<String, String> wearables = Maps.newHashMap();

    static
    {
        ItemMegawearable.registerWearable("ring", "FINGER");
        ItemMegawearable.registerWearable("belt", "WAIST");
        ItemMegawearable.registerWearable("hat", "HAT");
    }

    public static String getSlot(String name)
    {
        return ItemMegawearable.wearables.get(name);
    }

    public static Collection<String> getWearables()
    {
        return ItemMegawearable.wearables.keySet();
    }

    public static void registerWearable(String name, String slot)
    {
        ItemMegawearable.wearables.put(name, slot);
    }

    public static List<ItemMegawearable> INSTANCES = Lists.newArrayList();

    public final String name;
    public final String slot;

    public ItemMegawearable(String name)
    {
        super(new Properties().tab(PokecubeItems.TAB_ITEMS).stacksTo(1));
        this.name = name;
        this.slot = wearables.get(name);
        INSTANCES.add(this);
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack)
    {
        final String name = RegHelper.getKey(this).getPath().replace("mega_", "");
        if (name.equals("megahat")) return EquipmentSlot.HEAD;
        return super.getEquipmentSlot(stack);
    }
}
