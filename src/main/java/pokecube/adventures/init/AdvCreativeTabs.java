package pokecube.adventures.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.init.CoreCreativeTabs;
import thut.bling.BlingItem;
import thut.core.init.ThutCreativeTabs;
import thut.wearables.ThutWearables;

@Mod.EventBusSubscriber(modid = PokecubeAdv.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AdvCreativeTabs extends CoreCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            PokecubeAdv.MODID);
    public static final RegistryObject<CreativeModeTab> BADGES_TAB = TABS.register("badges_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.pokecube_adventures.badges"))
                    .icon(() -> new ItemStack(PokecubeItems.getStack("pokecube_adventures:badge_rock").getItem()))
                    .withTabsBefore(CoreCreativeTabs.BERRIES_TAB.getId()).build());

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS)
        {
            if (event.hasPermissions())
            {
                addAfter(event, Items.REPEATING_COMMAND_BLOCK, PokecubeAdv.AFA.get());
                addAfter(event, PokecubeAdv.AFA.get(), PokecubeAdv.COMMANDER.get());
                addAfter(event, PokecubeAdv.COMMANDER.get(), PokecubeAdv.DAYCARE.get());
                addBefore(event, Items.DEBUG_STICK, PokecubeAdv.LINKER.get());
            }
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.WARPED_FUNGUS_ON_A_STICK, PokecubeAdv.LINKER.get());
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.BLAST_FURNACE, PokecubeAdv.CLONER.get());
            addAfter(event, PokecubeAdv.CLONER.get(), PokecubeAdv.EXTRACTOR.get());
            addAfter(event, PokecubeAdv.EXTRACTOR.get(), PokecubeAdv.SPLICER.get());
            addAfter(event, PokecubeAdv.SPLICER.get(), PokecubeAdv.SIPHON.get());

            addAfter(event, Items.LODESTONE, PokecubeAdv.WARP_PAD.get());
            addAfter(event, PokecubeAdv.WARP_PAD.get(), PokecubeAdv.AFA.get());
            addAfter(event, PokecubeAdv.AFA.get(), PokecubeAdv.COMMANDER.get());
            addAfter(event, PokecubeAdv.COMMANDER.get(), PokecubeAdv.DAYCARE.get());

            addBefore(event, Items.SKELETON_SKULL, PokecubeAdv.STATUE.get());
        }

        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addBefore(event, Items.BELL, PokecubeAdv.WARP_PAD.get());
        }

        if (event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            addAfter(event, Items.PINK_STAINED_GLASS, PokecubeAdv.LAB_GLASS.get());
        }

        if (event.getTabKey().equals(CoreCreativeTabs.BLOCKS_ITEMS_TAB.getKey()))
        {
            addAfter(event, PokecubeItems.POKEWATCH.get(), PokecubeAdv.LINKER.get());
        }
        
        if (event.getTabKey().equals(AdvCreativeTabs.BADGES_TAB.getKey()))
        {
            for (final PokeType type : PokecubeAdv.BADGES.keySet())
            {
                add(event, PokecubeAdv.BADGES.get(type));
            }
        }
        
        if (event.getTabKey().equals(ThutCreativeTabs.UTILITIES_TAB.getKey()))
        {
            event.accept(PokecubeItems.getStack("pokecube_adventures:linker"));
        }

        if (event.getTabKey().equals(ThutWearables.WEARABLES_TAB.getKey()))
        {
            addAfter(event, BlingItem.getStack("pokecube:pokewatch").getItem(), BlingItem.getStack("pokecube_adventures:bag").getItem());
        }
    }
}
