package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class StanceHandler extends DefaultHandler
{
    public static final byte STAY   = 0;
    public static final byte GUARD  = 1;
    public static final byte SIT    = 2;
    public static final byte GZMOVE = 3;

    boolean state;
    byte    key;

    public StanceHandler()
    {
    }

    public StanceHandler(final Boolean state, final Byte key)
    {
        this.state = state;
        this.key = key;
    }

    @Override
    public void handleCommand(final IPokemob pokemob) throws Exception
    {
        switch (this.key)
        {
        case STAY:
            boolean stay;
            pokemob.setGeneralState(GeneralStates.STAYING, stay = !pokemob.getGeneralState(GeneralStates.STAYING));
            final IGuardAICapability guard = pokemob.getEntity().getCapability(CapHolders.GUARDAI_CAP, null)
                    .orElse(null);
            if (stay)
            {
                final Vector3 mid = Vector3.getNewVector().set(pokemob.getEntity());
                if (guard != null)
                {
                    guard.getPrimaryTask().setActiveTime(TimePeriod.fullDay);
                    guard.getPrimaryTask().setPos(mid.getPos());
                }
            }
            else if (guard != null) guard.getPrimaryTask().setActiveTime(TimePeriod.never);
            break;
        case GUARD:
            if (PokecubeCore.getConfig().guardModeEnabled) pokemob.setCombatState(CombatStates.GUARDING, !pokemob
                    .getCombatState(CombatStates.GUARDING));
            else pokemob.displayMessageToOwner(new TranslationTextComponent("pokecube.config.guarddisabled"));
            break;
        case SIT:
            pokemob.setLogicState(LogicStates.SITTING, !pokemob.getLogicState(LogicStates.SITTING));
            break;
        case GZMOVE:
            pokemob.setCombatState(CombatStates.USINGGZMOVE, !pokemob.getCombatState(CombatStates.USINGGZMOVE));
            pokemob.displayMessageToOwner(new TranslationTextComponent("pokecube.gzmode." + (pokemob.getCombatState(
                    CombatStates.USINGGZMOVE) ? "set" : "unset")));
            break;
        }
    }

    @Override
    public void readFromBuf(final ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.state = buf.readBoolean();
        this.key = buf.readByte();
    }

    @Override
    public void writeToBuf(final ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeBoolean(this.state);
        buf.writeByte(this.key);
    }

}
