package pokecube.core.moves.animations.presets;

import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

import java.util.Random;

@AnimPreset(getPreset = "flow")
public class ParticleFlow extends MoveAnimationBase
{
    public ParticleFlow()
    {}

    @Override
    public void spawnClientEntities(final MovePacketInfo info, float partialTicks)
    {
        final Vector3 source = values.reverse ? new Vector3(info.target) : new Vector3(info.source);
        final Vector3 target = values.reverse ? new Vector3(info.source) : new Vector3(info.target);
        this.initColour(info.currentTick, info.move);
        final double dist = source.distanceTo(target);
        float time = info.currentTick;
        final double timeFractionNow = time / (float) this.getDuration();
        final double distanceFractionNow = dist * timeFractionNow;
        final double frac3 = dist * time / this.getDuration();
        Vector3 dir = target.subtract(source);
        final Vector3 temp = dir.normalize();
        final Random rand = ThutCore.newRandom();
        final Vector3 temp1 = new Vector3();
        final Vector3 angleF = temp.horizonalPerp();
        if (values.flat)
        {
            angleF.rotateAboutLine(temp.normalize(), values.angle, temp1);
            angleF.set(temp1);
        }
        dir.scalarMultBy(0.05);
        for (double i = 0; i < frac3; i += 0.1)
        {
            if (values.density < 1 && Math.random() > values.density) continue;
            double factor = Math.min(i, 1);
            factor *= values.width * 2;
            for (int j = 0; j < values.density * 10; j++)
            {
                temp1.set(temp).scalarMultBy(i);
                if (values.flat) temp1.addTo(angleF.scalarMult(factor * (0.5 - rand.nextDouble())));
                else temp1.addTo(factor * (0.5 - rand.nextDouble()), factor * (0.5 - rand.nextDouble()),
                        factor * (0.5 - rand.nextDouble()));
                PokecubeCore.spawnParticle(info.level, values.particle, source.add(temp1), dir, values.rgba,
                        values.lifetime); // .scalarMult(i) was this
            }
        }
    }
}
