package pokecube.gimmicks.nests.tasks.ants.sensors;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.blocks.nests.NestTile;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.nest.AntHabitat;
import thut.core.common.ThutCore;

public class NestSensor extends Sensor<Mob>
{
    public static int NESTSPACING = 64;

    public static class AntNest
    {
        public final NestTile nest;
        public final AntHabitat hab;

        public AntNest(final NestTile tile, final AntHabitat hab)
        {
            this.nest = tile;
            this.hab = hab;
        }
    }

    public static Optional<AntNest> getNest(final Mob mob)
    {
        final Brain<?> brain = mob.getBrain();
        if (!brain.hasMemoryValue(MemoryModules.NEST_POS.get())) return Optional.empty();
        final Optional<GlobalPos> pos_opt = brain.getMemory(MemoryModules.NEST_POS.get());
        if (pos_opt.isPresent())
        {
            final Level world = mob.getLevel();
            final GlobalPos pos = pos_opt.get();
            final boolean notHere = pos.dimension() != world.dimension();
            if (notHere) return Optional.empty();
            if (!world.isLoaded(pos.pos())) return Optional.empty();
            final BlockEntity tile = world.getBlockEntity(pos.pos());
            if (tile instanceof NestTile nest)
            {
                if (!nest.isType(AntTasks.NESTLOC)) return Optional.empty();
                if (nest.getWrappedHab() instanceof AntHabitat hab) return Optional.of(new AntNest(nest, hab));
            }
        }
        return Optional.empty();
    }

    @Override
    protected void doTick(final ServerLevel worldIn, final Mob entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemoryValue(MemoryModules.NEST_POS.get())) return;

        final PoiManager pois = worldIn.getPoiManager();
        final BlockPos pos = entityIn.blockPosition();
        final Random rand = ThutCore.newRandom();
        final Optional<BlockPos> opt = pois.getRandom(p -> p == PointsOfInterest.NEST.get(),
                p -> this.validNest(p, worldIn, entityIn), Occupancy.ANY, pos, NestSensor.NESTSPACING, rand);
        if (opt.isPresent())
        {
            // Randomize this so we don't always pick the same hive if it was
            // cleared for some reason
            brain.eraseMemory(MemoryModules.NO_NEST_TIMER.get());
            brain.setMemory(MemoryModules.NEST_POS.get(), GlobalPos.of(entityIn.getLevel().dimension(), opt.get()));
        }
        else
        {
            int timer = 0;
            if (brain.hasMemoryValue(MemoryModules.NO_NEST_TIMER.get()))
                timer = brain.getMemory(MemoryModules.NO_NEST_TIMER.get()).get();
            brain.setMemory(MemoryModules.NO_NEST_TIMER.get(), timer + 1);
        }
    }

    private boolean validNest(final BlockPos p, final ServerLevel worldIn, final Mob entityIn)
    {
        if (!entityIn.level.isLoaded(p)) return false;
        final BlockEntity tile = worldIn.getBlockEntity(p);
        if (!(tile instanceof NestTile nest)) return false;
        return nest.isType(AntTasks.NESTLOC);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(MemoryModules.NEST_POS.get(), MemoryModules.NO_NEST_TIMER.get());
    }

}
