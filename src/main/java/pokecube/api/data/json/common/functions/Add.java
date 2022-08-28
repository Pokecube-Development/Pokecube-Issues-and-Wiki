package pokecube.api.data.json.common.functions;

import pokecube.api.data.json.common.BaseBiFunction;

public class Add extends BaseBiFunction<Float, Float, Float>
{
    @Override
    public Float apply(Float t, Float u)
    {
        return t + u;
    }
}
