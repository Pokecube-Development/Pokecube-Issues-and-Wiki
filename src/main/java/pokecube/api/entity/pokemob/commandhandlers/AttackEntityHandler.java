package pokecube.api.entity.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.combat.CommandAttackEvent;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import thut.lib.TComponent;

public class AttackEntityHandler extends DefaultHandler
{
    public int targetId;

    public AttackEntityHandler()
    {}

    public AttackEntityHandler(final Integer targetId)
    {
        this.targetId = targetId;
    }

    @Override
    public void handleCommand(final IPokemob pokemob)
    {
        final Level world = pokemob.getEntity().getLevel();
        final Entity target = PokecubeAPI.getEntityProvider().getEntity(world, this.targetId, true);
        if (!(target instanceof LivingEntity living))
        {
            if (PokecubeCore.getConfig().debug_commands) PokecubeAPI.LOGGER.error("Invalid Target!",
                    new IllegalArgumentException(pokemob.getEntity() + " " + target));
            return;
        }
        final int currentMove = pokemob.getMoveIndex();
        final CommandAttackEvent event = new CommandAttackEvent(pokemob.getEntity(), target);
        PokecubeAPI.POKEMOB_BUS.post(event);
        if (!event.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            final MoveEntry move = MovesUtils.getMove(pokemob.getMoves()[currentMove]);
            MoveApplication toApply = new MoveApplication(move, pokemob, null);
            final boolean self = MoveApplicationRegistry.getValidator(move).test(toApply);
            if (self) pokemob.executeMove(pokemob.getEntity(), null, 0);
            else
            {
                final Component mess = TComponent.translatable("pokemob.command.attack", pokemob.getDisplayName(),
                        target.getDisplayName(),
                        TComponent.translatable(MovesUtils.getUnlocalizedMove(move.getName())));
                if (this.fromOwner()) pokemob.displayMessageToOwner(mess);
                BrainUtils.initiateCombat(pokemob.getEntity(), living);
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
