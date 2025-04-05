package pokecube.core.ai.logic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.LogicStates;

/**
 * This checks if the pokemob is in lava or water. The checks are done on a
 * seperate thread via doLogic() for performance reasons.
 */
public class LogicInLiquid extends LogicBase
{
    private static final ResourceLocation id = ResourceLocation.parse("pokecube:swim_gravity");
    private static final AttributeModifier SWIMGRAVITY = new AttributeModifier(id, -0.75,
            Operation.ADD_MULTIPLIED_TOTAL);

    public LogicInLiquid(IPokemob pokemob_)
    {
        super(pokemob_);
    }

    @Override
    public void tick(Level world)
    {
        if (world == null) return;
        AttributeInstance gravity = entity.getAttribute(Attributes.GRAVITY);

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
