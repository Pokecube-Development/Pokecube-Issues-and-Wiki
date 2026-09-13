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
        values.density = 0.05f / values.density;
        values.f_x = "rand()*0.01";
        values.f_y = "rand()*0.01";
        // d is distance to target, m is maximum time, t is current time
        values.f_z = "d*t/m"; // Forwards direction is z

        super.init(preset);
        return this;
    }
}
