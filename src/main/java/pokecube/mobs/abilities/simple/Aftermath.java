package pokecube.mobs.abilities.simple;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ExplosionEvent;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;

public class Aftermath extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob != move.attacked || move.pre || move.attacker == move.attacked) return;
        final Move_Base attack = move.getMove();
        if (attack == null || (attack.getAttackCategory(move.attacker) & IMoveConstants.CATEGORY_CONTACT) == 0) return;

        if (mob.getEntity().getHealth() <= 0)
        {
            final Explosion boom = new Explosion(move.attacked.getLevel(), move.attacked, move.attacked.getX(),
                    move.attacked.getY(), move.attacked.getZ(), 0, false, BlockInteraction.BREAK);
            final ExplosionEvent evt = new ExplosionEvent.Start(move.attacked.getLevel(), boom);
            MinecraftForge.EVENT_BUS.post(evt);
            if (!evt.isCanceled())
            {
                final LivingEntity attacker = move.attacker.getEntity();
                final float hp = attacker.getHealth();
                attacker.hurt(DamageSource.MAGIC, hp / 4);
            }
        }
    }
}
