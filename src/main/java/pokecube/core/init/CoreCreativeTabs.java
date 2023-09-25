package pokecube.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.vitamins.ItemVitamin;

@Mod.EventBusSubscriber(modid = PokecubeCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            PokecubeCore.MODID);

    // Order of items in creative tabs depends on the order items are listed in
    public static final RegistryObject<CreativeModeTab> BLOCKS_ITEMS_TAB = TABS.register("blocks_items_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.pokecube.blocks_items"))
                    .icon(() -> new ItemStack(PokecubeItems.POKEDEX.get())).build());

    public static final RegistryObject<CreativeModeTab> POKECUBES_TAB = TABS.register("cubes_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.pokecube.cubes"))
                    .icon(() -> new ItemStack(PokecubeItems.POKECUBE_CUBES.getItem()))
                    .withTabsBefore(BLOCKS_ITEMS_TAB.getId()).build());
    public static final RegistryObject<CreativeModeTab> BERRIES_TAB = TABS.register("berries_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.pokecube.berries"))
                    .icon(() -> new ItemStack(BerryManager.getBerryItem("cheri"))).withTabsBefore(POKECUBES_TAB.getId())
                    .build());

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS)
        {
            addBefore(event, Items.MILK_BUCKET, PokecubeItems.BERRYJUICE.get());
            addAfter(event, Items.PUMPKIN_PIE, PokecubeItems.CANDY.get());
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            add(event, PokecubeItems.SECRET_BASE.get());
            add(event, PokecubeItems.NEST.get());
            add(event, PokecubeItems.REPEL.get());
            add(event, PokecubeItems.HEALER.get());
            add(event, PokecubeItems.PC_TOP.get());
            add(event, PokecubeItems.PC_BASE.get());
            add(event, PokecubeItems.TRADER.get());
            add(event, PokecubeItems.TM_MACHINE.get());
            add(event, PokecubeItems.TM.get());
            add(event, PokecubeItems.DYNAMAX.get());
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS)
        {
            add(event, PokecubeItems.NEST.get());
            add(event, PokecubeItems.FOSSIL_ORE.get());
            add(event, PokecubeItems.DEEPSLATE_FOSSIL_ORE.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            addAfter(event, Items.EMERALD, PokecubeItems.EMERALDSHARD.get());
        }

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS)
        {
            add(event, PokecubeItems.SPAWN_EGG.get());
        }

        if (event.getTabKey().equals(BLOCKS_ITEMS_TAB.getKey()))
        {
            add(event, PokecubeItems.POKEDEX.get());
            add(event, PokecubeItems.POKEWATCH.get());

            add(event, PokecubeItems.NEST.get());
            add(event, PokecubeItems.FOSSIL_ORE.get());
            add(event, PokecubeItems.DEEPSLATE_FOSSIL_ORE.get());
            add(event, PokecubeItems.SECRET_BASE.get());
            add(event, PokecubeItems.REPEL.get());

            add(event, PokecubeItems.HEALER.get());
            add(event, PokecubeItems.PC_TOP.get());
            add(event, PokecubeItems.PC_BASE.get());
            add(event, PokecubeItems.TRADER.get());
            add(event, PokecubeItems.TM_MACHINE.get());
            add(event, PokecubeItems.TM.get());
            add(event, PokecubeItems.DYNAMAX.get());

            add(event, PokecubeItems.BERRYJUICE.get());
            add(event, PokecubeItems.CANDY.get());
            add(event, PokecubeItems.REVIVE.get());
            add(event, PokecubeItems.LUCKYEGG.get());
            add(event, PokecubeItems.EMERALDSHARD.get());
            add(event, PokecubeItems.SPAWN_EGG.get());

            for (String type : ItemVitamin.vitamins) add(event, PokecubeItems.getStack("vitamin_" + type));
            for (String type : ItemGenerator.fossilVariants) add(event, ItemGenerator.fossils.get(type).get());
            for (String type : ItemGenerator.misc) add(event, ItemGenerator.miscItems.get(type).get());
            for (String type : ItemGenerator.variants) add(event, ItemGenerator.variantItems.get(type).get());
        }

        if (event.getTabKey().equals(POKECUBES_TAB.getKey()))
        {
            PokecubeItems.cubeIds.forEach(id -> {
                add(event, PokecubeItems.getStack(id));
            });
        }

        if (event.getTabKey().equals(BERRIES_TAB.getKey()))
        {

            for (var berry : BerryManager.berryItems.values())
            {
                add(event, berry.get());
            }

            add(event, PokecubeItems.getStack("enigma_boat"));
            add(event, PokecubeItems.getStack("enigma_chest_boat"));
            add(event, PokecubeItems.getStack("enigma_sign"));
            add(event, PokecubeItems.getStack("leppa_boat"));
            add(event, PokecubeItems.getStack("leppa_chest_boat"));
            add(event, PokecubeItems.getStack("leppa_sign"));
            add(event, PokecubeItems.getStack("nanab_boat"));
            add(event, PokecubeItems.getStack("nanab_chest_boat"));
            add(event, PokecubeItems.getStack("nanab_sign"));
            add(event, PokecubeItems.getStack("oran_boat"));
            add(event, PokecubeItems.getStack("oran_chest_boat"));
            add(event, PokecubeItems.getStack("oran_sign"));
            add(event, PokecubeItems.getStack("pecha_boat"));
            add(event, PokecubeItems.getStack("pecha_chest_boat"));
            add(event, PokecubeItems.getStack("pecha_sign"));
            add(event, PokecubeItems.getStack("sitrus_boat"));
            add(event, PokecubeItems.getStack("sitrus_chest_boat"));
            add(event, PokecubeItems.getStack("sitrus_sign"));

            for (final String type : ItemGenerator.berryWoods.keySet())
            {
                add(event, ItemGenerator.leaves.get(type).get());
                add(event, ItemGenerator.logs.get(type).get());
                add(event, ItemGenerator.woods.get(type).get());
                add(event, ItemGenerator.stripped_logs.get(type).get());
                add(event, ItemGenerator.stripped_woods.get(type).get());
                add(event, ItemGenerator.planks.get(type).get());
                add(event, ItemGenerator.stairs.get(type).get());
                add(event, ItemGenerator.slabs.get(type).get());
                add(event, ItemGenerator.fences.get(type).get());
                add(event, ItemGenerator.fence_gates.get(type).get());
                add(event, ItemGenerator.doors.get(type).get());
                add(event, ItemGenerator.trapdoors.get(type).get());
                add(event, ItemGenerator.pressure_plates.get(type).get());
                add(event, ItemGenerator.buttons.get(type).get());
            }

            add(event, PokecubeItems.ENIGMA_BARREL.get());
            add(event, PokecubeItems.ENIGMA_BOOKSHELF_EMPTY.get());
            add(event, PokecubeItems.LEPPA_BARREL.get());
            add(event, PokecubeItems.LEPPA_BOOKSHELF_EMPTY.get());
            add(event, PokecubeItems.NANAB_BARREL.get());
            add(event, PokecubeItems.NANAB_BOOKSHELF_EMPTY.get());
            add(event, PokecubeItems.ORAN_BARREL.get());
            add(event, PokecubeItems.ORAN_BOOKSHELF_EMPTY.get());
            add(event, PokecubeItems.PECHA_BARREL.get());
            add(event, PokecubeItems.PECHA_BOOKSHELF_EMPTY.get());
            add(event, PokecubeItems.SITRUS_BARREL.get());
            add(event, PokecubeItems.SITRUS_BOOKSHELF_EMPTY.get());

            for (final String type : ItemGenerator.onlyBerryLeaves.keySet())
            {
                add(event, ItemGenerator.leaves.get(type).get());
            }
        }
    }

    public static void add(BuildCreativeModeTabContentsEvent event, ItemLike item)
    {
        ItemStack stack = new ItemStack(item);
        add(event, stack);
    }

    public static void add(BuildCreativeModeTabContentsEvent event, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            PokecubeAPI.LOGGER.error("Warning, Attempting to register an empty stack to tab!",
                    new IllegalArgumentException());
            return;
        }
        event.accept(stack);
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, ItemLike afterItem, ItemLike item) {
        event.getEntries().putAfter(new ItemStack(afterItem), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void addBefore(BuildCreativeModeTabContentsEvent event, ItemLike beforeItem, ItemLike item) {
        event.getEntries().putBefore(new ItemStack(beforeItem), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
