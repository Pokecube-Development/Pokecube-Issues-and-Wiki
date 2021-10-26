package thut.core.common.mobs;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import thut.api.entity.IMobColourable;

public class DefaultColourable implements IMobColourable
{
    @CapabilityInject(IMobColourable.class)
    public static final Capability<IMobColourable> CAPABILITY = null;

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
    public void setDyeColour(int colour)
    {
        this.colour = colour;
    }

    @Override
    public void setRGBA(int... colours)
    {
        assert this.RGBA.length == colours.length;
        this.RGBA = colours;
    }

}
