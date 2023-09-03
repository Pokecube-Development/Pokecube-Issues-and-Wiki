package pokecube.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.items.berries.BerryManager;

@Mod.EventBusSubscriber(modid = PokecubeCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PokecubeCore.MODID);

    public static final RegistryObject<CreativeModeTab> BERRIES_TAB = TABS.register("pokecube_berries_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_berries"))
            .icon(() -> new ItemStack(BerryManager.getBerryItem("leppa")))
            .displayItems((parameters, output) -> {
                output.accept((ItemLike) ItemGenerator.woods.keySet());
                output.accept((ItemLike) ItemGenerator.stripped_logs);
                output.accept(BerryManager.getBerryItem("oran"));

                ItemGenerator.BOATS.forEach(boat ->
                {
                    output.accept(boat.block().get());
                });
            }).build());

    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = TABS.register("pokecube_items_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_items"))
            .icon(() -> {
                for (final String type : ItemGenerator.fossilVariants)
                    return new ItemStack(ItemGenerator.fossils.get(type).get());
                return null;
            })
            .displayItems((parameters, output) -> {
                final Item.Properties props = new Item.Properties();

                for (final String type : ItemGenerator.fossilVariants)
                    output.accept(ItemGenerator.fossils.get(type).get());

                for (final String type : ItemGenerator.trapdoors.keySet())
                    output.accept(ItemGenerator.trapdoors.get(type).get());
            }).build());
}
