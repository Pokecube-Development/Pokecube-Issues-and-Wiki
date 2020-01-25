package thut.core.client.render.tabula;

import thut.core.client.render.model.parts.Part;

public class TblPart extends Part
{

    public TblPart(final String name)
    {
        super(name);
    }

    @Override
    public String getType()
    {
        return "tbl";
    }

}
