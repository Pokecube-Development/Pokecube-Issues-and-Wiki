package pokecube.adventures.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
            return this.npc.getEntity().blockPosition();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof Entry) return ((Entry) obj).npc.getEntity().getUUID().equals(this.npc.getEntity()
                    .getUUID());
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.npc.getEntity().getUUID().hashCode();
        }

        @Override
        public int compareTo(final Entry o)
        {
            return this.getPos().compareTo(o.getPos());
        }
    }

    private static Map<ResourceKey<Level>, List<Entry>> mobMap = new HashMap<>();

    public static void add(final TrainerBase npc)
    {
        // First remove the mob from all maps, incase it is in one.
        TrainerTracker.removeTrainer(npc);

        final ResourceKey<Level> dim = npc.getEntity().getCommandSenderWorld().dimension();
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

    public static int countTrainers(final Level world, final AABB box, final Predicate<TrainerBase> matches)
    {
        final ResourceKey<Level> dim = world.dimension();
        final Entry[] mobList = TrainerTracker.mobMap.getOrDefault(dim, new ArrayList<>()).toArray(new Entry[0]);
        int num = 0;
        for (final Entry e : mobList)
            if (box.contains(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()) && matches.test(e.npc)) num++;
        return num;
    }

    public static int countTrainers(final Level world, final AABB box)
    {
        return TrainerTracker.countTrainers(world, box, e -> true);
    }

    public static int countTrainers(final Level world, final Vector3 location, final double radius)
    {
        final AABB box = location.getAABB().inflate(radius, radius, radius);
        return TrainerTracker.countTrainers(world, box);
    }

    @SubscribeEvent
    public static void worldLoadEvent(final Load evt)
    {
        if (evt.getWorld().isClientSide() || !(evt.getWorld() instanceof Level)) return;
        // Reset the tracked map for this world
        TrainerTracker.mobMap.put(((Level)evt.getWorld()).dimension(), new ArrayList<>());
    }
}
