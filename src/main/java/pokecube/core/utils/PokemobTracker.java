package pokecube.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent.Load;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import thut.api.maths.Vector3;

public class PokemobTracker
{
    public static class MobEntry implements Comparable<MobEntry>
    {
        public final IPokemob pokemob;

        final UUID id;

        public MobEntry(final IPokemob pokemob)
        {
            this.pokemob = pokemob;
            this.id = pokemob.getEntity().getUniqueID();
        }

        public BlockPos getPos()
        {
            return this.pokemob.getEntity().getPosition();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof MobEntry) return ((MobEntry) obj).id.equals(this.id);
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.pokemob.getEntity().getUniqueID().hashCode();
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
            return this.cube.getEntity().getPosition();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof MobEntry) return ((MobEntry) obj).pokemob.getEntity().getUniqueID().equals(this.cube
                    .getEntity().getUniqueID());
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.cube.getEntity().getUniqueID().hashCode();
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
        return mob.getEntityWorld() instanceof ServerWorld ? PokemobTracker.SERVER : PokemobTracker.CLIENT;
    }

    private static PokemobTracker getFor(final IWorld mob)
    {
        return mob.isRemote() ? PokemobTracker.CLIENT : PokemobTracker.SERVER;
    }

    private final Map<RegistryKey<World>, List<MobEntry>> liveMobs = new ConcurrentHashMap<>();

    private final Map<UUID, Set<MobEntry>>  ownerMap   = new ConcurrentHashMap<>();
    private final Map<UUID, Set<CubeEntry>> ownedCubes = new ConcurrentHashMap<>();

    private final Map<UUID, MobEntry> entries = Maps.newConcurrentMap();

    private RegistryKey<World> defaults = World.OVERWORLD;

    private void setDim(final RegistryKey<World> dim)
    {
        this.defaults = dim;
    }

    private MobEntry _addPokemob(final IPokemob pokemob)
    {
        // First remove the mob from all maps, incase it is in one.
        this._removePokemob(pokemob);
        if (pokemob.getAbility() != null) pokemob.getAbility().init(pokemob);
        final MobEntry e = new MobEntry(pokemob);
        RegistryKey<World> dim = pokemob.getEntity().getEntityWorld().getDimensionKey();
        if (dim == null) dim = this.defaults;
        // Find the appropriate map
        final List<MobEntry> mobList = this.liveMobs.getOrDefault(dim, new ArrayList<>());
        // Register the dimension if not already there
        if (!this.liveMobs.containsKey(dim)) this.liveMobs.put(dim, mobList);
        // Add the pokemob to the list
        mobList.add(e);
        this.entries.put(e.getUUID(), e);

        final UUID owner = pokemob.getOwnerId();
        if (owner == null) return e;

        final Set<MobEntry> owned = this.ownerMap.getOrDefault(owner, new HashSet<>());
        // Register the dimension if not already there
        if (!this.ownerMap.containsKey(owner)) this.ownerMap.put(owner, owned);
        // Add the pokemob to the list
        owned.add(e);
        return e;
    }

    private MobEntry _removePokemob(final IPokemob pokemob)
    {
        if (pokemob.getAbility() != null) pokemob.getAbility().destroy();
        final MobEntry e = this._removeMobEntry(pokemob.getEntity().getUniqueID());
        return e;
    }

    private MobEntry _removeMobEntry(final UUID id)
    {
        final MobEntry e = this.entries.remove(id);
        if (e != null)
        {
            // Remove the mob from all maps, incase it is in one.
            this.liveMobs.forEach((d, m) -> m.remove(e));
            // Remove the mob from all maps, incase it is in one.
            this.ownerMap.forEach((d, m) -> m.remove(e));
        }
        return e;
    }

    private CubeEntry _addPokecube(final EntityPokecubeBase cube)
    {
        final UUID owner = cube.containedMob != null ? cube.containedMob.getOwnerId() : cube.shooter;
        if (owner == null) return null;
        final CubeEntry e = PokemobTracker.removePokecube(cube);
        final Set<CubeEntry> owned = this.ownedCubes.getOrDefault(owner, new HashSet<>());
        // Register the dimension if not already there
        if (!this.ownedCubes.containsKey(owner)) this.ownedCubes.put(owner, owned);
        // Add the pokemob to the list
        owned.add(e);
        return e;
    }

    private CubeEntry _removePokecube(final EntityPokecubeBase cube)
    {
        final CubeEntry e = new CubeEntry(cube);
        // Remove the mob from all maps, incase it is in one.
        this.ownedCubes.forEach((d, m) -> m.remove(e));
        return e;
    }

    public static MobEntry getMobEntry(final UUID id, final IWorld world)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(world);
        return tracker.entries.get(id);
    }

    public static void removeMobEntry(final UUID id, final IWorld world)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(world);
        tracker._removeMobEntry(id);
    }

    public static MobEntry addPokemob(final IPokemob pokemob)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(pokemob.getEntity());
        return tracker._addPokemob(pokemob);
    }

    public static MobEntry removePokemob(final IPokemob pokemob)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(pokemob.getEntity());
        return tracker._removePokemob(pokemob);
    }

    public static CubeEntry addPokecube(final EntityPokecubeBase cube)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(cube);
        return tracker._addPokecube(cube);
    }

    public static CubeEntry removePokecube(final EntityPokecubeBase cube)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(cube);
        return tracker._removePokecube(cube);
    }

    public static int countPokemobs(final IWorld world, final AxisAlignedBB box, final Predicate<IPokemob> matches)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(world);
        RegistryKey<World> key = World.OVERWORLD;
        if (world instanceof World) key = ((World) world).getDimensionKey();
        final MobEntry[] mobList = tracker.liveMobs.getOrDefault(key, new ArrayList<>()).toArray(new MobEntry[0]);
        int num = 0;
        for (final MobEntry e : mobList)
            if (box.contains(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()) && e.pokemob.getEntity().isAlive()
                    && matches.test(e.pokemob)) num++;
        return num;
    }

    public static int countPokemobs(final IWorld world, final AxisAlignedBB box)
    {
        return PokemobTracker.countPokemobs(world, box, e -> true);
    }

    public static int countPokemobs(final Vector3 location, final IWorld world, final double distance,
            final PokedexEntry entry)
    {
        final AxisAlignedBB box = location.getAABB().grow(distance, distance, distance);
        return PokemobTracker.countPokemobs(world, box, e -> e.getPokedexEntry() == entry);
    }

    public static int countPokemobs(final Vector3 location, final IWorld world, final double distance,
            final PokeType type)
    {
        final AxisAlignedBB box = location.getAABB().grow(distance, distance, distance);
        return PokemobTracker.countPokemobs(world, box, e -> e.isType(type));
    }

    public static int countPokemobs(final IWorld world, final Vector3 location, final double radius)
    {
        final AxisAlignedBB box = location.getAABB().grow(radius, radius, radius);
        return PokemobTracker.countPokemobs(world, box);
    }

    public static List<Entity> getMobs(final Entity owner, final Predicate<Entity> matcher)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(owner);
        final List<Entity> pokemobs = Lists.newArrayList();
        final UUID id = owner.getUniqueID();
        final Set<MobEntry> mobs = tracker.ownerMap.getOrDefault(id, Collections.emptySet());
        final Set<CubeEntry> cubes = tracker.ownedCubes.getOrDefault(id, Collections.emptySet());
        mobs.forEach(e ->
        {
            if (matcher.test(e.pokemob.getEntity()) && e.pokemob.getEntity().isAlive()) pokemobs.add(e.pokemob
                    .getEntity());
        });
        cubes.forEach(e ->
        {
            if (matcher.test(e.cube)) pokemobs.add(e.cube);
        });
        return pokemobs;
    }

    public static void onWorldLoad(final Load evt)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(evt.getWorld());
        if (evt.getWorld().isRemote())
        {
            tracker.ownedCubes.clear();
            tracker.ownerMap.clear();
        }
        RegistryKey<World> key = World.OVERWORLD;
        if (evt.getWorld() instanceof World) key = ((World) evt.getWorld()).getDimensionKey();
        // Reset the tracked map for this world
        tracker.liveMobs.put(key, new ArrayList<>());
        if (tracker == PokemobTracker.CLIENT) tracker.setDim(key);
    }
}
