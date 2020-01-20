package thut.core.common.genetics;

import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

public class DefaultGeneStorage implements Capability.IStorage<IMobGenetics>
{

    @Override
    public void readNBT(final Capability<IMobGenetics> capability, final IMobGenetics instance, final Direction side,
            final INBT nbt)
    {
        final ListNBT list = (ListNBT) nbt;
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundNBT tag = list.getCompound(i);
            final Alleles alleles = new Alleles();
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
    public INBT writeNBT(final Capability<IMobGenetics> capability, final IMobGenetics instance, final Direction side)
    {
        final ListNBT genes = new ListNBT();
        for (final Map.Entry<ResourceLocation, Alleles> entry : instance.getAlleles().entrySet())
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putString("K", entry.getKey().toString());
            tag.put("V", entry.getValue().save());
            genes.add(tag);
        }
        return genes;
    }
}
