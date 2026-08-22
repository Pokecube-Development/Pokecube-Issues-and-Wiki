package thut.core.client.render.bbmodel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import thut.api.util.JsonUtil;
import thut.core.client.render.model.parts.Material;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BBModelTemplate
{
    public static interface IBBPart
    {
        float[] getOrigin();

        float[] getRotation();

        String getName();
    }

    public Meta meta;
    public String name = "";
    public List<Element> elements = new ArrayList<>();
    public List<JsonGroup> outliner = new ArrayList<>(); // In BB 4.5 this was the groups, after 5.0 it is just child map
    public List<JsonGroup> groups = new ArrayList<>(); // Added in BB 5.0, contains info except children
    public List<Texture> textures = new ArrayList<>();
    public List<BBAnimation> animations = new ArrayList<>();
    public Resolution resolution = new Resolution();

    public Map<String, Object> _by_uuid = new HashMap<>();
    Map<String, Material> _materials = Maps.newHashMap();

    public void init()
    {
        elements.forEach(e -> _by_uuid.put(e.uuid, e));
        textures.forEach(e -> _by_uuid.put(e.uuid, e));
        // Groups did not exist prior to 5.0
        if(groups != null) groups.forEach(e -> _by_uuid.put(e.uuid, e));
        if(meta.format_version.startsWith("4."))
        {
            // Groups were in the outliner here
            outliner.forEach(e -> e.init(this));
        }
        else if(meta.format_version.startsWith("5."))
        {
            // First process the outliner
            outliner.forEach(e -> e.init(this));
            // Now replace outliner with the groups, as outliner is arranged appropriately for render order
            outliner.replaceAll(g-> (JsonGroup) _by_uuid.get(g.uuid));
            // Now re-run the init with proper groups
            outliner.forEach(e -> e.init(this));
        }
        else throw new RuntimeException("No bb model processor for format {meta.model_format}");
    }

    @Override
    public String toString()
    {
        return name + " parts:" + elements + " groups:" + groups;
    }

    public static class Meta
    {
        public boolean box_uv = false;
        public String model_format = "free";
        public String format_version = "5.0";
    }

    public static class Resolution
    {
        public float width = 16;
        public float height = 16;
    }

    public static class Texture
    {
        public String name;
        public String id;
        public String render_mode;
        public String uuid;
        public String file_format = "png";
        public String source;
        public Integer width;
        public Integer height;
        public Integer uv_width;
        public Integer uv_height;
        public boolean visible = true;
    }

    public float getTexWidth(int index)
    {
        Texture tex = this.textures.get(index);
        return tex.width != null ? tex.width : resolution.width;
    }

    public float getTexHeight(int index)
    {
        Texture tex = this.textures.get(index);
        return tex.height != null ? tex.height : resolution.height;
    }

    public static class BBModelQuad
    {
        public Vector3f[] points = new Vector3f[4];
        public Vector2f[] tex = new Vector2f[4];
        public int texture;
        public int rotation = 0;
        public float[] uvs;

        public boolean isValid()
        {
            // Check if we actually have any area
            Vector3f v1 = new Vector3f(points[0]);
            Vector3f v2 = new Vector3f(points[1]);
            Vector3f v3 = new Vector3f(points[2]);

            v2.sub(v1);
            v3.sub(v1);
            // This happens for faces with no area.
            // Otherwise, we are valid
            return v2.dot(v3) != 0;
        }
    }

    public static class BBCubeElement
    {
        BBModelQuad[] quads = new BBModelQuad[6];

        public BBCubeElement(BBModelTemplate template, Element b)
        {
            float[] from = new float[] { 0, 0, 0 };
            float[] to = new float[] { 0, 0, 0 };
            float[] origin_offset = new float[] { 0, 0, 0 };
            float[] mid_offset = new float[] { 0, 0, 0 };

            float f = b.inflate;

            for (int i = 0; i < 3; i++)
            {
                float size = (b.to[i] - b.from[i]);
                size = size + 2 * f;
                float mid = (b.to[i] + b.from[i]) / 2;
                origin_offset[i] = -b.origin[i] + b.from[i] - f;
                to[i] = size;
                mid_offset[i] = mid - size / 2;
            }

            CubeFace up = null, down = null, east = null, west = null, north = null, south = null;

            if (b.faces.containsKey("up"))
            {
                up = JsonUtil.gson.fromJson(b.faces.get("up"), CubeFace.class);
            }
            if (b.faces.containsKey("down"))
            {
                down = JsonUtil.gson.fromJson(b.faces.get("down"), CubeFace.class);
            }
            if (b.faces.containsKey("east"))
            {
                east = JsonUtil.gson.fromJson(b.faces.get("east"), CubeFace.class);
            }
            if (b.faces.containsKey("west"))
            {
                west = JsonUtil.gson.fromJson(b.faces.get("west"), CubeFace.class);
            }
            if (b.faces.containsKey("north"))
            {
                north = JsonUtil.gson.fromJson(b.faces.get("north"), CubeFace.class);
            }
            if (b.faces.containsKey("south"))
            {
                south = JsonUtil.gson.fromJson(b.faces.get("south"), CubeFace.class);
            }

            if (down != null && down.texture != null)
            {
                // y low face, so y is from[1]
                BBModelQuad face = new BBModelQuad();
                face.points[0] = new Vector3f(from[0], from[1], to[2]);
                face.points[1] = new Vector3f(from[0], from[1], from[2]);
                face.points[2] = new Vector3f(to[0], from[1], from[2]);
                face.points[3] = new Vector3f(to[0], from[1], to[2]);
                face.rotation = down.rotation;
                face.texture = down.getTexture();
                face.uvs = down.uv;
                if (face.isValid()) quads[Direction.DOWN.ordinal()] = face;
            }
            if (up != null && up.texture != null)
            {
                // y high face, so y is to[1]
                BBModelQuad face = new BBModelQuad();
                face.points[0] = new Vector3f(from[0], to[1], from[2]);
                face.points[1] = new Vector3f(from[0], to[1], to[2]);
                face.points[2] = new Vector3f(to[0], to[1], to[2]);
                face.points[3] = new Vector3f(to[0], to[1], from[2]);
                face.rotation = up.rotation;
                face.texture = up.getTexture();
                face.uvs = up.uv;
                if (face.isValid()) quads[Direction.UP.ordinal()] = face;
            }
            if (west != null && west.texture != null)
            {
                // x low face, so x is from[0]
                BBModelQuad face = new BBModelQuad();
                face.points[0] = new Vector3f(from[0], to[1], from[2]);
                face.points[1] = new Vector3f(from[0], from[1], from[2]);
                face.points[2] = new Vector3f(from[0], from[1], to[2]);
                face.points[3] = new Vector3f(from[0], to[1], to[2]);
                face.rotation = west.rotation;
                face.texture = west.getTexture();
                face.uvs = west.uv;
                if (face.isValid()) quads[Direction.WEST.ordinal()] = face;
            }
            if (east != null && east.texture != null)
            {
                // x high face, so x is to[0]
                BBModelQuad face = new BBModelQuad();
                face.points[0] = new Vector3f(to[0], to[1], to[2]);
                face.points[1] = new Vector3f(to[0], from[1], to[2]);
                face.points[2] = new Vector3f(to[0], from[1], from[2]);
                face.points[3] = new Vector3f(to[0], to[1], from[2]);
                face.rotation = east.rotation;
                face.texture = east.getTexture();
                face.uvs = east.uv;
                if (face.isValid()) quads[Direction.EAST.ordinal()] = face;
            }
            if (north != null && north.texture != null)
            {
                // z low face, so z is from[2]
                BBModelQuad face = new BBModelQuad();
                face.points[0] = new Vector3f(to[0], to[1], from[2]);
                face.points[1] = new Vector3f(to[0], from[1], from[2]);
                face.points[2] = new Vector3f(from[0], from[1], from[2]);
                face.points[3] = new Vector3f(from[0], to[1], from[2]);
                face.rotation = north.rotation;
                face.texture = north.getTexture();
                face.uvs = north.uv;
                if (face.isValid()) quads[Direction.NORTH.ordinal()] = face;
            }
            if (south != null && south.texture != null)
            {
                // z high face, so z is to[2]
                BBModelQuad face = new BBModelQuad();
                face.points[0] = new Vector3f(from[0], to[1], to[2]);
                face.points[1] = new Vector3f(from[0], from[1], to[2]);
                face.points[2] = new Vector3f(to[0], from[1], to[2]);
                face.points[3] = new Vector3f(to[0], to[1], to[2]);
                face.rotation = south.rotation;
                face.texture = south.getTexture();
                face.uvs = south.uv;
                if (face.isValid()) quads[Direction.SOUTH.ordinal()] = face;
            }

            Quaternionf quat = new Quaternionf(0, 0, 0, 1);

            if (b.getRotation() != null)
            {
                float x = b.getRotation()[0];
                float y = b.getRotation()[1];
                float z = b.getRotation()[2];
                if (template.meta.model_format.equals("bedrock"))
                {
                    if (z != 0) quat.mul(AxisAngles.ZP.rotationDegrees(z));
                    if (y != 0) quat.mul(AxisAngles.YP.rotationDegrees(y));
                    if (x != 0) quat.mul(AxisAngles.XP.rotationDegrees(x));
                }
                else
                {
                    if (b.box_uv)
                    {
                        if (z != 0) quat.mul(AxisAngles.ZP.rotationDegrees(z));
                        if (y != 0) quat.mul(AxisAngles.YP.rotationDegrees(y));
                        if (x != 0) quat.mul(AxisAngles.XP.rotationDegrees(x));
                    }
                    else
                    {
                        if (z != 0) quat.mul(AxisAngles.ZP.rotationDegrees(z));
                        if (x != 0) quat.mul(AxisAngles.XP.rotationDegrees(x));
                        if (y != 0) quat.mul(AxisAngles.YP.rotationDegrees(y));
                    }
                }
            }

            Vector3f origin = new Vector3f(origin_offset);
            Vector3f shift = new Vector3f(mid_offset);

            int[][] tex_order = { { 0, 1 }, { 0, 3 }, { 2, 3 }, { 2, 1 } };

            for (var face : quads)
            {
                if (face == null) continue;

                for (int j = 0; j < 4; j++)
                {
                    Vector3f v = face.points[j];
                    // This should be a point on a box with a corner at 0,0,0.
                    Vector3f vec = new Vector3f(v.x, v.y, v.z);

                    // We need to translate to rotation point, then rotate, then
                    // translate back.
                    vec.add(origin);
                    quat.transform(vec);
                    vec.sub(origin);

                    // Now translate to where it should be
                    vec.add(shift);

                    v.set(vec.x() / 16, -vec.z() / 16, vec.y() / 16);
                    int i = (j + face.rotation / 90) % 4;

                    float us = template.getTexWidth(face.texture);
                    float vs = template.getTexHeight(face.texture);

                    int u0 = tex_order[i][0];
                    int v0 = tex_order[i][1];
                    face.tex[j] = new Vector2f(face.uvs[u0] / us, face.uvs[v0] / vs);
                }
            }
        }
    }

    public static class BBMeshElement
    {
        List<BBModelQuad> quads = Lists.newArrayList();
        List<BBModelQuad> tris = Lists.newArrayList();

        private boolean brokenQuad(MeshFace face, Map<String, Vector3f> verts)
        {
            if (face.vertices.size() != 4) return false;
            Vector3f toNext = new Vector3f();
            Vector3f toPrev = new Vector3f();
            Vector3f toOpp = new Vector3f();
            var vert = verts.get(face.vertices.get(0));
            var next = verts.get(face.vertices.get((1)));
            var crnr = verts.get(face.vertices.get((2)));
            var prev = verts.get(face.vertices.get((3)));

            next.sub(vert, toNext);
            prev.sub(vert, toPrev);
            crnr.sub(vert, toOpp);

            double angleNext = toNext.angle(toPrev);
            double angleOpp = toNext.angle(toOpp);
            double anglePrevOpp = toPrev.angle(toOpp);

            return angleOpp > angleNext || anglePrevOpp > angleNext;
        }

        public BBMeshElement(BBModelTemplate template, Element b)
        {
            Quaternionf quat = new Quaternionf(0, 0, 0, 1);

            if (b.getRotation() != null)
            {
                float x = b.getRotation()[0];
                float y = b.getRotation()[1];
                float z = b.getRotation()[2];
                if (x != 0) quat.mul(Axis.XP.rotationDegrees(x));
                if (y != 0) quat.mul(Axis.YP.rotationDegrees(y));
                if (z != 0) quat.mul(Axis.ZP.rotationDegrees(z));
            }

            Vector3f origin = new Vector3f(b.origin);

            Map<String, Vector3f> verts = Maps.newHashMap();

            for (var entry : b.vertices.entrySet())
            {
                var key = entry.getKey();
                var array = entry.getValue();

                Vector3f v = new Vector3f(0, 0, 0);
                // This should be a point on a box with a corner at 0,0,0.
                Vector3f vec = new Vector3f(array);

                // We need to translate to rotation point, then rotate, then
                // translate back.
                quat.transform(vec);
                vec.add(origin);

                float x = vec.x() / 16f;
                float y = -vec.z() / 16f;
                float z = vec.y() / 16f;

                v.set(x, y, z);
                verts.put(key, v);
            }
            for (var entry : b.faces.entrySet())
            {
                var json = entry.getValue();
                MeshFace face = JsonUtil.gson.fromJson(json, MeshFace.class);
                BBModelQuad quad = new BBModelQuad();

                var map_order = face.vertices;
                var uv_order = face.uv;
                boolean borked = brokenQuad(face, verts);
                boolean same = !borked;

                if (borked)
                {
                    map_order = new ArrayList<>(face.vertices);
                    var a = map_order.get(2);
                    map_order.set(2, map_order.get(3));
                    map_order.set(3, a);
                    same = true;
                    var bak = face.vertices;
                    face.vertices = map_order;
                    borked = brokenQuad(face, verts);
                    face.vertices = bak;
                    if (borked)
                    {
                        map_order = new ArrayList<>(face.vertices);
                        a = map_order.get(1);
                        map_order.set(1, map_order.get(2));
                        map_order.set(2, a);
                    }
                }

                for (int j = 0; j < map_order.size(); j++)
                {
                    int i = same ? j : map_order.size() - j - 1;
                    String vert_key = map_order.get(i);
                    Vector3f v = verts.get(vert_key);
                    float[] uv = uv_order.get(vert_key);
                    quad.texture = face.getTexture();
                    float us = template.getTexWidth(quad.texture);
                    float vs = template.getTexHeight(quad.texture);
                    quad.points[j] = v;
                    quad.tex[j] = new Vector2f(uv[0] / us, uv[1] / vs);
                }

                if (map_order.size() == 4) this.quads.add(quad);
                else if (map_order.size() == 3) this.tris.add(quad);
                else
                {
                    ThutCore.LOGGER.error("Unsupported Vector3f count: {}", map_order.size());
                }
            }
        }
    }

    public static class Element implements IBBPart
    {
        public String name;
        public String type;
        public String uuid;
        public String shading = "flat";
        public String render_order = "default";
        public float[] from;
        public float[] to;
        public float[] origin;   // Used by mesh and cube types
        public float[] rotation;
        public float[] position; // Used by locator types
        public int color;
        public Boolean box_uv = false;
        public boolean visibility = true;
        public boolean locked = false;
        public boolean export = true;
        public boolean allow_mirror_modeling = true;
        public float inflate = 0.0f;
        public Map<String, JsonObject> faces;
        public Map<String, float[]> vertices;
        public JsonGroup _parent = null;

        public void toMeshs(BBModelTemplate t, Map<String, List<List<Object>>> quads_materials,
                Map<String, List<List<Object>>> tris_materials)
        {
            if (!this.visibility) return;
            if (this.type.equals("cube"))
            {
                BBCubeElement box = new BBCubeElement(t, this);
                for (var face : box.quads)
                {
                    if (face == null) continue;
                    List<Object> order = Lists.newArrayList();
                    List<Object> verts = Lists.newArrayList();
                    List<Object> tex = Lists.newArrayList();
                    String material = t.textures.get(face.texture).name;
                    if (quads_materials.containsKey(material))
                    {
                        List<List<Object>> lists = quads_materials.get(material);
                        order = lists.get(0);
                        verts = lists.get(1);
                        tex = lists.get(2);
                    }
                    for (int i = 0; i < 4; i++)
                    {
                        Integer o = order.size();
                        Vector3f v = face.points[i];
                        var tx = face.tex[i];
                        order.add(o);
                        verts.add(v);
                        tex.add(tx);
                    }
                    quads_materials.put(material, Lists.newArrayList(order, verts, tex));
                }
            }
            else if (this.type.equals("mesh"))
            {
                BBMeshElement box = new BBMeshElement(t, this);
                for (var face : box.quads)
                {
                    if (face == null) continue;
                    List<Object> order = Lists.newArrayList();
                    List<Object> verts = Lists.newArrayList();
                    List<Object> tex = Lists.newArrayList();
                    String material = t.textures.get(face.texture).name;
                    if (quads_materials.containsKey(material))
                    {
                        List<List<Object>> lists = quads_materials.get(material);
                        order = lists.get(0);
                        verts = lists.get(1);
                        tex = lists.get(2);
                    }
                    for (int i = 0; i < 4; i++)
                    {
                        Integer o = order.size();
                        Vector3f v = face.points[i];
                        var tx = face.tex[i];
                        order.add(o);
                        verts.add(v);
                        tex.add(tx);
                    }
                    quads_materials.put(material, Lists.newArrayList(order, verts, tex));
                }
                for (var face : box.tris)
                {
                    if (face == null) continue;
                    List<Object> order = Lists.newArrayList();
                    List<Object> verts = Lists.newArrayList();
                    List<Object> tex = Lists.newArrayList();
                    String material = t.textures.get(face.texture).name;
                    if (tris_materials.containsKey(material))
                    {
                        List<List<Object>> lists = tris_materials.get(material);
                        order = lists.get(0);
                        verts = lists.get(1);
                        tex = lists.get(2);
                    }
                    for (int i = 0; i < 3; i++)
                    {
                        Integer o = order.size();
                        Vector3f v = face.points[i];
                        var tx = face.tex[i];
                        order.add(o);
                        verts.add(v);
                        tex.add(tx);
                    }
                    tris_materials.put(material, Lists.newArrayList(order, verts, tex));
                }
            }
        }

        @Override
        public String toString()
        {
            return name + " " + Arrays.toString(from) + " " + Arrays.toString(to) + " " + color + " " + faces;
        }

        @Override
        public float[] getOrigin()
        {
            if (this.position != null) return this.position;
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

        public void shift(float[] origin)
        {
            if (this.type.equals("cube"))
            {
                for (int i = 0; i < 3; i++)
                {
                    this.origin[i] -= origin[i];
                    this.from[i] -= origin[i];
                    this.to[i] -= origin[i];
                }
            }
            else
            {
                for (int i = 0; i < 3; i++) this.getOrigin()[i] -= origin[i];
            }
        }
    }

    public static class CubeFace
    {
        public float[] uv;
        public Object texture = 0;
        public int rotation = 0;

        public int getTexture()
        {
            return texture instanceof Number n? n.intValue():0;
        }
    }

    public static class MeshFace
    {
        public Map<String, float[]> uv;
        public List<String> vertices;
        public Object texture = 0;

        public int getTexture()
        {
            return texture instanceof Number n? n.intValue():0;
        }
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
            return name + " " + (origin!=null?Arrays.toString(origin):"outline") + " " + color + " " + children;
        }

        public void init(BBModelTemplate template)
        {
            // Older versions outliner was the groups.
            if("4.5".equals(template.meta.format_version) || this.origin!=null)
            {
                List<Object> newChildren = new ArrayList<>();
                for (Object o : children)
                {
                    if (o == null)
                    {
                        continue;
                    }
                    if (o instanceof String)
                    {
                        Element b = (Element) template._by_uuid.get(o);
                        if (b == null) continue;
                        if (b.name.equals("cube")) b.name = this.name;
                        b._parent = this;
                        b.shift(this.origin);
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
            else
            {
                var _group = template._by_uuid.get(uuid);
                if(_group instanceof JsonGroup group)
                {
                    for(var b: this.children)
                    {
                        if (b == null)
                        {
                            continue;
                        }
                        if (b instanceof String)
                        {
                            group.children.add(b);
                        }
                        else
                        {
                            String json = JsonUtil.gson.toJson(b);
                            JsonGroup g = JsonUtil.gson.fromJson(json, JsonGroup.class);
                            group.children.add(template._by_uuid.get(g.uuid));
                            g.init(template);
                        }
                    }
                }
            }
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
            public boolean rotation_global; // added 5.0
            public boolean quaternion_interpolation; // added 5.0
            public List<BBKeyFrame> keyframes = new ArrayList<>();
        }

        public static class BBKeyFrame implements Comparable<BBKeyFrame>
        {
            public String channel;
            public String uuid;
            public String interpolation;
            public double time;
            public List<BBDataPoint> data_points = new ArrayList<>();

            @Override
            public int compareTo(BBKeyFrame o)
            {
                return Double.compare(time, o.time);
            }
        }

        public static class BBDataPoint
        {
            public Object x;
            public Object y;
            public Object z;
            public String effect;
            public String locator;
            public String script;
            public String file;
        }
    }

}
