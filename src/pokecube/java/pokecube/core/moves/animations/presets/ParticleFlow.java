package pokecube.core.moves.animations.presets;

import com.google.gson.JsonObject;
import pokecube.api.effects.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.presets.parametric.CartesianFunction;

@AnimPreset(getPreset = "flow")
public class ParticleFlow extends CartesianFunction
{
    public ParticleFlow()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
    {
        // Load in initial values
        this.loadValues(preset);
        // Now we override the ones to make our spread flow cartesian function
        values.absolute = true;
        values.horizontal = false;
        values.reverse = !values.reverse;
        values.density = 0.075f / values.density;
        values.f_x = "rand()*t*" + values.width;
        values.f_y = values.flat ? "0" : "rand()*t*" + values.width;
        // d is distance to target, m is maximum time, t is current time
        values.f_z = "t*d/m"; // Forwards direction is z
        if (values.v_z == null) values.v_z = "0.05";
        super.init(preset);
        return this;
    }
}
