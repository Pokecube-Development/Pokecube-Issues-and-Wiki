package pokecube.core.moves.animations.presets;

import java.util.Random;

import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "pons")
public class ParticlesOnSource extends ParticlesOnTarget
{
    public ParticlesOnSource()
    {
        super();
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (Math.random() > this.density) return;
        this.initColour(info.attacker.getEntityWorld().getDayTime(), 0, info.move);
        final Vector3 temp = Vector3.getNewVector().set(info.source);
        final Random rand = new Random();
        float dw = 0.25f;
        if (info.attacker != null) dw = info.attacker.getWidth();
        final float width = this.width * dw;
        temp.addTo(rand.nextGaussian() * width, rand.nextGaussian() * width, rand.nextGaussian() * width);
        PokecubeCore.spawnParticle(info.attacker.getEntityWorld(), this.particle, temp, null, this.rgba);
    }
}
