package pokecube.core.moves;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.animations.EntityMoveUse;

public class MoveQueue
{
    @Mod.EventBusSubscriber
    public static class MoveQueuer
    {
        private static final Map<IWorld, MoveQueue> queues = Maps.newHashMap();

        @SubscribeEvent
        public static void load(final WorldEvent.Load evt)
        {
            MoveQueuer.queues.put(evt.getWorld(), new MoveQueue(evt.getWorld()));
        }

        public static void queueMove(final EntityMoveUse move)
        {
            final MoveQueue queue = MoveQueuer.queues.get(move.getEntityWorld());
            if (queue == null) throw new NullPointerException("why is world queue null?");
            if (move.getUser() != null) queue.moves.add(move);
        }

        @SubscribeEvent
        public static void tick(final WorldTickEvent evt)
        {
            if (evt.phase == Phase.END && evt.side == LogicalSide.SERVER)
            {
                final MoveQueue queue = MoveQueuer.queues.get(evt.world);
                if (queue == null)
                {
                    PokecubeCore.LOGGER.error("Critical Error with world for dimension " + evt.world.dimension
                            .getDimension() + " It is somehow ticking when not loaded, this should not happen.",
                            new Exception());
                    return;
                }
                final long time = System.nanoTime();
                queue.executeMoves();
                final double dt = (System.nanoTime() - time) / 1000d;
                if (dt > 100) PokecubeCore.LOGGER.debug("move queue took " + dt + " for world " + evt.world);
            }
        }

        @SubscribeEvent
        public static void unload(final WorldEvent.Unload evt)
        {
            MoveQueuer.queues.remove(evt.getWorld());
        }
    }

    public List<EntityMoveUse> moves = Lists.newArrayList();
    final IWorld               world;

    public MoveQueue(final IWorld iWorld)
    {
        this.world = iWorld;
    }

    public void executeMoves()
    {
        synchronized (this.moves)
        {
            Collections.sort(this.moves, (o1, o2) ->
            {
                final IPokemob user1 = CapabilityPokemob.getPokemobFor(o1.getUser());
                final IPokemob user2 = CapabilityPokemob.getPokemobFor(o2.getUser());
                final int speed1 = user1 == null ? 0 : user1.getStat(Stats.VIT, true);
                final int speed2 = user2 == null ? 0 : user2.getStat(Stats.VIT, true);
                // TODO also factor in move priority here.
                return speed1 - speed2;
            });
            for (final EntityMoveUse move : this.moves)
            {
                if (move.getUser() == null || !move.getUser().isAlive()) continue;
                boolean toUse = true;
                if (move.getUser() instanceof LivingEntity) toUse = ((LivingEntity) move.getUser()).getHealth() >= 1;
                if (toUse)
                {
                    final IPokemob mob = CapabilityPokemob.getPokemobFor(move.getUser());
                    this.world.addEntity(move);
                    move.getMove().applyHungerCost(mob);
                    MovesUtils.displayMoveMessages(mob, move.getTarget(), move.getMove().name);
                }
            }
            this.moves.clear();
        }
    }

}
