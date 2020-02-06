package thut.core.client.render.x3d;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationComponent;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.client.render.x3d.X3dXML.Appearance;
import thut.core.client.render.x3d.X3dXML.IndexedTriangleSet;
import thut.core.client.render.x3d.X3dXML.Transform;
import thut.core.common.ThutCore;

public class X3dModel implements IModelCustom, IModel, IRetexturableModel
{
    public HashMap<String, IExtendedModelPart> parts = new HashMap<>();
    Map<String, Material>                      mats  = Maps.newHashMap();
    Set<String>                                heads = Sets.newHashSet();
    final HeadInfo                             info  = new HeadInfo();
    public String                              name;
    protected boolean                          valid = true;

    public X3dModel()
    {
        this.valid = false;
    }

    public X3dModel(final ResourceLocation l)
    {
        this();
        this.loadModel(l);
    }

    private void addChildren(final Set<Transform> allTransforms, final Transform transform)
    {
        for (final Transform f : transform.transforms)
            if (!f.DEF.contains("ifs_TRANSFORM"))
            {
                allTransforms.add(f);
                this.addChildren(allTransforms, f);
            }
    }

    @Override
    public void applyAnimation(final Entity entity, final IAnimationHolder animate, final IModelRenderer<?> renderer,
            final float partialTicks, final float limbSwing)
    {
        this.updateAnimation(entity, renderer, renderer.getAnimation(entity), partialTicks, this.getHeadInfo().headYaw,
                this.getHeadInfo().headYaw, limbSwing);
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return this.info;
    }

