package pokecube.core.ai.tasks.combat.attacks;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.ai.combat.CombatModes;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.combat.CombatTask;

import java.util.Map;

public class SelectMoveTask extends CombatTask
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> MEMS = Maps.newHashMap();

    static
    {
        MEMS.put(MemoryModules.TIMER_SWAPMOVE.get(), MemoryStatus.REGISTERED);
    }

    public SelectMoveTask()
    {
        super(MEMS);
    }

    @Override
    public void reset(Mob entityIn)
    {
        entityIn.getBrain().eraseMemory(MemoryModules.TIMER_SWAPMOVE.get());
    }

    /** If in combat, select a move to use. */
    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        var selector = CombatModes.getMode(pokemob);
        if (selector != null) selector.checkMoves(pokemob);
    }

    /**
     * Check if the mob is in combat.
     */
    @Override
    public boolean shouldRun(Mob entityIn)
    {
        var pokemob = PokemobCaps.getPokemobFor(entityIn);
        // Should not swap moves if this is set.
        if (pokemob.getCombatState(CombatStates.NOMOVESWAP)) return false;
        // Only swap moves during combat.
        if (!pokemob.getCombatState(CombatStates.BATTLING)) return false;
        var target = this.getAttackTarget(entityIn);
        return target != null;
    }
}
