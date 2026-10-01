package thut.core.client.render.x3d;

import thut.api.model.Part;

public class X3dPart extends Part
{
    public X3dPart(final String name)
    {
        super(name);
    }

    @Override
    public String getType()
    {
        return "x3d";
    }
}
