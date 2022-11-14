package pokecube.core.moves.animations.presets;

import java.util.Random;

import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AnimPreset(getPreset = "flow")
public class ParticleFlow extends MoveAnimationBase
{
    public ParticleFlow()
    {}

    @Override
    public void spawnClientEntities(final MovePacketInfo info)
    {
        final Vector3 source = values.reverse ? info.target : info.source;
        final Vector3 target = values.reverse ? info.source : info.target;
        this.initColour(info.attacker.getLevel().getDayTime() * 20, 0, info.move);
        final double dist = source.distanceTo(target);
        final double frac2 = info.currentTick / (float) this.getDuration();
        final double frac = dist * frac2;
        final double frac3 = dist * (info.currentTick + 1) / this.getDuration();
        final Vector3 temp = new Vector3().set(target).subtractFrom(source).norm();
        final Random rand = ThutCore.newRandom();
        final Vector3 temp1 = new Vector3();
        final Vector3 angleF = temp.horizonalPerp();
        if (values.flat)
        {
            angleF.rotateAboutLine(temp.normalize(), values.angle, temp1);
            angleF.set(temp1);
        }
        final Vector3 dir = target.subtract(source).norm().scalarMult(0.01);
        for (double i = frac; i < frac3; i += 0.1)
        {
            if (values.density < 1 && Math.random() > values.density) continue;
            double factor = Math.min(frac2, 1);
            factor *= values.width * 2;
            for (int j = 0; j < values.density; j++)
            {
                if (values.flat) temp1.set(angleF.scalarMult(factor * (0.5 - rand.nextDouble())));
                else temp1.set(factor * (0.5 - rand.nextDouble()), factor * (0.5 - rand.nextDouble()),
                        factor * (0.5 - rand.nextDouble()));
                PokecubeCore.spawnParticle(info.attacker.getLevel(), values.particle,
                        source.add(temp.scalarMult(i).addTo(temp1)), dir, values.rgba, values.lifetime);
            }
        }
    }
}
