package thut.core.client.render.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import thut.api.util.JsonUtil;

public class JsonTemplate
{
    public String credit = "";
    public List<JsonBlock> elements = new ArrayList<>();
    public List<JsonGroup> groups = new ArrayList<>();

    public void init()
    {
        for (JsonGroup g : groups) g.init();
    }

    @Override
    public String toString()
    {
        return credit + " parts:" + elements + " groups:" + groups;
    }

    public static class JsonBlock
    {
        public String name;
        public float[] from;
        public float[] to;
        public int color;
        public JsonFaces faces;
        public JsonRotation rotation;

        @Override
        public String toString()
        {
            return name + " " + Arrays.toString(from) + " " + Arrays.toString(to) + " " + color + " " + faces;
        }
    }
    
    public static class JsonRotation
    {
        public float angle;
        public String axis;
        public float[] origin;
    }

    public static class JsonFaces
    {
        public JsonFace north;
        public JsonFace east;
        public JsonFace south;
        public JsonFace west;
        public JsonFace up;
        public JsonFace down;
    }

    public static class JsonFace
    {
        public float[] uv;
        public String texture;
    }

    public static class JsonGroup
    {
        public String name;
        public float[] origin;
        public int color;
        // This list can contain either integer indices of parts, or groups.
        public List<Object> children = new ArrayList<>();

        @Override
        public String toString()
        {
            return name + " " + Arrays.toString(origin) + " " + color + " " + children;
        }

        public void init()
        {
            List<Object> newChildren = new ArrayList<>();
            for (Object o : children)
            {
                if (o instanceof Double)
                {
                    newChildren.add((int) ((double)o));
                }
                else
                {
                    JsonGroup g = JsonUtil.gson.fromJson(o.toString(), JsonGroup.class);
                    g.init();
                    newChildren.add(g);
                }
            }
            this.children = newChildren;
        }
    }
}
