package thut.core.client.render.obj;

import thut.core.client.render.model.parts.Part;

public class ObjPart extends Part
{
    public ObjPart(final String name)
    {
        super(name);
    }

    @Override
    public String getType()
    {
        return ".obj";
    }

}
