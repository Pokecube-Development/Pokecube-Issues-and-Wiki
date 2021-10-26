package thut.core.common.mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import thut.api.entity.IMobColourable;

public class DefaultColourable implements IMobColourable, INBTSerializable<CompoundTag>
{
    public static final Capability<IMobColourable> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    int[] RGBA   = { 255, 255, 255, 255 };
    int   colour = 0;

    @Override
    public int getDyeColour()
    {
        return this.colour;
    }

    @Override
    public int[] getRGBA()
    {
        return this.RGBA;
    }

    @Override
    public void setDyeColour(final int colour)
    {
        this.colour = colour;
    }

    @Override
    public void setRGBA(final int... colours)
    {
        assert this.RGBA.length == colours.length;
        this.RGBA = colours;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("c", this.getDyeColour());
        tag.putIntArray("rgba", this.getRGBA());
        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundTag tag)
    {
        if (tag.contains("c")) this.setDyeColour(tag.getInt("c"));
        if (tag.contains("rgba")) this.setRGBA(tag.getIntArray("rgba"));
    }

}
