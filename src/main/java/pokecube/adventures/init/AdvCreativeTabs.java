package pokecube.adventures.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;

@Mod.EventBusSubscriber(modid = PokecubeAdv.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AdvCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PokecubeAdv.MODID);
    public static final RegistryObject<CreativeModeTab> POKECUBE_ADVENTURES_TAB = TABS.register("adventures_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_adventures"))
            .icon(() -> new ItemStack(PokecubeAdv.WARP_PAD.get()))
            .displayItems((parameters, output) -> {
                output.accept(PokecubeAdv.LAB_GLASS.get());

                output.accept(PokecubeAdv.AFA.get());
                output.accept(PokecubeAdv.COMMANDER.get());
                output.accept(PokecubeAdv.DAYCARE.get());
                output.accept(PokecubeAdv.WARP_PAD.get());

                output.accept(PokecubeAdv.CLONER.get());
                output.accept(PokecubeAdv.EXTRACTOR.get());
                output.accept(PokecubeAdv.SPLICER.get());
                output.accept(PokecubeAdv.SIPHON.get());

                output.accept(PokecubeAdv.STATUE.get());

                output.accept(PokecubeAdv.EXPSHARE.get());
                output.accept(PokecubeAdv.LINKER.get());
                output.accept(PokecubeAdv.BAG.get());
            }).build());
}
