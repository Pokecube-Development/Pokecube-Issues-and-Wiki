package pokecube.core.effects.presets;

import com.google.gson.JsonObject;

import pokecube.api.effects.IMoveAnimation;
import pokecube.core.effects.AnimPreset;
import pokecube.core.effects.presets.parametric.CartesianFunction;

@AnimPreset(getPreset = "powder")
public class AnimationPowder extends CartesianFunction
{
    public AnimationPowder()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
    {
        // Load in initial values
        this.loadValues(preset);
        if (!preset.has("particle")) this.values.particle = "powder";

        values.density = 0.01f / values.density;
        values.f_x = "guassian()*4";
        values.f_y = "guassian()*4";
        values.f_z = "guassian()*4";
        values.v_y = "-0.05";

        super.init(preset);
        return this;
    }
}
