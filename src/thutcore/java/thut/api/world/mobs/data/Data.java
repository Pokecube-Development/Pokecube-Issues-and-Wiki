package thut.api.world.mobs.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;

public interface Data<T>
{
	void setHolderLookup(HolderLookup.Provider provider);
		
    boolean dirty();

    T get();

    int getID();

    int getUID();

    void read(ByteBuf buf);

    void set(T value);

    void setDirty(boolean dirty);

    void setID(int id);

    void setUID(int id);

    void write(ByteBuf buf);

    boolean isRealtime();

    Data<T> setRealtime();
}
