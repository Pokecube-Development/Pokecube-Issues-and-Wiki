package pokecube.core.ai.tasks.burrows.burrow;

import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
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
    public static BurrowHab makeFor(final IPokemob pokemob, final BlockPos pos)
    {
        final BurrowHab hab = new BurrowHab();
        hab.setMaker(pokemob.getPokedexEntry());
        hab.setPos(pos);
        return hab;
    }

    private PokedexEntry maker;

    public Room burrow;

    Predicate<PokedexEntry> valid;

    public PokedexEntry getMaker()
    {
        return this.maker;
    }

    private void addRelations(final PokedexEntry parent, final Set<PokedexEntry> related)
    {
        if (related.contains(parent)) return;
        related.add(parent);
        if (!related.contains(parent.getChild())) this.addRelations(parent.getChild(), related);
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
    public void setPos(final BlockPos pos)
    {
        if (this.burrow == null)
        {
            // In this case, this is called from the very initial setting of the
            // burrow, so we will try to make a burrow, and then adjust the
            // location so that the nest is on the floor of the burrow made.

            final Set<PokedexEntry> related = Sets.newHashSet();
            this.addRelations(this.maker, related);

            // Pick a room size based on the biggest pokemob in the related
            // list.
            final float height = related.stream().max((o1, o2) -> Float.compare(o1.height, o2.height)).get().height;
            final float size = height + 1;
            final float direction = new Random().nextInt(360);
            this.burrow = new Room(direction, size);
            this.burrow.setCenter(pos.down((int) (size + 2)), size, direction);
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putString("maker", this.maker.getTrimmedName());
        nbt.put("burrow", this.burrow.serializeNBT());
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
        this.burrow = new Room();
        this.burrow.deserializeNBT(nbt.getCompound("burrow"));
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
