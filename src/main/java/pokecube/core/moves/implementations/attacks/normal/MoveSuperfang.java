package pokecube.core.moves.implementations.attacks.normal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class MoveSuperfang extends Move_Basic
{

    public MoveSuperfang()
    {
        super("superfang");
        this.setFixedDamage();
    }

    @Override
    public int getPWR(IPokemob attacker, Entity attacked)
    {
        if (!(attacked instanceof LivingEntity)) return 0;
        return (int) Math.ceil(((LivingEntity) attacked).getHealth() / 2);
    }
}
