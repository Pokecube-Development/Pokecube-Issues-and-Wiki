package pokecube.core.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Load;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import thut.api.maths.Vector3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@EventBusSubscriber
public class PokemobTracker
{
    public static class MobEntry implements Comparable<MobEntry>
    {
        public final IPokemob pokemob;

        public final UUID id;

        public MobEntry(final IPokemob pokemob)
        {
            this.pokemob = pokemob;
            this.id = pokemob.getEntity().getUUID();
        }

        public BlockPos getPos()
        {
            return this.pokemob.getEntity().blockPosition();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof MobEntry entry) return entry.id.equals(this.id);
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.pokemob.getEntity().getUUID().hashCode();
        }

        @Override
        public int compareTo(final MobEntry o)
        {
            return this.getPos().compareTo(o.getPos());
        }

        public UUID getUUID()
        {
            return this.id;
        }
    }

    public static class CubeEntry implements Comparable<CubeEntry>
    {
        final EntityPokecubeBase cube;

        public CubeEntry(final EntityPokecubeBase cube)
        {
            this.cube = cube;
        }

        public BlockPos getPos()
        {
            return this.cube.blockPosition();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof MobEntry entry) return entry.pokemob.getEntity().getUUID().equals(this.cube.getUUID());
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.cube.getUUID().hashCode();
        }

        @Override
        public int compareTo(final CubeEntry o)
        {
            return this.getPos().compareTo(o.getPos());
        }
    }

    // Client and server instances as they operate seperate worlds
    private static final PokemobTracker CLIENT = new PokemobTracker();
    private static final PokemobTracker SERVER = new PokemobTracker();

    private static PokemobTracker getFor(final Entity mob)
    {
        return mob.level() instanceof ServerLevel ? PokemobTracker.SERVER : PokemobTracker.CLIENT;
    }

    private static PokemobTracker getFor(final LevelAccessor mob)
    {
        return mob.isClientSide() ? PokemobTracker.CLIENT : PokemobTracker.SERVER;
    }

    private final Map<ResourceKey<Level>, List<MobEntry>> liveMobs = new ConcurrentHashMap<>();

    private final Map<UUID, Set<MobEntry>> ownerMap = new ConcurrentHashMap<>();
    private final Map<UUID, Set<CubeEntry>> ownedCubes = new ConcurrentHashMap<>();

    private final Map<UUID, MobEntry> entries = Maps.newConcurrentMap();

    private ResourceKey<Level> defaults = Level.OVERWORLD;

    private void setDim(final ResourceKey<Level> dim)
    {
        this.defaults = dim;
    }

    private void _addPokemob(final IPokemob pokemob)
    {
        // First remove the mob from all maps, incase it is in one.
        this._removePokemob(pokemob);
        if (pokemob.getAbility() != null) pokemob.getAbility().init(pokemob);
        final MobEntry e = new MobEntry(pokemob);
        ResourceKey<Level> dim = pokemob.getEntity().level().dimension();
        if (dim == null) dim = this.defaults;
        // Find the appropriate map
        final List<MobEntry> mobList = this.liveMobs.getOrDefault(dim, new ArrayList<>());
        // Register the dimension if not already there
        if (!this.liveMobs.containsKey(dim)) this.liveMobs.put(dim, mobList);

        // Check if the mob is already in list (ie moved elsewhere, then back),
        // if so, remove it
        mobList.removeIf(e2 -> e2.getUUID().equals(e.getUUID()));

        // Add the pokemob to the list
        mobList.add(e);
        this.entries.put(e.getUUID(), e);

        final UUID owner = pokemob.getOwnerId();
        if (owner == null) return;

        final Set<MobEntry> owned = this.ownerMap.getOrDefault(owner, new HashSet<>());
        // Register the dimension if not already there
        if (!this.ownerMap.containsKey(owner)) this.ownerMap.put(owner, owned);
        // Add the pokemob to the list
        owned.add(e);
    }

    private void _removePokemob(final IPokemob pokemob)
    {
        if (pokemob.getAbility() != null) pokemob.getAbility().destroy(pokemob);
        this._removeMobEntry(pokemob.getEntity().getUUID());
    }

    private void _removeMobEntry(final UUID id)
    {
        final MobEntry e = this.entries.remove(id);
        if (e != null)
        {
            // Remove the mob from all maps, incase it is in one.
            this.liveMobs.forEach((d, m) -> m.remove(e));
            // Remove the mob from all maps, incase it is in one.
            this.ownerMap.forEach((d, m) -> m.remove(e));
        }
    }

    private void _addPokecube(final EntityPokecubeBase cube)
    {
        final UUID owner = cube.containedMob != null ? cube.containedMob.getOwnerId() : cube.shooter;
        if (owner == null) return;
        final CubeEntry e = PokemobTracker.removePokecube(cube);
        final Set<CubeEntry> owned = this.ownedCubes.getOrDefault(owner, new HashSet<>());
        // Register the dimension if not already there
        if (!this.ownedCubes.containsKey(owner)) this.ownedCubes.put(owner, owned);
        // Add the pokemob to the list
        owned.add(e);
    }

    private CubeEntry _removePokecube(final EntityPokecubeBase cube)
    {
        final CubeEntry e = new CubeEntry(cube);
        // Remove the mob from all maps, incase it is in one.
        this.ownedCubes.forEach((d, m) -> m.remove(e));
        return e;
    }

    public static MobEntry getMobEntry(final UUID id, final LevelAccessor world)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(world);
        return tracker.entries.get(id);
    }

    public static void removeMobEntry(final UUID id, final LevelAccessor world)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(world);
        tracker._removeMobEntry(id);
    }

    public static void addPokemob(final IPokemob pokemob)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(pokemob.getEntity());
        tracker._addPokemob(pokemob);
    }

    public static void removePokemob(final IPokemob pokemob)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(pokemob.getEntity());
        tracker._removePokemob(pokemob);
    }

    public static void addPokecube(final EntityPokecubeBase cube)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(cube);
        tracker._addPokecube(cube);
    }

    public static CubeEntry removePokecube(final EntityPokecubeBase cube)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(cube);
        return tracker._removePokecube(cube);
    }

    public static int countPokemobs(final LevelAccessor world, final AABB box, final Predicate<IPokemob> matches)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(world);
        ResourceKey<Level> key = Level.OVERWORLD;
        if (world instanceof Level level) key = level.dimension();
        final MobEntry[] mobList = tracker.liveMobs.getOrDefault(key, new ArrayList<>()).toArray(new MobEntry[0]);
        int num = 0;
        for (final MobEntry e : mobList)
            if (box.contains(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()) && matches.test(e.pokemob)) num++;
        return num;
    }

    public static int countPokemobs(final LevelAccessor world, final AABB box)
    {
        return PokemobTracker.countPokemobs(world, box, e -> true);
    }

    public static int countPokemobs(final Vector3 location, final LevelAccessor world, final double distance,
            final PokedexEntry entry)
    {
        final AABB box = location.getAABB().inflate(distance, distance, distance);
        return PokemobTracker.countPokemobs(world, box, e -> e.getPokedexEntry() == entry);
    }

    public static int countPokemobs(final LevelAccessor world, final Vector3 location, final double radius)
    {
        final AABB box = location.getAABB().inflate(radius, radius, radius);
        return PokemobTracker.countPokemobs(world, box);
    }

    public static List<Entity> getMobs(final Entity owner, final Predicate<Entity> matcher)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(owner);
        final List<Entity> pokemobs = Lists.newArrayList();
        final UUID id = owner.getUUID();
        final Set<MobEntry> mobs = tracker.ownerMap.getOrDefault(id, Collections.emptySet());
        final Set<CubeEntry> cubes = tracker.ownedCubes.getOrDefault(id, Collections.emptySet());
        mobs.forEach(e -> {
            if (matcher.test(e.pokemob.getEntity()) && e.pokemob.getEntity().isAlive())
                pokemobs.add(e.pokemob.getEntity());
        });
        cubes.forEach(e -> {
            if (matcher.test(e.cube)) pokemobs.add(e.cube);
        });
        return pokemobs;
    }

    @SubscribeEvent
    public static void onWorldLoad(final Load evt)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(evt.getLevel());
        if (evt.getLevel().isClientSide())
        {
            tracker.ownedCubes.clear();
            tracker.ownerMap.clear();
        }
        ResourceKey<Level> key = Level.OVERWORLD;
        if (evt.getLevel() instanceof Level level) key = level.dimension();
        // Reset the tracked map for this world
        tracker.liveMobs.put(key, new ArrayList<>());
        if (tracker == PokemobTracker.CLIENT) tracker.setDim(key);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMobAddedToWorld(final EntityJoinLevelEvent event)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            // Init the tracker
            PokemobTracker.addPokemob(pokemob);
            if (pokemob.isPlayerOwned() && pokemob.getOwnerId() != null) PlayerPokemobCache.UpdateCache(pokemob);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMobRemovedFromWorld(final EntityLeaveLevelEvent event)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            PokemobTracker.removePokemob(pokemob);
            if (pokemob.isPlayerOwned() && pokemob.getOwnerId() != null) PlayerPokemobCache.UpdateCache(pokemob);
        }
    }

    private void clear()
    {
        this.liveMobs.clear();
        this.ownerMap.clear();
        this.entries.clear();
        this.ownedCubes.clear();
    }

    public static void clearAll()
    {
        CLIENT.clear();
        SERVER.clear();
    }
}
