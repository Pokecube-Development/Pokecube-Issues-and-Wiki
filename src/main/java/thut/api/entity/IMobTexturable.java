package thut.api.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public interface IMobTexturable
{

    public static class Defaults implements IMobTexturable
    {

    }

    public static class Storage implements Capability.IStorage<IMobTexturable>
    {

        @Override
        public void readNBT(Capability<IMobTexturable> capability, IMobTexturable instance, Direction side, INBT nbt)
        {
        }

        @Override
        public INBT writeNBT(Capability<IMobTexturable> capability, IMobTexturable instance, Direction side)
        {
            return null;
        }

    }

    default LivingEntity getEntity()
    {
        return null;
    }

    default String getModId()
    {
        return null;
    }

    default ResourceLocation getTexture(@Nullable String part)
    {
        return new ResourceLocation(this.getModId(), part);
    }

    default List<String> getTextureStates()
    {
        return null;
    }

    default ResourceLocation preApply(ResourceLocation in)
    {
        return in;
    }

}
