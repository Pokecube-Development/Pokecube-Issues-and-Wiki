package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.math.Quaternion;

import pokecube.api.PokecubeAPI;
import thut.api.maths.Vector4;
import thut.core.client.render.bbmodel.BBModelTemplate.BBModelBox;
import thut.core.client.render.bbmodel.BBModelTemplate.Element;
import thut.core.client.render.bbmodel.BBModelTemplate.IBBPart;
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
        for (int i = 0; i < 3; i++)
        {
            parentOffsets[i] = -group.origin[i];
        }
        for (Object o : group.children)
        {
            if (o instanceof JsonGroup g)
            {
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
        part.setShapes(shapes);
        float[] offsets = b.getOrigin().clone();
        for (int i = 0; i < 3; i++)
        {
            offsets[i] += parentOffsets[i];
        }
        if (b.getRotation() != null)
        {
            float x = b.getRotation()[0];
            float y = -b.getRotation()[2];
            float z = b.getRotation()[1];
            Quaternion quat = new Quaternion(x, y, z, true);
            final Vector4 rotations = new Vector4(quat);
            part.rotations.set(rotations.x, rotations.y, rotations.z, rotations.w);
        }

        offsets[0] /= 16;
        offsets[1] /= 16;
        offsets[2] /= 16;

        float[] use = offsets.clone();
        use[0] = offsets[0];
        use[1] = -offsets[2];
        use[2] = offsets[1];

        part.offset.set(use);
        return part;
    }

    private static List<Mesh> makeShapes(BBModelTemplate t, Element b, float[] origin)
    {
        List<Mesh> shapes = new ArrayList<>();

        Map<String, List<List<Object>>> materials = Maps.newHashMap();

        BBModelBox box = new BBModelBox(t, b);

        boolean bedrock = t.meta.model_format.equals("bedrock");
        for (var face : box.faces)
        {
            if (face == null) continue;
            List<Object> order = Lists.newArrayList();
            List<Object> verts = Lists.newArrayList();
            List<Object> tex = Lists.newArrayList();
            String material = t.textures.get(face.texture).name;
            if (materials.containsKey(material))
            {
                List<List<Object>> lists = materials.get(material);
                order = lists.get(0);
                verts = lists.get(1);
                tex = lists.get(2);
            }
            for (int i = 0; i < 4; i++)
            {
                int index = i;
                if (bedrock) index = 3 - i;

                Integer o = order.size();
                Vertex v = face.points[index];
                var tx = face.tex[index];
                order.add(o);
                verts.add(v);
                tex.add(tx);
            }
            materials.put(material, Lists.newArrayList(order, verts, tex));
        }

        if (materials.isEmpty()) PokecubeAPI.logDebug("No parts for " + t.name + " " + b.name);

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
