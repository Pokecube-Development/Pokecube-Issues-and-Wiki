package pokecube.core.moves.animations.presets;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "beam")
public class ParticleBeam extends MoveAnimationBase
{
    Vector3 v   = Vector3.getNewVector();
    boolean old = false;
    Vector3 v1  = Vector3.getNewVector();

    public ParticleBeam()
    {
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        this.rgba = 0xFFFFFFFF;
        final String[] args = preset.split(":");
        this.particle = "misc";
        this.density = 0.5f;
        for (int i = 1; i < args.length; i++)
        {
            final String ident = args[i].substring(0, 1);
            final String val = args[i].substring(1);
            if (ident.equals("d")) this.density = Float.parseFloat(val);
            else if (ident.equals("p")) this.particle = val;
            else if (ident.equals("l")) this.particleLife = Integer.parseInt(val);
            else if (ident.equals("c")) this.initRGBA(val);
        }
        return this;
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        final Vector3 source = info.source;
        final Vector3 target = info.target;
        this.initColour(info.attacker.getEntityWorld().getDayTime() * 20, 0, info.move);
        final double dist = source.distanceTo(target);
        final double frac = dist * info.currentTick / this.getDuration();
        final Vector3 temp = Vector3.getNewVector().set(target).subtractFrom(source).norm();
        for (double i = frac; i < dist; i += 0.1)
            PokecubeCore.spawnParticle(info.attacker.getEntityWorld(), this.particle, source.add(temp.scalarMult(i)),
                    null, this.rgba, this.particleLife);
    }
}
