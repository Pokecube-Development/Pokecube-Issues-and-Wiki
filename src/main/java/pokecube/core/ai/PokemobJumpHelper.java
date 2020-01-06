package pokecube.core.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.JumpController;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

/**
 * Overriden to allow setting the pokemob's specific jumping state, and calling
 * the custom jump implementation.
 */
public class PokemobJumpHelper extends JumpController
{

    private final IPokemob pokemob;

    public PokemobJumpHelper(final MobEntity entityIn)
    {
        super(entityIn);
        this.pokemob = CapabilityPokemob.getPokemobFor(entityIn);
    }

    /** Called to actually make the entity jump if isJumping is true. */
    @Override
    public void tick()
    {
        super.tick();
        if (this.pokemob.getLogicState(LogicStates.JUMPING)) this.pokemob.setLogicState(LogicStates.JUMPING, false);
    }

}
