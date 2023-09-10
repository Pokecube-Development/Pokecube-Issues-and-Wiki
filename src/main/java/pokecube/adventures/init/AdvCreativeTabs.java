package pokecube.adventures.init;

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
import pokecube.adventures.PokecubeAdv;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeItems;
import pokecube.core.init.CoreCreativeTabs;

@Mod.EventBusSubscriber(modid = PokecubeAdv.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AdvCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PokecubeAdv.MODID);
    public static final RegistryObject<CreativeModeTab> BADGES_TAB = TABS.register("badges_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_adventures.badges"))
            .icon(() -> new ItemStack(PokecubeItems.getStack("pokecube_adventures:badge_rock").getItem()))
            .withTabsBefore(CoreCreativeTabs.BERRIES_TAB.getId())
            .displayItems((parameters, output) -> {
                output.accept(PokecubeAdv.LAB_GLASS.get());

                output.accept(PokecubeAdv.WARP_PAD.get());
                output.accept(PokecubeAdv.AFA.get());
                output.accept(PokecubeAdv.COMMANDER.get());
                output.accept(PokecubeAdv.DAYCARE.get());

                output.accept(PokecubeAdv.CLONER.get());
                output.accept(PokecubeAdv.EXTRACTOR.get());
                output.accept(PokecubeAdv.SPLICER.get());
                output.accept(PokecubeAdv.SIPHON.get());

                output.accept(PokecubeAdv.STATUE.get());

                output.accept(PokecubeAdv.BAG.get());
                output.accept(PokecubeAdv.EXPSHARE.get());
                output.accept(PokecubeAdv.LINKER.get());

                for (final PokeType type : PokecubeAdv.BADGES.keySet())
                {
                    output.accept(PokecubeAdv.BADGES.get(type));
                }
            }).build());

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            if (event.hasPermissions()) {
                event.accept(PokecubeAdv.AFA.get());
                event.accept(PokecubeAdv.COMMANDER.get());
                event.accept(PokecubeAdv.DAYCARE.get());
                event.accept(PokecubeAdv.LINKER.get());
            }
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PokecubeAdv.LINKER.get());
        }
    }
}
