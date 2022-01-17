package pokecube.core.ai.logic;

import java.util.UUID;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.level.Level;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

/**
 * This checks if the pokemob is in lava or water. The checks are done on a
 * seperate thread via doLogic() for performance reasons.
 */
public class LogicInLiquid extends LogicBase
{
    private static final UUID id = new UUID(134123546, 4356456);
    private static final AttributeModifier SWIMGRAVITY = new AttributeModifier(id, "swim_gravity", -0.75,
            Operation.MULTIPLY_TOTAL);

    public LogicInLiquid(IPokemob pokemob_)
    {
        super(pokemob_);
    }

    @Override
    public void tick(Level world)
    {
        if (world == null) return;
        AttributeInstance gravity = entity.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());

        boolean water = this.entity.isInWater();
        boolean lava = this.entity.isInLava();

        this.pokemob.setLogicState(LogicStates.INLAVA, lava);
        this.pokemob.setLogicState(LogicStates.INWATER, water);

        if (water)
        {
            if (gravity.getModifier(id) == null)
            {
                gravity.addTransientModifier(SWIMGRAVITY);
            }
        }
        else gravity.removeModifier(id);
    }
}
