package thut.core.client.render.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thut.api.maths.Vector4;
import thut.core.client.render.json.JsonTemplate.JsonBlock;
import thut.core.client.render.json.JsonTemplate.JsonFace;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.model.parts.Part;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;

public class JsonPart extends Part
{

    public static List<JsonPart> makeParts(JsonTemplate t, JsonBlock b, int index)
    {
        final List<JsonPart> parts = new ArrayList<>();
        float[] offsets;

        if (b.rotation == null) offsets = new float[]
        { 0, 0, 0 };
        else offsets = b.rotation.origin.clone();

        List<Mesh> shapes = makeShapes(t, b, offsets);
        JsonPart root = make(shapes.get(0), b.name + "_" + index, b, index);
        parts.add(root);
        if (shapes.size() > 1)
        {
            int i = 0;
            for (Mesh s : shapes)
            {
                JsonPart part = make(s, b.name + "_" + index + "_" + i++, b, index);
                parts.add(part);
            }
        }
        return parts;
    }

    private static JsonPart make(Mesh mesh, String name, JsonBlock b, int index)
    {
        JsonPart part = new JsonPart(name);
        part.index = index;
        part.addShape(mesh);
        float[] offsets = b.from.clone();

        if (b.rotation != null)
        {
            offsets[0] -= b.rotation.origin[0];
            offsets[1] -= b.rotation.origin[1];
            offsets[2] -= b.rotation.origin[2];

            float x = 0;
            float y = 0;
            float z = 0;
            if (b.rotation.axis.equals("x"))
            {
                x = b.rotation.angle;
            }
            if (b.rotation.axis.equals("y"))
            {
                y = b.rotation.angle;
            }
            if (b.rotation.axis.equals("z"))
            {
                z = b.rotation.angle;
            }
            part.rotations.set(x, y, z, 1);
        }
        offsets[0] /= 16.0f;
        offsets[1] /= 16.0f;
        offsets[2] /= 16.0f;
        part.offset.set(offsets);
        return part;
    }

