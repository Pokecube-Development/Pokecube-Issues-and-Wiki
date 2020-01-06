package pokecube.core.moves.implementations.attacks.flying;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class MoveAcrobatics extends Move_Basic
{

    public MoveAcrobatics()
    {
        super("acrobatics");
    }

    @Override
    public int getPWR(IPokemob attacker, Entity attacked)
    {
        int bonus = 1;
        if (attacker.getHeldItem().isEmpty()) bonus = 2;
        return this.getPWR() * bonus;
    }
}
