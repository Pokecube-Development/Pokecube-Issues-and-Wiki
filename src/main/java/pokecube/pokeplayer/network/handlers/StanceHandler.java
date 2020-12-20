package pokecube.pokeplayer.network.handlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.network.PacketTransform;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.world.mobs.data.PacketDataSync;

public class StanceHandler extends pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler
{
    public static final byte BUTTONTOGGLESIT = 2;
    public static final byte SELFINTERACT    = -2;
    public static final byte SYNCUPDATE      = -3;

    boolean                  state;
    byte                     key;

    public StanceHandler()
    {
    }

    public StanceHandler(Boolean state, Byte key)
    {
        this.state = state;
        this.key = key;
    }

    @Override
    public void handleCommand(IPokemob pokemob) throws Exception
    {
    	super.handleCommand(pokemob);
        // Start by handling the default stance messages.
        pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler defaults = new pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler(
                this.state, this.key);
        defaults.handleCommand(pokemob);

        // Handle pokeplayer specific things.
        if (pokemob.getEntity().getPersistentData().getBoolean("is_a_player"))
        {
            Entity entity = pokemob.getEntity().getEntityWorld().getEntityByID(pokemob.getEntity().getEntityId());
            if (entity instanceof PlayerEntity)
            {
                PlayerEntity player = (PlayerEntity) entity;

                if (key == SELFINTERACT)
                {
                    EntityInteractSpecific evt = new EntityInteractSpecific(player, Hand.MAIN_HAND,
                            pokemob.getEntity(), new Vector3d(0, 0, 0));

                    // Apply interaction, also do not allow saddle.
                    ItemStack saddle = pokemob.getInventory().getStackInSlot(0);
                    if (!saddle.isEmpty()) pokemob.getInventory().setInventorySlotContents(0, ItemStack.EMPTY);
                    PokecubeCore.MOVE_BUS.post(evt);
                    if (!saddle.isEmpty()) pokemob.getInventory().setInventorySlotContents(0, saddle);

                    PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
                    info.save(player);
                }
                else if (key == SYNCUPDATE)
                {
                    PacketDataSync.sync((ServerPlayerEntity) player, pokemob.dataSync(), player.getEntityId(), true);
                }
                else if (key == BUTTONTOGGLESIT)
                {
                	PacketTransform packet = new PacketTransform();
                    packet.id = player.getEntityId();
                    packet.getTag().putBoolean("U", true);
                    packet.getTag().putBoolean("S", pokemob.getLogicState(LogicStates.SITTING));
                    PacketTransform.sendPacket(player, (ServerPlayerEntity) player);
                }
            }
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeBoolean(state);
        buf.writeByte(key);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        state = buf.readBoolean();
        key = buf.readByte();
    }

}
