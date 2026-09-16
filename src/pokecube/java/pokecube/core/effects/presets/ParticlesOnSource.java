package pokecube.core.effects.presets;

import java.util.Random;

import pokecube.core.PokecubeCore;
import pokecube.core.effects.AnimPreset;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AnimPreset(getPreset = "pons")
public class ParticlesOnSource extends ParticlesOnTarget
{
    public ParticlesOnSource()
    {
        super();
    }

    @Override
    public void spawnClientEntities(EffectPacketInfo info, float partialTicks)
    {
        if (Math.random() > this.values.density) return;
        final Vector3 temp = new Vector3();
        final Random rand = ThutCore.newRandom();
        float dw = info.sourceScale;
        final float width = this.values.width * dw;
        for (int i = 0; i < 50 * values.density; i++)
        {
            temp.set(info.getSource()).addTo(rand.nextGaussian() * width, rand.nextGaussian() * width, rand.nextGaussian() * width);
            PokecubeCore.spawnParticle(info.level, values.particle, temp, null, values.rgba);
        }
    }
}
