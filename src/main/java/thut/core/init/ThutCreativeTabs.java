package thut.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.core.common.ThutCore;

@Mod.EventBusSubscriber(modid = ThutCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ThutCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThutCore.MODID);

    public static final RegistryObject<CreativeModeTab> UTILITIES_TAB = TABS.register("utilities_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.thutcore.utilities")).icon(() -> ThutCore.THUTICON).build());
}
