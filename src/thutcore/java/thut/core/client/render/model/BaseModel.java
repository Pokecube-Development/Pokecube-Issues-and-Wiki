package thut.core.client.render.model;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.Part;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

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
            // Load the model possibly async, this should be most of the time.
            this.toLoad.loadModel(this.res);
            synchronized (this.toLoad)
            {
                // if we have a callback, run that
                if (this.toLoad.callback != null) this.toLoad.callback.run(this.toLoad);
                // Then clear the callback
                this.toLoad.callback = null;
                // Then flag as loaded
                this.toLoad.loaded = true;
                // Then mark as no longer loading
                this.toLoad.loading = false;
            }
        }

        public void start()
        {
            String key = "ThutCore: Model Load: " + res;
            final Thread loader = new Thread(this);
            loader.setName(key);
            if (ThutCore.conf.asyncModelLoads) loader.start();
            else loader.run();
        }
    }

    public static class RootPart extends Part
    {
        public RootPart()
        {
            super("__root__");
        }

        @Override
        public String getType()
        {
            return "__root__";
        }
    }

    IExtendedModelPart root_part = null;
    public Map<String, IExtendedModelPart> parts = new Object2ObjectOpenHashMap<>();

    private final List<IExtendedModelPart> renderOrder = Lists.newArrayList();
    private final List<IExtendedModelPart> animOrder = Lists.newArrayList();
    protected Map<String, Material> mats = new Object2ObjectOpenHashMap<>();

    IRetexturableModel.Holder<IAnimationChanger> animChangeHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IAnimationHolder> animHolderHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IPartTexturer> texChangeHolder = new IRetexturableModel.Holder<>();

    Set<String> heads = new HashSet<>();
    public String name;
    protected boolean valid = true;
    protected boolean loaded = false;
    protected boolean loading = false;
    protected ResourceLocation last_loaded = null;

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
            this.last_loaded = l;
            if (!ResourceHelper.exists(l, Minecraft.getInstance().getResourceManager()))
            {
                this.valid = false;
                return;
            }
            loading = true;
            // If it did exist, then lets schedule load on another thread
            Loader loader = new Loader(this, l);
            loader.start();
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
        if (this.isValid() && !this.loading)
        {
            callback.run(this);
            this.loaded = true;
        }
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

    @Override
    public List<IExtendedModelPart> getRenderOrder()
    {
        if ((this.renderOrder.isEmpty()) && this.isValid())
        {
            if (this.callback != null) this.callback.run(this);
            this.callback = null;
            try
            {
                IExtendedModelPart.sort(this.renderOrder, this.getParts());
            }
            catch (Exception e)
            {
                ThutCore.LOGGER.error("Error sorting parts for {} {}", this.last_loaded, this.name);
                ThutCore.LOGGER.error(e);
            }
            for (var part : this.renderOrder) part.preProcess();
        }
        return this.renderOrder;
    }

    @Override
    public Map<String, IExtendedModelPart> getParts()
    {
        if (root_part == null)
        {
            root_part = new RootPart();
            for (var part : this.parts.values()) if (part.getParent() == null) root_part.addChild(part);
            this.parts.put(root_part.getName(), root_part);
        }
        return this.parts;
    }

    @Override
    public boolean isValid()
    {
        return this.valid;
    }

    @Override
    public void renderAll(final PoseStack mat, final VertexConsumer buffer)
    {
        for (var part : this.getRenderOrder()) part.render(mat, buffer);
    }

    @Override
    public void renderAllExcept(final PoseStack mat, final VertexConsumer buffer,
            final Collection<String> excludedGroupNames)
    {
        for (var part : this.getRenderOrder())
            if (!excludedGroupNames.contains(part.getName())) part.render(mat, buffer);
    }

    @Override
    public void renderOnly(final PoseStack mat, final VertexConsumer buffer, final Collection<String> groupNames)
    {
        for (var part : this.getRenderOrder()) part.renderOnly(mat, buffer, groupNames);
    }

    @Override
    public void renderPart(final PoseStack mat, final VertexConsumer buffer, final String partName)
    {
        for (var part : this.getRenderOrder()) if (part.getParent() == null) part.renderPart(mat, buffer, partName);
    }

    @Override
    public void applyAnimation(final Entity entity, final IModelRenderer<?> renderer, final float partialTicks,
            final float limbSwing)
    {
        if (this.getRenderOrder().isEmpty()) return;
        String currentPhase = renderer.getAnimation(entity);
        final IAnimationHolder holder = renderer.getAnimationHolder();
        boolean anim = renderer.getAnimations().containsKey(currentPhase);
        final List<Animation> anims = Lists.newArrayList();
        if (holder != null)
        {
            anims.addAll(holder.getTransientPlaying());
            anims.addAll(holder.getPlaying());
            anim = !anims.isEmpty();
        }
        else if (anim) anims.addAll(renderer.getAnimations().get(currentPhase));
        this.updateAnimation(entity, renderer, anims, currentPhase, partialTicks, holder, limbSwing);
    }

    private void addChildrenToOrder(IExtendedModelPart part)
    {
        part.setHeadPart(this.getHeadParts().contains(part.getName()));
        for (var child : part.getSubParts().values())
        {
            animOrder.add(child);
            addChildrenToOrder(child);
        }
    }

    protected void updateAnimation(final Entity entity, final IModelRenderer<?> renderer, List<Animation> playingAnims,
            final String currentPhase, final float partialTicks, IAnimationHolder holder, final float limbSwing)
    {
        if (this.getRenderOrder().isEmpty()) return;
        if (animOrder.isEmpty())
        {
            for (var part : this.getParts().values())
            {
                animOrder.add(part);
                addChildrenToOrder(part);
            }
        }

        for (var part : animOrder)
            this.updatePart(entity, renderer, playingAnims, currentPhase, partialTicks, part, holder, limbSwing);
    }

    private void updatePart(final Entity entity, final IModelRenderer<?> renderer, List<Animation> anims,
            final String currentPhase, final float partialTick, final IExtendedModelPart part, IAnimationHolder holder,
            final float limbSwing)
    {
        if (this.getRenderOrder().isEmpty()) return;
        if (part == null) return;

        part.resetToInit();
        boolean anim = !anims.isEmpty();
        if (anim) AnimationHelper.doAnimation(anims, holder, entity, part, partialTick, limbSwing);
        if (part.isHeadPart())
        {
            HeadInfo info = holder.getHeadInfo();
            float ang;
            float ang2 = -info.headPitch;
            float head = info.headYaw + 180;
            float diff = 0;
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
            else dir2 = new Vector4(info.pitchDirection, 0, 0, ang2);
            final Vector4 combined = new Vector4();
            combined.mul(dir.toQuaternion(), dir2.toQuaternion());
            part.setPostRotations(combined);
        }
    }

    @Override
    public Holder<IAnimationChanger> getAnimationChanger()
    {
        return this.animChangeHolder;
    }

    @Override
    public void setAnimationChanger(Holder<IAnimationChanger> input)
    {
        this.animChangeHolder = input;
        for (var part : this.getRenderOrder()) if (part instanceof IRetexturableModel p) p.setAnimationChanger(input);
    }

    @Override
    public Holder<IPartTexturer> getTexturerChanger()
    {
        return this.texChangeHolder;
    }

    @Override
    public void setTexturerChanger(Holder<IPartTexturer> input)
    {
        this.texChangeHolder = input;
        for (var part : this.getRenderOrder()) if (part instanceof IRetexturableModel p) p.setTexturerChanger(input);
    }

    @Override
    public void setAnimationHolder(IAnimationHolder holder)
    {
        this.animHolderHolder.set(holder);
    }
}
