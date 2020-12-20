package pokecube.pokeplayer.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.pokeplayer.PokeInfo;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.NBTPacket;
import thut.core.common.network.PacketAssembly;

public class PacketTransform extends NBTPacket
{
    public int            id;
    
    public static final PacketAssembly<PacketTransform> ASSEMBLY = PacketAssembly.registerAssembler(
    		PacketTransform.class, PacketTransform::new, PokecubeCore.packets);

    public static void sendPacket(PlayerEntity toSend, ServerPlayerEntity sendTo)
    {
    	PacketTransform.ASSEMBLY.sendTo(getPacket(toSend), sendTo);
    }

    public static PacketTransform getPacket(PlayerEntity toSend)
    {
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(toSend).getData(PokeInfo.class);
        PacketTransform message = new PacketTransform();
        info.writeToNBT(message.getTag());
        message.id = toSend.getEntityId();
        return message;
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

//    public void fromBytes(ByteBuf buf) throws IOException
//    {
//        id = buf.readInt();
//        new PacketBuffer(buf).readCompoundTag();
//    }
//
//
//    public void toBytes(ByteBuf buf)
//    {
//        buf.writeInt(id);
//        new PacketBuffer(buf).writeCompoundTag(this.getTag());
//    }

    public PacketTransform() {
    	super();
    }
    
    public PacketTransform(final CompoundNBT tag) {
    	super();
    	this.tag = tag;
    }
    
    public PacketTransform(final PacketBuffer buffer) {
    	super(buffer);
    }
    
    @Override
    protected void onCompleteClient()
    {
        World world = PokecubeCore.proxy.getWorld();
        Entity e = PokecubeCore.getEntityProvider().getEntity(world, this.id, false);
        if (this.getTag().contains("U"))
        {
            PlayerEntity player = PokecubeCore.proxy.getPlayer();
            if (this.getTag().contains("H"))
            {
                PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
                IPokemob pokemob = info.getPokemob(world);
                if (pokemob == null) { return; }
                float health = this.getTag().getFloat("H");
                if (pokemob.getEntity() == null) return;
                //float max = message.getTag().getFloat("M");
                //pokemob.getEntity().getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(max);
                pokemob.setHealth(health);
                player.setHealth(health);
            }
            else if (this.getTag().contains("S"))
            {
                PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
                IPokemob pokemob = info.getPokemob(world);
                if (pokemob == null) { return; }
                pokemob.setLogicState(LogicStates.SITTING, this.getTag().getBoolean("S"));
            }
            return;
        }
        if (e instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) e;
            PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
            info.clear();
            info.readFromNBT(this.getTag());
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
