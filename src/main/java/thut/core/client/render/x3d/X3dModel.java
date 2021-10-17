package thut.core.client.render.x3d;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
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
    public static class Loader implements Runnable
    {
        final X3dModel toLoad;

        final ResourceLocation res;

        public Loader(final X3dModel model, final ResourceLocation res)
        {
            this.toLoad = model;
            this.res = res;
        }

        @Override
        public void run()
        {
            this.toLoad.loadModel(this.res);
            // Flag as loaded before running the callback
            this.toLoad.loaded = true;
            if (this.toLoad.callback != null) this.toLoad.callback.run(this.toLoad);
            this.toLoad.callback = null;
        }
    }

    public HashMap<String, IExtendedModelPart> parts = new HashMap<>();

    private final List<String> order = Lists.newArrayList();
    Map<String, Material>      mats  = Maps.newHashMap();

    Set<String>       heads  = Sets.newHashSet();
    final HeadInfo    info   = new HeadInfo();
    public String     name;
    protected boolean valid  = true;
    protected boolean loaded = false;

    protected IModelCallback callback = null;

    public X3dModel()
    {
        this.valid = true;
    }

    public X3dModel(final ResourceLocation l)
    {
        this();

        try
        {
            // Check if the model even exists
            final Resource res = Minecraft.getInstance().getResourceManager().getResource(l);
            if (res == null)
            {
                this.valid = false;
                return;
            }
            res.close();
            // If it did exist, then lets schedule load on another thread
            final Thread loader = new Thread(new Loader(this, l));
            loader.setName("ThutCore: X3d Load: " + l);
            if (ThutCore.conf.asyncModelLoads) loader.start();
            else loader.run();
        }
        catch (final Exception e)
        {
            // Otherwise mark as invalid and exit
            this.valid = false;
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading " + l, e);
        }

    }

    @Override
    public IModel init(final IModelCallback callback)
    {
        if (this.loaded && this.isValid()) callback.run(this);
        else this.callback = callback;
        return this;
    }

    @Override
    public boolean isLoaded()
    {
        return this.loaded;
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
    public HeadInfo getHeadInfo()
    {
        return this.info;
    }

    @Override
    public Set<String> getHeadParts()
    {
        return this.heads;
    }

    public List<String> getOrder()
    {
        if (this.order.isEmpty() && this.loaded)
        {
            if (this.callback != null) this.callback.run(this);
            this.callback = null;
            IExtendedModelPart.sort(this.order, this.getParts());
            for (final String s : this.order)
            {
                final IExtendedModelPart o = this.parts.get(s);
                o.preProcess();
            }
        }
        return this.order;
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
            if (material == null) material = new Material(matName, texName, mat.getDiffuse(), mat.getSpecular(), mat
                    .getEmissive(), mat.ambientIntensity, mat.shininess);
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
            final Vector3f axis = new Vector3f(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]), Float
                    .parseFloat(offset[2]));
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
                final X3dMesh renderShape = new X3dMesh(triangleSet.getOrder(), triangleSet.getVertices(), triangleSet
                        .getNormals(), triangleSet.getTexture());
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
        return this.parts;
    }

    @Override
    public void preProcessAnimations(final Collection<Animation> animations)
    {
        // a: animation, comps: component lists
        animations.forEach(a -> a.sets.forEach((s, comps) -> comps.forEach(comp ->
        {
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

    @Override
    public void renderAll(final PoseStack mat, final VertexConsumer buffer)
    {
        for (final String s : this.getOrder())
        {
            final IExtendedModelPart o = this.parts.get(s);
            if (o.getParent() == null) o.renderAll(mat, buffer);
        }
    }

    @Override
    public void renderAllExcept(final PoseStack mat, final VertexConsumer buffer, final String... excludedGroupNames)
    {
        for (final String s : this.getOrder())
        {
            final IExtendedModelPart o = this.parts.get(s);
            if (o.getParent() == null) o.renderAllExcept(mat, buffer, excludedGroupNames);
        }
    }

    @Override
    public void renderOnly(final PoseStack mat, final VertexConsumer buffer, final String... groupNames)
    {
        for (final String s : this.getOrder())
        {
            final IExtendedModelPart o = this.parts.get(s);
            if (o.getParent() == null) o.renderOnly(mat, buffer, groupNames);
        }
    }

    @Override
    public void renderPart(final PoseStack mat, final VertexConsumer buffer, final String partName)
    {
        for (final String s : this.getOrder())
        {
            final IExtendedModelPart o = this.parts.get(s);
            if (o.getParent() == null) o.renderPart(mat, buffer, partName);
        }
    }

    @Override
    public void applyAnimation(final Entity entity, final IModelRenderer<?> renderer, final float partialTicks,
            final float limbSwing)
    {
        if (this.getOrder().isEmpty()) return;
        this.updateAnimation(entity, renderer, renderer.getAnimation(entity), partialTicks, this.getHeadInfo().headYaw,
                this.getHeadInfo().headYaw, limbSwing);
    }

    @Override
    public void setAnimationChanger(final IAnimationChanger changer)
    {
        if (this.getOrder().isEmpty()) return;
        for (final IExtendedModelPart part : this.parts.values())
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setAnimationChanger(changer);
    }

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        if (this.getOrder().isEmpty()) return;
        for (final IExtendedModelPart part : this.parts.values())
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
    }

    protected void updateAnimation(final Entity entity, final IModelRenderer<?> renderer, final String currentPhase,
            final float partialTicks, final float headYaw, final float headPitch, final float limbSwing)
    {
        if (this.getOrder().isEmpty()) return;
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
        if (this.getOrder().isEmpty()) return;
        if (parent == null) return;
        final HeadInfo info = this.getHeadInfo();

        parent.resetToInit();
        boolean anim = renderer.getAnimations().containsKey(currentPhase);
        final List<Animation> anims = Lists.newArrayList();

        final IAnimationHolder animHolder = parent.getAnimationHolder();
        if (animHolder != null)
        {
            anims.addAll(animHolder.getPlaying());
            anim = !anims.isEmpty();
        }
        else if (anim) anims.addAll(renderer.getAnimations().get(currentPhase));

        if (anim) AnimationHelper.doAnimation(anims, entity, parent.getName(), parent, partialTick, limbSwing);
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
            if (info.pitchAxis == 2) dir2 = new Vector4(0, 0, info.pitchDirection, ang2);
            else if (info.pitchAxis == 1) dir2 = new Vector4(0, info.pitchDirection, 0, ang2);
            else dir2 = new Vector4(info.yawDirection, 0, 0, ang2);
            final Vector4 combined = new Vector4();
            combined.mul(dir.toQuaternion(), dir2.toQuaternion());
            parent.setPostRotations(combined);
        }
        for (final String partName : parent.getSubParts().keySet())
        {
            final IExtendedModelPart part = parent.getSubParts().get(partName);
            this.updateSubParts(entity, renderer, currentPhase, partialTick, part, headYaw, headPitch, limbSwing);
        }
    }
}
