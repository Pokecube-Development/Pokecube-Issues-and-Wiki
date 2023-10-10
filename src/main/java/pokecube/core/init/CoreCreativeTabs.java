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
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;
import thut.bling.BlingItem;
import thut.wearables.ThutWearables;

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
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addBefore(event, Items.MILK_BUCKET, PokecubeItems.BERRYJUICE.get());
            addAfter(event, Items.PUMPKIN_PIE, PokecubeItems.CANDY.get());
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.BLAST_FURNACE, PokecubeItems.HEALER.get());
            addAfter(event, PokecubeItems.HEALER.get(), PokecubeItems.PC_TOP.get());
            addAfter(event, PokecubeItems.PC_TOP.get(), PokecubeItems.PC_BASE.get());
            addAfter(event, PokecubeItems.PC_BASE.get(), PokecubeItems.TRADER.get());
            addAfter(event, PokecubeItems.TRADER.get(), PokecubeItems.TM_MACHINE.get());
            addAfter(event, PokecubeItems.TM_MACHINE.get(), PokecubeItems.TM.get());
            addAfter(event, PokecubeItems.TM.get(), PokecubeItems.DYNAMAX.get());
            addAfter(event, Items.LODESTONE, PokecubeItems.SECRET_BASE.get());
            addBefore(event, Items.BEE_NEST, PokecubeItems.NEST.get());
            addBefore(event, Items.SUSPICIOUS_SAND, PokecubeItems.REPEL.get());

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
            addAfter(event, PokecubeItems.getStack("enigma_sign").getItem(), PokecubeItems.getStack("enigma_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_hanging_sign").getItem(), PokecubeItems.getStack("leppa_sign").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_sign").getItem(), PokecubeItems.getStack("leppa_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_hanging_sign").getItem(), PokecubeItems.getStack("nanab_sign").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_sign").getItem(), PokecubeItems.getStack("nanab_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_hanging_sign").getItem(), PokecubeItems.getStack("oran_sign").getItem());
            addAfter(event, PokecubeItems.getStack("oran_sign").getItem(), PokecubeItems.getStack("oran_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("oran_hanging_sign").getItem(), PokecubeItems.getStack("pecha_sign").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_sign").getItem(), PokecubeItems.getStack("pecha_hanging_sign").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_hanging_sign").getItem(), PokecubeItems.getStack("sitrus_sign").getItem());
            addAfter(event, PokecubeItems.getStack("sitrus_sign").getItem(), PokecubeItems.getStack("sitrus_hanging_sign").getItem());
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.WARPED_FUNGUS_ON_A_STICK, PokecubeItems.REVIVE.get());
            addAfter(event, PokecubeItems.REVIVE.get(), PokecubeItems.LUCKYEGG.get());
            addAfter(event, PokecubeItems.LUCKYEGG.get(), PokecubeItems.TM.get());

            addAfter(event, Items.BAMBOO_CHEST_RAFT, PokecubeItems.getStack("enigma_boat").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_boat").getItem(), PokecubeItems.getStack("enigma_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("enigma_chest_boat").getItem(), PokecubeItems.getStack("leppa_boat").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_boat").getItem(), PokecubeItems.getStack("leppa_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("leppa_chest_boat").getItem(), PokecubeItems.getStack("nanab_boat").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_boat").getItem(), PokecubeItems.getStack("nanab_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("nanab_chest_boat").getItem(), PokecubeItems.getStack("oran_boat").getItem());
            addAfter(event, PokecubeItems.getStack("oran_boat").getItem(), PokecubeItems.getStack("oran_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("oran_chest_boat").getItem(), PokecubeItems.getStack("pecha_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_boat").getItem(), PokecubeItems.getStack("pecha_chest_boat").getItem());
            addAfter(event, PokecubeItems.getStack("pecha_chest_boat").getItem(), PokecubeItems.getStack("sitrus_boat").getItem());
            addAfter(event, PokecubeItems.getStack("sitrus_boat").getItem(), PokecubeItems.getStack("sitrus_chest_boat").getItem());
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.DEEPSLATE_COAL_ORE, PokecubeItems.FOSSIL_ORE.get());
            addAfter(event, PokecubeItems.FOSSIL_ORE.get(), PokecubeItems.DEEPSLATE_FOSSIL_ORE.get());
            addBefore(event, Items.BEE_NEST, PokecubeItems.NEST.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS && PokecubeCore.getConfig().itemsInVanillaTabs)
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
                add(event, ItemGenerator.leaves.get(type).get());
            }

            for (final String type : ItemGenerator.berryWoods.keySet())
            {
                add(event, ItemGenerator.leaves.get(type).get());
                add(event, ItemGenerator.logs.get(type).get());
                add(event, ItemGenerator.woods.get(type).get());
                add(event, ItemGenerator.stripped_logs.get(type).get());
                add(event, ItemGenerator.stripped_woods.get(type).get());
                add(event, ItemGenerator.barrels.get(type).get());
                add(event, ItemGenerator.bookshelves.get(type).get());
                add(event, ItemGenerator.fillable_shelves.get(type).get());
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
        }

        if (event.getTabKey().equals(ThutWearables.WEARABLES_TAB.getKey()))
        {
            addBefore(event, BlingItem.getStack("bling_bag").getItem(), BlingItem.getStack("pokecube:pokewatch").getItem());
            addBefore(event, BlingItem.getStack("bling_hat").getItem(), BlingItem.getStack("pokecube:mega_hat").getItem());
            addBefore(event, BlingItem.getStack("pokecube:mega_hat").getItem(), BlingItem.getStack("pokecube:mega_tiara").getItem());
            addBefore(event, BlingItem.getStack("bling_eye").getItem(), BlingItem.getStack("pokecube:mega_glasses").getItem());
            addBefore(event, BlingItem.getStack("bling_neck").getItem(), BlingItem.getStack("pokecube:mega_pendant").getItem());
            addBefore(event, BlingItem.getStack("bling_ear").getItem(), BlingItem.getStack("pokecube:mega_earring").getItem());
            addBefore(event, BlingItem.getStack("bling_waist").getItem(), BlingItem.getStack("pokecube:mega_belt").getItem());
            addBefore(event, BlingItem.getStack("bling_ring").getItem(), BlingItem.getStack("pokecube:mega_ring").getItem());
            addBefore(event, BlingItem.getStack("bling_ankle").getItem(), BlingItem.getStack("pokecube:mega_ankletzinnia").getItem());

            for (final String type : ItemGenerator.megaWearables.keySet())
                add(event, ItemGenerator.megaWearables.get(type).get());
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
