package thut.api.world.mobs.ai;

import thut.api.world.mobs.Mob;

public interface AI
{
    /**
     * The mob being attacked by the mob holding this AI.
     *
     * @return
     */
    Mob getAttackTarget();

    /**
     * Sets the attack target.
     *
     * @param mob
     */
    void setAttackTarget(Mob mob);
}
