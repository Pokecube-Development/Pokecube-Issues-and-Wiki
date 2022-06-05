package thut.core.client.render.model;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.common.ThutCore;

public abstract class BaseModel implements IModelCustom, IModel, IRetexturableModel
{

    public static class Loader implements Runnable
    {
        final BaseModel toLoad;

        final ResourceLocation res;

        public Loader(final BaseModel model, final ResourceLocation res)
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
    protected Map<String, Material> mats = Maps.newHashMap();

    Set<String> heads = Sets.newHashSet();
    public String name;
    protected boolean valid = true;
    protected boolean loaded = false;

    protected IModelCallback callback = null;

    public BaseModel()
    {
        this.valid = true;
    }
    
    public BaseModel(final ResourceLocation l)
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
            loader.setName("ThutCore: Model Load: " + l);
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
    
    protected abstract void loadModel(final ResourceLocation model);

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
        final IAnimationHolder holder = renderer.getAnimationHolder();
        this.updateAnimation(entity, renderer, renderer.getAnimation(entity), partialTicks,
                holder.getHeadInfo().headYaw, holder.getHeadInfo().headYaw, limbSwing);
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

        parent.resetToInit();
        boolean anim = renderer.getAnimations().containsKey(currentPhase);
        final List<Animation> anims = Lists.newArrayList();

        final IAnimationHolder animHolder = parent.getAnimationHolder();
        HeadInfo info = null;
        if (animHolder != null)
        {
            anims.addAll(animHolder.getPlaying());
            anim = !anims.isEmpty();
            info = animHolder.getHeadInfo();
        }
        else if (anim) anims.addAll(renderer.getAnimations().get(currentPhase));

        if (anim) AnimationHelper.doAnimation(anims, entity, parent.getName(), parent, partialTick, limbSwing);
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
