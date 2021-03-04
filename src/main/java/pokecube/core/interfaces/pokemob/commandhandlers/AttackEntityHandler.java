package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.events.pokemob.combat.CommandAttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import pokecube.core.utils.EntityTools;

public class AttackEntityHandler extends DefaultHandler
{
    public int targetId;

    public AttackEntityHandler()
    {
    }

    public AttackEntityHandler(final Integer targetId)
    {
        this.targetId = targetId;
    }

    @Override
    public void handleCommand(final IPokemob pokemob)
    {
        final World world = pokemob.getEntity().getEntityWorld();
        final Entity target = PokecubeCore.getEntityProvider().getEntity(world, this.targetId, true);
        final LivingEntity living = EntityTools.getCoreLiving(target);
        System.out.println(target + " " + living);
        if (target == null || !(target instanceof LivingEntity))
        {
            if (PokecubeMod.debug) if (target == null) PokecubeCore.LOGGER.error("Target Mob cannot be null!",
                    new IllegalArgumentException(pokemob.getEntity().toString()));
            else PokecubeCore.LOGGER.error("Invalid Target!", new IllegalArgumentException(pokemob.getEntity() + " "
                    + target));
            return;
        }
        final int currentMove = pokemob.getMoveIndex();
        final CommandAttackEvent event = new CommandAttackEvent(pokemob.getEntity(), target);
        PokecubeCore.POKEMOB_BUS.post(event);
        if (!event.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            final Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            if (move.isSelfMove()) pokemob.executeMove(pokemob.getEntity(), null, 0);
            else
            {
                final ITextComponent mess = new TranslationTextComponent("pokemob.command.attack", pokemob
                        .getDisplayName(), target.getDisplayName(), new TranslationTextComponent(MovesUtils
                                .getUnlocalizedMove(move.getName())));
                if (this.fromOwner()) pokemob.displayMessageToOwner(mess);
                BrainUtils.initiateCombat(pokemob.getEntity(), (LivingEntity) target);
            }
        }
    }

    @Override
    public void readFromBuf(final ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.targetId = buf.readInt();
    }

    @Override
    public void writeToBuf(final ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeInt(this.targetId);
    }
}
