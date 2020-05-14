package pokecube.core.ai.tasks.combat;

import java.util.Random;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.Entity;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.entity.ai.IAICombat;

public class AISelectMove extends AIBase implements IAICombat
{
    Entity      target;
    private int moveIndexCounter = 0;

    public AISelectMove(final IPokemob mob)
    {
        super(mob);
        this.setMutex(0);
    }

    @Override
    public void reset()
    {
        this.target = null;
        this.moveIndexCounter = 0;
    }

    /** If in combat, select a move to use. */
    @Override
    public void run()
    {
        // Pokemobs hunting or guarding will always select whatever is strongest
        if (this.pokemob.getCombatState(CombatStates.GUARDING) || this.pokemob.getCombatState(CombatStates.HUNTING))
        {
            this.selectHighestDamage();
            return;
        }

        // Tame pokemobs only run this if they are on guard, otherwise their
        // owner is selecting moves for them, staying pokemobs however behave
        // wild, so this doesn't apply to them.
        if (this.pokemob.getGeneralState(GeneralStates.TAMED) && !this.pokemob.getGeneralState(GeneralStates.STAYING))
            return;

        // Select a random move to use.
        this.selectRandomMove();
    }

    /**
     * Determine which move to use based on whatever should apply the most
     * damage to the current target.
     *
     * @return if move swapped
     */
    protected boolean selectHighestDamage()
    {
        int index = this.pokemob.getMoveIndex();
        int max = 0;
        final String[] moves = this.pokemob.getMoves();
        final double dist = this.entity.getDistanceSq(this.target.posX, this.target.posY, this.target.posZ);
        for (int i = 0; i < 4; i++)
        {
            final String s = moves[i];
            // Cannot select a disabled move.
            if (this.pokemob.getDisableTimer(i) > 0) continue;
            if (s != null)
            {
                final Move_Base m = MovesUtils.getMoveFromName(s);
                if (m == null) continue;
                int temp = Tools.getPower(s, this.pokemob, this.target);
                if (dist > 5 && (m.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0) temp *= 1.5;
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        // Update index if it changed.
        if (index != this.pokemob.getMoveIndex())
        {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.log(Level.TRACE, "Move Swap to Highest Damage, " + this.pokemob
                    .getEntity() + " g:" + this.pokemob.getCombatState(CombatStates.GUARDING) + " h:" + this.pokemob
                            .getCombatState(CombatStates.HUNTING));
            this.pokemob.setMoveIndex(index);
            return true;
        }
        return false;
    }

    /**
     * Pick a random move on a bit of a random timer.
     *
     * @return if move swapped
     */
    protected boolean selectRandomMove()
    {
        final Random rand = new Random();
        if (this.moveIndexCounter++ > rand.nextInt(30))
        {
            final int nb = rand.nextInt(5);
            int index = 0;
            for (int i = 0; i < 4; i++)
            {
                index = (nb + i) % 4;
                if (this.pokemob.getDisableTimer(index) > 0) continue;
                if (this.pokemob.getMove(index) == null) continue;
                break;
            }
            this.moveIndexCounter = 0;
            if (index != this.pokemob.getMoveIndex())
            {
                if (PokecubeMod.debug) PokecubeCore.LOGGER.log(Level.TRACE, "Move Swap to Random Move, " + this.pokemob
                        .getEntity() + " g:" + this.pokemob.getCombatState(CombatStates.GUARDING) + " h:" + this.pokemob
                                .getCombatState(CombatStates.HUNTING));
                this.pokemob.setMoveIndex(index);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the mob is in combat.
     *
     * @return
     */
    @Override
    public boolean shouldRun()
    {
        // Should not swap moves if this is set.
        if (this.pokemob.getCombatState(CombatStates.NOMOVESWAP)) return false;
        // Only swap moves during combat.
        return this.pokemob.getCombatState(CombatStates.ANGRY) && (this.target = this.entity.getAttackTarget()) != null;
    }
}
