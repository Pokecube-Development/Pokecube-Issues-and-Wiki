package thut.core.client.render.model;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.Mesh;
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
                if (this.toLoad.callback != null)
                {
                    this.toLoad.callback.run(this.toLoad);
                }
                // Then call postInit
                this.toLoad.postInit();
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

    private final List<IExtendedModelPart> partsList = new ArrayList<>();
    private final List<Mesh> renderOrderMeshs = new ArrayList<>();
    private final List<IExtendedModelPart> animOrder = new ArrayList<>();
    protected Map<String, Material> mats = new Object2ObjectOpenHashMap<>();

    IRetexturableModel.Holder<IAnimationChanger> animChangeHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IAnimationHolder> animHolderHolder = new IRetexturableModel.Holder<>();
    IRetexturableModel.Holder<IPartTexturer> texChangeHolder = new IRetexturableModel.Holder<>();

    Set<String> heads = new HashSet<>();
    public String name;
    protected boolean valid;
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
            if (!ResourceHelper.exists(l))
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
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading {}", l, e);
        }

    }

    protected abstract void loadModel(final ResourceLocation model);

    @Override
    public IModel init(final IModelCallback callback)
    {
        if (this.isValid() && !this.loading)
        {
            callback.run(this);
            // Now handle post processing cleanup
            postInit();
        }
        else this.callback = callback;
        return this;
    }

    @Override
    public void postInit()
    {
        // Collect all materials, Before further processing
        synchronized (this.partsList)
        {
            Set<Material> allMats = new HashSet<>();
            var parts = new ArrayList<>(this.parts.values());
            parts.forEach(e -> allMats.addAll(e.getMaterials()));

            // Now merge duplicated materials
            this.materials.clear();
            List<Material> toSort = new ArrayList<>(allMats);
            toSort.sort(null);
            toSort = toSort.reversed();
            toSort.forEach(m->{
                var matches = this.materials.stream().filter(m1->m.compareTo(m1)==0).findFirst();
                if(matches.isEmpty()) {
                    this.materials.add(m);
                }
            });
            // Finally set the parts materials
            parts.forEach(p -> p.updateMaterials(materials));
        }
        this.loaded = true;
        this.loading = false;
        this.callback = null;
    }

    @Override
    public void updateMaterials(List<Material> materials)
    {
        this.materials.clear();
        this.materials.addAll(materials);
        var parts = new ArrayList<>(this.parts.values());
        parts.forEach(p -> p.updateMaterials(materials));
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
    public List<IExtendedModelPart> getPartsList()
    {
        if ((this.partsList.isEmpty()) && this.isValid())
        {
            synchronized (partsList)
            {
                if (this.callback != null) this.callback.run(this);
                this.callback = null;
                var parts = this.getParts();
                this.partsList.addAll(parts.values());
                synchronized (renderOrderMeshs)
                {
                    this.renderOrderMeshs.clear();
                    int n = -1;
                    // Repeat part.preProcess until we
                    // have a constant number of meshes
                    while(n != this.renderOrderMeshs.size())
                    {
                        n = this.renderOrderMeshs.size();
                        this.renderOrderMeshs.clear();
                        for (var part : this.partsList)
                        {
                            part.preProcess();
                            this.renderOrderMeshs.addAll(part.getRenderMeshes());
                        }
                    }
                    IExtendedModelPart.sortMeshes(this.renderOrderMeshs);
                }
            }
        }
        return this.partsList;
    }

    final List<IExtendedModelPart> customParts = new ArrayList<>();

    @Override
    public void addCustomPart(IExtendedModelPart part)
    {
        synchronized (partsList)
        {
            if(this.partsList.contains(part)) return;
            partsList.add(part);
        }
        synchronized (customParts)
        {
            if(this.customParts.contains(part)) return;
            customParts.add(part);
            synchronized (renderOrderMeshs)
            {
                part.preProcess();
                this.getParts().put(part.getName(), part);
            }
        }
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
    public void renderLegacy(final PoseStack mat, final VertexConsumer buffer)
    {
        this.prepareRender();
        for (var part : this.getPartsList()) part.render(mat, buffer);
    }

    @Override
    public void render(final PoseStack mat, final VertexConsumer buffer)
    {
        var last = mat.last();
        Matrix4f pos = new Matrix4f();
        Matrix3f norm = new Matrix3f();

        // Render custom parts first via legacy rendering
        for (var part : this.customParts) part.render(mat, buffer);

        for(var m: this.renderOrderMeshs)
        {
            // Attempt to multiply correctly
            last.pose().mul(m.poseInfo.pose(), pos);
            m.poseInfo.pose().set(pos);

            last.normal().mul(m.poseInfo.normal(), norm);
            m.poseInfo.normal().set(norm);

            m.renderShape(buffer);
        }
    }

    @Override
    public void applyAnimation(final Entity entity, final IModelRenderer<?> renderer, final float partialTicks,
            final float limbSwing)
    {
        if (this.getPartsList().isEmpty()) return;
        String currentPhase = renderer.getAnimation(entity);
        final IAnimationHolder holder = renderer.getAnimationHolder();
        boolean anim = renderer.getAnimations().containsKey(currentPhase);
        final List<Animation> anims = Lists.newArrayList();
        if (holder != null)
        {
            anims.addAll(holder.getTransientPlaying());
            anims.addAll(holder.getPlaying());
        }
        else if (anim) anims.addAll(renderer.getAnimations().get(currentPhase));
        this.updateAnimation(anims, holder);
    }

    private void addChildrenToOrder(IExtendedModelPart part)
    {
        part.setHeadPart(this.getHeadParts().contains(part.getName()));
        for (var child : part.getSubParts().values())
        {
            if (!animOrder.contains(child)) animOrder.add(child);
            addChildrenToOrder(child);
        }
    }

    @Override
    public void updateAnimation(List<Animation> playingAnims, IAnimationHolder holder)
    {
        if (this.getPartsList().isEmpty()) return;
        if (animOrder.isEmpty())
        {
            for (var part : this.getParts().values())
            {
                if (!animOrder.contains(part)) animOrder.add(part);
                addChildrenToOrder(part);
            }
            animOrder.sort(null);
        }
        // Then apply animations
        for (var part : animOrder)
            this.updatePart(playingAnims, part, holder);
    }

    private void updatePart(List<Animation> anims, final IExtendedModelPart part, IAnimationHolder holder)
    {
        part.resetToInit();
        // If animated, compute adjustments
        if(part.isAnimated())
        {
            boolean anim = !anims.isEmpty();
            // This computes transform for us from animations
            if (anim) AnimationHelper.doAnimation(anims, holder, part);
            // This computes head rotation
            if (part.isHeadPart() && !part.isHidden())
            {
                HeadInfo info = holder.getHeadInfo();
                float ang;
                float ang2 = -info.headPitch;
                float head = info.headYaw + 180;
                float diff;
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
        part.transformForRender();
    }

    final List<Material> materials = new ArrayList<>();

    @Override
    public List<Material> getMaterials()
    {
        return materials;
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
        for (var part : this.getPartsList()) if (part instanceof IRetexturableModel p) p.setAnimationChanger(input);
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
        for (var part : this.getPartsList()) if (part instanceof IRetexturableModel p) p.setTexturerChanger(input);
    }

    @Override
    public void setAnimationHolder(IAnimationHolder holder)
    {
        this.animHolderHolder.set(holder);
    }

    @Override
    public void preProcessAnimations(Collection<Animation> collection)
    {
        Set<String> animatedParts = new HashSet<>();
        collection.forEach(a->{
            animatedParts.addAll(a.sets.keySet());
        });
        var parts = this.getParts();

        for(var s: animatedParts) if(parts.containsKey(s)) parts.get(s).markAsAnimated();

        var copy = new Object2ObjectOpenHashMap<String, IExtendedModelPart>();
        this.parts.values().forEach(part->{
            part.tryCombineChildren();
            if(!(part.getMaterials().isEmpty() && part.getSubParts().isEmpty() && !part.getName().startsWith("__"))) {
                copy.put(part.getName(), part);
            }
        });
        this.partsList.clear();
        this.parts = copy;
    }
}
