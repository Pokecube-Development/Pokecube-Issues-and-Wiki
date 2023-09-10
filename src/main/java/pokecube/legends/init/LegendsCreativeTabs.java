package pokecube.legends.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.Reference;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LegendsCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.ID);

    public static final RegistryObject<CreativeModeTab> DECORATIONS_TAB = TABS.register("decorations_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_legends.decorations"))
            .icon(() -> new ItemStack(BlockInit.SKY_BRICKS.get()))
            .displayItems((parameters, output) -> {
                output.accept(BlockInit.SKY_BRICKS.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> DIMENSIONS_TAB = TABS.register("dimensions_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_legends.dimensions"))
            .icon(() -> new ItemStack(BlockInit.DISTORTIC_GRASS_BLOCK.get()))
            .displayItems((parameters, output) -> {
                output.accept(BlockInit.DISTORTIC_GRASS_BLOCK.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> LEGENDS_TAB = TABS.register("legends_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_legends.legends"))
            .icon(() -> new ItemStack(ItemInit.RAINBOW_ORB.get()))
            .displayItems((parameters, output) -> {
                output.accept(ItemInit.RAINBOW_ORB.get());

//                ItemInit.BOATS.forEach(boat ->
//                {
//                    output.accept(boat.block().get());
//                });
                ItemInit.BOATS.get(5);
            }).build());
}
