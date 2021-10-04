package thut.core.common.genetics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

public class DefaultGeneStorage implements Capability.IStorage<IMobGenetics>
{

    @Override
    public void readNBT(final Capability<IMobGenetics> capability, final IMobGenetics instance, final Direction side,
            final Tag nbt)
    {
        final ListTag list = (ListTag) nbt;
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundTag tag = list.getCompound(i);
            final Alleles<?, ?> alleles = new Alleles<>();
            final ResourceLocation key = new ResourceLocation(tag.getString("K"));
            try
            {
                alleles.load(tag.getCompound("V"));
                instance.getAlleles().put(key, alleles);
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Error loading gene for key: " + key, e);
            }
        }
    }

    @Override
    public Tag writeNBT(final Capability<IMobGenetics> capability, final IMobGenetics instance, final Direction side)
    {
        final ListTag genes = new ListTag();

        final List<ResourceLocation> keys = Lists.newArrayList(instance.getKeys());
        Collections.sort(keys);

        for (final ResourceLocation key : keys)
        {
            final CompoundTag tag = new CompoundTag();
            final Alleles<?, ?> gene = instance.getAlleles(key);
            tag.putString("K", key.toString());
            tag.put("V", gene.save());
            genes.add(tag);
        }
        return genes;
    }
}
