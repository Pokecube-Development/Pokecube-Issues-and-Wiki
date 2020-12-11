package pokecube.compat.thutessentials;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.core.PokecubeCore;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.MatchResult;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.SpawnBiomeMatcher.StructureMatcher;
import pokecube.core.events.pokemob.SpawnCheckEvent;
import pokecube.core.handlers.TeamManager;
import pokecube.core.handlers.TeamManager.ITeamProvider;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.utils.PokemobTracker;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.entity.TeleLoadEvent;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.essentials.Essentials;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.world.IHasStructures;
import thut.essentials.util.world.WorldStructures;

public class Impl
{
    private static class StructChecker implements StructureMatcher
    {
        @Override
        public MatchResult structuresMatch(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
        {
            if (matcher._validStructures.isEmpty()) return MatchResult.PASS;
            if (checker.world instanceof ServerWorld)
            {
                final LazyOptional<IHasStructures> opt = ((ServerWorld) checker.world).getCapability(
                        WorldStructures.CAPABILITY);
                if (opt.isPresent())
                {
                    final IHasStructures holder = opt.orElseGet(null);
                    final Collection<ResourceLocation> hits = holder.getStructures(checker.location.getPos());
                    for (final ResourceLocation hit : hits)
                        if (matcher._validStructures.contains(hit.toString())) return MatchResult.SUCCEED;
                }
            }
            return MatchResult.FAIL;
        }
    }

    private static class TeamProvider implements ITeamProvider
    {
        public TeamProvider()
        {
        }

        @Override
        public String getTeam(final Entity entityIn)
        {
            if (entityIn.getEntityWorld().isRemote) return "";
            final IOwnable ownable = OwnableCaps.getOwnable(entityIn);
            UUID id = ownable != null ? ownable.getOwnerId() : null;
            if (id == null) id = entityIn.getUniqueID();
            final LandTeam team = LandManager.getTeam(id);
            if (team != LandManager.getDefaultTeam()) return team.teamName;
            return "";
        }

        @Override
        public boolean areAllied(final String team, final Entity target)
        {
            final LandTeam teamA = LandManager.getInstance().getTeam(team, false);
            final String targTeam = this.getTeam(target);
            final LandTeam teamB = LandManager.getInstance().getTeam(targTeam, false);
            if (teamA != null && teamB != null)
            {
                if (teamA == teamB) return true;
                if (teamA.isAlly(teamB)) return true;
                if (teamA.isAlly(target.getUniqueID())) return true;
                return false;
            }
            return false;
        }
    }

    private static class TeleDestManager
    {
        public static void initMatcher(final TeleLoadEvent event)
        {
            // We handle this identically to the equivalent in Essentials for
            // its own type of TeleDest.
            final TeleDest dest = event.getOverride();
            if (dest == null) return;
            if (dest.version != Essentials.config.dim_verison)
            {
                if (!Essentials.config.versioned_dim_keys.contains(dest.getPos().getDimension().getLocation())) return;
                Essentials.LOGGER.info("Invalidating stale teledest {} ({})", dest.getName(), dest.getPos());
                event.setCanceled(true);
                event.setOverride(null);
            }
        }
    }

    public static void register()
    {
        PokecubeCore.LOGGER.debug("Registering ThutEssentials Support");
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, TeleDestManager::initMatcher);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, Impl::init);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, Impl::recallOutMobsOnLogout);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, Impl::recallOutMobsOnUnload);
        TeamManager.provider = new TeamProvider();
    }

    public static void init(final SpawnCheckEvent.Init event)
    {
        event.matcher._structs = StructureMatcher.or(new StructChecker(), event.matcher._structs);
    }

    public static void recallOutMobsOnLogout(final PlayerLoggedOutEvent event)
    {
        if (!(event.getPlayer().getEntityWorld() instanceof ServerWorld)) return;
        final ServerWorld world = (ServerWorld) event.getPlayer().getEntityWorld();
        if (!Essentials.config.versioned_dim_keys.contains(world.getDimensionKey().getLocation())) return;
        final List<Entity> mobs = PokemobTracker.getMobs(event.getPlayer(), e -> Essentials.config.versioned_dim_keys
                .contains(e.getEntityWorld().getDimensionKey().getLocation()));
        PCEventsHandler.recallAll(mobs, true);
    }

    public static void recallOutMobsOnUnload(final ChunkEvent.Unload event)
    {
        if (event.getWorld() == null || event.getWorld().isRemote()) return;
        if (!(event.getWorld() instanceof ServerWorld && event.getChunk() instanceof Chunk)) return;
        final ServerWorld world = (ServerWorld) event.getWorld();
        if (!Essentials.config.versioned_dim_keys.contains(world.getDimensionKey().getLocation())) return;
        final List<Entity> mobs = Lists.newArrayList();
        final Chunk chunk = (Chunk) event.getChunk();
        for (final ClassInheritanceMultiMap<Entity> list : chunk.getEntityLists())
            list.forEach(e ->
            {
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
                if (pokemob != null && pokemob.getOwnerId() != null || e instanceof EntityPokecube) mobs.add(e);
            });
        PCEventsHandler.recallAll(mobs, true);
    }
}
