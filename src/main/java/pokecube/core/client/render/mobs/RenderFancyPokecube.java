package pokecube.core.client.render.mobs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.render.mobs.RenderPokecube.ModelPokecube;
import pokecube.core.client.render.mobs.overlays.Evolution;
import pokecube.core.entity.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import thut.api.IOwnable;
import thut.api.ModelHolder;
import thut.api.OwnableCaps;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector3;
import thut.bling.client.render.Util;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.lib.AxisAngles;
import thut.lib.ResourceHelper;

public class RenderFancyPokecube extends LivingEntityRenderer<EntityPokecube, EntityModel<EntityPokecube>>
        implements IModelRenderer<EntityPokecube>
{
    static final ResourceLocation MODEL = new ResourceLocation(PokecubeCore.MODID, "models/pokecubes/");
    static final ResourceLocation TEXTURE = new ResourceLocation(PokecubeCore.MODID, "textures/item/pokecubefront.png");

    public static record ModelSet(IAnimationChanger changer, IPartTexturer texer, ModelWrapper<EntityPokecube> model,
            Vector3 offset, Vector3 scale, HashMap<String, List<Animation>> anims)
    {
    }

    private Set<ResourceLocation> noModel = new HashSet<>();
    private Map<ResourceLocation, ModelSet> models = new HashMap<>();

    // holder is set per entity, so doesn't need to be in the ModelSet
    private IAnimationHolder holder = null;
    // This one is used as a temporary holder
    private Vector3 rotPoint = new Vector3();

    // Temp listes for presently running animations
    private final List<String> toRunNames = Lists.newArrayList();
    private final List<Animation> toRun = Lists.newArrayList();

    // These below need to be from the model set, as depend on the model itself
    private HashMap<String, List<Animation>> anims = Maps.newHashMap();

    private IAnimationChanger changer = null;
    private IPartTexturer texer = null;

    private Vector3 offset = new Vector3();
    private Vector3 scale = new Vector3();

    EntityModel<EntityPokecube> baseModel;

    public RenderFancyPokecube(final EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new ModelPokecube(), 0.0f);
        baseModel = this.getModel();
        for (ResourceLocation l : PokecubeItems.pokecubes.keySet())
        {
            var model = this.makeModel(l);
            if (model != null) RenderPokecube.pokecubeRenderers.computeIfAbsent(l, l2 -> this);
        }
    }

    private ModelWrapper<EntityPokecube> makeModel(ResourceLocation cube)
    {
        // First try to find a unique model for the name
        ModelWrapper<EntityPokecube> ret = null;
        {
            var modelKey = new ResourceLocation(cube.getNamespace(), MODEL.getPath() + cube.getPath());
            var holder = new ModelHolder(modelKey);
            var model = new ModelWrapper<EntityPokecube>(holder, this);
            IModel m2 = ModelFactory.create(model.model, m -> {
                synchronized (models)
                {
                    model.setModel(m);
                    this.changer = null;
                    this.texer = null;
                    this.anims = Maps.newHashMap();
                    AnimationLoader.parse(holder, model, this);
                    this.models.put(cube,
                            new ModelSet(getAnimationChanger(), getTexturer(), model, offset, scale, anims));
                }
            });
            if (m2.isValid()) return model;
        }
        // Next lets try just the xml
        xml:
        {
            var animKey = new ResourceLocation(cube.getNamespace(), MODEL.getPath() + cube.getPath() + ".xml");

            if (!ResourceHelper.exists(animKey, Minecraft.getInstance().getResourceManager())) break xml;

            var modelKey = new ResourceLocation(cube.getNamespace(), MODEL.getPath() + "pokecube");
            var holder = new ModelHolder(modelKey);
            holder.animation = animKey;
            var model = new ModelWrapper<EntityPokecube>(holder, this);
            IModel m2 = ModelFactory.create(model.model, m -> {
                synchronized (models)
                {
                    model.setModel(m);
                    this.changer = null;
                    this.texer = null;
                    this.anims = Maps.newHashMap();
                    AnimationLoader.parse(holder, model, this);
                    this.models.put(cube,
                            new ModelSet(getAnimationChanger(), getTexturer(), model, offset, scale, anims));
                }
            });
            if (m2.isValid()) return model;
        }
        return ret;
    }

    @Override
    public void render(final EntityPokecube entity, final float entityYaw, final float partialTicks,
            final PoseStack stack, final MultiBufferSource bufferIn, final int packedLightIn)
    {
        ItemStack item = entity.getItem();
        this.model = baseModel;
        if (Util.shouldReloadModel())
        {
            noModel.clear();
            models.clear();
        }
        final ResourceLocation num = PokecubeItems.getCubeId(item);
        synchronized (models)
        {
            if (models.containsKey(num))
            {
                var m = models.get(num);
                this.model = m.model();
                this.setTexturer(m.texer());
                this.setAnimationChanger(m.changer());
                this.offset = m.offset();
                this.scale = m.scale();
                this.anims = m.anims();
            }
            else if (!noModel.contains(num))
            {
                var model = makeModel(num);
                if (model == null) noModel.add(num);
                else
                {
                    var m = models.get(num);
                    if (m == null) return;
                    this.model = m.model();
                    this.setTexturer(m.texer());
                    this.setAnimationChanger(m.changer());
                    this.offset = m.offset();
                    this.scale = m.scale();
                    this.anims = m.anims();
                }
            }
        }
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);

        LivingEntity capturing = entity.getCapturing();
        if (capturing != null)
        {
            int duration = CaptureManager.CAPTURE_SHRINK_TIMER;
            var renderer = this.entityRenderDispatcher.getRenderer(capturing);
            float dt = (duration - (capturing.tickCount + partialTicks));
            float scale = dt / duration;
            if (scale > 0)
            {
                if (capturing.getAttributes().hasAttribute(SharedAttributes.MOB_SIZE_SCALE.get()))
                {
                    capturing.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get()).setBaseValue(scale);
                }
                IPokemob pokemob = PokemobCaps.getPokemobFor(capturing);
                stack.pushPose();
                Vector3 capt = entity.capturePos;
                stack.translate(capt.x - entity.getX(), capt.y - entity.getY(), capt.z - entity.getZ());
                if (pokemob != null)
                {
                    float scaleShift = 0;
                    final PokedexEntry entry = pokemob.getPokedexEntry();
                    var dims = entry.getModelSize();
                    scaleShift = dims.y * pokemob.getSize() * scale / 2;
                    float mobScale = pokemob.getSize();
                    scale = 0.1f * Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
                    Evolution.renderEffect(pokemob, stack, bufferIn, partialTicks, (int) dt, duration, scale,
                            scaleShift, true);
                }
                renderer.render(capturing, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
                stack.popPose();
            }
        }
    }

    @Override
    protected RenderType getRenderType(final EntityPokecube entity, final boolean bool_a, final boolean bool_b,
            final boolean bool_c)
    {
        final RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(this.getTextureLocation(entity), false, false))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () ->
                {
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                }, () -> {
                    RenderSystem.disableBlend();
                })).setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setCullState(new RenderStateShard.CullStateShard(false))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .setOverlayState(new RenderStateShard.OverlayStateShard(true)).createCompositeState(false);
        return RenderType.create("pokecube_legends:wormhole", DefaultVertexFormat.NEW_ENTITY, Mode.TRIANGLES, 256,
                bool_a, bool_b, rendertype$state);
    }

    @Override
    public ResourceLocation getTextureLocation(final EntityPokecube mob)
    {
        return RenderFancyPokecube.TEXTURE;
    }

    @Override
    protected boolean shouldShowName(final EntityPokecube entity)
    {
        return false;
    }

    @Override
    public IAnimationChanger getAnimationChanger()
    {
        return this.changer;
    }

    @Override
    public Map<String, List<Animation>> getAnimations()
    {
        return this.anims;
    }

    @Override
    public IPartTexturer getTexturer()
    {
        return this.texer;
    }

    @Override
    public boolean hasAnimation(final String phase, final Entity entity)
    {
        return this.getAnimations().containsKey(phase);
    }

    @Override
    public List<Animation> getAnimations(Entity entity, String phase)
    {
        this.toRun.clear();
        this.toRunNames.clear();
        if (this.getAnimationChanger() != null)
            this.getAnimationChanger().getAlternates(this.toRunNames, this.getAnimations().keySet(), entity, phase);
        for (final String name : this.toRunNames)
        {
            final List<Animation> anims = this.getAnimations().get(name);
            if (anims != null) this.toRun.addAll(anims);
        }
        return this.toRun;
    }

    @Override
    public String getAnimation(final Entity entityIn)
    {
        if (entityIn instanceof EntityPokecube cube)
        {
            ItemStack item = cube.getItem();
            if (cube.isReleasing())
            {
                var e = cube.getReleased();
                boolean onFail = false;
                IOwnable test;
                if (e instanceof LivingEntity living && (test = OwnableCaps.getOwnable(living)) != null)
                {
                    onFail = test.getOwnerId() == null;
                }
                return onFail ? "broken_out" : "releasing";
            }
            boolean shaking = PokecubeManager.getTilt(item) > 0;
            LivingEntity capturing = cube.getCapturing();
            if (shaking && capturing != null)
            {
                shaking = capturing.tickCount >= CaptureManager.CAPTURE_SHRINK_TIMER;
            }

            if (shaking) return "shaking";
            else if (capturing != null) return "capturing";

            if (!cube.isOnGround())
            {
                var v = cube.getDeltaMovement();
                double dh = Math.fma(v.x, v.x, v.z * v.z);
                if (dh > 0) return cube.isSeeking() ? "seeking" : "flying";
            }
            return "idle";
        }

        final IAnimationHolder holder = this.getAnimationHolder();
        if (holder != null)
        {
            String result = holder.getAnimation(entityIn);
            if (result.isEmpty()) result = IModelRenderer.DEFAULTPHASE;
            return result;
        }
        return IModelRenderer.DEFAULTPHASE;
    }

    @Override
    public Vector3 getRotationOffset()
    {
        return this.offset;
    }

    @Override
    public Vector3 getScale()
    {
        return this.scale;
    }

    @Override
    public void scaleEntity(final PoseStack mat, final Entity entity, final IModel model, final float partialTick)
    {
        final float s = 16;
        float sx = (float) this.getScale().x;
        float sy = (float) this.getScale().y;
        float sz = (float) this.getScale().z;
        sx *= s;
        sy *= s;
        sz *= s;
        this.rotPoint.set(this.getRotationOffset()).scalarMultBy(s);
        model.setOffset(this.rotPoint);
        mat.mulPose(AxisAngles.ZP.rotationDegrees(90));
        if (!this.getScale().isEmpty()) mat.scale(sx, sy, sz);
        else mat.scale(s, s, s);
    }

    @Override
    public void setAnimationHolder(final IAnimationHolder holder)
    {
        this.holder = holder;
        if (this.changer != null) this.changer.setAnimationHolder(holder);
        if (this.model instanceof ModelWrapper<?> wrap) wrap.setAnimationHolder(holder);
    }

    @Override
    public IAnimationHolder getAnimationHolder()
    {
        if (this.model instanceof ModelWrapper<?> wrap) return wrap.animHolderHolder.get();
        return this.holder;
    }

    @Override
    public void setAnimationChanger(final IAnimationChanger changer)
    {
        this.changer = changer;
    }

    @Override
    public void setRotationOffset(final Vector3 offset)
    {
        this.offset = offset;
    }

    @Override
    public void setScale(final Vector3 scale)
    {
        this.scale = scale;
    }

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        this.texer = texturer;
    }

    @Override
    public void updateModel(final Map<String, List<Vector5>> phaseList, final ModelHolder model)
    {}

    @Override
    public HeadInfo getHeadInfo()
    {
        return HeadInfo.DUMMY;
    }

}
