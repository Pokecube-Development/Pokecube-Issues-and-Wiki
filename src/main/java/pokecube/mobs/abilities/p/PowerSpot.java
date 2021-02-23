package pokecube.mobs.abilities.p;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class PowerSpot extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        LivingEntity target = null;
        if (move.attacked instanceof LivingEntity) target = (LivingEntity) move.attacked;
        // TODO replace with forge multipart entity in 1.16.5
        else if (move.attacked instanceof EnderDragonPartEntity)
            target = ((EnderDragonPartEntity) move.attacked).dragon;
        if (target == null) return;
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob == null) return;

        final IPokemob attacker = move.attacker;

        if (move.pre || attacker == move.attacked) return;
        if (targetMob != null) move.PWR *= 1.3;
    }
}
