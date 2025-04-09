package pokecube.core.entity.pokemobs.helper;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.Nullable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.impl.capabilities.DefaultPokemob;

import java.util.function.Supplier;

public abstract class PokemobBase extends TamableAnimal
        implements FlyingAnimal, InventoryCarrier, IEntityWithComplexSpawn
{
    private DefaultPokemob pokemobCap;

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
    }

    public DefaultPokemob getPokemob()
    {
        if (this.pokemobCap == null)
        {
            IPokemob pokemob = PokemobCaps.getPokemobFor(this);
            // Internally this sets the data
            if (pokemob instanceof DefaultPokemob poke) this.pokemobCap = poke;
            else this.setData(PokemobCaps.POKEMOB, new DefaultPokemob(this));
        }
        return this.pokemobCap;
    }

    @Override
    public <T> @Nullable T setData(Supplier<AttachmentType<T>> type, T data)
    {
        T resp = super.setData(type, data);
        if (data instanceof DefaultPokemob poke)
        {
            this.pokemobCap = poke;
            if (poke.getEntity() != this) poke.setEntity(this);
        }
        return resp;
    }

    @Override
    public SimpleContainer getInventory()
    {
        return getPokemob().getInventory();
    }
}