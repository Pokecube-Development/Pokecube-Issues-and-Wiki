package thut.api.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface IMobColourable extends INBTSerializable<CompoundTag>
{
    /**
     * These are specific changes for when dye is used on the mob.
     *
     * @return
     */
    int getDyeColour();

    /**
     * These are global colour changes.
     *
     * @param colours
     */
    int[] getRGBA();

    void setDyeColour(int colour);

    void setRGBA(int... colours);

    @Override
    default CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("c", this.getDyeColour());
        tag.putIntArray("rgba", this.getRGBA());
        return tag;
    }

    @Override
    default void deserializeNBT(HolderLookup.Provider provider, final CompoundTag tag)
    {
        if (tag.contains("c")) this.setDyeColour(tag.getInt("c"));
        if (tag.contains("rgba")) this.setRGBA(tag.getIntArray("rgba"));
    }
}
