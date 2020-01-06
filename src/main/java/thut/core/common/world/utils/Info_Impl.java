package thut.core.common.world.utils;

import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import thut.api.world.utils.Info;

public class Info_Impl implements Info
{
    private static final long serialVersionUID = 4947657861031977837L;

    CompoundNBT nbt = new CompoundNBT();

    @Override
    public void deserialize(String value)
    {
        try
        {
            this.nbt = JsonToNBT.getTagFromJson(value);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String serialize()
    {
        return this.nbt.toString();
    }

    @Override
    public <T> void set(String key, T value)
    {
        final Class<?> type = value.getClass();
        if (type == UUID.class) this.nbt.putUniqueId(key, (UUID) value);
        switch (type.getName())
        {
        case "C":
            return;
        // case "Z":
        // nbt.putBoolean(key, (boolean) value);
        // return;
        // case "B":
        // nbt.setByte(key, (byte) value);
        // return;
        // case "[B":
        // nbt.putByteArray(key, (byte[]) value);
        // return;
        // case "D":
        // nbt.putDouble(key, (double) value);
        // return;
        // case "F":
        // nbt.putFloat(key, (float) value);
        // return;
        // case "I":
        // nbt.putInt(key, (int) value);
        // return;
        // case "[I":
        // nbt.putIntArray(key, (int[]) value);
        // return;
        // case "J":
        // nbt.putLong(key, (long) value);
        // return;
        // case "S":
        // nbt.setShort(key, (short) value);
        // return;
        default:
            if (type == CompoundNBT.class)
            {
                this.nbt.put(key, (INBT) value);
                return;
            }
            else if (type == ListNBT.class)
            {
                this.nbt.put(key, (INBT) value);
                return;
            }
            else if (type == String.class)
            {
                this.nbt.putString(key, (String) value);
                return;
            }
            break;
        }
    }

    @Override
    public <T> T value(String key, Class<T> type)
    {
        if (type == UUID.class && this.nbt.hasUniqueId(key)) return type.cast(this.nbt.getUniqueId(key));
        if (!this.nbt.contains(key)) return null;
        switch (type.getName())
        {
        case "C":
            return null;
        // case "Z":
        // return type.cast(nbt.getBoolean(key));
        // case "B":
        // return type.cast(nbt.getByte(key));
        // case "[B":
        // return type.cast(nbt.getByteArray(key));
        // case "D":
        // return type.cast(nbt.getDouble(key));
        // case "F":
        // return type.cast(nbt.getFloat(key));
        // case "I":
        // return type.cast(nbt.getInt(key));
        // case "[I":
        // return type.cast(nbt.getIntArray(key));
        // case "J":
        // return type.cast(nbt.getLong(key));
        // case "S":
        // return type.cast(nbt.getShort(key));
        default:
            if (type == CompoundNBT.class) return type.cast(this.nbt.getCompound(key));
            else if (type == ListNBT.class) return type.cast(this.nbt.get(key));
            else if (type == String.class) return type.cast(this.nbt.getString(key));
            break;
        }
        return null;
    }

}
