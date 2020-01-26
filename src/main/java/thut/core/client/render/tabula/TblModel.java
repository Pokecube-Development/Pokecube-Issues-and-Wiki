package thut.core.client.render.tabula;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

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
import thut.core.client.render.tabula.json.CubeGroup;
import thut.core.client.render.tabula.json.CubeInfo;
import thut.core.client.render.tabula.json.TblJson;
import thut.core.client.render.tabula.old.JsonFactory;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.common.ThutCore;

public class TblModel implements IModelCustom, IModel, IRetexturableModel
{
    public static final Integer[] ORDER = new Integer[24];
    static
    {
        for (int i = 0; i < 24; i++)
            TblModel.ORDER[i] = i;
    }

    public static Material                     MAT   = new Material("tblmodel");
    public HashMap<String, IExtendedModelPart> parts = new HashMap<>();
    Map<String, Material>                      mats  = Maps.newHashMap();
    Set<String>                                heads = Sets.newHashSet();
    final HeadInfo                             info  = new HeadInfo();
    public String                              name;
    private boolean                            valid = true;

    public TblModel()
    {
        this.valid = false;
    }

    public TblModel(final ResourceLocation l)
    {
        this();
        this.loadModel(l);
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
            final ZipInputStream zip = new ZipInputStream(res.getInputStream());
            final Scanner scanner = new Scanner(zip);
            zip.getNextEntry();
            final String json = scanner.nextLine();
            scanner.close();
            final InputStream in = IOUtils.toInputStream(json, "UTF-8");
            final InputStreamReader reader = new InputStreamReader(in);
            final TblJson xml = JsonFactory.getGson().fromJson(reader, TblJson.class);
            res.close();
            this.makeObjects(xml);
        }
        catch (final Exception e)
        {
            this.valid = false;
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading " + model, e);
        }
    }

    private void handleCubeGroup(final TblJson json, final CubeGroup group)
    {
        for (final CubeInfo cube : group.cubes)
            this.handleCubeInfo(json, cube, null);
        for (final CubeGroup group1 : group.cubeGroups)
            this.handleCubeGroup(json, group1);
    }

    private void handleCubeInfo(final TblJson json, final CubeInfo cube, final TblPart parent)
    {
        final Vertex[] verteces = new Vertex[24];
        final Vertex[] normals = new Vertex[24];
        final TextureCoordinate[] tex = new TextureCoordinate[24];
        int i = 0;
        // Top face
        verteces[i++] = new Vertex(1.0f, 1.0f, -1.0f);
        verteces[i++] = new Vertex(-1.0f, 1.0f, -1.0f);
        verteces[i++] = new Vertex(-1.0f, 1.0f, 1.0f);
        verteces[i++] = new Vertex(1.0f, 1.0f, 1.0f);

        // Bottom face
        verteces[i++] = new Vertex(1.0f, -1.0f, -1.0f);
        verteces[i++] = new Vertex(-1.0f, -1.0f, -1.0f);
        verteces[i++] = new Vertex(-1.0f, -1.0f, 1.0f);
        verteces[i++] = new Vertex(1.0f, -1.0f, 1.0f);

        // Front face
        verteces[i++] = new Vertex(1.0f, 1.0f, 1.0f);
        verteces[i++] = new Vertex(-1.0f, 1.0f, 1.0f);
        verteces[i++] = new Vertex(-1.0f, -1.0f, 1.0f);
        verteces[i++] = new Vertex(1.0f, -1.0f, 1.0f);

        // Back face
        verteces[i++] = new Vertex(1.0f, -1.0f, -1.0f);
        verteces[i++] = new Vertex(-1.0f, -1.0f, -1.0f);
        verteces[i++] = new Vertex(-1.0f, 1.0f, -1.0f);
        verteces[i++] = new Vertex(1.0f, 1.0f, -1.0f);

        // Left face
        verteces[i++] = new Vertex(-1.0f, 1.0f, 1.0f);
        verteces[i++] = new Vertex(-1.0f, 1.0f, -1.0f);
        verteces[i++] = new Vertex(-1.0f, -1.0f, -1.0f);
        verteces[i++] = new Vertex(-1.0f, -1.0f, 1.0f);

        // Right face
        verteces[i++] = new Vertex(1.0f, 1.0f, 1.0f);
        verteces[i++] = new Vertex(1.0f, 1.0f, -1.0f);
        verteces[i++] = new Vertex(1.0f, -1.0f, -1.0f);
        verteces[i++] = new Vertex(1.0f, -1.0f, 1.0f);

        final float maxU = json.getTextureWidth();
        final float maxV = json.getTextureHeight();

        final float u0 = cube.txOffset[0] / maxU;
        final float v0 = cube.txOffset[0] / maxV;

        for (i = 0; i < 24; i++)
        {
            normals[i] = new Vertex(0, 0, 0);
            final float dx = cube.dimensions[0];
            final float dy = cube.dimensions[1];
            final float dz = cube.dimensions[2];

            verteces[i].x *= dx / 16f;// * cube.scale[0] / 16f;
            verteces[i].y *= dy / 16f;// * cube.scale[1] / 16f;
            verteces[i].z *= dz / 16f;// * cube.scale[2] / 16f;
            tex[i] = new TextureCoordinate(u0, v0);
            switch (i / 4)
            {
            case 0: // top
                // This doesn't care about dz, just dx/dy.
                switch (i % 4)
                {
                case 0:
                    tex[i].u = u0 + 2 * dx / maxU;
                    tex[i].v = v0 + 0 * dy / maxV;
                    break;
                case 1:
                    tex[i].u = u0 + 1 * dx / maxU;
                    tex[i].v = v0 + 0 * dy / maxV;
                    break;
                case 2:
                    tex[i].u = u0 + 1 * dx / maxU;
                    tex[i].v = v0 + 1 * dy / maxV;
                    break;
                case 3:
                    tex[i].u = u0 + 2 * dx / maxU;
                    tex[i].v = v0 + 1 * dy / maxV;
                    break;
                }
                break;
            case 1: // bottom
                // This doesn't care about dz, just dx/dy.
                switch (i % 4)
                {
                case 0:
                    tex[i].u = u0 + 3 * dx / maxU;
                    tex[i].v = v0 + 0 * dy / maxV;
                    break;
                case 1:
                    tex[i].u = u0 + 2 * dx / maxU;
                    tex[i].v = v0 + 0 * dy / maxV;
                    break;
                case 2:
                    tex[i].u = u0 + 2 * dx / maxU;
                    tex[i].v = v0 + 1 * dy / maxV;
                    break;
                case 3:
                    tex[i].u = u0 + 3 * dx / maxU;
                    tex[i].v = v0 + 1 * dy / maxV;
                    break;
                }
                break;
            case 2: // front
                switch (i % 4)
                {
                case 0:
                    tex[i].u = u0 + 2 * dx / maxU;
                    tex[i].v = v0 + 1 * dz / maxV;
                    break;
                case 1:
                    tex[i].u = u0 + 1 * dx / maxU;
                    tex[i].v = v0 + 1 * dz / maxV;
                    break;
                case 2:
                    tex[i].u = u0 + 1 * dx / maxU;
                    tex[i].v = v0 + 2 * dz / maxV;
                    break;
                case 3:
                    tex[i].u = u0 + 2 * dx / maxU;
                    tex[i].v = v0 + 2 * dz / maxV;
                    break;
                }
                break;
            case 3: // back
                switch (i % 4)
                {
                case 0:
                    tex[i].u = u0 + 4 * dx / maxU;
                    tex[i].v = v0 + 1 * dz / maxV;
                    break;
                case 1:
                    tex[i].u = u0 + 3 * dx / maxU;
                    tex[i].v = v0 + 1 * dz / maxV;
                    break;
                case 2:
                    tex[i].u = u0 + 3 * dx / maxU;
                    tex[i].v = v0 + 2 * dz / maxV;
                    break;
                case 3:
                    tex[i].u = u0 + 4 * dx / maxU;
                    tex[i].v = v0 + 2 * dz / maxV;
                    break;
                }
                break;
            case 4: // left
                switch (i % 4)
                {
                case 0:
                    tex[i].u = u0 + 1 * dy / maxU;
                    tex[i].v = v0 + 1 * dz / maxV;
                    break;
                case 1:
                    tex[i].u = u0 + 0 * dy / maxU;
                    tex[i].v = v0 + 1 * dz / maxV;
                    break;
                case 2:
                    tex[i].u = u0 + 0 * dy / maxU;
                    tex[i].v = v0 + 2 * dz / maxV;
                    break;
                case 3:
                    tex[i].u = u0 + 1 * dy / maxU;
                    tex[i].v = v0 + 2 * dz / maxV;
                    break;
                }
                break;
            case 5: // right
                switch (i % 4)
                {
                case 0:
                    tex[i].u = u0 + 3 * dy / maxU;
                    tex[i].v = v0 + 1 * dz / maxV;
                    break;
                case 1:
                    tex[i].u = u0 + 2 * dy / maxU;
                    tex[i].v = v0 + 1 * dz / maxV;
                    break;
                case 2:
                    tex[i].u = u0 + 2 * dy / maxU;
                    tex[i].v = v0 + 2 * dz / maxV;
                    break;
                case 3:
                    tex[i].u = u0 + 3 * dy / maxU;
                    tex[i].v = v0 + 2 * dz / maxV;
                    break;
                }
                break;
            }
        }

        final String name = cube.name == null || this.parts.containsKey(cube.name) ? cube.identifier : cube.name;
        final TblPart part = new TblPart(name);
        final Vector3 offset = Vector3.getNewVector().set(cube.offset).scalarMultBy(1 / 16f);
        final Vector3 pre = Vector3.getNewVector().set(cube.position).scalarMultBy(1 / 16f);
        pre.subtractFrom(offset);
        if (parent == null) pre.addTo(0, 0, 1.5);
        part.offset.set(pre);
        Vector4 angle = Vector4.fromAngles((float) cube.rotation[0], (float) cube.rotation[1],
                (float) cube.rotation[2]);

        if (parent != null) angle = angle.subtractAngles(parent.rotations);
        part.rotations.set(angle.x, angle.y, angle.z, angle.w);

        part.scale.x = (float) cube.scale[0];
        part.scale.y = (float) cube.scale[1];
        part.scale.z = (float) cube.scale[2];

        part.shapes.add(new TblMesh(TblModel.ORDER, verteces, normals, tex));
        this.parts.put(part.getName(), part);
        if (parent != null) parent.addChild(part);
        for (final CubeInfo subCube : cube.children)
            this.handleCubeInfo(json, subCube, part);

    }

    public HashMap<String, IExtendedModelPart> makeObjects(final TblJson xml) throws Exception
    {
        for (final CubeGroup group : xml.getCubeGroups())
            this.handleCubeGroup(xml, group);

        for (final CubeInfo cube : xml.getCubes())
            this.handleCubeInfo(xml, cube, null);
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
