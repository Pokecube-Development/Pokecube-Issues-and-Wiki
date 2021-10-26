package pokecube.mobs.abilities.a;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class Aftermath extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob != move.attacked || move.pre || move.attacker == move.attacked) return;
        final Move_Base attack = move.getMove();
        if (attack == null || (attack.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) == 0) return;

        if (mob.getEntity().getHealth() <= 0)
        {
            final Explosion boom = new Explosion(move.attacked.getCommandSenderWorld(), move.attacked, move.attacked.getX(),
                    move.attacked.getY(), move.attacked.getZ(), 0, false, BlockInteraction.BREAK);
            final ExplosionEvent evt = new ExplosionEvent.Start(move.attacked.getCommandSenderWorld(), boom);
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
