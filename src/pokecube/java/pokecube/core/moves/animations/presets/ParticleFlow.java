package pokecube.core.moves.animations.presets;

import com.google.gson.JsonObject;
import pokecube.api.moves.utils.IMoveAnimation;
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
        // Now we override the ones to make our beam-shaped cartesian function

        values.absolute = true;
        values.horizontal = false;
        values.reverse = !values.reverse;
        values.density /= 10;
        values.f_x = "rand()*t*" + values.width;
        values.f_y = values.flat ? "0" : "rand()*t*" + values.width;
        values.f_z = "t*d/m"; // Forwards direction is z

        values.v_x = "0";
        values.v_y = "0";
        values.v_z = "0.05";

        super.init(preset);
        return this;
    }
}
