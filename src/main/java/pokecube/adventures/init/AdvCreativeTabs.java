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
import pokecube.core.PokecubeItems;
import pokecube.core.init.CoreCreativeTabs;

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
                event.accept(PokecubeAdv.AFA.get());
                event.accept(PokecubeAdv.COMMANDER.get());
                event.accept(PokecubeAdv.DAYCARE.get());
                event.accept(PokecubeAdv.STATUE.get());
                event.accept(PokecubeAdv.LINKER.get());
            }
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            add(event, Items.WARPED_FUNGUS_ON_A_STICK, PokecubeAdv.LINKER.get());
        }

        if (event.getTabKey().equals(CoreCreativeTabs.BLOCKS_ITEMS_TAB.getKey()))
        {
            add(event, PokecubeItems.POKEWATCH.get(), PokecubeAdv.LINKER.get());
        }

        if (event.getTabKey().equals(AdvCreativeTabs.BADGES_TAB.getKey()))
        {
            for (final PokeType type : PokecubeAdv.BADGES.keySet())
            {
                add(event, PokecubeAdv.BADGES.get(type));
            }
        }
    }
}
