package pokecube.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.items.berries.BerryManager;
import pokecube.legends.init.ItemInit;

@Mod.EventBusSubscriber(modid = PokecubeCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PokecubeCore.MODID);

    // Order of items in creative tabs depends on the order items are listed in
    public static final RegistryObject<CreativeModeTab> BLOCKS_ITEMS_TAB = TABS.register("blocks_items_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube.blocks_items"))
            .icon(() -> new ItemStack(PokecubeItems.POKEDEX.get()))
            .displayItems((parameters, output) -> {
                output.accept(PokecubeItems.POKEDEX.get());
                output.accept(PokecubeItems.POKEWATCH.get());
                output.accept(PokecubeItems.getStack("pokecube_adventures:linker"));

                output.accept(PokecubeItems.NEST.get());
                output.accept(PokecubeItems.FOSSIL_ORE.get());
                output.accept(PokecubeItems.DEEPSLATE_FOSSIL_ORE.get());
                output.accept(PokecubeItems.SECRET_BASE.get());
                output.accept(PokecubeItems.REPEL.get());

                output.accept(PokecubeItems.HEALER.get());
                output.accept(PokecubeItems.PC_TOP.get());
                output.accept(PokecubeItems.PC_BASE.get());
                output.accept(PokecubeItems.TRADER.get());
                output.accept(PokecubeItems.TM_MACHINE.get());
                output.accept(PokecubeItems.TM.get());
                output.accept(PokecubeItems.DYNAMAX.get());

                output.accept(PokecubeItems.BERRYJUICE.get());
                output.accept(PokecubeItems.CANDY.get());
                output.accept(PokecubeItems.REVIVE.get());
                output.accept(PokecubeItems.LUCKYEGG.get());
                output.accept(PokecubeItems.SPAWN_EGG.get());
                output.accept(PokecubeItems.EMERALDSHARD.get());

                output.accept(PokecubeItems.getStack("vitamin_protein"));
                output.accept(PokecubeItems.getStack("vitamin_calcium"));
                output.accept(PokecubeItems.getStack("vitamin_iron"));
                output.accept(PokecubeItems.getStack("vitamin_zinc"));
                output.accept(PokecubeItems.getStack("vitamin_hpup"));
                output.accept(PokecubeItems.getStack("vitamin_carbos"));

                for (final String type : ItemGenerator.fossilVariants)
                    output.accept(ItemGenerator.fossils.get(type).get());
                for (final String type : ItemGenerator.misc)
                    output.accept(ItemGenerator.miscItems.get(type).get());
                for (final String type : ItemGenerator.variants)
                    output.accept(ItemGenerator.variantItems.get(type).get());

            }).build());

    public static final RegistryObject<CreativeModeTab> POKECUBES_TAB = TABS.register("cubes_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube.cubes"))
            .icon(() -> new ItemStack(PokecubeItems.POKECUBE_CUBES.getItem()))
            .withTabsBefore(BLOCKS_ITEMS_TAB.getId())
            .displayItems((parameters, output) -> {
                output.accept(PokecubeItems.getStack("pokeseal"));
                output.accept(PokecubeItems.getStack("pokecube"));
                output.accept(PokecubeItems.getStack("cherishcube"));
                output.accept(PokecubeItems.getStack("repeatcube"));
                output.accept(PokecubeItems.getStack("sportcube"));
                output.accept(PokecubeItems.getStack("fastcube"));
                output.accept(PokecubeItems.getStack("parkcube"));
                output.accept(PokecubeItems.getStack("ultracube"));
                output.accept(PokecubeItems.getStack("duskcube"));
                output.accept(PokecubeItems.getStack("friendcube"));
                output.accept(PokecubeItems.getStack("nestcube"));
                output.accept(PokecubeItems.getStack("safaricube"));
                output.accept(PokecubeItems.getStack("netcube"));
                output.accept(PokecubeItems.getStack("greatcube"));
                output.accept(PokecubeItems.getStack("lurecube"));
                output.accept(PokecubeItems.getStack("mooncube"));
                output.accept(PokecubeItems.getStack("quickcube"));
                output.accept(PokecubeItems.getStack("typingcube"));
                output.accept(PokecubeItems.getStack("divecube"));
                output.accept(PokecubeItems.getStack("heavycube"));
                output.accept(PokecubeItems.getStack("mastercube"));
                output.accept(PokecubeItems.getStack("snagcube"));
                output.accept(PokecubeItems.getStack("lovecube"));
                output.accept(PokecubeItems.getStack("dreamcube"));
                output.accept(PokecubeItems.getStack("healcube"));
                output.accept(PokecubeItems.getStack("dynacube"));
                output.accept(PokecubeItems.getStack("teamaquacube"));
                output.accept(PokecubeItems.getStack("premiercube"));
                output.accept(PokecubeItems.getStack("timercube"));
                output.accept(PokecubeItems.getStack("rocketcube"));
                output.accept(PokecubeItems.getStack("teammagmacube"));
                output.accept(PokecubeItems.getStack("levelcube"));
                output.accept(PokecubeItems.getStack("luxurycube"));
                output.accept(PokecubeItems.getStack("beastcube"));
                output.accept(PokecubeItems.getStack("clonecube"));
            }).build());

    public static final RegistryObject<CreativeModeTab> BERRIES_TAB = TABS.register("berries_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube.berries"))
            .icon(() -> new ItemStack(BerryManager.getBerryItem("cheri")))
            .withTabsBefore(POKECUBES_TAB.getId())
            .displayItems((parameters, output) -> {
                for (final Integer type : BerryManager.berryItems.keySet())
                {
                    output.accept(BerryManager.berryItems.get(type).get());
                }

                output.accept(ItemInit.NULL_POKEPUFF.get());
                output.accept(ItemInit.CHERI_POKEPUFF.get());
                output.accept(ItemInit.CHESTO_POKEPUFF.get());
                output.accept(ItemInit.PECHA_POKEPUFF.get());
                output.accept(ItemInit.RAWST_POKEPUFF.get());
                output.accept(ItemInit.ASPEAR_POKEPUFF.get());
                output.accept(ItemInit.LEPPA_POKEPUFF.get());
                output.accept(ItemInit.ORAN_POKEPUFF.get());
                output.accept(ItemInit.PERSIM_POKEPUFF.get());
                output.accept(ItemInit.LUM_POKEPUFF.get());
                output.accept(ItemInit.SITRUS_POKEPUFF.get());
                output.accept(ItemInit.NANAB_POKEPUFF.get());
                output.accept(ItemInit.PINAP_POKEPUFF.get());
                output.accept(ItemInit.POMEG_POKEPUFF.get());
                output.accept(ItemInit.KELPSY_POKEPUFF.get());
                output.accept(ItemInit.QUALOT_POKEPUFF.get());
                output.accept(ItemInit.HONDEW_POKEPUFF.get());
                output.accept(ItemInit.GREPA_POKEPUFF.get());
                output.accept(ItemInit.TAMATO_POKEPUFF.get());
                output.accept(ItemInit.CORNN_POKEPUFF.get());
                output.accept(ItemInit.ENIGMA_POKEPUFF.get());
                output.accept(ItemInit.JABOCA_POKEPUFF.get());
                output.accept(ItemInit.ROWAP_POKEPUFF.get());

                output.accept(PokecubeItems.getStack("enigma_boat"));
                output.accept(PokecubeItems.getStack("enigma_chest_boat"));
                output.accept(PokecubeItems.getStack("enigma_sign"));
                output.accept(PokecubeItems.getStack("leppa_boat"));
                output.accept(PokecubeItems.getStack("leppa_chest_boat"));
                output.accept(PokecubeItems.getStack("leppa_sign"));
                output.accept(PokecubeItems.getStack("nanab_boat"));
                output.accept(PokecubeItems.getStack("nanab_chest_boat"));
                output.accept(PokecubeItems.getStack("nanab_sign"));
                output.accept(PokecubeItems.getStack("oran_boat"));
                output.accept(PokecubeItems.getStack("oran_chest_boat"));
                output.accept(PokecubeItems.getStack("oran_sign"));
                output.accept(PokecubeItems.getStack("pecha_boat"));
                output.accept(PokecubeItems.getStack("pecha_chest_boat"));
                output.accept(PokecubeItems.getStack("pecha_sign"));
                output.accept(PokecubeItems.getStack("sitrus_boat"));
                output.accept(PokecubeItems.getStack("sitrus_chest_boat"));
                output.accept(PokecubeItems.getStack("sitrus_sign"));

                for (final String type : ItemGenerator.berryWoods.keySet())
                {
                    output.accept(ItemGenerator.leaves.get(type).get());
                    output.accept(ItemGenerator.logs.get(type).get());
                    output.accept(ItemGenerator.woods.get(type).get());
                    output.accept(ItemGenerator.stripped_logs.get(type).get());
                    output.accept(ItemGenerator.stripped_woods.get(type).get());
                    output.accept(ItemGenerator.planks.get(type).get());
                    output.accept(ItemGenerator.stairs.get(type).get());
                    output.accept(ItemGenerator.slabs.get(type).get());
                    output.accept(ItemGenerator.fences.get(type).get());
                    output.accept(ItemGenerator.fence_gates.get(type).get());
                    output.accept(ItemGenerator.doors.get(type).get());
                    output.accept(ItemGenerator.trapdoors.get(type).get());
                    output.accept(ItemGenerator.pressure_plates.get(type).get());
                    output.accept(ItemGenerator.buttons.get(type).get());
                }

                output.accept(PokecubeItems.ENIGMA_BARREL.get());
                output.accept(PokecubeItems.ENIGMA_BOOKSHELF_EMPTY.get());
                output.accept(PokecubeItems.LEPPA_BARREL.get());
                output.accept(PokecubeItems.LEPPA_BOOKSHELF_EMPTY.get());
                output.accept(PokecubeItems.NANAB_BARREL.get());
                output.accept(PokecubeItems.NANAB_BOOKSHELF_EMPTY.get());
                output.accept(PokecubeItems.ORAN_BARREL.get());
                output.accept(PokecubeItems.ORAN_BOOKSHELF_EMPTY.get());
                output.accept(PokecubeItems.PECHA_BARREL.get());
                output.accept(PokecubeItems.PECHA_BOOKSHELF_EMPTY.get());
                output.accept(PokecubeItems.SITRUS_BARREL.get());
                output.accept(PokecubeItems.SITRUS_BOOKSHELF_EMPTY.get());

                for (final String type : ItemGenerator.onlyBerryLeaves.keySet())
                {
                    output.accept(ItemGenerator.leaves.get(type).get());
                }
            }).build());

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS)
        {
            event.accept(PokecubeItems.BERRYJUICE.get());
            event.accept(PokecubeItems.CANDY.get());

            event.accept(ItemInit.NULL_POKEPUFF.get());
            event.accept(ItemInit.CHERI_POKEPUFF.get());
            event.accept(ItemInit.CHESTO_POKEPUFF.get());
            event.accept(ItemInit.PECHA_POKEPUFF.get());
            event.accept(ItemInit.RAWST_POKEPUFF.get());
            event.accept(ItemInit.ASPEAR_POKEPUFF.get());
            event.accept(ItemInit.LEPPA_POKEPUFF.get());
            event.accept(ItemInit.ORAN_POKEPUFF.get());
            event.accept(ItemInit.PERSIM_POKEPUFF.get());
            event.accept(ItemInit.LUM_POKEPUFF.get());
            event.accept(ItemInit.SITRUS_POKEPUFF.get());
            event.accept(ItemInit.NANAB_POKEPUFF.get());
            event.accept(ItemInit.PINAP_POKEPUFF.get());
            event.accept(ItemInit.POMEG_POKEPUFF.get());
            event.accept(ItemInit.KELPSY_POKEPUFF.get());
            event.accept(ItemInit.QUALOT_POKEPUFF.get());
            event.accept(ItemInit.HONDEW_POKEPUFF.get());
            event.accept(ItemInit.GREPA_POKEPUFF.get());
            event.accept(ItemInit.TAMATO_POKEPUFF.get());
            event.accept(ItemInit.CORNN_POKEPUFF.get());
            event.accept(ItemInit.ENIGMA_POKEPUFF.get());
            event.accept(ItemInit.JABOCA_POKEPUFF.get());
            event.accept(ItemInit.ROWAP_POKEPUFF.get());
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            event.accept(PokecubeItems.SECRET_BASE.get());
            event.accept(PokecubeItems.NEST.get());
            event.accept(PokecubeItems.REPEL.get());
            event.accept(PokecubeItems.HEALER.get());
            event.accept(PokecubeItems.PC_TOP.get());
            event.accept(PokecubeItems.PC_BASE.get());
            event.accept(PokecubeItems.TRADER.get());
            event.accept(PokecubeItems.TM_MACHINE.get());
            event.accept(PokecubeItems.TM.get());
            event.accept(PokecubeItems.DYNAMAX.get());
        }

        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS)
        {
            event.accept(PokecubeItems.NEST.get());
            event.accept(PokecubeItems.FOSSIL_ORE.get());
            event.accept(PokecubeItems.DEEPSLATE_FOSSIL_ORE.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(PokecubeItems.EMERALDSHARD.get());
        }

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS)
        {
            event.accept(PokecubeItems.SPAWN_EGG.get());
        }
    }
}
