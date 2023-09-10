package thut.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.init.ItemGenerator;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;
import thut.bling.BlingItem;
import thut.bling.ThutBling;
import thut.crafts.ThutCrafts;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ThutCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.ID);

    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = TABS.register("items_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.thutcore"))
            .icon(() -> ItemStack.EMPTY)
            .displayItems((parameters, output) -> {
                output.accept(ThutCrafts.CRAFTMAKER.get());
                output.accept(PokecubeAdv.LINKER.get());
                output.accept(PokecubeAdv.BAG.get());

                for (final String type : ItemGenerator.megaWearables.keySet())
                    output.accept(ItemGenerator.megaWearables.get(type).get());

                for (final String type : BlingItem.blingWearables.keySet()) {
                    output.accept(BlingItem.blingWearables.get(type).get());
                }
            }).build());
}
