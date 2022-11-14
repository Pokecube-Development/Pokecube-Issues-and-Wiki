package pokecube.core.moves.animations.presets;

import java.util.Random;

import com.google.gson.JsonObject;

import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

@AnimPreset(getPreset = "powder")
public class AnimationPowder extends MoveAnimationBase
{
    int meshId = 0;

    public AnimationPowder()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
    {
        super.init(preset);
        if (!preset.has("particle")) this.values.particle = "powder";
        return this;
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        final Vector3 target = info.target;
        this.initColour(info.attacker.getLevel().getDayTime() * 20, 0, info.move);
        final Vector3 temp = new Vector3();
        final Random rand = ThutCore.newRandom();
        for (int i = 0; i < 100 * values.density; i++)
        {
            temp.set(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
            temp.scalarMult(0.010 * values.width);
            temp.addTo(target);
            PokecubeCore.spawnParticle(info.attacker.getLevel(), values.particle, temp.copy(), null, values.rgba,
                    values.lifetime);
        }
    }

}
