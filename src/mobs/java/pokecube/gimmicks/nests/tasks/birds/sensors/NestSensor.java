package pokecube.gimmicks.nests.tasks.birds.sensors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.api.blocks.IInhabitable;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.gimmicks.nests.NestTasks;
import pokecube.gimmicks.nests.blocks.NestTile;
import pokecube.gimmicks.nests.tasks.birds.BirdTasks;
import pokecube.gimmicks.nests.tasks.birds.nest.BirdHab;

import java.util.Optional;
import java.util.Set;

public class NestSensor extends Sensor<Mob>
{
    public record BirdNest(NestTile nest, BirdHab hab){}

    public static Optional<BirdNest> getNest(final Mob mob)
    {
        final Brain<?> brain = mob.getBrain();
        if (!brain.hasMemoryValue(MemoryModules.NEST_POS.get())) return Optional.empty();
        final Optional<GlobalPos> pos_opt = brain.getMemory(MemoryModules.NEST_POS.get());
        if (pos_opt.isPresent())
        {
            final Level world = mob.level();
            final GlobalPos pos = pos_opt.get();
            final boolean notHere = pos.dimension() != world.dimension();
            if (notHere) return Optional.empty();
            if (!world.isLoaded(pos.pos())) return Optional.empty();
            final BlockEntity tile = world.getBlockEntity(pos.pos());
            if (tile instanceof NestTile nest)
            {
                if (!nest.isType(BirdTasks.NESTLOC)) return Optional.empty();
                if (nest.getWrappedHab() instanceof BirdHab hab) return Optional.of(new BirdNest(nest, hab));
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
        final RandomSource rand = entityIn.getRandom();
        final Optional<BlockPos> opt = pois.getRandom(NestTasks.NEST_POI,
                p -> this.validNest(p, worldIn, entityIn), PoiManager.Occupancy.ANY, pos, 64, rand);
        if (opt.isPresent())
        {
            // Randomize this so we don't always pick the same hive if it was
            // cleared for some reason
            brain.eraseMemory(MemoryModules.NO_NEST_TIMER.get());
            brain.setMemory(MemoryModules.NEST_POS.get(), GlobalPos.of(entityIn.level().dimension(), opt.get()));
        }
        else
        {
            int timer = 0;
            if (brain.hasMemoryValue(MemoryModules.NO_NEST_TIMER.get()))
                timer = brain.getMemory(MemoryModules.NO_NEST_TIMER.get()).orElse(0);
            brain.setMemory(MemoryModules.NO_NEST_TIMER.get(), timer + 1);
        }
    }

    private boolean validNest(final BlockPos p, final ServerLevel worldIn, final Mob entityIn)
    {
        if (!entityIn.level.isLoaded(p)) return false;
        final BlockEntity tile = worldIn.getBlockEntity(p);
        if (!(tile instanceof NestTile nest)) return false;
        if (!nest.isType(BirdTasks.NESTLOC)) return false;
        final IInhabitable habitat = nest.getWrappedHab();
        return habitat.canEnterHabitat(entityIn);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(MemoryModules.NEST_POS.get(), MemoryModules.NO_NEST_TIMER.get());
    }

}
