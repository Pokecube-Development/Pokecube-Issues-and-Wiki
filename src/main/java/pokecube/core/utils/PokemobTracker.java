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

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import thut.api.maths.Vector3;

public class PokemobTracker
{
    private static class MobEntry implements Comparable<MobEntry>
    {
        final IPokemob pokemob;

        public MobEntry(final IPokemob pokemob)
        {
            this.pokemob = pokemob;
        }

        public BlockPos getPos()
        {
            return this.pokemob.getEntity().getPosition();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof MobEntry) return ((MobEntry) obj).pokemob.getEntity().getUniqueID().equals(this.pokemob
                    .getEntity().getUniqueID());
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
    }

    private static class CubeEntry implements Comparable<CubeEntry>
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
        return mob.getWorld() instanceof ServerWorld ? PokemobTracker.SERVER : PokemobTracker.CLIENT;
    }

    private final Map<DimensionType, List<MobEntry>> liveMobs   = new ConcurrentHashMap<>();
    private final Map<UUID, Set<MobEntry>>           ownerMap   = new ConcurrentHashMap<>();
    private final Map<UUID, Set<CubeEntry>>          ownedCubes = new ConcurrentHashMap<>();

    private MobEntry _addPokemob(final IPokemob pokemob)
    {
        // First remove the mob from all maps, incase it is in one.
        final MobEntry e = PokemobTracker.removePokemob(pokemob);
        final DimensionType dim = pokemob.getEntity().dimension;
        // Find the appropriate map
        final List<MobEntry> mobList = this.liveMobs.getOrDefault(dim, new ArrayList<>());
        // Register the dimension if not already there
        if (!this.liveMobs.containsKey(dim)) this.liveMobs.put(dim, mobList);
        // Add the pokemob to the list
        mobList.add(e);

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
        final MobEntry e = new MobEntry(pokemob);
        // Remove the mob from all maps, incase it is in one.
        this.liveMobs.forEach((d, m) -> m.remove(e));
        // Remove the mob from all maps, incase it is in one.
        this.ownerMap.forEach((d, m) -> m.remove(e));
        // Thread.dumpStack();
        return e;
    }

    private CubeEntry _addPokecube(final EntityPokecubeBase cube)
    {
        final UUID owner = cube.containedMob != null ? cube.containedMob.getOwnerId() : null;
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
        final DimensionType dim = world.getDimension().getType();
        final MobEntry[] mobList = tracker.liveMobs.getOrDefault(dim, new ArrayList<>()).toArray(new MobEntry[0]);
        int num = 0;
        for (final MobEntry e : mobList)
            if (box.contains(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()) && matches.test(e.pokemob)) num++;
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
            if (matcher.test(e.pokemob.getEntity())) pokemobs.add(e.pokemob.getEntity());
        });
        cubes.forEach(e ->
        {
            if (matcher.test(e.cube)) pokemobs.add(e.cube);
        });
        return pokemobs;
    }

    @SubscribeEvent
    public static void worldLoadEvent(final Load evt)
    {
        final PokemobTracker tracker = PokemobTracker.getFor(evt.getWorld());
        // Reset the tracked map for this world
        tracker.liveMobs.put(evt.getWorld().getDimension().getType(), new ArrayList<>());
    }
}
