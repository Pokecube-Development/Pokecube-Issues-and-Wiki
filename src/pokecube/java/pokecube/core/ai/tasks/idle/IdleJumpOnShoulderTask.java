package pokecube.core.ai.tasks.idle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.ai.brain.MemoryModules;
import thut.api.maths.Vector3;

public class IdleJumpOnShoulderTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> _MEMS = Maps.newHashMap();

    private static final Map<MemoryModuleType<?>, MemoryStatus> _getMems()
    {
        if (_MEMS.isEmpty())
        {
            // Dont run if have a walk target
            _MEMS.put(MemoryModules.WALK_TARGET, MemoryStatus.VALUE_ABSENT);
            // Don't run if have a target location for moves
            _MEMS.put(MemoryModules.MOVE_TARGET.get(), MemoryStatus.VALUE_ABSENT);
            // Don't run if we have a path
            _MEMS.put(MemoryModules.PATH, MemoryStatus.VALUE_ABSENT);
        }
        return _MEMS;
    }

    private int restTimer = 0;

    public IdleJumpOnShoulderTask(IPokemob pokemob)
    {
        super(pokemob, _getMems());
        restTimer = this.entity.getRandom().nextInt(IdleWalkTask.IDLETIMER);
    }

    @Override
    public void reset()
    {
        restTimer = 20 + this.entity.getRandom().nextInt(IdleWalkTask.IDLETIMER);
    }

    @Override
    public void run()
    {
        boolean sitting = pokemob.getLogicState(LogicStates.SITTING);
        restTimer--;
        if (restTimer > 0 || !(pokemob.getOwner() instanceof Player player)) return;

        if (sitting)
        {
            pokemob.setLogicState(LogicStates.SITTING, false);
            reset();
            restTimer *= 5;
        }
        else if (pokemob.getEntity().distanceTo(player) < 1)
        {
            pokemob.moveToShoulder(player);
            reset();
            restTimer *= 5;
        }
        else
        {
            this.setWalkTo(new Vector3(player), 1, 1);
        }
    }

    @Override
    public boolean shouldRun()
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (IdleWalkTask.IDLETIMER <= 0) return false;

        // Wander disabled, so don't run.
        if (!this.pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        // Only happy mobs do this!
        if (this.pokemob.getHappiness() < 200) return false;

        final boolean tameFactor = this.pokemob.getOwner() instanceof Player
                && !this.pokemob.getGeneralState(GeneralStates.STAYING);
        return tameFactor;
    }
}
