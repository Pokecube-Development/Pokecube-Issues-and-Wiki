package pokecube.mobs.moves.attacks.multihit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.moves.templates.Move_MultiHit;

public class Triplekick extends Move_MultiHit
{
    public Triplekick()
    {
        super("triplekick");
    }

    @Override
    public int getCount(@Nonnull IPokemob user, @Nullable LivingEntity target)
    {
        return 3;
    }
}
