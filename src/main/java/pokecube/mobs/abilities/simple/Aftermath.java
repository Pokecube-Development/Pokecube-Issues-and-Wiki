package pokecube.mobs.abilities.simple;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraftforge.event.world.ExplosionEvent;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import thut.core.common.ThutCore;

@AbilityProvider(name = "aftermath")
public class Aftermath extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        final MoveEntry attack = move.getMove();
        if (attack == null || !attack.isContact(move.getUser())) return;

        if (mob.getEntity().getHealth() <= 0)
        {
            final Explosion boom = new Explosion(move.getTarget().getLevel(), move.getTarget(), move.getTarget().getX(),
                    move.getTarget().getY(), move.getTarget().getZ(), 0, false, BlockInteraction.BREAK);
            final ExplosionEvent evt = new ExplosionEvent.Start(move.getTarget().getLevel(), boom);
            ThutCore.FORGE_BUS.post(evt);
            if (!evt.isCanceled())
            {
                final LivingEntity attacker = move.getUser().getEntity();
                final float hp = attacker.getHealth();
                attacker.hurt(DamageSource.MAGIC, hp / 4);
            }
        }
    }
}
