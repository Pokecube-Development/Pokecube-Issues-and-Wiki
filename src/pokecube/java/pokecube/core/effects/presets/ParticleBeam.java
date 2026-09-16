package pokecube.core.effects.presets;

import com.google.gson.JsonObject;
import pokecube.api.effects.IMoveAnimation;
import pokecube.core.effects.AnimPreset;
import pokecube.core.effects.presets.parametric.CartesianFunction;

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
        values.lifetime = values.duration + 2; // +2 lets the beam visually ricochet a bit
        values.density = 0.01f / values.density;
        values.f_x = "rand()*0.01";
        values.f_y = "rand()*0.01";
        // d is distance to target, m is maximum time, t is current time
        values.f_z = "0"; // Forwards direction is z
        values.v_z = "rand()*d/m";

        super.init(preset);
        return this;
    }
}
