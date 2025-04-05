package pokecube.gimmicks.nests.tasks.ants.sensors;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntRoom;
import pokecube.gimmicks.nests.tasks.ants.nest.AntHabitat;
import pokecube.gimmicks.nests.tasks.ants.nest.Node;
import pokecube.gimmicks.nests.tasks.ants.sensors.NestSensor.AntNest;
import thut.api.Tracker;

public class EggSensor extends Sensor<Mob>
{
    @Override
    protected void doTick(final ServerLevel worldIn, final Mob entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemoryValue(MemoryModules.EGG.get())) return;
        if (brain.hasMemoryValue(MemoryModules.WORK_POS.get())) return;
        if (!brain.hasMemoryValue(MemoryModules.NEST_POS.get())) return;
        if (!brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) return;
        final Optional<AntNest> nest = NestSensor.getNest(entityIn);
        if (nest.isPresent())
        {
            final NestTile tile = nest.get().nest;
            final AntHabitat hab = nest.get().hab;

            final Optional<BlockPos> eggRoom = hab.getFreeEggRoom();
            if (!eggRoom.isPresent()) return;

            final List<EntityPokemobEgg> eggs = Lists.newArrayList();
            final Iterable<LivingEntity> mobs = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get()
                    .findAll(e -> true);
            mobs.forEach(e -> this.addIfEgg(e, eggs));
            eggs.removeIf(egg -> EggSensor.isInEggRoomOrCarried(tile, hab, egg));
            if (!eggs.isEmpty())
            {
                final EntityPokemobEgg egg = eggs.get(0);
                egg.getPersistentData().putLong("__carried__", Tracker.instance().getTick() + 100);
                brain.setMemory(MemoryModules.EGG.get(), egg);
                brain.setMemory(MemoryModules.WORK_POS.get(), GlobalPos.of(worldIn.dimension(), eggRoom.get()));
            }
        }
    }

    public static boolean isInEggRoomOrCarried(final NestTile tile, final AntHabitat hab, final EntityPokemobEgg egg)
    {
        final List<Node> eggRooms = hab.getRooms(AntRoom.EGG);
        for (final Node p : eggRooms) if (p.getCenter().closerToCenterThan(egg.position(), 3)) return true;
        final long carryTick = egg.getPersistentData().getLong("__carried__");
        if (carryTick > Tracker.instance().getTick()) return true;
        return false;
    }

    private void addIfEgg(final LivingEntity e, final List<EntityPokemobEgg> eggs)
    {
        if (e instanceof EntityPokemobEgg egg)
        {
            final IPokemob mob = egg.getPokemob(false);
            if (mob == null) return;
            if (AntTasks.isValid(mob.getEntity())) eggs.add(egg);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(MemoryModules.NEST_POS.get(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModules.EGG.get(), MemoryModules.WORK_POS.get());
    }

}
