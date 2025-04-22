package pokecube.gimmicks.shoulder_mobs;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.utils.TagNames;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.idle.BaseIdleTask;
import pokecube.core.ai.tasks.idle.IdleWalkTask;
import thut.api.maths.Vector3;

import java.util.Map;

public class IdleJumpOnShoulderTask extends BaseIdleTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> _MEMS = Maps.newHashMap();

    private static Map<MemoryModuleType<?>, MemoryStatus> _getMems()
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
            moveToShoulder(player, pokemob);
            reset();
            restTimer *= 5;
        }
        else
        {
            this.setWalkTo(new Vector3(player), 1, 1);
        }
    }

    public static boolean moveToShoulder(Player player, IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        final float scale = pokemob.getSize();
        final float width = pokemob.getPokedexEntry().width * scale;
        final float height = pokemob.getPokedexEntry().height * scale;
        final float length = pokemob.getPokedexEntry().length * scale;
        boolean rightSize = width < 1 && height < 1 && length < 1;
        rightSize |= pokemob.getPokedexEntry().canSitShoulder;
        if (!rightSize) return false;

        if (entity instanceof ShoulderRidingEntity mob)
        {
            if (player instanceof ServerPlayer splayer) return mob.setEntityOnShoulder(splayer);
        }
        else
        {
            if (entity.isAlive() && !entity.isPassenger() && player.getPassengers().isEmpty())
            {
                entity.getPersistentData().putBoolean(ShoulderMobs.ON_SHOULDER, true);
                pokemob.setLogicState(LogicStates.SITTING, true);
                entity.startRiding(player, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRun()
    {
        // Configs can set this to -1 to disable idle movement entirely.
        if (IdleWalkTask.IDLETIMER <= 0) return false;

        // Wander disabled, so don't run.
        if (!this.pokemob.isRoutineEnabled(AIRoutine.WANDER)) return false;

        // Shoulder disabled, so don't run.
        if (!this.pokemob.isRoutineEnabled(ShoulderMobs.SHOULDER)) return false;

        // Only happy mobs do this!
        if (this.pokemob.getHappiness() < 200) return false;

        return this.pokemob.getOwner() instanceof Player && !this.pokemob.getGeneralState(GeneralStates.STAYING);
    }
}
