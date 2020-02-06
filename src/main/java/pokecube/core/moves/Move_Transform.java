/**
 *
 */
package pokecube.core.moves;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.entity.IBreedingMob;

/** @author Manchou */
public class Move_Transform extends Move_Basic
{

    public static class Animation implements IMoveAnimation
    {
        @Override
        public void clientAnimation(final MatrixStack mat, final IRenderTypeBuffer buffer, final MovePacketInfo info,
                final float partialTick)
        {
        }

        @Override
        public int getApplicationTick()
        {
            return 0;
        }

        @Override
        public int getDuration()
        {
            return 0;
        }

        @Override
        public void reallyInitRGBA()
        {

        }

        @Override
        public void setDuration(final int arg0)
        {

        }

        @Override
        public void spawnClientEntities(final MovePacketInfo info)
        {

        }

    }

    /**
     * @param name
     * @param type
     * @param PWR
     * @param PRE
     * @param PP
     * @param attackCategory
     */
    public Move_Transform(final String name)
    {
        super(name);
        this.setAnimation(new Animation());
        this.setSelf();
        this.setNotInterceptable();

    }

    @Override
    public void attack(final IPokemob attacker, final Entity attacked)
    {
        if (attacker.getTransformedTo() == null && attacked instanceof LivingEntity)
        {
            if (MovesUtils.contactAttack(attacker, attacked))
            {
                if (attacked instanceof IPokemob) if (attacked instanceof IPokemob)
                    if (!(attacked instanceof IBreedingMob) || attacked != ((IBreedingMob) attacker).getLover())
                        ((MobEntity) attacked).setAttackTarget((LivingEntity) attacker);
                attacker.setTransformedTo(attacked);
            }
        }
        else if (attacked instanceof IPokemob)
        {
            final String move = ((IPokemob) attacked).getMove(0);
            if (move != null && !IMoveNames.MOVE_TRANSFORM.equals(move)) MovesUtils.doAttack(move, attacker, attacked);
            else if (MovesUtils.contactAttack(attacker, attacked)) MovesUtils.displayEfficiencyMessages(attacker,
                    attacked, 0F, 1F);
        }
        else if (attacked instanceof PlayerEntity) if (MovesUtils.contactAttack(attacker, attacked))
        {
            // TODO see if this needs tweaking.
            final MovePacket packet = new MovePacket(attacker, attacked, this.name, this.getType(attacker), 25, 1,
                    IMoveConstants.STATUS_NON, IMoveConstants.CHANGE_NONE);
            this.onAttack(packet);
        }
    }
}
