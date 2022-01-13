package pokecube.legends.init;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class PokecubeDim
{

    public double beast(final IPokemob mob)
    {
        double x = 1;
        final Entity entity = mob.getEntity();
        final ResourceKey<Level> key = entity.getLevel().dimension();
        if (key == FeaturesInit.DISTORTEDWORLD_KEY || key == FeaturesInit.ULTRASPACE_KEY) x = 3.7;
        return x;
    }

    // Dynamax/Gigantamax
    public double dyna(final IPokemob mob)
    {
        final double x = 3;
        return x;
    }
    
    // Teams
    public double teamR(final IPokemob mob)
    {
        final double x = 1.5;
        return x;
    }

    public double teamMagma(final IPokemob mob)
    {
        double x = 1.5;
        if (mob.isType(PokeType.getType("fire"))) x = 2.5;
        return x;
    }

    public double teamAqua(final IPokemob mob)
    {
        double x = 1.5;
        if (mob.isType(PokeType.getType("ice"))) x = 2.5;
        return x;
    }
    //

    public double typingB(final IPokemob mob)
    {
        double x = 1;
        if (mob.isType(PokeType.getType("ground"))) x = 2.5;
        return x;
    }

    public double clone(final IPokemob mob)
    {
        double x = 1.5;
        if (mob.isType(PokeType.getType("ghost")) || mob.isType(PokeType.getType("psychic"))) x = 3;
        return x;
    }
}
