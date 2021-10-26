package thut.core.common.mobs;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import thut.api.entity.IMobColourable;

public class DefaultColourableStorage implements Capability.IStorage<IMobColourable>
{

    @Override
    public void readNBT(Capability<IMobColourable> capability, IMobColourable instance, Direction side, INBT nbt)
    {
        final CompoundNBT tag = (CompoundNBT) nbt;
        if (tag.contains("c")) instance.setDyeColour(tag.getInt("c"));
        if (tag.contains("rgba")) instance.setRGBA(tag.getIntArray("rgba"));
    }

    @Override
    public INBT writeNBT(Capability<IMobColourable> capability, IMobColourable instance, Direction side)
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putInt("c", instance.getDyeColour());
        tag.putIntArray("rgba", instance.getRGBA());
        return tag;
    }

}
