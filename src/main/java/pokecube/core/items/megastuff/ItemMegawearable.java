package pokecube.core.items.megastuff;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeItems;

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
        if (name.equals("hat")) return EquipmentSlot.HEAD;
        return super.getEquipmentSlot(stack);
    }

    @Override
    public int getColor(ItemStack stack)
    {
        CompoundTag compoundtag = stack.getTagElement("display");
        if (compoundtag != null && compoundtag.contains("color", 99)) return compoundtag.getInt("color");
        if (name.equals("pendant")) return 0xFFFFFFFF;
        if (name.equals("tiara")) return 0xFF3c44aa;
        if (name.equals("earring")) return 0xFFB02E26;
        if (name.equals("glasses")) return 0xFF282828;
        if (name.equals("ankletzinnia")) return 0xFF169c9c;
        return 0xFFA06540;
    }
}
