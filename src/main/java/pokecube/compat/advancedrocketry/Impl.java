package pokecube.compat.advancedrocketry;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.item.ItemList;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.event.AtmosphereEvent;
import zmaster587.libVulpes.util.ZUtils;

public class Impl
{
    public static final ResourceLocation vacuumSafe = new ResourceLocation("pokecube_compat", "vacuum_compatible");

    public static final String MOON  = "moon";
    public static final String SPACE = "space";

    static PokedexEntry megaray;

    public static void register()
    {
        Impl.megaray = Database.getEntry("rayquaza_mega");

        MinecraftForge.EVENT_BUS.register(Impl.class);
    }

    @SubscribeEvent
    public static void onSuffocate(final AtmosphereEvent event)
    {
        if (ItemList.is(Impl.vacuumSafe, event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void toOrbit(final PlayerTickEvent event)
    {
        final World tworld = event.player.getCommandSenderWorld();
        if (!(tworld instanceof ServerWorld)) return;

        final ServerWorld world = (ServerWorld) tworld;
        final Entity riding = event.player.getRootVehicle();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(riding);
        if (pokemob == null || pokemob.getPokedexEntry() != Impl.megaray) return;
        final boolean isSpace = ARConfiguration.GetSpaceDimId().equals(ZUtils.getDimensionIdentifier(world));
        if (!isSpace)
        {

        }

        // final PokedexEntry entry = Database.getEntry("rayquaza_mega");
        // if (entry == pokemob.getPokedexEntry())
        // {
        // final boolean up = mob.getY() > dim.getHeight() + 10 &&
        // dim.dimension() == World.OVERWORLD;
        // final boolean down = mob.getY() < -10 && dim.dimension() ==
        // World.END;
        //
        // BlockPos pos = mob.blockPosition().below((int) mob.getY());
        //
        // if (up)
        // {
        // final TeleDest dest = new TeleDest();
        // dest.setPos(GlobalPos.of(World.END, pos));
        // ThutTeleporter.transferTo(mob, dest);
        // }
        // else if (down)
        // {
        // final TeleDest dest = new TeleDest();
        // pos = pos.above(250);
        // dest.setPos(GlobalPos.of(World.OVERWORLD, pos));
        // ThutTeleporter.transferTo(mob, dest);
        // }
        // }

    }
}
