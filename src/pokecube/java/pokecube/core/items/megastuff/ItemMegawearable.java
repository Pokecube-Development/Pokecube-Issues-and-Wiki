package pokecube.core.items.megastuff;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

public class ItemMegawearable extends Item
{
    private static final Map<String, String> wearables = Maps.newHashMap();

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
        super(new Properties().stacksTo(1));
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

    @SubscribeEvent
    public static void modifyComponents(ModifyDefaultComponentsEvent event)
    {
        event.getAllItems().forEach(item -> {
            if (item instanceof ItemMegawearable wearable)
            {
                event.modify(wearable, builder -> builder.set(DataComponents.DYED_COLOR,
                        new DyedItemColor(wearable.getColor(), false)));
            }
        });
    }

    public int getColor()
    {
        return switch (name)
        {
            case "pendant" -> 0xFFFFFFFF;
            case "tiara" -> 0xFF3c44aa;
            case "earring" -> 0xFFB02E26;
            case "glasses" -> 0xFF282828;
            case "ankletzinnia" -> 0xFF169c9c;
            default -> 0xFFA06540;
        };
    }
}
