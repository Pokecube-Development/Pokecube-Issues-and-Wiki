package pokecube.adventures.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.entity.trainer.TrainerBase;
import thut.api.maths.Vector3;

public class TrainerTracker
{
    private static class Entry implements Comparable<Entry>
    {
        final TrainerBase npc;

        public Entry(final TrainerBase npc)
        {
            this.npc = npc;
        }

        public BlockPos getPos()
        {
            return this.npc.getEntity().getPosition();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof Entry) return ((Entry) obj).npc.getEntity().getUniqueID().equals(this.npc.getEntity()
                    .getUniqueID());
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.npc.getEntity().getUniqueID().hashCode();
        }

        @Override
        public int compareTo(final Entry o)
        {
            return this.getPos().compareTo(o.getPos());
        }
    }

    private static Map<RegistryKey<World>, List<Entry>> mobMap = new HashMap<>();

    public static void add(final TrainerBase npc)
    {
        // First remove the mob from all maps, incase it is in one.
        TrainerTracker.removeTrainer(npc);

        final RegistryKey<World> dim = npc.getEntity().getEntityWorld().getDimensionKey();
        // Find the appropriate map
        final List<Entry> mobList = TrainerTracker.mobMap.getOrDefault(dim, new ArrayList<>());
        // Register the dimension if not already there
        if (!TrainerTracker.mobMap.containsKey(dim)) TrainerTracker.mobMap.put(dim, mobList);
        // Add the mob to the list
        mobList.add(new Entry(npc));
    }

    public static void removeTrainer(final TrainerBase pokemob)
    {
        final Entry e = new Entry(pokemob);
        // Remove the mob from all maps, incase it is in one.
        TrainerTracker.mobMap.forEach((d, m) -> m.remove(e));
    }

    public static int countTrainers(final World world, final AxisAlignedBB box, final Predicate<TrainerBase> matches)
    {
        final RegistryKey<World> dim = world.getDimensionKey();
        final Entry[] mobList = TrainerTracker.mobMap.getOrDefault(dim, new ArrayList<>()).toArray(new Entry[0]);
        int num = 0;
        for (final Entry e : mobList)
            if (box.contains(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()) && matches.test(e.npc)) num++;
        return num;
    }

    public static int countTrainers(final World world, final AxisAlignedBB box)
    {
        return TrainerTracker.countTrainers(world, box, e -> true);
    }

    public static int countTrainers(final World world, final Vector3 location, final double radius)
    {
        final AxisAlignedBB box = location.getAABB().grow(radius, radius, radius);
        return TrainerTracker.countTrainers(world, box);
    }

    @SubscribeEvent
    public static void worldLoadEvent(final Load evt)
    {
        if (evt.getWorld().isRemote() || !(evt.getWorld() instanceof World)) return;
        // Reset the tracked map for this world
        TrainerTracker.mobMap.put(((World)evt.getWorld()).getDimensionKey(), new ArrayList<>());
    }
}
