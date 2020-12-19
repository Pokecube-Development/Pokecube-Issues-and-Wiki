package pokecube.pokeplayer.network;

import java.io.IOException;
import javax.xml.ws.handler.MessageContext;

import com.minecolonies.api.network.IMessage;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.pokeplayer.PokeInfo;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.Packet;

public class PacketTransform
{
    public CompoundNBT data = new CompoundNBT();
    public int            id;

    public static void sendPacket(Packet packet, PlayerEntity toSend, ServerPlayerEntity sendTo)
    {
        PokecubeCore.packets.sendTo(packet, sendTo);
    }

    public static PacketTransform getPacket(Packet packet, PlayerEntity toSend)
    {
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(toSend).getData(PokeInfo.class);
        PacketTransform message = new PacketTransform();
        info.writeToNBT(message.data);
        message.id = toSend.getEntityId();
        return message;
    }

    public PacketTransform()
    {
    }

//    @Override
//    public IMessage onMessage(final PacketTransform message, final MessageContext ctx)
//    {
//        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                apply(message, ctx);
//            }
//        });
//        return null;
//    }

    public void fromBytes(ByteBuf buf) throws IOException
    {
        id = buf.readInt();
        data = new PacketBuffer(buf).readCompoundTag();
    }


    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(id);
        new PacketBuffer(buf).writeCompoundTag(data);
    }

    static void apply(PacketTransform message, MessageContext ctx)
    {
        World world = PokecubeCore.proxy.getWorld();
        Entity e = PokecubeCore.getEntityProvider().getEntity(world, message.id, false);
        if (message.data.contains("U"))
        {
            PlayerEntity player = PokecubeCore.proxy.getPlayer();
            if (message.data.contains("H"))
            {
                PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
                IPokemob pokemob = info.getPokemob(world);
                if (pokemob == null) { return; }
                float health = message.data.getFloat("H");
                if (pokemob.getEntity() == null) return;
                float max = message.data.getFloat("M");
                //pokemob.getEntity().getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(max);
                pokemob.setHealth(health);
                player.setHealth(health);
            }
            else if (message.data.contains("S"))
            {
                PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
                IPokemob pokemob = info.getPokemob(world);
                if (pokemob == null) { return; }
                pokemob.setLogicState(LogicStates.SITTING, message.data.getBoolean("S"));
            }
            return;
        }
        if (e instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) e;
            PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
            info.clear();
            info.readFromNBT(message.data);
            IPokemob pokemob = info.getPokemob(world);
            if (pokemob != null)
            {
                info.set(pokemob, player);
                // Callback to let server know to update us.
                pokemob.getEntity().setEntityId(player.getEntityId());
                PacketCommand.sendCommand(pokemob, Command.STANCE, new StanceHandler(true, (byte) -3));
            }
            else
            {
                info.resetPlayer(player);
            }
        }
    }

}
