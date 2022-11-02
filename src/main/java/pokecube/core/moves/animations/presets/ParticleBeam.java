package pokecube.core.moves.animations.presets;

import com.google.gson.JsonObject;

import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "beam")
public class ParticleBeam extends MoveAnimationBase
{
    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    public ParticleBeam()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
    {
        super.init(preset);
        if (!preset.has("density")) values.density = 0.5f;
        return this;
    }

    @Override
    public void spawnClientEntities(final MovePacketInfo info)
    {
        final Vector3 source = info.source;
        final Vector3 target = info.target;
        this.initColour(info.attacker.getLevel().getDayTime() * 20, 0, info.move);
        final double dist = source.distanceTo(target);
        final double frac = dist * info.currentTick / this.getDuration();
        final Vector3 temp = new Vector3().set(target).subtractFrom(source).norm();
        final Vector3 dir = target.subtract(source).norm().scalarMult(0.01);
        for (double i = frac; i < dist; i += 0.1) PokecubeCore.spawnParticle(info.attacker.getLevel(), values.particle,
                source.add(temp.scalarMult(i)), dir, values.rgba, values.lifetime);
    }
}
