package thut.core.client.render.bbmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import thut.api.maths.Vector4;
import thut.core.client.render.bbmodel.BBModelTemplate.Element;
import thut.core.client.render.bbmodel.BBModelTemplate.IBBPart;
import thut.core.client.render.bbmodel.BBModelTemplate.JsonGroup;
import thut.core.client.render.json.JsonMesh;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.model.parts.Part;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.client.render.x3d.X3dMesh;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;

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
        BBModelPart root = make(t, allShapes, nextName(names, group), group, -1, parentOffsets);
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

    private static BBModelPart make(BBModelTemplate template, List<Mesh> shapes, String name, IBBPart b, int index,
            float[] parentOffsets)
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
            float y = b.getRotation()[1];
            float z = b.getRotation()[2];
            part.rotations.set(x, y, z, 1);
        }

        offsets[0] /= 16.0f;
        offsets[1] /= 16.0f;
        offsets[2] /= 16.0f;

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

        Map<String, List<List<Object>>> quads_materials = new HashMap<>();
        Map<String, List<List<Object>>> tris_materials = new HashMap<>();

        float[] oldRots = b.rotation;
        float[] newRots = oldRots;
        if (oldRots != null && !t.meta.model_format.equals("bedrock") && !b.box_uv)
        {
            newRots = Arrays.copyOf(oldRots, 3);

            Quaternion quat = new Quaternion(0, 0, 0, 1);
            float x = b.getRotation()[0];
            float y = b.getRotation()[1];
            float z = b.getRotation()[2];

            if (b.type.equals("cube"))
            {
                if (y != 0) quat.mul(AxisAngles.YP.rotationDegrees(y));
                if (x != 0) quat.mul(AxisAngles.XP.rotationDegrees(x));
                if (z != 0) quat.mul(AxisAngles.ZP.rotationDegrees(z));
            }
            else if (b.type.equals("mesh"))
            {
                if (x != 0) quat.mul(AxisAngles.XP.rotationDegrees(x));
                if (y != 0) quat.mul(AxisAngles.YP.rotationDegrees(y));
                if (z != 0) quat.mul(AxisAngles.ZP.rotationDegrees(z));
            }

            Vector3f xyz = quat.toYXZDegrees();
            newRots[0] = xyz.x();
            newRots[1] = xyz.y();
            newRots[2] = xyz.z();
        }

        b.rotation = newRots;
        b.toMeshs(t, quads_materials, tris_materials);
        b.rotation = oldRots;

        if (quads_materials.isEmpty() && tris_materials.isEmpty() && !b.type.equals("locator"))
            ThutCore.logDebug("No parts for " + t.name + " " + b.name);
        var mats = t._materials;

        quads_materials.forEach((key, lists) -> {
            List<Object> order = lists.get(0);
            List<Object> verts = lists.get(1);
            List<Object> tex = lists.get(2);
            Mesh m = new JsonMesh(order.toArray(new Integer[0]), verts.toArray(new Vertex[0]),
                    tex.toArray(new TextureCoordinate[0]));
            m.name = ThutCore.trim(key);
            Material mat = mats.getOrDefault(m.name, new Material(m.name));
            mat.expectedTexH = t.resolution.height;
            mat.expectedTexW = t.resolution.width;
            mats.put(m.name, mat);
            if (b.box_uv || t.meta.box_uv) mat.cull = true;
            m.setMaterial(mat);
            shapes.add(m);
        });

        tris_materials.forEach((key, lists) -> {
            List<Object> order = lists.get(0);
            List<Object> verts = lists.get(1);
            List<Object> tex = lists.get(2);

            Mesh m = new X3dMesh(order.toArray(new Integer[0]), verts.toArray(new Vertex[0]), null,
                    tex.toArray(new TextureCoordinate[0]));
            m.name = ThutCore.trim(key);
            Material mat = mats.getOrDefault(m.name, new Material(m.name));
            mat.expectedTexH = t.resolution.height;
            mat.expectedTexW = t.resolution.width;
            mats.put(m.name, mat);
            if (b.box_uv || t.meta.box_uv) mat.cull = true;
            m.setMaterial(mat);
            shapes.add(m);
        });

        return shapes;
    }

    public int index = 0;
    private float rx = 0, ry = 0, rz = 0;

    public BBModelPart(String name)
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
