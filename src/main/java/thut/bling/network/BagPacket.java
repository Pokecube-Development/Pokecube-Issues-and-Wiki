package thut.bling.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import thut.wearables.network.Packet;

public class BagPacket extends Packet
{
    public static final byte SETPAGE = 0;
    public static final byte RENAME  = 1;
    public static final byte ONOPEN  = 2;
    public static final byte OPEN    = 3;

    public static void OpenBag(final PlayerEntity playerIn)
    {
        // TODO Auto-generated method stub

    }

    byte               message;
    public CompoundNBT data = new CompoundNBT();

    public BagPacket(final PacketBuffer buffer)
    {
        this.message = buffer.readByte();
        this.data = buffer.readCompoundTag();
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeByte(this.message);
        buffer.writeCompoundTag(this.data);
    }

}
