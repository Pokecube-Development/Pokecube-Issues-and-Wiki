package thut.core.init;

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
import pokecube.core.init.ItemGenerator;
import pokecube.legends.Reference;
import thut.bling.BlingItem;
import thut.crafts.ThutCrafts;

@Mod.EventBusSubscriber(modid = Reference.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ThutCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.ID);

    public static final RegistryObject<CreativeModeTab> WEARABLES_TAB = TABS.register("wearables_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.thutcore.wearables"))
            .icon(() -> {
                for (final String type : BlingItem.blingWearables.keySet())
                    return new ItemStack(BlingItem.getStack("bling_hat").getItem());
                return new ItemStack(ThutCrafts.CRAFTMAKER.get());
            })
            .displayItems((parameters, output) -> {
                output.accept(BlingItem.getStack("pokecube:pokewatch"));
                output.accept(BlingItem.getStack("pokecube_adventures:bag"));
                output.accept(BlingItem.getStack("bling_bag"));
                output.accept(BlingItem.getStack("bling_bag_ender_vanilla"));
                output.accept(BlingItem.getStack("bling_bag_ender_large"));
                output.accept(BlingItem.getStack("bling_hat"));
                output.accept(BlingItem.getStack("pokecube:mega_hat"));
                output.accept(BlingItem.getStack("pokecube:mega_tiara"));
                output.accept(BlingItem.getStack("bling_eye"));
                output.accept(BlingItem.getStack("pokecube:mega_glasses"));
                output.accept(BlingItem.getStack("bling_neck"));
                output.accept(BlingItem.getStack("pokecube:mega_pendant"));
                output.accept(BlingItem.getStack("bling_ear"));
                output.accept(BlingItem.getStack("pokecube:mega_earring"));
                output.accept(BlingItem.getStack("bling_waist"));
                output.accept(BlingItem.getStack("pokecube:mega_belt"));
                output.accept(BlingItem.getStack("bling_ring"));
                output.accept(BlingItem.getStack("pokecube:mega_ring"));
                output.accept(BlingItem.getStack("bling_wrist"));
                output.accept(BlingItem.getStack("bling_ankle"));
                output.accept(BlingItem.getStack("pokecube:mega_ankletzinnia"));

                for (final String type : ItemGenerator.megaWearables.keySet())
                    output.accept(ItemGenerator.megaWearables.get(type).get());

                for (final String type : BlingItem.blingWearables.keySet())
                    output.accept(BlingItem.blingWearables.get(type).get());

            }).build());

    public static final RegistryObject<CreativeModeTab> UTILITIES_TAB = TABS.register("utilities_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.thutcore.utilities"))
            .icon(() -> new ItemStack(ThutCrafts.CRAFTMAKER.get()))
            .displayItems((parameters, output) -> {
                output.accept(ThutCrafts.CRAFTMAKER.get());
            }).build());

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ThutCrafts.CRAFTMAKER.get());
        }
    }
}
