package pokecube.mobs.moves.attacks.multihit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_MultiHit;

public class Triplekick extends Move_MultiHit
{
    public Triplekick()
    {
        super("triplekick");
    }

    @Override
    public int getCount(@Nonnull IPokemob user, @Nullable Entity target)
    {
        return 3;
    }
}
