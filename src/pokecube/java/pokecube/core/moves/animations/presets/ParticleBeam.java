package pokecube.core.moves.animations.presets;

import com.google.gson.JsonObject;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.presets.parametric.CartesianFunction;

@AnimPreset(getPreset = "beam")
public class ParticleBeam extends CartesianFunction
{
    public ParticleBeam()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
    {
        // Load in initial values
        this.loadValues(preset);
        // Now we override the ones to make our beam-shaped cartesian function

        values.absolute = true;
        values.horizontal = false;
        values.reverse = !values.reverse;
        values.width = 1;
        values.lifetime = 5;
        values.density /= 20;
        values.f_x = "rand()*0.01";
        values.f_y = "rand()*0.01";
        values.f_z = "d*t/m"; // Forwards direction is z

        values.v_x = "0";
        values.v_y = "0";
        values.v_z = "0";

        super.init(preset);
        return this;
    }
}
