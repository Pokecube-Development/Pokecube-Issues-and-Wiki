package thut.core.common.mobs;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import thut.api.entity.IMobColourable;

public class DefaultColourableStorage implements Capability.IStorage<IMobColourable>
{

    @Override
    public void readNBT(Capability<IMobColourable> capability, IMobColourable instance, Direction side, Tag nbt)
    {
        final CompoundTag tag = (CompoundTag) nbt;
        if (tag.contains("c")) instance.setDyeColour(tag.getInt("c"));
        if (tag.contains("rgba")) instance.setRGBA(tag.getIntArray("rgba"));
    }

    @Override
    public Tag writeNBT(Capability<IMobColourable> capability, IMobColourable instance, Direction side)
    {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("c", instance.getDyeColour());
        tag.putIntArray("rgba", instance.getRGBA());
        return tag;
    }

}
