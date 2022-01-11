package pokecube.core.moves.animations.presets;

import java.util.Random;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AnimPreset(getPreset = "pont")
public class ParticlesOnTarget extends MoveAnimationBase
{
    public ParticlesOnTarget()
    {
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        this.particle = "misc";
        final String[] args = preset.split(":");
        for (int i = 1; i < args.length; i++)
        {
            final String ident = args[i].substring(0, 1);
            final String val = args[i].substring(1);
            try
            {
                if (ident.equals("w")) this.width = Float.parseFloat(val);
                else if (ident.equals("d")) this.density = Float.parseFloat(val);
                else if (ident.equals("p")) this.particle = val;
                else if (ident.equals("l")) this.particleLife = Integer.parseInt(val);
                else if (ident.equals("c")) this.initRGBA(val);
            }
            catch (final NumberFormatException e)
            {
                System.err.println(preset);
            }
        }
        return this;
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (Math.random() > this.density) return;
        this.initColour(info.attacker.getLevel().getDayTime(), 0, info.move);
        final Vector3 temp = Vector3.getNewVector().set(info.target);
        final Random rand = ThutCore.newRandom();
        float dw = 0.25f;
        if (info.attacked != null) dw = info.attacked.getBbWidth();
        final float width = this.width * dw;
        temp.addTo(rand.nextGaussian() * width, rand.nextGaussian() * width, rand.nextGaussian() * width);
        PokecubeCore.spawnParticle(info.attacker.getLevel(), this.particle, temp, null, this.rgba);
    }
}
