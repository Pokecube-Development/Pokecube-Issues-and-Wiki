/**
 *
 */
package pokecube.core.items.berries;

import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.berries.ItemBerry.BerryType;

/**
 * @author Oracion
 * @author Manchou
 */
public class BerryManager
{
    /** Map of berry id -> fruit crops */
    public static Int2ObjectArrayMap<RegistryObject<Block>> berryCrops = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit items */
    public static Int2ObjectArrayMap<RegistryObject<Block>> berryFruits = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit items */
    public static Int2ObjectArrayMap<RegistryObject<ItemBerry>> berryItems = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit leaves */
    public static Int2ObjectArrayMap<RegistryObject<Block>> berryLeaves = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit logs */
    public static Int2ObjectArrayMap<RegistryObject<Block>> berryLogs = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit planks */
    public static Int2ObjectArrayMap<RegistryObject<Block>> berryPlanks = new Int2ObjectArrayMap<>();
    /** Map of berry id -> name of berry */
    public static Int2ObjectArrayMap<String> berryNames = new Int2ObjectArrayMap<>();
    /** Map of berry id -> name of berry */
    public static Map<String, RegistryObject<ItemBerry>> byName = Maps.newHashMap();
    /** Map of berry id -> name of berry */
    public static Map<String, Integer> indexByName = Maps.newHashMap();
    /** Map of berry id -> block of potted berries */
    public static Int2ObjectArrayMap<RegistryObject<Block>> pottedBerries = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit items */
    public static Int2ObjectArrayMap<BerryType> berryTypes = new Int2ObjectArrayMap<>();

    public static void addBerry(final RegistryObject<ItemBerry> berry, final BerryType type)
    {
        BerryManager.berryItems.put(type.index, berry);
        BerryManager.byName.put(type.name, berry);
        if (type.effect != null) UsableItemEffects.BerryUsable.effects.put(type.index, type.effect);
    }

    public static Item getBerryItem(final String name)
    {
        return BerryManager.byName.get(name).get();
    }

    public static Block getCrop(final ItemBerry berry)
    {
        return BerryManager.berryCrops.get(berry.type.index).get();
    }

    public static Block getFruit(final ItemBerry berry)
    {
        return BerryManager.berryFruits.get(berry.type.index).get();
    }

    public static Block getPottedBerry(final ItemBerry berry)
    {
        return BerryManager.pottedBerries.get(berry.type.index).get();
    }
}
