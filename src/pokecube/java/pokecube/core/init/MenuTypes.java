package pokecube.core.init;

import java.util.function.Supplier;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import pokecube.core.PokecubeCore;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pokemob.PokemobContainer;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.inventory.trade.TradeContainer;

public class MenuTypes
{

    public static final Supplier<MenuType<PokemobContainer>> POKEMOB;
    public static final Supplier<MenuType<HealerContainer>> HEALER;
    public static final Supplier<MenuType<PCContainer>> PC;
    public static final Supplier<MenuType<TMContainer>> TMS;
    public static final Supplier<MenuType<TradeContainer>> TRADE;

    static
    {
        POKEMOB = PokecubeCore.MENU.register("pokemob",
                () -> new MenuType<>((IContainerFactory<PokemobContainer>) PokemobContainer::new, FeatureFlags.REGISTRY.allFlags()));
        HEALER = PokecubeCore.MENU.register("healer", () -> new MenuType<>(HealerContainer::new, FeatureFlags.REGISTRY.allFlags()));
        PC = PokecubeCore.MENU.register("pc", () -> new MenuType<>((IContainerFactory<PCContainer>) PCContainer::new, FeatureFlags.REGISTRY.allFlags()));
        TMS = PokecubeCore.MENU.register("tm_machine", () -> new MenuType<>(TMContainer::new, FeatureFlags.REGISTRY.allFlags()));
        TRADE = PokecubeCore.MENU.register("trade_machine", () -> new MenuType<>(TradeContainer::new, FeatureFlags.REGISTRY.allFlags()));
    }

    public static void init()
    {}
}
