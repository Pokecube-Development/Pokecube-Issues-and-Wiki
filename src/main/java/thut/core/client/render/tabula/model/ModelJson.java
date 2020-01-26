package thut.core.client.render.tabula.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.tabula.json.CubeGroup;
import thut.core.client.render.tabula.json.CubeInfo;
import thut.core.client.render.tabula.json.TblJson;
import thut.core.client.render.texturing.IPartTexturer;

@OnlyIn(Dist.CLIENT)
public class ModelJson<T extends Entity> extends TabulaModelBase<T> implements IModel
{
    public TblJson tabulaModel;

    public HashMap<String, IExtendedModelPart> nameMap       = Maps.newHashMap();
    public Map<String, TabulaRenderer>         identifierMap = Maps.newHashMap();

    /**
     * This is an ordered list of CubeGroup Identifiers. It is used to ensure
     * that translucent parts render in the correct order.
     */
    ArrayList<String>                              groupIdents = Lists.newArrayList();
    /**
     * Map of CubeGroup Identifiers to Sets of Root parts on the group. Uses
     * the above list to get keys
     */
    public Map<String, Collection<TabulaRenderer>> groupMap    = Maps.newHashMap();

    /**
     * Map of names to animations, used to get animations for rendering more
     * easily
     */
    public HashMap<String, List<Animation>> animationMap = Maps.newHashMap();

    public IPartTexturer     texturer;
    public IAnimationChanger changer;

    final Set<String> head = Sets.newHashSet();
    final HeadInfo    info = new HeadInfo();

    public boolean valid = false;

    public ModelJson(final TblJson model)
    {
        this.tabulaModel = model;

        this.textureWidth = model.getTextureWidth();
        this.textureHeight = model.getTextureHeight();

        final ArrayList<Animation> animations = model.getAnimations();

        if (animations != null) for (final Animation animation : animations)
        {
            List<Animation> anims = this.animationMap.get(animation.name);
            if (anims == null) this.animationMap.put(animation.name, anims = Lists.newArrayList());
            anims.add(animation);
        }
        for (final CubeInfo c : model.getCubes())
            this.cube(c, null, "null");

        for (final CubeGroup g : model.getCubeGroups())
            this.cubeGroup(g);
        // The groups come in in the opposite order from what is needed here, so
        // reverse it
        Collections.reverse(this.groupIdents);
    }

    @Override
    public void applyAnimation(final Entity entity, final IAnimationHolder animate, final IModelRenderer<?> renderer,
            final float partialTicks, final float limbSwing)
    {
        this.updateAnimation(entity, renderer, renderer.getAnimation(entity), partialTicks, this.getHeadInfo().headYaw,
                this.getHeadInfo().headYaw, limbSwing);
    }

    private TabulaRenderer createRendererModel(final CubeInfo cubeInfo, final TabulaRenderer parent)
    {
        final TabulaRenderer cube = new TabulaRenderer(this, parent, cubeInfo);
        this.addPart(cube);
        return cube;
    }

    private void cube(final CubeInfo cube, final TabulaRenderer parent, final String group)
    {
        final TabulaRenderer modelRenderer = this.createRendererModel(cube, parent);

        this.nameMap.put(cube.name, modelRenderer);
        this.identifierMap.put(cube.identifier, modelRenderer);

        if (parent != null) parent.addChild(modelRenderer);

        // Only add root parts to the group set.
        if (parent == null)
        {
            ArrayList<TabulaRenderer> cubes;
            if (this.groupMap.containsKey(group)) cubes = (ArrayList<TabulaRenderer>) this.groupMap.get(group);
            else
            {
                cubes = Lists.newArrayList();
                this.groupMap.put(group, cubes);
                this.groupIdents.add(group);
            }
            cubes.add(modelRenderer);

            Collections.sort(cubes, (o1, o2) ->
            {
                final String name1 = o1.name;
                final String name2 = o2.name;
                if (o1.transluscent && !o2.transluscent) return 1;
                if (o2.transluscent && !o1.transluscent) return -1;
                return name1.compareTo(name2);
            });
        }
        for (final CubeInfo c : cube.children)
            this.cube(c, modelRenderer, group);
    }

    private void cubeGroup(final CubeGroup group)
    {
        for (final CubeInfo cube : group.cubes)
            this.cube(cube, null, group.identifier);

        for (final CubeGroup c : group.cubeGroups)
            this.cubeGroup(c);
    }

    @Override
    public Set<String> getBuiltInAnimations()
    {
        return this.animationMap.keySet();
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return this.info;
    }

    @Override
    public Set<String> getHeadParts()
    {
        return this.head;
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        return this.nameMap;
    }

    @Override
    public void globalFix(final float dx, final float dy, final float dz)
    {
        GlStateManager.translated(0, 1, 0);
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

    @Override
    public void preProcessAnimations(final Collection<List<Animation>> collection)
    {
        for (final TabulaRenderer render : this.parts)
            for (final String id : render.info.metadata)
                if (id.equalsIgnoreCase("head"))
                {
                    this.head.add(render.name);
                    this.head.add(render.identifier);
                }
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
        // System.out.println(entity);
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