    private static void addFace(Map<String, List<List<Object>>> materials, JsonTemplate template, JsonBlock b,
            JsonFace face, Direction dir, float[] offsets)
    {
        if (face == null || b == null) return;
        List<Object> order = Lists.newArrayList();
        List<Object> verts = Lists.newArrayList();
        List<Object> tex = Lists.newArrayList();
        String material = face.texture;
        if (materials.containsKey(material))
        {
            List<List<Object>> lists = materials.get(material);
            order = lists.get(0);
            verts = lists.get(1);
            tex = lists.get(2);
        }
        else
        {
            materials.put(material, Lists.newArrayList(order, verts, tex));
        }

        float[] _origin = new float[]
        { 0, 0, 0 };
        float[] from = new float[]
        { 0, 0, 0 };
        float[] to = new float[]
        { 0, 0, 0 };
        float[] origin_offset = new float[]
        { 0, 0, 0 };
        float[] mid_offset = new float[]
        { 0, 0, 0 };

        if (b.rotation != null) _origin = b.rotation.origin;

        for (int i = 0; i < 3; i++)
        {
            float size = (b.to[i] - b.from[i]);
            float mid = (b.to[i] + b.from[i]) / 2;
            origin_offset[i] = -_origin[i] + b.from[i];
            to[i] = size;
            mid_offset[i] = mid - size / 2;
        }

        float[][] coords = new float[4][3];

        switch (dir)
        {
        case DOWN:
            coords[0][0] = to[0];
            coords[0][2] = from[2];

            coords[1][0] = to[0];
            coords[1][2] = to[2];

            coords[2][0] = from[0];
            coords[2][2] = to[2];

            coords[3][0] = from[0];
            coords[3][2] = from[2];

            coords[0][1] = from[1];
            coords[1][1] = from[1];
            coords[2][1] = from[1];
            coords[3][1] = from[1];
            break;
        case UP:
            coords[3][0] = to[0];
            coords[3][2] = from[2];

            coords[2][0] = to[0];
            coords[2][2] = to[2];

            coords[1][0] = from[0];
            coords[1][2] = to[2];

            coords[0][0] = from[0];
            coords[0][2] = from[2];

            coords[0][1] = to[1];
            coords[1][1] = to[1];
            coords[2][1] = to[1];
            coords[3][1] = to[1];
            break;
        case EAST:
            coords[0][1] = to[1];
            coords[0][2] = to[2];

            coords[1][1] = from[1];
            coords[1][2] = to[2];

            coords[2][1] = from[1];
            coords[2][2] = from[2];

            coords[3][1] = to[1];
            coords[3][2] = from[2];

            coords[0][0] = to[0];
            coords[1][0] = to[0];
            coords[2][0] = to[0];
            coords[3][0] = to[0];
            break;
        case WEST:

            coords[0][1] = to[1];
            coords[0][2] = from[2];

            coords[1][1] = from[1];
            coords[1][2] = from[2];

            coords[2][1] = from[1];
            coords[2][2] = to[2];

            coords[3][1] = to[1];
            coords[3][2] = to[2];

            coords[0][0] = from[0];
            coords[1][0] = from[0];
            coords[2][0] = from[0];
            coords[3][0] = from[0];

            break;
        case NORTH:
            coords[0][1] = to[1];
            coords[0][0] = to[0];

            coords[1][1] = from[1];
            coords[1][0] = to[0];

            coords[2][1] = from[1];
            coords[2][0] = from[0];

            coords[3][1] = to[1];
            coords[3][0] = from[0];

            coords[0][2] = from[2];
            coords[1][2] = from[2];
            coords[2][2] = from[2];
            coords[3][2] = from[2];
            break;
        case SOUTH:

            coords[0][1] = to[1];
            coords[0][0] = from[0];

            coords[1][1] = from[1];
            coords[1][0] = from[0];

            coords[2][1] = from[1];
            coords[2][0] = to[0];

            coords[3][1] = to[1];
            coords[3][0] = to[0];

            coords[0][2] = to[2];
            coords[1][2] = to[2];
            coords[2][2] = to[2];
            coords[3][2] = to[2];
            break;
        default:
            break;
        }

        int[][] tex_order =
        {
                { 0, 1 },
                { 0, 3 },
                { 2, 3 },
                { 2, 1 } };

        float us = 16f;
        float vs = 16f;

        Quaternion quat = new Quaternion(0, 0, 0, 1);

        if (b.rotation != null)
        {
            float x = 0;
            float y = 0;
            float z = 0;
            if (b.rotation.axis.equals("x"))
            {
                x = b.rotation.angle;
            }
            if (b.rotation.axis.equals("y"))
            {
                y = b.rotation.angle;
            }
            if (b.rotation.axis.equals("z"))
            {
                z = b.rotation.angle;
            }
            if (y != 0) quat.mul(AxisAngles.ZP.rotationDegrees(y));
            if (z != 0) quat.mul(AxisAngles.YP.rotationDegrees(z));
            if (x != 0) quat.mul(AxisAngles.XP.rotationDegrees(x));
        }

        Vector3f origin = new Vector3f(origin_offset);
        Vector3f shift = new Vector3f(mid_offset);

        for (int i = 0; i < 4; i++)
        {
            float[] c = coords[i];
            Vertex v = new Vertex(c[0], c[1], c[2]);
            Vector3f vec = new Vector3f(v.x, v.y, v.z);

            // We need to translate to rotation point, then rotate, then
            // translate back.
            vec.add(origin);
            vec.transform(quat);
            vec.sub(origin);

            // Now translate to where it should be
            vec.add(shift);

            v.set(vec.x() / 16, vec.y() / 16, vec.z() / 16);
            Integer o = order.size();
            int u0 = tex_order[i][0];
            int v0 = tex_order[i][1];
            TextureCoordinate t = new TextureCoordinate(face.uv[u0] / us, face.uv[v0] / vs);
            order.add(o);
            verts.add(v);
            tex.add(t);
        }
    }

