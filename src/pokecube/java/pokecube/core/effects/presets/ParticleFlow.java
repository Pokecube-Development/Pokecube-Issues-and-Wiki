package pokecube.core.effects.presets;

import com.google.gson.JsonObject;
import pokecube.api.effects.IMoveAnimation;
import pokecube.core.effects.AnimPreset;
import pokecube.core.effects.presets.parametric.CartesianFunction;

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
        values.lifetime = 10;
        values.reverse = !values.reverse;
        values.density = 0.01f / values.density;
        // d is distance to target, m is maximum time, t is current time
        values.v_z = "(0.5+rand())*d/m";
        values.v_x = "guassian()*" + values.width + "*0.05";
        values.v_y = values.flat ? "0" : "guassian()*" + values.width + "*0.05";
        super.init(preset);
        return this;
    }
}
