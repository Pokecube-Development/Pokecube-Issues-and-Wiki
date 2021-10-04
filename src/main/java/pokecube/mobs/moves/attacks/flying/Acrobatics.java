package pokecube.mobs.moves.attacks.flying;

import net.minecraft.world.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class Acrobatics extends Move_Basic
{

    public Acrobatics()
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
