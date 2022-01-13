package pokecube.core.moves.animations.presets;

import java.util.Random;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AnimPreset(getPreset = "powder")
public class AnimationPowder extends MoveAnimationBase
{

    String  particle;
    float   width   = 1;
    boolean reverse = false;
    int     meshId  = 0;

    public AnimationPowder()
    {
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        this.particle = "powder";
        this.duration = 50;
        this.particleLife = 1;
        final String[] args = preset.split(":");
        for (int i = 1; i < args.length; i++)
        {
            final String ident = args[i].substring(0, 1);
            final String val = args[i].substring(1);
            if (ident.equals("w")) this.width = Float.parseFloat(val);
            else if (ident.equals("d")) this.density = Float.parseFloat(val);
            else if (ident.equals("r")) this.reverse = Boolean.parseBoolean(val);
            else if (ident.equals("c")) this.initRGBA(val);
            else if (ident.equals("l")) this.particleLife = Integer.parseInt(val);
            else if (ident.equals("p")) this.particle = val;
        }
        return this;
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        final Vector3 target = info.target;
        this.initColour(info.attacker.getLevel().getDayTime() * 20, 0, info.move);
        final Vector3 temp = new Vector3();
        final Random rand = ThutCore.newRandom();
        for (int i = 0; i < 100 * this.density; i++)
        {
            temp.set(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
            temp.scalarMult(0.010 * this.width);
            temp.addTo(target);
            PokecubeCore.spawnParticle(info.attacker.getLevel(), this.particle, temp.copy(), null, this.rgba,
                    this.particleLife);
        }
    }

}
