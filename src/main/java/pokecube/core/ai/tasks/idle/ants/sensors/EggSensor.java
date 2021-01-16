package pokecube.core.ai.tasks.idle.ants.sensors;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntHabitat;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntRoom;
import pokecube.core.ai.tasks.idle.ants.sensors.NestSensor.AntNest;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class EggSensor extends Sensor<MobEntity>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(AntTasks.NEST_POS,
            MemoryModuleType.VISIBLE_MOBS, AntTasks.EGG, AntTasks.WORK_POS);

    @Override
    protected void update(final ServerWorld worldIn, final MobEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemory(AntTasks.EGG)) return;
        if (brain.hasMemory(AntTasks.WORK_POS)) return;
        if (!brain.hasMemory(AntTasks.NEST_POS)) return;
        if (!brain.hasMemory(MemoryModuleType.VISIBLE_MOBS)) return;
        final Optional<AntNest> nest = NestSensor.getNest(entityIn);
        if (nest.isPresent())
        {
            final NestTile tile = nest.get().nest;
            final AntHabitat hab = nest.get().hab;

            final Optional<BlockPos> eggRoom = hab.getFreeEggRoom();
            if (!eggRoom.isPresent()) return;

            final List<EntityPokemobEgg> eggs = Lists.newArrayList();
            final List<LivingEntity> mobs = brain.getMemory(MemoryModuleType.VISIBLE_MOBS).get();
            mobs.forEach(e -> this.addIfEgg(e, eggs));
            eggs.removeIf(egg -> EggSensor.isInEggRoomOrCarried(tile, hab, egg));
            if (!eggs.isEmpty())
            {
                final EntityPokemobEgg egg = eggs.get(0);
                egg.getPersistentData().putLong("__carried__", egg.world.getGameTime() + 100);
                brain.setMemory(AntTasks.EGG, egg);
                brain.setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(worldIn.getDimensionKey(), eggRoom.get()));
            }
        }
    }

    public static boolean isInEggRoomOrCarried(final NestTile tile, final AntHabitat hab, final EntityPokemobEgg egg)
    {
        final Set<BlockPos> eggRooms = hab.getRooms(AntRoom.EGG);
        for (final BlockPos p : eggRooms)
            if (p.withinDistance(egg.getPositionVec(), 3)) return true;
        final long carryTick = egg.getPersistentData().getLong("__carried__");
        if (carryTick > egg.world.getGameTime()) return true;
        return false;
    }

    private void addIfEgg(final LivingEntity e, final List<EntityPokemobEgg> eggs)
    {
        if (e instanceof EntityPokemobEgg)
        {
            final EntityPokemobEgg egg = (EntityPokemobEgg) e;
            final IPokemob mob = egg.getPokemob(false);
            if (mob == null) return;
            if (AntTasks.isValidAnt(mob.getEntity())) eggs.add(egg);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return EggSensor.MEMS;
    }

}
