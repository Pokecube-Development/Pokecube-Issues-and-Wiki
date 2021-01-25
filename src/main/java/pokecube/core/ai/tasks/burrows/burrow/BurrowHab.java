package pokecube.core.ai.tasks.burrows.burrow;

import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.world.IWorldTickListener;

public class BurrowHab implements IInhabitable, INBTSerializable<CompoundNBT>, IWorldTickListener
{
    private PokedexEntry maker;

    Predicate<PokedexEntry> valid;

    public PokedexEntry getMaker()
    {
        return this.maker;
    }

    private void addRelations(final PokedexEntry parent, final Set<PokedexEntry> related)
    {
        if (related.contains(parent)) return;
        related.add(parent);
        if (!related.contains(parent.getChild())) related.add(parent.getChild());
        for (final PokedexEntry[] arr : parent.childNumbers.values())
            for (final PokedexEntry e : arr)
                this.addRelations(e, related);
        for (final EvolutionData d : parent.evolutions)
            this.addRelations(d.evolution, related);
    }

    public void setMaker(final PokedexEntry maker)
    {
        this.maker = maker;
        final Set<PokedexEntry> related = Sets.newHashSet();
        this.addRelations(maker, related);
        this.valid = e ->
        {
            return related.contains(e);
        };

    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putString("maker", this.maker.getTrimmedName());
        return nbt;
    }

    @Override
    public void onTickEnd(final ServerWorld world)
    {
        // TODO Auto-generated method stub
        IWorldTickListener.super.onTickEnd(world);
    }

    @Override
    public void onBroken(final ServerWorld world)
    {
        // TODO Auto-generated method stub
        IInhabitable.super.onBroken(world);
    }

    @Override
    public void onTick(final ServerWorld world)
    {
        // TODO Auto-generated method stub
        IInhabitable.super.onTick(world);
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        this.setMaker(Database.getEntry(nbt.getString("maker")));

    }

    @Override
    public void onExitHabitat(final MobEntity mob)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onEnterHabitat(final MobEntity mob)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canEnterHabitat(final MobEntity mob)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return false;
        return this.valid.test(pokemob.getPokedexEntry());
    }

}
