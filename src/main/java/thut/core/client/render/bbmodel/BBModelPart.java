package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.core.Direction;
import thut.api.maths.Vector4;
import thut.core.client.render.bbmodel.BBModelTemplate.Element;
import thut.core.client.render.bbmodel.BBModelTemplate.IBBPart;
import thut.core.client.render.bbmodel.BBModelTemplate.JsonFace;
import thut.core.client.render.bbmodel.BBModelTemplate.JsonGroup;
import thut.core.client.render.json.JsonMesh;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.model.parts.Part;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.common.ThutCore;

public class BBModelPart extends Part
{
    private static String nextName(Set<String> names, IBBPart part)
    {
        String nextName = part.getName();
        int i = 1;
        while (names.contains(nextName))
        {
            nextName = part.getName() + "_" + i;
            i++;
        }
        return nextName;
    }

    public static void makeParts(BBModelTemplate t, JsonGroup group, List<BBModelPart> parts,
            List<BBModelPart> children, Set<String> names, float[] parentOffsets)
    {
        List<BBModelPart> ours = new ArrayList<>();
        List<Mesh> allShapes = new ArrayList<>();
        // Handle parts first
        for (Object o : group.children)
        {
            if (o instanceof Element b)
            {
                List<Mesh> shapes = makeShapes(t, b, group.origin);
                allShapes.addAll(shapes);
            }
        }
        BBModelPart root = make(allShapes, nextName(names, group), group, -1, parentOffsets);
        ours.add(root);
        parts.add(root);
        // then handle groups
        children.clear();
        for (Object o : group.children)
        {
            if (o instanceof JsonGroup g)
            {
                for (int i = 0; i < 3; i++)
                {
                    parentOffsets[i] = -group.origin[i];
                }
                makeParts(t, g, parts, children, names, parentOffsets.clone());
                children.forEach(root::addChild);
            }
        }
        children.clear();
        children.addAll(ours);
    }

    private static BBModelPart make(List<Mesh> shapes, String name, IBBPart b, int index, float[] parentOffsets)
    {
        BBModelPart part = new BBModelPart(name);
        part.index = index;
        shapes.forEach(part::addShape);
        float[] offsets = b.getOrigin().clone();
        for (int i = 0; i < 3; i++)
        {
            offsets[i] += parentOffsets[i];
        }
        if (b.getRotation() != null)
        {
            float x = b.getRotation()[0];
            float y = b.getRotation()[2];
            float z = -b.getRotation()[1];
            Quaternion quat = new Quaternion(x, y, z, true);
            final Vector4 rotations = new Vector4(quat);
            part.rotations.set(rotations.x, rotations.y, rotations.z, rotations.w);
        }
        offsets[0] /= 16;
        offsets[1] /= 16;
        offsets[2] /= 16;

        float[] use = offsets.clone();
        use[0] = -offsets[0];
        use[1] = -offsets[2];
        use[2] = offsets[1];

        part.offset.set(use);
        return part;
    }

    private static void addFace(Map<String, List<List<Object>>> materials, BBModelTemplate template, Element b,
            JsonFace face, Direction dir, float[] origin)
    {
        if (face == null || b == null) return;

        float[] from = new float[]
        { 0, 0, 0 };
        float[] to = new float[]
        { 0, 0, 0 };
        float[] dr = new float[]
        { 0, 0, 0 };

        for (int i = 0; i < 3; i++)
        {
            float size = b.to[i] - b.from[i];
            from[i] = -size / 2;
            to[i] = size / 2;
            float mid = origin[i] - (b.to[i] + b.from[i]) / 2;
            dr[i] = mid;
            from[i] -= mid;
            to[i] -= mid;
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

        // Check if we actually have any area
        Vertex v1 = new Vertex(coords[0][0] / 16f, coords[0][2] / 16f, coords[0][1] / 16f);
        Vertex v2 = new Vertex(coords[1][0] / 16f, coords[1][2] / 16f, coords[1][1] / 16f);
        Vertex v3 = new Vertex(coords[2][0] / 16f, coords[2][2] / 16f, coords[2][1] / 16f);

        v2.sub(v1);
        v3.sub(v1);
        // This happens for faces with no area.
        if (v2.dot(v3) == 0) return;

        int[][] tex_order =
        {
                { 0, 1 },
                { 0, 3 },
                { 2, 3 },
                { 2, 1 } };

        float us = template.resolution.width;
        float vs = template.resolution.height;

        int di = face.rotation / 90;

        List<Object> order = Lists.newArrayList();
        List<Object> verts = Lists.newArrayList();
        List<Object> tex = Lists.newArrayList();
        String material = template.textures.get(face.texture).name;
        if (materials.containsKey(material))
        {
            List<List<Object>> lists = materials.get(material);
            order = lists.get(0);
            verts = lists.get(1);
            tex = lists.get(2);
        }

        Quaternion quat = new Quaternion(0, 0, 0, true);

        if (b.getRotation() != null)
        {
            float x = b.getRotation()[0];
            float y = b.getRotation()[1];
            float z = b.getRotation()[2];
            quat = new Quaternion(x, y, z, true);
        }
        Vector3f shift = new Vector3f(dr);

        for (int index = 3; index >= 0; index--)
        {
            int i = index;
            float[] c = coords[i];
            Vector3f vec = new Vector3f(c);

            vec.add(shift);
            vec.transform(quat);
            vec.sub(shift);
            vec.mul(1 / 16f);

            Vertex v = new Vertex(-vec.x(), -vec.z(), vec.y());
            Integer o = order.size();
            i = (index + di) % 4;
            int u0 = tex_order[i][0];
            int v0 = tex_order[i][1];
            TextureCoordinate t = new TextureCoordinate(face.uv[u0] / us, face.uv[v0] / vs);
            order.add(o);
            verts.add(v);
            tex.add(t);
        }
        materials.put(material, Lists.newArrayList(order, verts, tex));
    }

    private static List<Mesh> makeShapes(BBModelTemplate t, Element b, float[] origin)
    {
        List<Mesh> shapes = new ArrayList<>();

        Map<String, List<List<Object>>> materials = Maps.newHashMap();

        // The vertices and order come from drawing the 6 quads, based on
        // the from and to coordinates of the block

        // The normals can be ignored, as they will be calculated by the mesh
        // and are assumed to be flat shaded.

        // The texture coordinates from from the faces,
        // if they have different coordinates, it makes a new mesh for that face

        addFace(materials, t, b, b.faces.north, Direction.NORTH, origin);
        addFace(materials, t, b, b.faces.east, Direction.EAST, origin);
        addFace(materials, t, b, b.faces.south, Direction.SOUTH, origin);
        addFace(materials, t, b, b.faces.west, Direction.WEST, origin);
        addFace(materials, t, b, b.faces.up, Direction.UP, origin);
        addFace(materials, t, b, b.faces.down, Direction.DOWN, origin);

        materials.forEach((key, lists) -> {
            List<Object> order = lists.get(0);
            List<Object> verts = lists.get(1);
            List<Object> tex = lists.get(2);
            Mesh m = new JsonMesh(order.toArray(new Integer[0]), verts.toArray(new Vertex[0]),
                    tex.toArray(new TextureCoordinate[0]));
            m.name = ThutCore.trim(key);
            Material mat = new Material(m.name);
            m.setMaterial(mat);
            shapes.add(m);
        });
        return shapes;
    }

    public int index = 0;

    public BBModelPart(String name)
    {
        super(name + "");
    }

    @Override
    public String getType()
    {
        return "json";
    }
}
