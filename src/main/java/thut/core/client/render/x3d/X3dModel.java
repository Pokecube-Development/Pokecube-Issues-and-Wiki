package thut.core.client.render.x3d;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import thut.api.entity.animation.Animation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.BaseModel;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.x3d.X3dXML.Appearance;
import thut.core.client.render.x3d.X3dXML.IndexedTriangleSet;
import thut.core.client.render.x3d.X3dXML.Transform;
import thut.core.common.ThutCore;

public class X3dModel extends BaseModel
{
    public X3dModel()
    {
        super();
    }

    public X3dModel(final ResourceLocation l)
    {
        super(l);
    }

    private void addChildren(final Set<Transform> allTransforms, final Transform transform)
    {
        for (final Transform f : transform.transforms) if (!f.DEF.contains("ifs_TRANSFORM"))
        {
            allTransforms.add(f);
            this.addChildren(allTransforms, f);
        }
    }

    private Material getMaterial(final X3dXML.Appearance appearance)
    {
        final X3dXML.Material mat = appearance.material;
        if (mat == null) return null;
        String matName = mat.DEF;
        final boolean isDef = matName != null;
        if (matName == null) matName = mat.USE.substring(3);
        else matName = matName.substring(3);
        matName = ThutCore.trim(matName);
        Material material = this.mats.get(matName);
        if (material == null || isDef)
        {
            String texName;
            if (appearance.tex != null && appearance.tex.DEF != null)
            {
                texName = appearance.tex.DEF.substring(3);
                if (texName.contains("_png")) texName = texName.substring(0, texName.lastIndexOf("_png"));
                texName = ThutCore.trim(texName);
            }
            else texName = null;
            if (material == null) material = new Material(matName, texName, mat.getDiffuse(), mat.getSpecular(),
                    mat.getEmissive(), mat.ambientIntensity, mat.shininess);
            if (isDef)
            {
                if (material.texture == null) material.texture = texName;
                material.ambientIntensity = mat.ambientIntensity;
                material.shininess = mat.shininess;
                material.emissiveColor = mat.getEmissive();
                material.specularColor = mat.getSpecular();
                material.diffuseColor = mat.getDiffuse();
                material.emissiveMagnitude = Math.min(1, (float) (mat.getEmissive().length() / Math.sqrt(3)) / 0.8f);
            }
            this.mats.put(matName, material);
        }
        return material;
    }

    public void loadModel(final ResourceLocation model)
    {
        this.valid = true;
        try
        {
            final Resource res = Minecraft.getInstance().getResourceManager().getResource(model);
            if (res == null)
            {
                this.valid = false;
                return;
            }
            final X3dXML xml = new X3dXML(res.getInputStream());
            res.close();
            this.makeObjects(xml);
        }
        catch (final Exception e)
        {
            this.valid = false;
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading " + model, e);
        }
    }

    private void makeObjects(final X3dXML xml) throws Exception
    {
        final Map<String, Set<String>> childMap = Maps.newHashMap();
        final Set<Transform> allTransforms = Sets.newHashSet();
        for (final Transform f : xml.model.scene.transforms)
        {
            allTransforms.add(f);
            this.addChildren(allTransforms, f);
        }
        for (Transform t : allTransforms)
        {
            String[] offset = t.translation.split(" ");
            final Vector3 translation = new Vector3().set(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]),
                    Float.parseFloat(offset[2]));
            offset = t.scale.split(" ");
            final Vertex scale = new Vertex(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]),
                    Float.parseFloat(offset[2]));
            offset = t.rotation.split(" ");
            final Vector3f axis = new Vector3f(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]),
                    Float.parseFloat(offset[2]));
            final Quaternion quat = new Quaternion(axis, Float.parseFloat(offset[3]), false);
            final Vector4 rotations = new Vector4(quat);

            final Set<String> children = t.getChildNames();
            t = t.getIfsTransform();
            // Probably a lamp or camera in this case?
            if (t == null) continue;
            final X3dXML.Group group = t.group;
            final String name = ThutCore.trim(t.getGroupName());
            final List<Mesh> shapes = Lists.newArrayList();
            for (final X3dXML.Shape shape : group.shapes)
            {
                final IndexedTriangleSet triangleSet = shape.triangleSet;
                final X3dMesh renderShape = new X3dMesh(triangleSet.getOrder(), triangleSet.getVertices(),
                        triangleSet.getNormals(), triangleSet.getTexture());
                shapes.add(renderShape);
                final Appearance appearance = shape.appearance;
                final Material material = this.getMaterial(appearance);
                renderShape.name = name;
                if (material != null) renderShape.setMaterial(material);
            }
            final X3dPart o = new X3dPart(name);
            o.setShapes(shapes);
            o.rotations.set(rotations.x, rotations.y, rotations.z, rotations.w);
            o.offset.set(translation);
            o.scale = scale;
            this.parts.put(name, o);
            childMap.put(name, children);
        }
        for (final Map.Entry<String, Set<String>> entry : childMap.entrySet())
        {
            final String key = entry.getKey();

            if (this.parts.get(key) != null)
            {
                final IExtendedModelPart part = this.parts.get(key);
                for (final String s : entry.getValue())
                    if (this.parts.get(s) != null && this.parts.get(s) != part) part.addChild(this.parts.get(s));
            }
        }
    }

    @Override
    public void preProcessAnimations(final Collection<Animation> animations)
    {
        // a: animation, comps: component lists
        animations.forEach(a -> a.sets.forEach((s, comps) -> comps.forEach(comp -> {
            double d0, d1, d2;
            // These get adjusted so the coordinate system is
            // consistant with the older versions.
            d0 = comp.posOffset[0] / 16;
            d1 = comp.posOffset[1] / 16;
            d2 = comp.posOffset[2] / 16;
            //
            comp.posOffset[0] = -d0;
            comp.posOffset[1] = d2;
            comp.posOffset[2] = -d1;
            //
            d0 = comp.posChange[0] / 16;
            d1 = comp.posChange[1] / 16;
            d2 = comp.posChange[2] / 16;
            //
            comp.posChange[0] = -d0;
            comp.posChange[1] = d2;
            comp.posChange[2] = -d1;

        })));
    }
}
