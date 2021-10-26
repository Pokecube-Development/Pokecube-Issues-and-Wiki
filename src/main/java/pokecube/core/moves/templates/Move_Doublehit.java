package pokecube.core.moves.templates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import pokecube.core.interfaces.IPokemob;

public class Move_Doublehit extends Move_MultiHit
{
    public Move_Doublehit(String name)
    {
        super(name);
    }

    @Override
    public int getCount(@Nonnull IPokemob user, @Nullable Entity target)
    {
        return 2;
    }
}
