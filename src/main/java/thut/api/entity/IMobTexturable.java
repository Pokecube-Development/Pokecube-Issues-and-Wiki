package thut.api.entity;

import java.util.Collections;
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
        @Override
        public LivingEntity getEntity()
        {
            return null;
        }
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

    LivingEntity getEntity();

    default String getModId()
    {
        return "minecraft";
    }

    default String getForm()
    {
        return "";
    }

    default int getRandomSeed()
    {
        return 0;
    }

    default ResourceLocation getTexture(@Nullable final String part)
    {
        if (part == null) return new ResourceLocation("");
        return new ResourceLocation(this.getModId(), part);
    }

    default List<String> getTextureStates()
    {
        return Collections.emptyList();
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
