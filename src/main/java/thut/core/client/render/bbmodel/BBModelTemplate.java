package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.core.Direction;
import thut.api.maths.vecmath.Vec3f;
import thut.api.util.JsonUtil;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.TextureCoordinate;

public class BBModelTemplate
{
    public static interface IBBPart
    {
        float[] getOrigin();

        float[] getRotation();

        String getName();
    }

    public String name = "";
    public Meta meta;
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

    public static class Meta
    {
        boolean box_uv;
        String model_format;
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

    public static class BBModelFace
    {
        public Vertex[] points = new Vertex[4];
        public TextureCoordinate[] tex = new TextureCoordinate[4];
        public int texture;
        public int rotation = 0;
        public float[] uvs;

        public boolean isValid()
        {
            // Check if we actually have any area
            Vec3f v1 = new Vec3f(points[0]);
            Vec3f v2 = new Vec3f(points[1]);
            Vec3f v3 = new Vec3f(points[2]);

            v2.sub(v1);
            v3.sub(v1);
            // This happens for faces with no area.
            if (v2.dot(v3) == 0) return false;

            // Otherwise, we are valid
            return true;
        }
    }

    public static class BBModelBox
    {
        BBModelFace[] faces = new BBModelFace[6];

        public BBModelBox(BBModelTemplate template, Element b)
        {
            float[] from = new float[]
            { 0, 0, 0 };
            float[] to = new float[]
            { 0, 0, 0 };
            float[] origin_offset = new float[]
            { 0, 0, 0 };
            float[] mid_offset = new float[]
            { 0, 0, 0 };

            float f = 1 + b.inflate;

            for (int i = 0; i < 3; i++)
            {
                float size = (b.to[i] - b.from[i]) * f;
                float mid = -(b.to[i] + b.from[i]) * f / 2;
                origin_offset[i] = -b.origin[i] + b.from[i];
                to[i] = size;
                mid_offset[i] = -mid - size / 2;
            }

            if (b.faces.down != null)
            {
                // y low face, so y is from[1]
                BBModelFace face = new BBModelFace();
                face.points[0] = new Vertex(from[0], from[1], to[2]);
                face.points[1] = new Vertex(from[0], from[1], from[2]);
                face.points[2] = new Vertex(to[0], from[1], from[2]);
                face.points[3] = new Vertex(to[0], from[1], to[2]);
                face.rotation = b.faces.down.rotation;
                face.texture = b.faces.down.texture;
                face.uvs = b.faces.down.uv;
                if (face.isValid()) faces[Direction.DOWN.ordinal()] = face;
            }
            if (b.faces.up != null)
            {
                // y high face, so y is to[1]
                BBModelFace face = new BBModelFace();
                face.points[0] = new Vertex(from[0], to[1], from[2]);
                face.points[1] = new Vertex(from[0], to[1], to[2]);
                face.points[2] = new Vertex(to[0], to[1], to[2]);
                face.points[3] = new Vertex(to[0], to[1], from[2]);
                face.rotation = b.faces.up.rotation;
                face.texture = b.faces.up.texture;
                face.uvs = b.faces.up.uv;
                if (face.isValid()) faces[Direction.UP.ordinal()] = face;
            }
            if (b.faces.west != null)
            {
                // x low face, so x is from[0]
                BBModelFace face = new BBModelFace();
                face.points[0] = new Vertex(from[0], to[1], from[2]);
                face.points[1] = new Vertex(from[0], from[1], from[2]);
                face.points[2] = new Vertex(from[0], from[1], to[2]);
                face.points[3] = new Vertex(from[0], to[1], to[2]);
                face.rotation = b.faces.west.rotation;
                face.texture = b.faces.west.texture;
                face.uvs = b.faces.west.uv;
                if (face.isValid()) faces[Direction.WEST.ordinal()] = face;
            }
            if (b.faces.east != null)
            {
                // x high face, so x is to[0]
                BBModelFace face = new BBModelFace();
                face.points[0] = new Vertex(to[0], to[1], to[2]);
                face.points[1] = new Vertex(to[0], from[1], to[2]);
                face.points[2] = new Vertex(to[0], from[1], from[2]);
                face.points[3] = new Vertex(to[0], to[1], from[2]);
                face.rotation = b.faces.east.rotation;
                face.texture = b.faces.east.texture;
                face.uvs = b.faces.east.uv;
                if (face.isValid()) faces[Direction.EAST.ordinal()] = face;
            }
            if (b.faces.north != null)
            {
                // z low face, so z is from[2]
                BBModelFace face = new BBModelFace();
                face.points[0] = new Vertex(to[0], to[1], from[2]);
                face.points[1] = new Vertex(to[0], from[1], from[2]);
                face.points[2] = new Vertex(from[0], from[1], from[2]);
                face.points[3] = new Vertex(from[0], to[1], from[2]);
                face.rotation = b.faces.north.rotation;
                face.texture = b.faces.north.texture;
                face.uvs = b.faces.north.uv;
                if (face.isValid()) faces[Direction.NORTH.ordinal()] = face;
            }
            if (b.faces.south != null)
            {
                // z high face, so z is to[2]
                BBModelFace face = new BBModelFace();
                face.points[0] = new Vertex(from[0], to[1], to[2]);
                face.points[1] = new Vertex(from[0], from[1], to[2]);
                face.points[2] = new Vertex(to[0], from[1], to[2]);
                face.points[3] = new Vertex(to[0], to[1], to[2]);
                face.rotation = b.faces.south.rotation;
                face.texture = b.faces.south.texture;
                face.uvs = b.faces.south.uv;
                if (face.isValid()) faces[Direction.SOUTH.ordinal()] = face;
            }

            float us = template.resolution.width;
            float vs = template.resolution.height;

            Quaternion quat = new Quaternion(0, 0, 0, true);

            if (b.getRotation() != null)
            {
                float x = b.getRotation()[0];
                float y = b.getRotation()[1];
                float z = -b.getRotation()[2];
                quat = new Quaternion(x, y, z, true);
            }

            Vector3f origin = new Vector3f(origin_offset);
            Vector3f shift = new Vector3f(mid_offset);

            boolean bedrock = template.meta.model_format.equals("bedrock");

            int[][] tex_order =
            {
                    { 0, 1 },
                    { 0, 3 },
                    { 2, 3 },
                    { 2, 1 } };

            for (var face : faces)
            {
                if (face == null) continue;

                for (int j = 0; j < 4; j++)
                {
                    int index = j;

                    Vertex v = face.points[index];
                    // This should be a point on a box with a corner at 0,0,0.
                    Vector3f vec = new Vector3f(v.x, v.y, v.z);

                    // We need to translate to rotation point, then rotate, then
                    // translate back.
                    vec.add(origin);
                    vec.transform(quat);
                    vec.sub(origin);

                    // Now translate to where it should be
                    vec.add(shift);

                    if (bedrock) v.set(-vec.x() / 16, -vec.z() / 16, vec.y() / 16);
                    else v.set(vec.x() / 16, -vec.z() / 16, vec.y() / 16);
                    int i = (index + face.rotation / 90) % 4;
                    int u0 = tex_order[i][0];
                    int v0 = tex_order[i][1];
                    face.tex[index] = new TextureCoordinate(face.uvs[u0] / us, face.uvs[v0] / vs);
                }
            }
        }
    }

