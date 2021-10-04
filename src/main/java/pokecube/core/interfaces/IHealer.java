package pokecube.core.interfaces;

import net.minecraft.world.level.Level;

public interface IHealer
{
    /** Should check your criteria and heal things accordingly */
    void heal(Level world);
}
