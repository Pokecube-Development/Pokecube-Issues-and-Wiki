/**
 *
 */
package pokecube.core.items.berries;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.UsableItemEffects.BerryUsable.BerryEffect;

/**
 * @author Oracion
 * @author Manchou
 */
public class BerryManager
{
    public static class BerryType extends Properties
    {
        public final int index;
        public final int[] flavours;
        public final String name;
        public final BerryEffect effect;

        public BerryType(final String name, final BerryEffect effect, final int index, final int... flavours)
        {
            this.name = name;
            this.effect = effect;
            this.index = index;
            this.flavours = flavours;
            Thread.dumpStack();
            // TODO need to post register flavours as defaults.
        }
    }

    /** Map of berry id -> fruit crops */
    public static Int2ObjectArrayMap<DeferredBlock<Block>> berryCrops = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit items */
    public static Int2ObjectArrayMap<DeferredBlock<Block>> berryFruits = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit items */
    public static Int2ObjectArrayMap<DeferredItem<ItemBerry>> berryItems = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit leaves */
    public static Int2ObjectArrayMap<DeferredBlock<Block>> berryLeaves = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit logs */
    public static Int2ObjectArrayMap<DeferredBlock<Block>> berryLogs = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit planks */
    public static Int2ObjectArrayMap<DeferredBlock<Block>> berryPlanks = new Int2ObjectArrayMap<>();
    /** Map of berry id -> name of berry */
    public static Int2ObjectArrayMap<String> berryNames = new Int2ObjectArrayMap<>();
    /** Map of berry id -> name of berry */
    public static Map<String, DeferredItem<ItemBerry>> byName = Maps.newHashMap();
    /** Map of berry id -> name of berry */
    public static Map<String, Integer> indexByName = Maps.newHashMap();
    /** Map of berry id -> block of potted berries */
    public static Int2ObjectArrayMap<DeferredBlock<Block>> pottedBerries = new Int2ObjectArrayMap<>();
    /** Map of berry id -> fruit items */
    public static Int2ObjectArrayMap<BerryType> berryTypes = new Int2ObjectArrayMap<>();

    public static Supplier<DataComponentType<PokeblocData>> TASTE_DATA;

    public static void registerComponents(DeferredRegister<DataComponentType<?>> registry)
    {
        TASTE_DATA = registry.register("pokebloc_flavours", name -> new DataComponentType.Builder<PokeblocData>()
                .persistent(PokeblocData.CODEC).networkSynchronized(PokeblocData.STREAM_CODEC).build());
    }

    public static void modifyComponents(ModifyDefaultComponentsEvent event)
    {
        event.getAllItems().forEach(item -> {
            if (item instanceof ItemBerry berry)
            {
                event.modify(item, b -> b.set(TASTE_DATA.get(), new PokeblocData(berry.type.flavours)));
            }
        });
    }

    public static void addBerry(final DeferredItem<ItemBerry> berry, final BerryType type)
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
