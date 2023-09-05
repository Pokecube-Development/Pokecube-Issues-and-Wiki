package pokecube.mobs.abilities.simple;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ExplosionEvent;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;

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
            final Explosion boom = new Explosion(move.getTarget().level(), move.getTarget(), move.getTarget().getX(),
                    move.getTarget().getY(), move.getTarget().getZ(), 0, false, BlockInteraction.DESTROY_WITH_DECAY);
            final ExplosionEvent evt = new ExplosionEvent.Start(move.getTarget().level(), boom);
            MinecraftForge.EVENT_BUS.post(evt);
            if (!evt.isCanceled())
            {
                final LivingEntity attacker = move.getUser().getEntity();
                final float hp = attacker.getHealth();
                attacker.hurt(attacker.damageSources().magic(), hp / 4);
            }
        }
    }
}
