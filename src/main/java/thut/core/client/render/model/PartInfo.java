package thut.core.client.render.model;

import java.util.HashMap;

public class PartInfo
{
    /** Unique name for this part, used for looking up in maps */
    public final String              name;
    /** Any child parts of this part */
    public HashMap<String, PartInfo> children = new HashMap<>();

    public PartInfo(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return this.name + " " + this.children;
    }
}
