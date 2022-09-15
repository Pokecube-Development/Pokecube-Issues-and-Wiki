package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.math.Quaternion;

import net.minecraft.core.Direction;
import thut.api.maths.Vector4;
import thut.core.client.render.bbmodel.BBModelTemplate.Element;
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

    public static void makeParts(BBModelTemplate t, JsonGroup group, List<BBModelPart> parts,
            List<BBModelPart> children, float[] offsets)
    {
        children.clear();
        int i = 0;
        List<BBModelPart> ours = new ArrayList<>();
        BBModelPart root = null;
        // Handle parts first
        for (Object o : group.children)
        {
            if (o instanceof Element b)
            {
                String name = i == 0 ? group.name : group.name + "_" + i;
                float[] use = group.origin;

                // This may cause problems later, we shall see.
                b.origin = use;

                List<Mesh> shapes = makeShapes(t, b, use);
                BBModelPart part = make(shapes, name, b, i, offsets);
                if (root == null) root = part;
                parts.add(part);
                ours.add(part);
                i++;
            }
        }

        offsets[0] = -group.origin[0];
        offsets[1] = -group.origin[1];
        offsets[2] = -group.origin[2];
        // then handle groups
        for (Object o : group.children)
        {
            if (o instanceof JsonGroup g)
            {
                makeParts(t, g, parts, children, offsets.clone());
                if (root != null) children.forEach(root::addChild);
            }
        }
        children.clear();
        children.addAll(ours);
    }

    private static BBModelPart make(List<Mesh> shapes, String name, Element b, int index, float[] offsets2)
    {
        BBModelPart part = new BBModelPart(name);
        part.index = index;
        shapes.forEach(part::addShape);
        float[] offsets = b.origin.clone();

        offsets[0] += offsets2[0];
        offsets[1] += offsets2[1];
        offsets[2] += offsets2[2];
        if (b.rotation != null)
        {
            float x = b.rotation[0];
            float y = b.rotation[2];
            float z = b.rotation[1];
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
            JsonFace face, Direction dir, float[] offsets)
    {
        if (face == null || b == null) return;
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
        else
        {
            materials.put(material, Lists.newArrayList(order, verts, tex));
        }

        float[] from = b.from.clone();
        float[] to = b.to.clone();

        from[0] -= offsets[0];
        from[1] -= offsets[1];
        from[2] -= offsets[2];

        to[0] -= offsets[0];
        to[1] -= offsets[1];
        to[2] -= offsets[2];

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

        float us = template.resolution.width;
        float vs = template.resolution.height;

        int di = face.rotation / 90;

        for (int index = 0; index < 4; index++)
        {
            int i = index;
            float[] c = coords[i];
            Vertex v = new Vertex(c[0] / 16f, -c[2] / 16f, c[1] / 16f);
            Integer o = order.size();
            i = (index + di) % 4;
            int u0 = tex_order[i][0];
            int v0 = tex_order[i][1];
            TextureCoordinate t = new TextureCoordinate(face.uv[u0] / us, face.uv[v0] / vs);
            order.add(o);
            verts.add(v);
            tex.add(t);
        }
    }

    private static List<Mesh> makeShapes(BBModelTemplate t, Element b, float[] offsets)
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
