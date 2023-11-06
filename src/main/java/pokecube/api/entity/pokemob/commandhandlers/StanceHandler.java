package pokecube.api.entity.pokemob.commandhandlers;

import java.util.Map;
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

public class StanceHandler extends DefaultHandler
{
    public static final byte STAY = 0;
    public static final byte GUARD = 1;
    public static final byte SIT = 2;
    public static final byte MODE = 3;

    public static record ModeInfo(IPokemob pokemob, boolean state, byte mode)
    {
    }

    public static Map<Byte, Consumer<ModeInfo>> MODE_LISTENERS = new Byte2ObjectArrayMap<>();

    boolean state;
    byte key;

    public StanceHandler()
    {}

    public StanceHandler(final Boolean state, final Byte key)
    {
        this.state = state;
        this.key = key;
    }

    @Override
    public void handleCommand(final IPokemob pokemob) throws Exception
    {
        boolean stay = pokemob.getGeneralState(GeneralStates.STAYING);
        final IGuardAICapability guard = CapHolders.getGuardAI(pokemob.getEntity());
        switch (this.key)
        {
        case STAY:
            pokemob.setGeneralState(GeneralStates.STAYING, stay = !pokemob.getGeneralState(GeneralStates.STAYING));
            break;
        case GUARD:
            if (PokecubeCore.getConfig().guardModeEnabled)
                pokemob.setCombatState(CombatStates.GUARDING, !pokemob.getCombatState(CombatStates.GUARDING));
            else pokemob.displayMessageToOwner(TComponent.translatable("pokecube.config.guarddisabled"));
            break;
        case SIT:
            pokemob.setLogicState(LogicStates.SITTING, !pokemob.getLogicState(LogicStates.SITTING));
            break;
        default:
            ModeInfo info = new ModeInfo(pokemob, this.state, this.key);
            MODE_LISTENERS.getOrDefault(this.key, m -> {}).accept(info);
            break;
        }
        if (stay)
        {
            final Vector3 mid = new Vector3().set(pokemob.getEntity());
            if (guard != null)
            {
                guard.getPrimaryTask().setActiveTime(TimePeriod.fullDay);
                guard.getPrimaryTask().setPos(mid.getPos());
            }
        }
        else if (guard != null) guard.getPrimaryTask().setActiveTime(TimePeriod.never);
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
