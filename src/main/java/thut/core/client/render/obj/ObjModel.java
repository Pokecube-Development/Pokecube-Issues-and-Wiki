package thut.core.client.render.obj;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animators.KeyframeAnimator;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

public class ObjModel implements IModelCustom, IModel, IRetexturableModel
{
    public HashMap<String, IExtendedModelPart> parts = new HashMap<>();
    Map<String, Material> mats = new HashMap<>();
    Set<String> heads = new HashSet<>();
    List<String> order = new ArrayList<>();
    public String name;
    private boolean valid = true;

    public ObjModel()
    {
        this.valid = false;
    }

    public ObjModel(final ResourceLocation l)
    {
        this();
        this.loadModel(l);
    }

    @Override
    public void applyAnimation(final Entity entity, final IModelRenderer<?> renderer, final float partialTicks,
            final float limbSwing)
    {
        // this.updateAnimation(entity, renderer, renderer.getAnimation(entity),
        // partialTicks, this.getHeadInfo().headYaw,
        // this.getHeadInfo().headYaw, limbSwing, 0);
    }

    @Override
    public Set<String> getHeadParts()
    {
        return this.heads;
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
            InputStream stream = ResourceHelper.getStream(model, Minecraft.getInstance().getResourceManager());
            if (stream == null)
            {
                this.valid = false;
                return;
            }
            this.makeObjects(stream);
            stream.close();
        }
        catch (final Exception e)
        {
            this.valid = false;
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading " + model, e.getMessage());
        }
    }

    private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");

    public HashMap<String, IExtendedModelPart> makeObjects(final InputStream stream) throws Exception
    {
        final InputStreamReader isReader = new InputStreamReader(stream);
        // Creating a BufferedReader object
        final BufferedReader reader = new BufferedReader(isReader);
        String line;

        final List<Vertex> vertices = Lists.newArrayList();
        final List<Vertex> normals = Lists.newArrayList();
        final List<TextureCoordinate> tex = Lists.newArrayList();
        final List<int[][]> faces = Lists.newArrayList();

        String currentPart = "";

        while ((line = reader.readLine()) != null)
        {
            if (line.startsWith("#") || line.trim().isEmpty()) continue;
            final String[] fields = ObjModel.WHITE_SPACE.split(line, 2);
            final String key = fields[0];
            final String data = fields[1];
            final String[] splitData = ObjModel.WHITE_SPACE.split(data);

            if (key.equalsIgnoreCase("o"))
            {
                if (!currentPart.isEmpty())
                {
                    final List<Vertex> vertices2 = Lists.newArrayList();
                    final List<Vertex> normals2 = Lists.newArrayList();
                    final List<TextureCoordinate> tex2 = Lists.newArrayList();
                    final List<Integer> order = Lists.newArrayList();
                    int i = 0;
                    for (final int[][] facepair : faces) for (final int[] element : facepair)
                    {
                        final Vertex v = vertices.get(element[0] - 1);
                        final TextureCoordinate coord = tex.get(element[1] - 1);
                        Vertex norm = new Vertex(0, 0);
                        if (normals.size() > element[0] - 1) norm = normals.get(element[0] - 1);
                        vertices2.add(v);
                        normals2.add(norm);
                        tex2.add(coord);
                        order.add(i++);
                    }
                    final ObjMesh mesh = new ObjMesh(order.toArray(new Integer[0]), vertices2.toArray(new Vertex[0]),
                            normals2.toArray(new Vertex[0]), tex2.toArray(new TextureCoordinate[0]));
                    final ObjPart part = new ObjPart(currentPart);
                    part.addShape(mesh);
                    this.parts.put(currentPart, part);
                }
                currentPart = data;
            }
            if (key.equalsIgnoreCase("v"))
            {
                final float[] coords = this.parseFloats(splitData);
                final Vertex pos = new Vertex(coords[0], coords[1] + 1.5f, coords[2]);
                vertices.add(pos);
            }
            else if (key.equalsIgnoreCase("vn"))
            {
                final float[] coords = this.parseFloats(splitData);
                final Vertex pos = new Vertex(coords[0], coords[1], coords[2]);
                normals.add(pos);
            }
            else if (key.equalsIgnoreCase("vt"))
            {
                final float[] coords = this.parseFloats(splitData);
                final TextureCoordinate pos = new TextureCoordinate(coords[0], 1 - coords[1]);
                tex.add(pos);
            }
            else if (key.equals("f"))
            {
                final int[][] facepairs = new int[splitData.length][2];
                for (int i = 0; i < splitData.length; i++)
                {
                    final String[] pts = splitData[i].split("/");
                    facepairs[i][0] = Integer.parseInt(pts[0]);
                    facepairs[i][1] = Integer.parseInt(pts[1]);
                }
                faces.add(facepairs);
            }
        }
        this.order.addAll(this.parts.keySet());
        return this.parts;
    }

    private float[] parseFloats(final String[] data) // Helper converting
    // strings to floats
    {
        final float[] ret = new float[data.length];
        for (int i = 0; i < data.length; i++) ret[i] = Float.parseFloat(data[i]);
        return ret;
    }

    @Override
    public void preProcessAnimations(final Collection<Animation> animations)
    {
        // a: animation, comps: component lists
        animations.forEach(a -> a.sets.forEach((s, anim) -> {
            if (anim instanceof KeyframeAnimator a2)
            {
                a2.components.forEach(comp -> {
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
                });
            }
        }));
    }

    @Override
    public void renderAll(final PoseStack mat, final VertexConsumer buffer)
    {
        for (final IExtendedModelPart o : this.parts.values()) if (o.getParent() == null) o.renderAll(mat, buffer);
    }

    @Override
    public void renderAllExcept(final PoseStack mat, final VertexConsumer buffer,
            final Collection<String> excludedGroupNames)
    {
        for (final IExtendedModelPart o : this.parts.values())
            if (o.getParent() == null) o.renderAllExcept(mat, buffer, excludedGroupNames);
    }

    @Override
    public void renderOnly(final PoseStack mat, final VertexConsumer buffer, final Collection<String> groupNames)
    {
        for (final IExtendedModelPart o : this.parts.values())
            if (o.getParent() == null) o.renderOnly(mat, buffer, groupNames);
    }

    @Override
    public void renderPart(final PoseStack mat, final VertexConsumer buffer, final String partName)
    {
        for (final IExtendedModelPart o : this.parts.values())
            if (o.getParent() == null) o.renderPart(mat, buffer, partName);
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
            final float partialTicks, final float headYaw, final float headPitch, final float limbSwing,
            final int brightness)
    {
        for (final String partName : this.getParts().keySet())
        {
            final IExtendedModelPart part = this.getParts().get(partName);
            this.updateSubParts(entity, renderer, currentPhase, partialTicks, part, headYaw, headPitch, limbSwing,
                    brightness);
        }
    }

    @Override
    public void globalFix(final PoseStack mat, final float dx, final float dy, final float dz)
    {
        // FIXME obj rotation
        mat.mulPose(Axis.XP.rotationDegrees(180));
        mat.mulPose(Axis.YP.rotationDegrees(180));
        mat.translate(0, -1.5, 0);
    }

    private void updateSubParts(final Entity entity, final IModelRenderer<?> renderer, final String currentPhase,
            final float partialTick, final IExtendedModelPart parent, final float headYaw, final float headPitch,
            final float limbSwing, final int brightnessIn)
    {
        if (parent == null) return;

        final HeadInfo info = renderer.getAnimationHolder().getHeadInfo();

        parent.resetToInit();
        final boolean anim = renderer.getAnimations().containsKey(currentPhase);
        if (anim) if (AnimationHelper.doAnimation(renderer.getAnimations().get(currentPhase),
                renderer.getAnimationHolder(), entity, parent, partialTick, limbSwing))
        {}
        if (this.isHead(parent.getName()))
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
            final Vector4 combined = new Vector4();
            combined.mul(dir.toQuaternion(), dir2.toQuaternion());
            parent.setPostRotations(combined);
        }
        for (final String partName : parent.getSubParts().keySet())
        {
            final IExtendedModelPart part = parent.getSubParts().get(partName);
            this.updateSubParts(entity, renderer, currentPhase, partialTick, part, headYaw, headPitch, limbSwing,
                    brightnessIn);
        }
    }

    @Override
    public List<String> getRenderOrder()
    {
        return order;
    }

    @Override
    public void setTexturerRaw(IPartTexturer texturer)
    {
        // We do nothing here, as raw does not filter to sub parts.
    }

    @Override
    public void setAnimationChangerRaw(IAnimationChanger changer)
    {
        // We do nothing here, as raw does not filter to sub parts.
    }
}
