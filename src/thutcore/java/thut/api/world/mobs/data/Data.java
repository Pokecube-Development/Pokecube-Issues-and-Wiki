package thut.api.world.mobs.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;

public interface Data<T>
{
    void setSync(DataSync sync);

    String getTag();

    Data<T> setTag(String tag);

    String getName();

	void setHolderLookup(HolderLookup.Provider provider);
		
    boolean dirty();

    T get();

    int getID();

    int getUID();

    void read(ByteBuf buf);

    Data<T> set(T value);

    void setDirty(boolean dirty);

    void setID(int id);

    void setUID(int id);

    void write(ByteBuf buf);

    boolean isRealtime();

    default Data<T> setRealtime()
    {
        return setRealtime(true);
    }

    Data<T> setRealtime(boolean realtime);

    void setRaw(Object value);
}
