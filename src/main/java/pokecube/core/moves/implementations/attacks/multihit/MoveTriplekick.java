package pokecube.core.moves.implementations.attacks.multihit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_MultiHit;

public class MoveTriplekick extends Move_MultiHit
{
    public MoveTriplekick()
    {
        super("triplekick");
    }

    @Override
    public int getCount(@Nonnull IPokemob user, @Nullable Entity target)
    {
        return 3;
    }
}
