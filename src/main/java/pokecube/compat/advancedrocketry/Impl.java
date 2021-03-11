package pokecube.compat.advancedrocketry;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class Impl
{

    public static final String MOON  = "moon";
    public static final String SPACE = "space";

    static PokedexEntry megaray;

    public static void register()
    {
        // TODO Auto-generated method stub
        Impl.megaray = Database.getEntry("rayquaza_mega");
    }

    public static void toOrbit(final PlayerTickEvent event)
    {
        if (!(event.player.getCommandSenderWorld() instanceof ServerWorld)) return;
//        final ServerWorld world = (ServerWorld) event.player.getEntityWorld();
//        final Entity riding = event.player.getRidingEntity();
//        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(riding);
//        if (pokemob == null || pokemob.getPokedexEntry() != Impl.megaray) return;
//
//        final boolean isSpace = ARConfiguration.GetSpaceDimId().equals(ZUtils.getDimensionIdentifier(
//                event.player.world));
//        if (!isSpace)
//        {
//
//        }

    }
}
