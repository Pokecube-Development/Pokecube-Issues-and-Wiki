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
import pokecube.core.PokecubeItems;
import pokecube.core.items.berries.BerryManager;

@Mod.EventBusSubscriber(modid = PokecubeCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PokecubeCore.MODID);

    public static final RegistryObject<CreativeModeTab> BERRIES_TAB = TABS.register("berries_tab", () -> CreativeModeTab.builder()
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
    public static final RegistryObject<CreativeModeTab> POKECUBES_TAB = TABS.register("cubes_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_cubes"))
            .icon(() -> new ItemStack(PokecubeItems.POKECUBE_CUBES.getItem()))
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
                output.accept(PokecubeItems.getStack("luxurycube"));
                output.accept(PokecubeItems.getStack("beastcube"));
                output.accept(PokecubeItems.getStack("clonecube"));
            }).build());

    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = TABS.register("items_tab", () -> CreativeModeTab.builder()
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

    public static final RegistryObject<CreativeModeTab> BLOCKS_TAB = TABS.register("blocks_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pokecube_blocks"))
            .icon(() -> new ItemStack(PokecubeItems.NEST.get()))
            .displayItems((parameters, output) -> {
                output.accept(PokecubeItems.NEST.get());
            }).build());

            .displayItems((parameters, output) -> {
            }).build());
}
