package pokecube.api.events.init;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * This is fired on the {@link PokecubeAPI#POKEMOB_BUS} when {@link Pokemake2}
 * arguments are applied to a pokemob.
 *
 */
public class PokemakeArgumentEvent extends Event
{
    private IPokemob pokemob;
    private Vec3 pos;
    private CompoundTag nbt;

    public PokemakeArgumentEvent(IPokemob pokemob, Vec3 pos, CompoundTag nbt)
    {
        this.setPokemob(pokemob);
        this.setPos(pos);
        this.setNbt(nbt);
    }

    // The pokemob being applied to, call setPokemob with the new one if you
    // change it!
    public IPokemob getPokemob()
    {
        return pokemob;
    }

    public void setPokemob(IPokemob pokemob)
    {
        this.pokemob = pokemob;
    }

    public Vec3 getPos()
    {
        return pos;
    }

    public void setPos(Vec3 pos)
    {
        this.pos = pos;
    }

    public CompoundTag getNbt()
    {
        return nbt;
    }

    public void setNbt(CompoundTag nbt)
    {
        this.nbt = nbt;
    }
}
