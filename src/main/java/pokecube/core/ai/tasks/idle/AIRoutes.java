package pokecube.core.ai.tasks.idle;

import net.minecraft.entity.MobEntity;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

/** This is the AIBase version of GuardAI */
public class AIRoutes extends AIBase
{
    public final GuardAI wrapped;
    private boolean      running;

    public AIRoutes(MobEntity mob, IGuardAICapability cap)
    {
        super(CapabilityPokemob.getPokemobFor(mob));
        this.wrapped = new GuardAI(mob, cap);
    }

    @Override
    public void reset()
    {
    }

    @Override
    /** This is just wrapping the guardAI, so it doesn't do anything here. */
    public void run()
    {
    }

    @Override
    /**
     * Runs if not a pokemob, or the pokemob is wild, or the pokemob is on
     * stay.
     */
    public boolean shouldRun()
    {
        // Shouldn't run if angry
        if (this.pokemob != null && this.pokemob.getCombatState(CombatStates.ANGRY)) return false;

        return this.pokemob == null || !this.pokemob.getGeneralState(GeneralStates.TAMED) || this.pokemob
                .getGeneralState(GeneralStates.STAYING);
    }

    @Override
    public void tick()
    {
        if (!this.shouldRun()) return;
        if (!this.running)
        {
            this.running = this.wrapped.shouldExecute();
            if (this.running) this.wrapped.startExecuting();
        }
        else
        {
            if (!this.wrapped.shouldContinueExecuting())
            {
                this.running = false;
                this.wrapped.resetTask();
                return;
            }
            this.wrapped.tick();
        }

    }

}
