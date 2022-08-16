package pokecube.api.entity.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.combat.CommandAttackEvent;
import pokecube.api.moves.Move_Base;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import thut.lib.TComponent;

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
        final Level world = pokemob.getEntity().getLevel();
        final Entity target = PokecubeAPI.getEntityProvider().getEntity(world, this.targetId, true);
        if (target == null || !(target instanceof LivingEntity))
        {
            if (PokecubeMod.debug) if (target == null) PokecubeAPI.LOGGER.error("Target Mob cannot be null!",
                    new IllegalArgumentException(pokemob.getEntity().toString()));
            else PokecubeAPI.LOGGER.error("Invalid Target!", new IllegalArgumentException(pokemob.getEntity() + " "
                    + target));
            return;
        }
        final int currentMove = pokemob.getMoveIndex();
        final CommandAttackEvent event = new CommandAttackEvent(pokemob.getEntity(), target);
        PokecubeAPI.POKEMOB_BUS.post(event);
        if (!event.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            final Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            if (move.isSelfMove()) pokemob.executeMove(pokemob.getEntity(), null, 0);
            else
            {
                final Component mess = TComponent.translatable("pokemob.command.attack", pokemob
                        .getDisplayName(), target.getDisplayName(), TComponent.translatable(MovesUtils
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
