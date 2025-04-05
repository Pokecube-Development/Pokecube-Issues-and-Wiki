package thut.core.init;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import thut.core.common.ThutCore;

@EventBusSubscriber(modid = ThutCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ThutCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThutCore.MODID);

    public static final Supplier<CreativeModeTab> UTILITIES_TAB = TABS.register("utilities_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.thutcore.utilities")).icon(() -> ThutCore.THUTICON).build());
    
    public static void addFront(BuildCreativeModeTabContentsEvent event, ItemLike item) {
        event.insertFirst(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
    
    public static void addAfter(BuildCreativeModeTabContentsEvent event, ItemLike afterItem, ItemLike item) {
        event.insertAfter(new ItemStack(afterItem), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void addBefore(BuildCreativeModeTabContentsEvent event, ItemLike beforeItem, ItemLike item) {
        event.insertBefore(new ItemStack(beforeItem), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
    
    @SubscribeEvent
    public static void DummyCall(NewRegistryEvent event) {
        // This function call ensures the above deferred register is initialised.
    }
}
