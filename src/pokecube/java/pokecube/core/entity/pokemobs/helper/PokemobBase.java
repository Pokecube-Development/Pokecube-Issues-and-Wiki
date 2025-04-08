package pokecube.core.entity.pokemobs.helper;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.impl.capabilities.DefaultPokemob;
import thut.api.entity.IMobColourable;

public abstract class PokemobBase extends TamableAnimal
        implements FlyingAnimal, IMobColourable, InventoryCarrier, IEntityWithComplexSpawn
{
    public final DefaultPokemob pokemobCap;

    public PokemobBase(final EntityType<? extends TamableAnimal> type, final Level worldIn)
    {
        super(type, worldIn);
        IPokemob pokemob = PokemobCaps.getPokemobFor(this);
        if (!(pokemob instanceof DefaultPokemob poke))
        {
            // Internally this sets the data
            this.pokemobCap = new DefaultPokemob(this);
            Thread.dumpStack();
        }
        else
        {
            this.pokemobCap = poke;
        }
        this.dimensions = EntityDimensions.fixed(pokemobCap.getPokedexEntry().width, pokemobCap.getPokedexEntry().height);
    }

    @Override
    public SimpleContainer getInventory()
    {
        return pokemobCap.getInventory();
    }

    @SuppressWarnings("removal")
    @Override
    public CompoundTag serializeNBT(Provider provider)
    {
        // TODO Auto-generated method stub
        return super.serializeNBT(provider);
    }

    @SuppressWarnings("removal")
    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt)
    {
        // TODO Auto-generated method stub
        super.deserializeNBT(provider, nbt);
    }
}