package pokecube.core.ai.tasks.combat.attacks;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.combat.CombatTask;
import pokecube.core.moves.MovesUtils;
import thut.api.entity.ai.IAICombat;
import thut.core.common.ThutCore;

import java.util.Map;
import java.util.Random;

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
        // Pokemobs hunting or guarding will always select whatever is strongest
        if (pokemob.getCombatState(CombatStates.GUARDING) || pokemob.getCombatState(CombatStates.HUNTING)
                || pokemob.getCombatState(CombatStates.MATEFIGHT))
        {
            this.selectHighestDamage(pokemob);
            return;
        }

        // Tame pokemobs only run this if they are on guard, otherwise their
        // owner is selecting moves for them, staying pokemobs however behave
        // wild, so this doesn't apply to them.
        if (pokemob.getGeneralState(GeneralStates.TAMED) && !pokemob.getGeneralState(GeneralStates.STAYING)) return;

        // Select a random move to use.
        this.selectRandomMove(pokemob);
    }

    /**
     * Determine which move to use based on whatever should apply the most damage to the current target.
     */
    protected void selectHighestDamage(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        int index = pokemob.getMoveIndex();
        float max = 0;
        var target = this.getAttackTarget(pokemob.getEntity());
        final double dist = entity.distanceToSqr(target.getX(), target.getY(), target.getZ());
        for (int i = 0; i < 4; i++)
        {
            final String s = pokemob.getMove(i);
            // Cannot select a disabled move.
            if (pokemob.getDisableTimer(i) > 0) continue;
            if (s != null)
            {
                final MoveEntry m = MovesUtils.getMove(s);
                if (m == null) continue;
                float temp = Tools.getPower(s, pokemob, target);
                if (dist > 5 && m.isRanged(pokemob)) temp *= 1.5f;
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        // Update index if it changed.
        if (index != pokemob.getMoveIndex())
        {
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo(
                    "Move Swap to Highest Damage, " + pokemob.getEntity() + " g:" + pokemob.getCombatState(
                            CombatStates.GUARDING) + " h:" + pokemob.getCombatState(CombatStates.HUNTING));
            pokemob.setMoveIndex(index);
        }
    }

    /**
     * Pick a random move on a bit of a random timer.
     */
    protected void selectRandomMove(IPokemob pokemob)
    {
        final Random rand = ThutCore.newRandom();
        int timer = pokemob.getEntity().getBrain().getMemory(MemoryModules.TIMER_SWAPMOVE.get()).orElse(0);
        if (timer++ > rand.nextInt(30))
        {
            final int nb = rand.nextInt(5);
            int index = 0;
            for (int i = 0; i < 4; i++)
            {
                index = (nb + i) % 4;
                if (pokemob.getDisableTimer(index) > 0) continue;
                if (pokemob.getMove(index) == null) continue;
                break;
            }
            timer = 0;
            if (index != pokemob.getMoveIndex())
            {
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo(
                        "Move Swap to Random Move, " + pokemob.getEntity() + " g:" + pokemob.getCombatState(
                                CombatStates.GUARDING) + " h:" + pokemob.getCombatState(CombatStates.HUNTING));
                pokemob.setMoveIndex(index);
            }
        }
        pokemob.getEntity().getBrain().setMemory(MemoryModules.TIMER_SWAPMOVE.get(), timer);
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
