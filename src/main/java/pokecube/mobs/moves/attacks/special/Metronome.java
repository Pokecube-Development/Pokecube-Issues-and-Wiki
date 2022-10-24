package pokecube.mobs.moves.attacks.special;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.maths.Vector3;

public class Metronome extends Move_Basic
{
    public Metronome()
    {
        super("metronome");
    }

    @Override
    public void ActualMoveUse(LivingEntity user, LivingEntity target, Vector3 start, Vector3 end)
    {
// TODO Rework this.
        PokecubeAPI.LOGGER.warn("Failed to find a move for metronome to use by " + user + " on " + target);
    }
}
