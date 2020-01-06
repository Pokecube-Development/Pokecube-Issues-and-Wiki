package pokecube.core.database.abilities.a;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import pokecube.core.PokecubeCore;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;

public class ArenaTrap extends Ability
{
    // the part that usually prevents switching is still "TODO"
    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getEntity().ticksExisted % 20 == 0)
        {
            if (!(mob.getOwner() instanceof ServerPlayerEntity)) return;
            PokecubeCore.spawner.doSpawnForPlayer((PlayerEntity) mob.getOwner(), mob.getOwner()
                    .getEntityWorld());
        }
    }
}
