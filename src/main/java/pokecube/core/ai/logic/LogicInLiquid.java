package pokecube.core.ai.logic;

import net.minecraft.world.level.Level;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Matrix3;

/**
 * This checks if the pokemob is in lava or water. The checks are done on a
 * seperate thread via doLogic() for performance reasons.
 */
public class LogicInLiquid extends LogicBase
{
    Matrix3 box = new Matrix3();

    public LogicInLiquid(IPokemob pokemob_)
    {
        super(pokemob_);
    }

    @Override
    public void tick(Level world)
    {
        if (world == null) return;
        this.pokemob.setLogicState(LogicStates.INLAVA, this.entity.isInLava());
        this.pokemob.setLogicState(LogicStates.INWATER, this.entity.isInWater());
    }
}
