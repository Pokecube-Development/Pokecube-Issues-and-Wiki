package pokecube.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class PokemobTracker
{
    private static class Entry implements Comparable<Entry>
    {
        final IPokemob pokemob;

        public Entry(final IPokemob pokemob)
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
            if (obj instanceof Entry) return ((Entry) obj).pokemob.getEntity().getUniqueID().equals(this.pokemob
                    .getEntity().getUniqueID());
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.pokemob.getEntity().getUniqueID().hashCode();
        }

        @Override
        public int compareTo(final Entry o)
        {
            return this.getPos().compareTo(o.getPos());
        }
    }

    private static Map<DimensionType, List<Entry>> mobMap = new HashMap<>();

    public static void addPokemob(final IPokemob pokemob)
    {
        // First remove the mob from all maps, incase it is in one.
        PokemobTracker.removePokemob(pokemob);

        final DimensionType dim = pokemob.getEntity().dimension;
        // Find the appropriate map
        final List<Entry> mobList = PokemobTracker.mobMap.getOrDefault(dim, new ArrayList<>());
        // Register the dimension if not already there
        if (!PokemobTracker.mobMap.containsKey(dim)) PokemobTracker.mobMap.put(dim, mobList);
        // Add the pokemob to the list
        mobList.add(new Entry(pokemob));
    }

    public static void removePokemob(final IPokemob pokemob)
    {
        final Entry e = new Entry(pokemob);
        // Remove the mob from all maps, incase it is in one.
        PokemobTracker.mobMap.forEach((d, m) -> m.remove(e));
    }

    public static int countPokemobs(final IWorld world, final AxisAlignedBB box, final Predicate<IPokemob> matches)
    {
        final DimensionType dim = world.getDimension().getType();
        final Entry[] mobList = PokemobTracker.mobMap.getOrDefault(dim, new ArrayList<>()).toArray(new Entry[0]);
        int num = 0;
        for (final Entry e : mobList)
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

    @SubscribeEvent
    public static void worldLoadEvent(final Load evt)
    {
        if (evt.getWorld().isRemote()) return;
        // Reset the tracked map for this world
        PokemobTracker.mobMap.put(evt.getWorld().getDimension().getType(), new ArrayList<>());
    }
}
