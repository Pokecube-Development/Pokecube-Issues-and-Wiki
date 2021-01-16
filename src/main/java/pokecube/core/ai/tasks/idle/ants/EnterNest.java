package pokecube.core.ai.tasks.idle.ants;

import java.util.Optional;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class EnterNest extends AntTask
{
    final Vector3 homePos = Vector3.getNewVector();

    public EnterNest(final IPokemob pokemob)
    {
        super(pokemob);
    }

    @Override
    public void reset()
    {
        this.homePos.clear();
    }

    @Override
    public void run()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<GlobalPos> pos_opt = brain.getMemory(AntTasks.NEST_POS);
        if (pos_opt.isPresent())
        {
            final World world = this.entity.getEntityWorld();
            final GlobalPos pos = pos_opt.get();
            boolean clearHive = pos.getDimension() != world.getDimensionKey();
            NestTile nest = null;
            TileEntity tile = null;
            if (!clearHive) // Not loaded, skip this check, hive may still be
                            // there.
                if (world.isAreaLoaded(pos.getPos(), 0))
            {
                tile = world.getTileEntity(pos.getPos());
                clearHive = !(tile instanceof NestTile);
            }

            // This will be cleared by CheckHive, so lets just exit here.
            if (clearHive) return;

            this.homePos.set(pos.getPos());
            if (tile != null) nest = (NestTile) tile;

            // If too far, lets path over.
            if (this.homePos.distToEntity(this.entity) > 2 || nest == null) this.setWalkTo(this.homePos, 1, 0);
            else
            {
                final IInhabitable habitat = nest.habitat;

                // if (habitat instanceof HabitatProvider) ((HabitatProvider)
                // habitat).wrapped = new AntHabitat();

                if (habitat.canEnterHabitat(this.entity))
                {
                    brain.setMemory(AntTasks.OUT_OF_HIVE_TIMER, 0);
                    habitat.onEnterHabitat(this.entity);
                }
                // Set the out of hive timer, so we don't try to re-enter
                // immediately!
                else brain.setMemory(AntTasks.OUT_OF_HIVE_TIMER, 100);
            }
        }
    }

    @Override
    boolean doTask()
    {
        // We were already heading home, so keep doing that.
        if (!this.homePos.isEmpty()) return true;
        if (this.nest == null) return false;
        final Brain<?> brain = this.entity.getBrain();
        final Optional<Integer> hiveTimer = brain.getMemory(AntTasks.OUT_OF_HIVE_TIMER);
        final int timer = hiveTimer.orElseGet(() -> 0);
        // This is our counter for if something angered us, and made is leave
        // the hive, if so, we don't return to hive.
        if (timer > 0) return false;

        if (AntTasks.shouldAntBeInNest(this.world, this.nest.nest.getPos())) return true;
        // Been out too long, we want to return!
        if (timer < -2400) return true;
        // Been out too long, we want to return!
        if (timer < -1200 && this.entity.getRNG().nextInt(200) == 0) return true;
        return false;
    }

}
