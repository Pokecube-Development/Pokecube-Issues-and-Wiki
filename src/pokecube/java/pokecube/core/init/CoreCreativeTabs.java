package pokecube.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.vitamins.ItemVitamin;
import thut.bling.BlingItem;
import thut.wearables.ThutWearables;

import java.util.function.Supplier;

@EventBusSubscriber(modid = PokecubeCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CoreCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            PokecubeCore.MODID);

    // Order of items in creative tabs depends on the order items are listed in
    public static final Supplier<CreativeModeTab> BLOCKS_ITEMS_TAB = TABS.register("blocks_items_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.pokecube.blocks_items"))
                    .icon(() -> new ItemStack(PokecubeItems.POKEDEX.get())).build());

    public static final Supplier<CreativeModeTab> POKECUBES_TAB = TABS.register("cubes_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.pokecube.cubes"))
                    .icon(() -> new ItemStack(PokecubeItems.POKECUBE_CUBES.getItem()))
                    .withTabsBefore(ResourceLocation.parse("pokecube:blocks_items_tab")).build());
    public static final Supplier<CreativeModeTab> BERRIES_TAB = TABS.register("berries_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.pokecube.berries"))
                    .icon(() -> new ItemStack(BerryManager.getBerryItem("cheri")))
                    .withTabsBefore(ResourceLocation.parse("pokecube:cubes_tab")).build());

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab().equals(BLOCKS_ITEMS_TAB.get()))
        {
            add(event, PokecubeItems.POKEDEX);
            add(event, PokecubeItems.POKEWATCH);

            add(event, PokecubeItems.NEST);
            add(event, PokecubeItems.FOSSIL_ORE);
            add(event, PokecubeItems.DEEPSLATE_FOSSIL_ORE);
            add(event, PokecubeItems.SECRET_BASE);
            add(event, PokecubeItems.REPEL);

            add(event, PokecubeItems.HEALER);
            add(event, PokecubeItems.PC_TOP);
            add(event, PokecubeItems.PC_BASE);
            add(event, PokecubeItems.TRADER);
            add(event, PokecubeItems.TM_MACHINE);
            add(event, PokecubeItems.TM);
            add(event, PokecubeItems.DYNAMAX);

            add(event, PokecubeItems.BERRYJUICE);
            add(event, PokecubeItems.CANDY);
            add(event, PokecubeItems.REVIVE);
            add(event, PokecubeItems.LUCKYEGG);
            add(event, PokecubeItems.EMERALDSHARD);
            add(event, PokecubeItems.SPAWN_EGG);

            for (String type : ItemVitamin.vitamins) add(event, PokecubeItems.getStack("vitamin_" + type));
            for (String type : ItemGenerator.fossilVariants) add(event, ItemGenerator.fossils.get(type));
            for (String type : ItemGenerator.misc) add(event, ItemGenerator.miscItems.get(type));
            for (String type : ItemGenerator.variants) add(event, ItemGenerator.variantItems.get(type));
        }

        if (event.getTab().equals(POKECUBES_TAB.get()))
        {
            PokecubeItems.cubeIds.forEach(id -> add(event, PokecubeItems.getStack(id)));
        }

        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addBefore(event, Items.MILK_BUCKET, PokecubeItems.BERRYJUICE);
            addAfter(event, Items.PUMPKIN_PIE, PokecubeItems.CANDY);
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.BLAST_FURNACE, PokecubeItems.REPEL);
            addAfter(event, PokecubeItems.REPEL, PokecubeItems.HEALER);
            addAfter(event, PokecubeItems.HEALER, PokecubeItems.PC_TOP);
            addAfter(event, PokecubeItems.PC_TOP, PokecubeItems.PC_BASE);
            addAfter(event, PokecubeItems.PC_BASE, PokecubeItems.TRADER);
            addAfter(event, PokecubeItems.TRADER, PokecubeItems.TM_MACHINE);
            addAfter(event, PokecubeItems.TM_MACHINE, PokecubeItems.TM);
            addAfter(event, PokecubeItems.TM, PokecubeItems.DYNAMAX);
            addAfter(event, Items.LODESTONE, PokecubeItems.SECRET_BASE);
            addBefore(event, Items.BEE_NEST, PokecubeItems.NEST);

            addAfter(event, Items.BOOKSHELF, PokecubeItems.getStack("enigma_bookshelf").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_bookshelf").getItem(),
                    PokecubeItems.getStack("leppa_bookshelf").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_bookshelf").getItem(),
                    PokecubeItems.getStack("nanab_bookshelf").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_bookshelf").getItem(),
                    PokecubeItems.getStack("oran_bookshelf").getItem());
            addAfter(event, PokecubeItems.getStack("oran_bookshelf").getItem(),
                    PokecubeItems.getStack("pecha_bookshelf").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_bookshelf").getItem(),
                    PokecubeItems.getStack("sitrus_bookshelf").getItem());

            addAfter(event, Items.CHISELED_BOOKSHELF, PokecubeItems.getStack("enigma_bookshelf_empty").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_bookshelf_empty").getItem(),
                    PokecubeItems.getStack("leppa_bookshelf_empty").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_bookshelf_empty").getItem(),
                    PokecubeItems.getStack("nanab_bookshelf_empty").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_bookshelf_empty").getItem(),
                    PokecubeItems.getStack("oran_bookshelf_empty").getItem());
            addAfter(event, PokecubeItems.getStack("oran_bookshelf_empty").getItem(),
                    PokecubeItems.getStack("pecha_bookshelf_empty").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_bookshelf_empty").getItem(),
                    PokecubeItems.getStack("sitrus_bookshelf_empty").getItem());

            addAfter(event, Items.BARREL, PokecubeItems.getStack("enigma_barrel").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_barrel").getItem(),
                    PokecubeItems.getStack("leppa_barrel").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_barrel").getItem(),
                    PokecubeItems.getStack("nanab_barrel").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_barrel").getItem(),
                    PokecubeItems.getStack("oran_barrel").getItem());
            addAfter(event, PokecubeItems.getStack("oran_barrel").getItem(),
                    PokecubeItems.getStack("pecha_barrel").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_barrel").getItem(),
                    PokecubeItems.getStack("sitrus_barrel").getItem());

            addAfter(event, Items.WARPED_HANGING_SIGN, PokecubeItems.getStack("enigma_sign").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_sign").getItem(),
                    PokecubeItems.getStack("enigma_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_hanging_sign").getItem(),
                    PokecubeItems.getStack("leppa_sign").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_sign").getItem(),
                    PokecubeItems.getStack("leppa_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_hanging_sign").getItem(),
                    PokecubeItems.getStack("nanab_sign").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_sign").getItem(),
                    PokecubeItems.getStack("nanab_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_hanging_sign").getItem(),
                    PokecubeItems.getStack("oran_sign").getItem());
            addAfter(event, PokecubeItems.getStack("oran_sign").getItem(),
                    PokecubeItems.getStack("oran_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("oran_hanging_sign").getItem(),
                    PokecubeItems.getStack("pecha_sign").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_sign").getItem(),
                    PokecubeItems.getStack("pecha_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_hanging_sign").getItem(),
                    PokecubeItems.getStack("sitrus_sign").getItem());
            addAfter(event, PokecubeItems.getStack("sitrus_sign").getItem(),
                    PokecubeItems.getStack("sitrus_hanging_sign").getItem());
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.WARPED_FUNGUS_ON_A_STICK, PokecubeItems.REVIVE);
            addAfter(event, PokecubeItems.REVIVE.get(), PokecubeItems.LUCKYEGG);
            addAfter(event, PokecubeItems.LUCKYEGG.get(), PokecubeItems.TM);

            addAfter(event, Items.BAMBOO_CHEST_RAFT, PokecubeItems.getStack("enigma_boat").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_boat").getItem(),
                    PokecubeItems.getStack("enigma_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_chest_boat").getItem(),
                    PokecubeItems.getStack("leppa_boat").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_boat").getItem(),
                    PokecubeItems.getStack("leppa_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_chest_boat").getItem(),
                    PokecubeItems.getStack("nanab_boat").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_boat").getItem(),
                    PokecubeItems.getStack("nanab_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_chest_boat").getItem(),
                    PokecubeItems.getStack("oran_boat").getItem());
            addAfter(event, PokecubeItems.getStack("oran_boat").getItem(),
                    PokecubeItems.getStack("oran_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("oran_chest_boat").getItem(),
                    PokecubeItems.getStack("pecha_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_boat").getItem(),
                    PokecubeItems.getStack("pecha_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_chest_boat").getItem(),
                    PokecubeItems.getStack("sitrus_boat").getItem());
            addAfter(event, PokecubeItems.getStack("sitrus_boat").getItem(),
                    PokecubeItems.getStack("sitrus_chest_boat").getItem());
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.DEEPSLATE_COAL_ORE, PokecubeItems.FOSSIL_ORE);
            addAfter(event, PokecubeItems.FOSSIL_ORE, PokecubeItems.DEEPSLATE_FOSSIL_ORE);
            addBefore(event, Items.BEE_NEST, PokecubeItems.NEST);
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.EMERALD, PokecubeItems.EMERALDSHARD);
        }

        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.REDSTONE_LAMP, PokecubeItems.REPEL);
        }

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS)
        {
            add(event, PokecubeItems.SPAWN_EGG);
        }

        if (event.getTab().equals(BERRIES_TAB.get()))
        {
            for (var berry : BerryManager.berryItems.values())
            {
                add(event, berry);
            }

            add(event, PokecubeItems.getStack("enigma_boat"));
            add(event, PokecubeItems.getStack("enigma_chest_boat"));
            add(event, PokecubeItems.getStack("leppa_boat"));
            add(event, PokecubeItems.getStack("leppa_chest_boat"));
            add(event, PokecubeItems.getStack("nanab_boat"));
            add(event, PokecubeItems.getStack("nanab_chest_boat"));
            add(event, PokecubeItems.getStack("oran_boat"));
            add(event, PokecubeItems.getStack("oran_chest_boat"));
            add(event, PokecubeItems.getStack("pecha_boat"));
            add(event, PokecubeItems.getStack("pecha_chest_boat"));
            add(event, PokecubeItems.getStack("sitrus_boat"));
            add(event, PokecubeItems.getStack("sitrus_chest_boat"));

            add(event, PokecubeItems.getStack("enigma_sign"));
            add(event, PokecubeItems.getStack("enigma_hanging_sign"));
            add(event, PokecubeItems.getStack("leppa_sign"));
            add(event, PokecubeItems.getStack("leppa_hanging_sign"));
            add(event, PokecubeItems.getStack("nanab_sign"));
            add(event, PokecubeItems.getStack("nanab_hanging_sign"));
            add(event, PokecubeItems.getStack("oran_sign"));
            add(event, PokecubeItems.getStack("oran_hanging_sign"));
            add(event, PokecubeItems.getStack("pecha_sign"));
            add(event, PokecubeItems.getStack("pecha_hanging_sign"));
            add(event, PokecubeItems.getStack("sitrus_sign"));
            add(event, PokecubeItems.getStack("sitrus_hanging_sign"));

            for (final String type : ItemGenerator.onlyBerryLeaves.keySet())
            {
                add(event, ItemGenerator.leaves.get(type));
            }

            for (final String type : ItemGenerator.berryWoods.keySet())
            {
                add(event, ItemGenerator.leaves.get(type));
                add(event, ItemGenerator.logs.get(type));
                add(event, ItemGenerator.woods.get(type));
                add(event, ItemGenerator.stripped_logs.get(type));
                add(event, ItemGenerator.stripped_woods.get(type));
                add(event, ItemGenerator.barrels.get(type));
                add(event, ItemGenerator.bookshelves.get(type));
                add(event, ItemGenerator.fillable_shelves.get(type));
                add(event, ItemGenerator.planks.get(type));
                add(event, ItemGenerator.stairs.get(type));
                add(event, ItemGenerator.slabs.get(type));
                add(event, ItemGenerator.fences.get(type));
                add(event, ItemGenerator.fence_gates.get(type));
                add(event, ItemGenerator.doors.get(type));
                add(event, ItemGenerator.trapdoors.get(type));
                add(event, ItemGenerator.pressure_plates.get(type));
                add(event, ItemGenerator.buttons.get(type));
            }
        }

        if (event.getTab().equals(ThutWearables.WEARABLES_TAB.get()))
        {
            addBefore(event, BlingItem.getStack("bling_bag").getItem(),
                    BlingItem.getStack("pokecube:pokewatch").getItem());
            addBefore(event, BlingItem.getStack("bling_hat").getItem(),
                    BlingItem.getStack("pokecube:mega_hat").getItem());
            addBefore(event, BlingItem.getStack("pokecube:mega_hat").getItem(),
                    BlingItem.getStack("pokecube:mega_tiara").getItem());
            addBefore(event, BlingItem.getStack("bling_eye").getItem(),
                    BlingItem.getStack("pokecube:mega_glasses").getItem());
            addBefore(event, BlingItem.getStack("bling_neck").getItem(),
                    BlingItem.getStack("pokecube:mega_pendant").getItem());
            addBefore(event, BlingItem.getStack("bling_ear").getItem(),
                    BlingItem.getStack("pokecube:mega_earring").getItem());
            addBefore(event, BlingItem.getStack("bling_waist").getItem(),
                    BlingItem.getStack("pokecube:mega_belt").getItem());
            addBefore(event, BlingItem.getStack("bling_ring").getItem(),
                    BlingItem.getStack("pokecube:mega_ring").getItem());
            addBefore(event, BlingItem.getStack("bling_ankle").getItem(),
                    BlingItem.getStack("pokecube:mega_ankletzinnia").getItem());
        }
    }

    public static void add(BuildCreativeModeTabContentsEvent event, Supplier<ItemLike> item)
    {
        if(item == null) return;
        ItemStack stack = new ItemStack(item.get());
        add(event, stack);
    }

    public static void add(BuildCreativeModeTabContentsEvent event, ItemLike item)
    {
        if(item == null) return;
        ItemStack stack = new ItemStack(item);
        add(event, stack);
    }

    public static void add(BuildCreativeModeTabContentsEvent event, ItemStack stack)
    {
        if (stack.isEmpty() || stack==null)
        {
            PokecubeAPI.LOGGER.error("Warning, Attempting to register an empty stack to tab!",
                    new IllegalArgumentException());
            return;
        }
        event.accept(stack);
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, ItemLike afterItem, ItemLike item)
    {
        event.insertAfter(new ItemStack(afterItem), new ItemStack(item),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void addBefore(BuildCreativeModeTabContentsEvent event, ItemLike beforeItem, ItemLike item)
    {
        event.insertBefore(new ItemStack(beforeItem), new ItemStack(item),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
