package pokecube.core.moves.animations.presets;

import java.util.Random;

import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AnimPreset(getPreset = "pont")
public class ParticlesOnTarget extends MoveAnimationBase
{

    public ParticlesOnTarget()
    {}

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (Math.random() > values.density) return;
        this.initColour(info.attacker.getLevel().getDayTime(), 0, info.move);
        final Vector3 temp = new Vector3().set(info.target);
        final Random rand = ThutCore.newRandom();
        float dw = 0.25f;
        if (info.attacked != null) dw = info.attacked.getBbWidth();
        final float width = values.width * dw;
        temp.addTo(rand.nextGaussian() * width, rand.nextGaussian() * width, rand.nextGaussian() * width);
        PokecubeCore.spawnParticle(info.attacker.getLevel(), values.particle, temp, null, values.rgba);
    }
}
