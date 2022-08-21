package pokecube.core.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.init.RegisterPokecubes;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.vitamins.ItemVitamin;
import thut.api.OwnableCaps;

public class ItemInit
{
    private static void addPokecubes()
    {
        final RegisterPokecubes event = new RegisterPokecubes();
        PokecubeAPI.POKEMOB_BUS.post(event);

        // Register any cube behaviours and cubes from event.
        for (final PokecubeBehaviour i : event.behaviors)
        {
            final String name = i.name + "cube";
            PokecubeBehaviour.addCubeBehavior(i);
            final Item.Properties props = new Item.Properties();
            props.tab(PokecubeItems.TAB_POKECUBES);
            props.durability(255).defaultDurability(255);
            PokecubeCore.ITEMS.register(name, () -> {
                final Pokecube cube = new Pokecube(props);
                PokecubeItems.addCube(new ResourceLocation("pokecube:" + name), new Item[]
                { cube });
                return cube;
            });
        }

        final Item.Properties props = new Item.Properties();
        props.tab(PokecubeItems.TAB_POKECUBES);
        PokecubeCore.ITEMS.register("pokeseal", () -> {
            final Pokecube pokeseal = new Pokecube(props);
            PokecubeBehaviour.POKESEAL = new ResourceLocation("pokecube:seal");
            PokecubeItems.addCube(PokecubeBehaviour.POKESEAL, new Item[]
            { pokeseal });
            return pokeseal;
        });
    }

    private static void addVitamins()
    {
        final Item.Properties props = new Item.Properties().tab(PokecubeItems.TAB_ITEMS);
        for (final String type : ItemVitamin.vitamins)
        {
            PokecubeCore.ITEMS.register("vitamin_" + type, () -> new ItemVitamin(props, Database.trim(type)));
        }
    }

    public static void init()
    {
        ItemGenerator.init();
        ItemInit.addPokecubes();
        ItemInit.addVitamins();
        ItemGenerator.registerItems();
    }

    public static void postInit()
    {
        // Register classes for ownable caps
        OwnableCaps.TILES.add(PokecubeItems.TRADE_TYPE.get());
        OwnableCaps.TILES.add(PokecubeItems.TM_TYPE.get());
        OwnableCaps.TILES.add(PokecubeItems.PC_TYPE.get());
        OwnableCaps.TILES.add(PokecubeItems.HEALER_TYPE.get());
        OwnableCaps.TILES.add(PokecubeItems.BASE_TYPE.get());

        PokecubeItems.POKECUBE_BLOCKS = new ItemStack(PokecubeItems.HEALER.get());
    }
}
