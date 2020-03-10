/**
 *
 */
package pokecube.core.items.berries;

import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.berries.ItemBerry.BerryType;

/**
 * @author Oracion
 * @author Manchou
 */
public class BerryManager
{
    /** Map of berry id -> block of crop */
    public static Int2ObjectArrayMap<Block>     berryCrops  = new Int2ObjectArrayMap<>();
    /** Map of berry id -> block of fruit */
    public static Int2ObjectArrayMap<Block>     berryFruits = new Int2ObjectArrayMap<>();
    /** Map of berry id -> block of fruit */
    public static Int2ObjectArrayMap<ItemBerry> berryItems  = new Int2ObjectArrayMap<>();
    /** Map of berry id -> name of berry */
    public static Int2ObjectArrayMap<String>    berryNames  = new Int2ObjectArrayMap<>();
    /** Map of berry id -> name of berry */
    public static Map<String, ItemBerry>        byName      = Maps.newHashMap();

    public static void addBerry(final ItemBerry berry)
    {
        final BerryType type = berry.type;

        BerryManager.berryNames.put(type.index, type.name);
        BerryManager.berryItems.put(type.index, berry);
        BerryManager.byName.put(type.name, berry);
        if (type.effect != null) UsableItemEffects.BerryUsable.effects.put(type.index, type.effect);
    }

    public static Item getBerryItem(final String name)
    {
        return BerryManager.byName.get(name);
    }

    public static Block getCrop(final ItemBerry berry)
    {
        return BerryManager.berryCrops.get(berry.type.index);
    }

    public static Block getFruit(final ItemBerry berry)
    {
        return BerryManager.berryFruits.get(berry.type.index);
    }

    public static void registerTrees()
    {
        // TODO remove this.
        // BerryGenManager.trees.put(3, new
        // GenericGrower(ItemGenerator.logs.get("pecha").getDefaultState()));
        // BerryGenManager.trees.put(6, new
        // GenericGrower(ItemGenerator.logs.get("leppa").getDefaultState()));
        // BerryGenManager.trees.put(7, new
        // GenericGrower(ItemGenerator.logs.get("oran").getDefaultState()));
        // BerryGenManager.trees.put(10, new
        // GenericGrower(ItemGenerator.logs.get("sitrus").getDefaultState()));
        // BerryGenManager.trees.put(60, new
        // GenericGrower(ItemGenerator.logs.get("enigma").getDefaultState()));
        // BerryGenManager.trees.put(18, new
        // GenericGrower(ItemGenerator.logs.get("nanab").getDefaultState()));
        // //
        // // // EV Berries
        // BerryGenManager.trees.put(21, new
        // GenericGrower(Blocks.OAK_LOG.getDefaultState()));
        // BerryGenManager.trees.put(22, new
        // GenericGrower(Blocks.OAK_LOG.getDefaultState()));
        // BerryGenManager.trees.put(23, new
        // GenericGrower(Blocks.OAK_LOG.getDefaultState()));
        // BerryGenManager.trees.put(24, new
        // GenericGrower(Blocks.OAK_LOG.getDefaultState()));
        // BerryGenManager.trees.put(25, new
        // GenericGrower(Blocks.OAK_LOG.getDefaultState()));
        // BerryGenManager.trees.put(26, new
        // GenericGrower(Blocks.OAK_LOG.getDefaultState()));
    }
}
