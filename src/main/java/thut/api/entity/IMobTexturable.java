package thut.api.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import thut.core.client.render.animation.AnimationXML.Phase;

public interface IMobTexturable
{

    public static class Defaults implements IMobTexturable
    {

    }

    public static class Storage implements Capability.IStorage<IMobTexturable>
    {

        @Override
        public void readNBT(final Capability<IMobTexturable> capability, final IMobTexturable instance,
                final Direction side, final INBT nbt)
        {
        }

        @Override
        public INBT writeNBT(final Capability<IMobTexturable> capability, final IMobTexturable instance,
                final Direction side)
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

    default ResourceLocation getTexture(@Nullable final String part)
    {
        return new ResourceLocation(this.getModId(), part);
    }

    default List<String> getTextureStates()
    {
        return null;
    }

    default ResourceLocation preApply(final ResourceLocation in)
    {
        return in;
    }

    default void applyTexturePhase(final Phase phase)
    {
        // Most things don't care about this, pokemobs do, they use it.
    }

}
