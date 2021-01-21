package pokecube.core.moves;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.world.IWorldTickListener;
import pokecube.core.world.WorldTickManager;

public class MoveQueue
{
    public static class MoveQueuer implements IWorldTickListener
    {
        private static final Map<RegistryKey<World>, MoveQueue> queues = Maps.newHashMap();

        static MoveQueuer INSTANCE = new MoveQueuer();

        public static void register()
        {
            WorldTickManager.registerStaticData(() -> MoveQueuer.INSTANCE, p -> true);
        }

        @Override
        public void onTickEnd(final ServerWorld world)
        {
            final MoveQueue queue = MoveQueuer.queues.get(world.getDimensionKey());
            if (queue == null)
            {
                PokecubeCore.LOGGER.error("Critical Error with world for dimension " + world.getDimensionKey()
                        + " It is somehow ticking when not loaded, this should not happen.", new Exception());
                return;
            }
            final long time = System.nanoTime();
            final int num = queue.moves.size();
            queue.executeMoves();
            final double dt = (System.nanoTime() - time) / 1000d;
            if (dt > 1000) PokecubeCore.LOGGER.debug("move queue took {}  for world {} for {} moves.", dt, world
                    .getDimensionKey(), num);
        }

        @Override
        public void onDetach(final ServerWorld world)
        {
            MoveQueuer.queues.remove(world.getDimensionKey());
        }

        @Override
        public void onAttach(final ServerWorld world)
        {
            MoveQueuer.queues.put(world.getDimensionKey(), new MoveQueue(world));
        }

        public static void queueMove(final EntityMoveUse move)
        {
            final MoveQueue queue = MoveQueuer.queues.get(move.getEntityWorld().getDimensionKey());
            if (queue == null) throw new NullPointerException("why is world queue null?");
            if (move.getUser() != null) queue.moves.add(move);
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
