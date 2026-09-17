package pokecube.core.effects.presets;

import java.util.Random;

import pokecube.api.effects.EffectPacketInfo;
import pokecube.core.PokecubeCore;
import pokecube.core.effects.AnimPreset;
import pokecube.core.effects.MoveAnimationBase;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AnimPreset(getPreset = "pont")
public class ParticlesOnTarget extends MoveAnimationBase
{

    public ParticlesOnTarget()
    {}

    @Override
    public void spawnClientEntities(EffectPacketInfo info, float partialTicks)
    {
        if (Math.random() > values.density) return;
        final Vector3 temp = new Vector3();
        final Random rand = ThutCore.newRandom();
        float dw = info.targetScale;
        final float width = values.width * dw;
        for (int i = 0; i < 50 * values.density; i++)
        {
            temp.set(info.getTarget()).addTo(rand.nextGaussian() * width, rand.nextGaussian() * width, rand.nextGaussian() * width);
            PokecubeCore.spawnParticle(info.level, values.particle, temp, null, values.rgba);
        }
    }
}
