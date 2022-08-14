package pokecube.core.inventory;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.inventory.trade.TradeContainer;

public class MenuTypes
{

    public static final RegistryObject<MenuType<ContainerPokemob>> POKEMOB;
    public static final RegistryObject<MenuType<HealerContainer>> HEALER;
    public static final RegistryObject<MenuType<PCContainer>> PC;
    public static final RegistryObject<MenuType<TMContainer>> TMS;
    public static final RegistryObject<MenuType<TradeContainer>> TRADE;

    static
    {
        POKEMOB = PokecubeCore.MENU.register("pokemob",
                () -> new MenuType<>((IContainerFactory<ContainerPokemob>) ContainerPokemob::new));
        HEALER = PokecubeCore.MENU.register("healer", () -> new MenuType<>(HealerContainer::new));
        PC = PokecubeCore.MENU.register("pc", () -> new MenuType<>((IContainerFactory<PCContainer>) PCContainer::new));
        TMS = PokecubeCore.MENU.register("tm_machine", () -> new MenuType<>(TMContainer::new));
        TRADE = PokecubeCore.MENU.register("trade_machine", () -> new MenuType<>(TradeContainer::new));
    }

    public static void init()
    {}
}