    public static class Element implements IBBPart
    {
        public String name;
        public String type;
        public String uuid;
        public float[] from;
        public float[] to;
        public float[] origin;
        public float[] rotation;
        public int color;
        public float inflate = 0.0f;
        public JsonFaces faces;

        @Override
        public String toString()
        {
            return name + " " + Arrays.toString(from) + " " + Arrays.toString(to) + " " + color + " " + faces;
        }

        @Override
        public float[] getOrigin()
        {
            return origin;
        }

        @Override
        public float[] getRotation()
        {
            return rotation;
        }

        @Override
        public String getName()
        {
            return name;
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
        public int rotation = 0;
    }

    public static class JsonGroup implements IBBPart
    {
        public String name;
        public float[] origin;
        public float[] rotation;
        public int color;
        public String uuid;
        // This list can contain either uuid of parts, or groups.
        // Init converts this to either Elements or Groups
        public List<Object> children = new ArrayList<>();

        public JsonGroup _parent = null;
        public boolean _empty = true;

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
                    Element b = (Element) template._by_uuid.get(o);
                    if (b.name.equals("cube")) b.name = this.name;
                    for (int i = 0; i < 3; i++)
                    {
                        b.origin[i] -= this.origin[i];
                        b.from[i] -= this.origin[i];
                        b.to[i] -= this.origin[i];
                    }
                    newChildren.add(b);
                    _empty = false;
                }
                else
                {
                    String json = JsonUtil.gson.toJson(o);
                    JsonGroup g = JsonUtil.gson.fromJson(json, JsonGroup.class);
                    g._parent = this;
                    g.init(template);
                    newChildren.add(g);
                }
            }
            this.children = newChildren;
        }

        @Override
        public float[] getOrigin()
        {
            return origin;
        }

        @Override
        public float[] getRotation()
        {
            return rotation;
        }

        @Override
        public String getName()
        {
            return name;
        }
    }

    public static class BBAnimation
    {
        public String uuid;
        public String name;
        public String loop;
        public boolean override;
        public double length;
        public int snapping;

        public Map<String, BBAnimator> animators = new HashMap<>();

        public static class BBAnimator
        {
            public String name;
            public String type;
            public List<BBKeyFrame> keyframes = new ArrayList<>();
        }

        public static class BBKeyFrame
        {
            public String channel;
            public String uuid;
            public String interpolation;
            public double time;
            public List<BBDataPoint> data_points = new ArrayList<>();
        }

        public static class BBDataPoint
        {
            Object x;
            Object y;
            Object z;
        }
    }

}
