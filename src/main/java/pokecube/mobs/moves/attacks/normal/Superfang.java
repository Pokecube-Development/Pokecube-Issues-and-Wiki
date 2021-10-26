package pokecube.mobs.moves.attacks.normal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class Superfang extends Move_Basic
{

    public Superfang()
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
