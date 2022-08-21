package pokecube.core.eventhandlers;

import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.network.packets.PacketDataSync;
import thut.core.common.handlers.PlayerDataHandler;

/** @author Thutmose */
public class StatsCollector
{
    public static void addCapture(final IPokemob captured)
    {
        String owner;
        if (captured.getOwner() instanceof ServerPlayer player && !(captured.getOwner() instanceof FakePlayer))
        {
            owner = captured.getOwner().getStringUUID();
            final PokedexEntry dbe = Database.getEntry(captured);
            final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(owner)
                    .getData(PokecubePlayerStats.class);
            stats.addCapture(dbe);
            PlayerDataHandler.getInstance().save(owner, stats.getIdentifier());
            if (!stats.hasFirst()) stats.setHasFirst(player);
            PlayerDataHandler.saveCustomData(stats.getIdentifier());
            Triggers.CATCHPOKEMOB.trigger(player, captured);
            PacketDataSync.syncData(player, stats.getIdentifier());
        }
    }

    public static void addHatched(final EntityPokemobEgg hatched)
    {
        String owner;
        IPokemob mob = null;
        if (hatched.getEggOwner() instanceof ServerPlayer player && !(hatched.getEggOwner() instanceof FakePlayer))
        {
            owner = player.getStringUUID();
            mob = hatched.getPokemob(true);
            if (mob == null)
            {
                new Exception().printStackTrace();
                return;
            }
            final PokedexEntry dbe = Database.getEntry(mob);
            final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(owner)
                    .getData(PokecubePlayerStats.class);
            stats.addHatch(dbe);
            PlayerDataHandler.getInstance().save(owner, stats.getIdentifier());
            Triggers.HATCHPOKEMOB.trigger(player, mob);
            PacketDataSync.syncData(player, stats.getIdentifier());
        }
    }

    public static void addKill(final IPokemob killed, final IPokemob killer)
    {
        if (killer == null || killed == null || killer.getOwner() instanceof FakePlayer) return;
        String owner;
        if (killer.getOwner() instanceof ServerPlayer player)
        {
            owner = player.getStringUUID();
            final PokedexEntry dbe = Database.getEntry(killed);
            final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(owner)
                    .getData(PokecubePlayerStats.class);
            stats.addKill(dbe);
            PlayerDataHandler.getInstance().save(owner, stats.getIdentifier());
            Triggers.KILLPOKEMOB.trigger(player, killed);
            PacketDataSync.syncData(player, stats.getIdentifier());
        }
    }

    public static int getCaptured(final PokedexEntry dbe, final Player player)
    {
        final Integer n = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerStats.class)
                .getCaptures().get(dbe);
        return n == null ? 0 : n;
    }

    public static Map<PokedexEntry, Integer> getCaptures(final UUID uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerStats.class).getCaptures();
    }

    public static int getHatched(final PokedexEntry dbe, final Player player)
    {
        final Integer n = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerStats.class)
                .getHatches().get(dbe);
        return n == null ? 0 : n;
    }

    public static Map<PokedexEntry, Integer> getHatches(final UUID uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerStats.class).getHatches();
    }

    public static int getKilled(final PokedexEntry dbe, final Player player)
    {
        final Integer n = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerStats.class)
                .getKills().get(dbe);
        return n == null ? 0 : n;
    }

    public static Map<PokedexEntry, Integer> getKills(final UUID uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerStats.class).getKills();
    }

    public StatsCollector()
    {}

}