    @Override
    public Set<String> getHeadParts()
    {
        return this.heads;
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
            }
            else texName = null;
            if (material == null) material = new Material(matName, texName, mat.getDiffuse(), mat.getSpecular(), mat
                    .getEmissive(), mat.ambientIntensity, mat.shininess, mat.transparency);
            if (isDef)
            {
                if (material.texture == null) material.texture = texName;
                material.ambientIntensity = mat.ambientIntensity;
                material.shininess = mat.shininess;
                material.transparency = mat.transparency;
                material.emissiveColor = mat.getEmissive();
                material.specularColor = mat.getSpecular();
                material.diffuseColor = mat.getDiffuse();
                material.emissiveMagnitude = Math.min(1, (float) (mat.getEmissive().length() / Math.sqrt(3)) / 0.8f);
            }
            this.mats.put(matName, material);
        }
        return material;
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        return this.parts;
    }

    private boolean isHead(final String partName)
    {
        return this.getHeadParts().contains(partName);
    }

    @Override
    public boolean isValid()
    {
        return this.valid;
    }

    public void loadModel(final ResourceLocation model)
    {
        this.valid = true;
        try
        {
            final IResource res = Minecraft.getInstance().getResourceManager().getResource(model);
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

    public HashMap<String, IExtendedModelPart> makeObjects(final X3dXML xml) throws Exception
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
            final Vector3 translation = Vector3.getNewVector().set(Float.parseFloat(offset[0]), Float.parseFloat(
                    offset[1]), Float.parseFloat(offset[2]));
            offset = t.scale.split(" ");
            final Vertex scale = new Vertex(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]), Float.parseFloat(
                    offset[2]));
            offset = t.rotation.split(" ");
            final Vector4 rotations = new Vector4(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]), Float
                    .parseFloat(offset[2]), (float) Math.toDegrees(Float.parseFloat(offset[3])));
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
                final X3dMesh renderShape = new X3dMesh(triangleSet.getOrder(), triangleSet.getVertices(), triangleSet
                        .getNormals(), triangleSet.getTexture());
                shapes.add(renderShape);
                final Appearance appearance = shape.appearance;
                final Material material = this.getMaterial(appearance);
                if (material != null) renderShape.setMaterial(material);
            }
            final X3dPart o = new X3dPart(name);
            o.shapes = shapes;
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
        return this.parts;
    }

    @Override
    public void preProcessAnimations(final Collection<List<Animation>> animations)
    {
        for (final List<Animation> list : animations)
            for (final Animation animation : list)
                for (final String s : animation.sets.keySet())
                {
                    final ArrayList<AnimationComponent> components = animation.sets.get(s);
                    for (final AnimationComponent comp : components)
                    {
                        comp.posOffset[0] /= -16;
                        comp.posOffset[1] /= -16;
                        comp.posOffset[2] /= -16;
                        comp.posChange[0] /= -16;
                        comp.posChange[1] /= -16;
                        comp.posChange[2] /= -16;
                    }
                }
    }

    @Override
    public void renderAll()
    {
        for (final IExtendedModelPart o : this.parts.values())
            if (o.getParent() == null) o.renderAll();
    }

    @Override
    public void renderAllExcept(final String... excludedGroupNames)
    {
        for (final IExtendedModelPart o : this.parts.values())
            if (o.getParent() == null) o.renderAllExcept(excludedGroupNames);
    }

    @Override
    public void renderOnly(final String... groupNames)
    {
        for (final IExtendedModelPart o : this.parts.values())
            if (o.getParent() == null) o.renderOnly(groupNames);
    }

    @Override
    public void renderPart(final String partName)
    {
        for (final IExtendedModelPart o : this.parts.values())
            if (o.getParent() == null) o.renderPart(partName);
    }

    @Override
    public void setAnimationChanger(final IAnimationChanger changer)
    {
        for (final IExtendedModelPart part : this.parts.values())
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setAnimationChanger(changer);
    }

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        for (final IExtendedModelPart part : this.parts.values())
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
    }

    protected void updateAnimation(final Entity entity, final IModelRenderer<?> renderer, final String currentPhase,
            final float partialTicks, final float headYaw, final float headPitch, final float limbSwing)
    {
        for (final String partName : this.getParts().keySet())
        {
            final IExtendedModelPart part = this.getParts().get(partName);
            this.updateSubParts(entity, renderer, currentPhase, partialTicks, part, headYaw, headPitch, limbSwing);
        }
    }

    private void updateSubParts(final Entity entity, final IModelRenderer<?> renderer, final String currentPhase,
            final float partialTick, final IExtendedModelPart parent, final float headYaw, final float headPitch,
            final float limbSwing)
    {
        if (parent == null) return;
        final HeadInfo info = this.getHeadInfo();

        parent.resetToInit();
        final boolean anim = renderer.getAnimations().containsKey(currentPhase);
        if (anim) if (AnimationHelper.doAnimation(renderer.getAnimations().get(currentPhase), entity, parent.getName(),
                parent, partialTick, limbSwing))
        {
        }
        if (info != null && this.isHead(parent.getName()))
        {
            float ang;
            float ang2 = -info.headPitch;
            float head = info.headYaw + 180;
            float diff = 0;
            if (info.yawDirection != -1) head *= -1;
            diff = head % 360;
            diff = (diff + 360) % 360;
            diff = (diff - 180) % 360;
            diff = Math.max(diff, info.yawCapMin);
            diff = Math.min(diff, info.yawCapMax);
            ang = diff;
            ang2 = Math.max(ang2, info.pitchCapMin);
            ang2 = Math.min(ang2, info.pitchCapMax);
            Vector4 dir;
            if (info.yawAxis == 0) dir = new Vector4(info.yawDirection, 0, 0, ang);
            else if (info.yawAxis == 2) dir = new Vector4(0, 0, info.yawDirection, ang);
            else dir = new Vector4(0, info.yawDirection, 0, ang);
            Vector4 dir2;
            if (info.pitchAxis == 2) dir2 = new Vector4(0, 0, info.yawDirection, ang2);
            else if (info.pitchAxis == 1) dir2 = new Vector4(0, info.yawDirection, 0, ang2);
            else dir2 = new Vector4(info.yawDirection, 0, 0, ang2);
            parent.setPostRotations(dir);
            parent.setPostRotations2(dir2);
        }

        final int red = 255, green = 255, blue = 255;
        final int brightness = entity.getBrightnessForRender();
        final int alpha = 255;
        final int[] rgbab = parent.getRGBAB();
        if (entity instanceof IMobColourable)
        {
            final IMobColourable poke = (IMobColourable) entity;
            rgbab[0] = poke.getRGBA()[0];
            rgbab[1] = poke.getRGBA()[1];
            rgbab[2] = poke.getRGBA()[2];
            rgbab[3] = poke.getRGBA()[3];
        }
        else
        {
            rgbab[0] = red;
            rgbab[1] = green;
            rgbab[2] = blue;
            rgbab[3] = alpha;
            rgbab[4] = brightness;
        }
        rgbab[4] = brightness;
        final IAnimationChanger animChanger = renderer.getAnimationChanger();
        if (animChanger != null)
        {
            final int default_ = new Color(rgbab[0], rgbab[1], rgbab[2], rgbab[3]).getRGB();
            final int rgb = animChanger.getColourForPart(parent.getName(), entity, default_);
            if (rgb != default_)
            {
                final Color col = new Color(rgb);
                rgbab[0] = col.getRed();
                rgbab[1] = col.getGreen();
                rgbab[2] = col.getBlue();
            }
        }
        parent.setRGBAB(rgbab);
        for (final String partName : parent.getSubParts().keySet())
        {
            final IExtendedModelPart part = (IExtendedModelPart) parent.getSubParts().get(partName);
            this.updateSubParts(entity, renderer, currentPhase, partialTick, part, headYaw, headPitch, limbSwing);
        }
    }
}
