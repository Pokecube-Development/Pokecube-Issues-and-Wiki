package pokecube.mobs.abilities.simple;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;

public class Aftermath extends Ability
{
    @Override
    public void postMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeTarget(mob, move)) return;
        final MoveEntry attack = move.getMove();
        if (attack == null || (attack.getAttackCategory(move.getUser()) & IMoveConstants.CATEGORY_CONTACT) == 0) return;

        if (mob.getEntity().getHealth() <= 0)
        {
            final Explosion boom = new Explosion(move.target.getLevel(), move.target, move.target.getX(),
                    move.target.getY(), move.target.getZ(), 0, false, BlockInteraction.BREAK);
            final ExplosionEvent evt = new ExplosionEvent.Start(move.target.getLevel(), boom);
            MinecraftForge.EVENT_BUS.post(evt);
            if (!evt.isCanceled())
            {
                final LivingEntity attacker = move.getUser().getEntity();
                final float hp = attacker.getHealth();
                attacker.hurt(DamageSource.MAGIC, hp / 4);
            }
        }
    }
}
