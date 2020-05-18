package pokecube.core.interfaces;

import net.minecraft.world.World;

public interface IHealer
{
    /** Should check your criteria and heal things accordingly */
    void heal(World world);
}
