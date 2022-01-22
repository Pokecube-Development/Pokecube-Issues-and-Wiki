package pokecube.core.moves.animations.presets;

import java.util.Random;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AnimPreset(getPreset = "flow")
public class ParticleFlow extends MoveAnimationBase
{
    public ParticleFlow()
    {
    }

    @Override
    public IMoveAnimation init(final String preset)
    {
        this.particle = "misc";
        final String[] args = preset.split(":");
        for (int i = 1; i < args.length; i++)
            try
            {
                final String ident = args[i].substring(0, 1);
                final String val = args[i].substring(1);
                if (ident.equals("w")) this.width = Float.parseFloat(val);
                else if (ident.equals("d")) this.density = Float.parseFloat(val);
                else if (ident.equals("f"))
                {
                    this.flat = true;
                    this.angle = (float) (Float.parseFloat(val) * Math.PI) / 180f;
                }
                else if (ident.equals("r")) this.reverse = Boolean.parseBoolean(val);
                else if (ident.equals("p")) this.particle = val;
                else if (ident.equals("l")) this.particleLife = Integer.parseInt(val);
                else if (ident.equals("c")) this.initRGBA(val);
            }
            catch (final NumberFormatException e)
            {
                System.err.println(preset + " " + e);
            }
        return this;
    }

    @Override
    public void spawnClientEntities(final MovePacketInfo info)
    {
        final Vector3 source = this.reverse ? info.target : info.source;
        final Vector3 target = this.reverse ? info.source : info.target;
        this.initColour(info.attacker.getLevel().getDayTime() * 20, 0, info.move);
        final double dist = source.distanceTo(target);
        final double frac2 = info.currentTick / (float) this.getDuration();
        final double frac = dist * frac2;
        final double frac3 = dist * (info.currentTick + 1) / this.getDuration();
        final Vector3 temp = new Vector3().set(target).subtractFrom(source).norm();
        final Random rand = ThutCore.newRandom();
        final Vector3 temp1 = new Vector3();
        final Vector3 angleF = temp.horizonalPerp();
        if (this.flat)
        {
            angleF.rotateAboutLine(temp.normalize(), this.angle, temp1);
            angleF.set(temp1);
        }
        final Vector3 dir = target.subtract(source).norm().scalarMult(0.01);
        for (double i = frac; i < frac3; i += 0.1)
        {
            if (this.density < 1 && Math.random() > this.density) continue;
            double factor = Math.min(frac2, 1);
            factor *= this.width * 2;
            for (int j = 0; j < this.density; j++)
            {
                if (this.flat) temp1.set(angleF.scalarMult(factor * (0.5 - rand.nextDouble())));
                else temp1.set(factor * (0.5 - rand.nextDouble()), factor * (0.5 - rand.nextDouble()), factor * (0.5
                        - rand.nextDouble()));
                PokecubeCore.spawnParticle(info.attacker.getLevel(), this.particle, source.add(temp.scalarMult(i)
                        .addTo(temp1)), dir, this.rgba, this.particleLife);
            }
        }
    }
}
