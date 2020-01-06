package thut.core.common.world.mobs.ai;

import thut.api.world.mobs.Mob;
import thut.api.world.mobs.ai.AI;

public class AI_Impl implements AI
{
    private Mob target;

    @Override
    public Mob getAttackTarget()
    {
        return this.target;
    }

    @Override
    public void setAttackTarget(Mob mob)
    {
        this.target = mob;
    }

}
