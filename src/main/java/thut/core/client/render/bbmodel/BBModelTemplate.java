package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thut.api.util.JsonUtil;

public class BBModelTemplate
{
    public String name = "";
    public List<Element> elements = new ArrayList<>();
    public List<JsonGroup> outliner = new ArrayList<>();
    public List<Texture> textures = new ArrayList<>();
    public Resolution resolution = new Resolution();

    public Map<String, Object> _by_uuid = new HashMap<>();

    public void init()
    {
        elements.forEach(e -> _by_uuid.put(e.uuid, e));
        textures.forEach(e -> _by_uuid.put(e.uuid, e));
        outliner.forEach(e -> e.init(this));
    }

    @Override
    public String toString()
    {
        return name + " parts:" + elements + " groups:" + outliner;
    }

    public static class Resolution
    {
        float width = 16;
        float height = 16;
    }

    public static class Texture
    {
        String name;
        String id;
        String render_mode;
        String uuid;
    }

    public static class Element
    {
        public String name;
        public String type;
        public String uuid;
        public float[] from;
        public float[] to;
        public float[] origin;
        public float[] rotation;
        public int color;
        public JsonFaces faces;

        @Override
        public String toString()
        {
            return name + " " + Arrays.toString(from) + " " + Arrays.toString(to) + " " + color + " " + faces;
        }
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
        public int texture;
    }

    public static class JsonGroup
    {
        public String name;
        public float[] origin;
        public int color;
        public String uuid;
        // This list can contain either uuid of parts, or groups.
        // Init converts this to either Elements or Groups
        public List<Object> children = new ArrayList<>();

        @Override
        public String toString()
        {
            return name + " " + Arrays.toString(origin) + " " + color + " " + children;
        }

        public void init(BBModelTemplate template)
        {
            List<Object> newChildren = new ArrayList<>();
            for (Object o : children)
            {
                if (o instanceof String)
                {
                    newChildren.add(template._by_uuid.get(o));
                }
                else
                {
                    String json = o.toString();
                    json = json.replace("bedrock_binding=, ", "");
                    JsonGroup g = JsonUtil.gson.fromJson(json, JsonGroup.class);
                    g.init(template);
                    newChildren.add(g);
                }
            }
            this.children = newChildren;
        }
    }
}