    private static List<Mesh> makeShapes(JsonTemplate t, JsonBlock b, float[] offsets)
    {
        List<Mesh> shapes = new ArrayList<>();

        Map<String, List<List<Object>>> materials = Maps.newHashMap();

        // The vertices and order come from drawing the 6 quads, based on
        // the from and to coordinates of the block

        // The normals can be ignored, as they will be calculated by the mesh
        // and are assumed to be flat shaded.

        // The texture coordinates from from the faces,
        // if they have different coordinates, it makes a new mesh for that face

        addFace(materials, t, b, b.faces.north, Direction.NORTH, offsets);
        addFace(materials, t, b, b.faces.east, Direction.EAST, offsets);
        addFace(materials, t, b, b.faces.south, Direction.SOUTH, offsets);
        addFace(materials, t, b, b.faces.west, Direction.WEST, offsets);
        addFace(materials, t, b, b.faces.up, Direction.UP, offsets);
        addFace(materials, t, b, b.faces.down, Direction.DOWN, offsets);

        materials.forEach((key, lists) -> {
            List<Object> order = lists.get(0);
            List<Object> verts = lists.get(1);
            List<Object> tex = lists.get(2);
            Mesh m = new JsonMesh(order.toArray(new Integer[0]), verts.toArray(new Vertex[0]),
                    tex.toArray(new TextureCoordinate[0]));
            m.name = key;
            Material mat = new Material(key);
            m.setMaterial(mat);

            if (t.textures != null && key.contains("#") && t.textures.has(key = key.replace("#", "")))
            {
                try
                {
                    String texture = t.textures.get(key).getAsString();
                    mat.tex = new ResourceLocation(texture);
                    if (!mat.tex.toString().contains("textures/"))
                        mat.tex = new ResourceLocation(mat.tex.getNamespace(), "textures/" + mat.tex.getPath());
                    if (!mat.tex.toString().contains(".png"))
                        mat.tex = new ResourceLocation(mat.tex.getNamespace(), mat.tex.getPath() + ".png");
                }
                catch (Exception e)
                {
                    ThutCore.LOGGER.error("Error loading Json Model Texture!", e);
                    ThutCore.LOGGER.error(t.textures);
                    ThutCore.LOGGER.error(key);
                }
            }
            shapes.add(m);
        });
        return shapes;
    }

    public int index = 0;
    private float rx = 0, ry = 0, rz = 0;

    public JsonPart(String name)
    {
        super(name + "");
    }

    @Override
    public void setPreRotations(Vector4 angles)
    {
        this.preRot.mul(angles, rotations);
    }

    @Override
    public void resetToInit()
    {
        super.resetToInit();
        rx = ry = rz = 0;
    }

    @Override
    public void setDefaultAngles(float rx, float ry, float rz)
    {
        this.rotations.x += rx;
        this.rotations.y += ry;
        this.rotations.z += rz;
    }

    @Override
    public void setAnimAngles(float rx, float ry, float rz)
    {
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    @Override
    public void preRender(PoseStack mat)
    {
        if (this.getParent() != null) getParent().preRender(mat);

        mat.pushPose();

        // Translate of offset for rotation.
        mat.translate(this.preTrans.x, this.preTrans.y, this.preTrans.z);
        mat.scale(this.preScale.x, this.preScale.y, this.preScale.z);

        float rx = this.rx + rotations.x;
        float ry = this.ry + rotations.y;
        float rz = this.rz + rotations.z;

        if (rz != 0) mat.mulPose(AxisAngles.YN.rotationDegrees(rz));
        if (ry != 0) mat.mulPose(AxisAngles.ZP.rotationDegrees(ry));
        if (rx != 0) mat.mulPose(AxisAngles.XP.rotationDegrees(rx));

        // Translate by post-PreOffset amount.
        mat.translate(this.postTrans.x, this.postTrans.y, this.postTrans.z);
        // Apply postRotation
        this.postRot.glRotate(mat);
        // Scale
        mat.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    @Override
    public String getType()
    {
        return "json";
    }
}
