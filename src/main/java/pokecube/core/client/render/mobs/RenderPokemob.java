package pokecube.core.client.render.mobs;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.DefaultFormeHolder.TexColours;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.impl.capabilities.TextureableCaps.PokemobCap;
import thut.api.AnimatedCaps;
import thut.api.ModelHolder;
import thut.api.Tracker;
import thut.api.entity.IAnimated;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.model.PartInfo;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;

public class RenderPokemob extends MobRenderer<Mob, ModelWrapper<Mob>>
{
    public static class PokemobTexHelper extends TextureHelper
    {
        static final QName male = new QName("male");
        static final QName female = new QName("female");

        final PokedexEntry entry;

        public PokemobTexHelper(final PokedexEntry entry)
        {
            super();
            this.entry = entry;
        }

        @Override
        public void init(final CustomTex customTex)
        {
            // First do the super init
            super.init(customTex);

            // Then do our extra stuff.
        }

        @Override
        public void modifiyRGBA(final String part, final int[] rgbaIn)
        {
            IPokemob mob = null;
            if (this.mob instanceof PokemobCap poke) mob = poke.pokemob;
            holders:
            if (mob != null)
            {
                final FormeHolder holder = mob.getCustomHolder();
                if (holder == null || holder.loaded_from == null || holder.loaded_from._colourMap_.isEmpty())
                    break holders;

                if (holder.loaded_from._colourMap_.containsKey(part))
                {
                    final TexColours c = holder.loaded_from._colourMap_.get(part);
                    final float r = c.red * rgbaIn[0] / 255f;
                    final float g = c.green * rgbaIn[1] / 255f;
                    final float b = c.blue * rgbaIn[2] / 255f;
                    final float a = c.alpha * rgbaIn[3] / 255f;
                    rgbaIn[0] = (int) (r * 255);
                    rgbaIn[1] = (int) (g * 255);
                    rgbaIn[2] = (int) (b * 255);
                    rgbaIn[3] = (int) (a * 255);
                    return;
                }
            }
            super.modifiyRGBA(part, rgbaIn);
        }

        @Override
        public boolean isHidden(final String part)
        {
            IPokemob mob = null;
            if (this.mob instanceof PokemobCap poke) mob = poke.pokemob;
            if (mob == null) return false;
            final FormeHolder holder = mob.getCustomHolder();
            if (holder == null || holder.loaded_from == null) return false;
            return holder.loaded_from._hide_.contains(part);
        }

        @Override
        public void applyTexturePhase(final Phase phase)
        {
            if (phase.values.containsKey(male)) this.entry.textureDetails[0] = this.fromValue(phase.values.get(male));
            if (phase.values.containsKey(female))
                this.entry.textureDetails[1] = this.fromValue(phase.values.get(female));
        }

        private String[] fromValue(final String string)
        {
            final String[] ret = string.split(",");
            for (int i = 0; i < ret.length; i++) ret[i] = ThutCore.trim(ret[i]);
            return ret;
        }
    }

    public static class Holder extends ModelHolder implements IModelRenderer<Mob>
    {
        public ModelWrapper<Mob> wrapper;
        final Vector3 rotPoint = new Vector3();
        Map<String, List<Animation>> anims = new Object2ObjectOpenHashMap<>();
        private IPartTexturer texturer;
        private IAnimationChanger animator;
        public String name;
        public Map<String, PartInfo> parts = new Object2ObjectOpenHashMap<>();
        Map<String, List<Vector5>> global;
        public Map<String, List<Animation>> animations = new Object2ObjectOpenHashMap<>();
        private final List<String> toRunNames = Lists.newArrayList();
        private final List<Animation> toRun = Lists.newArrayList();
        private Vector3 offset = new Vector3();;
        private Vector3 scale = new Vector3();
        PokedexEntry entry;

        boolean checkedAnims = false;
        boolean hasSleepAnim = false;
        boolean hasDeathAnim = false;

        public boolean reload = false;
        public boolean overrideAnim = false;
        public String anim = "";

        // This will decrement if above 0, and if so, we don't render, this
        // gives some time to actually load the model.
        protected int loadTimer = 3;

        // This increments while the model is not found, and if it exceeds 100,
        // will render missingno model.
        protected int failTimer = 0;

        IAnimationHolder currentHolder = null;

        HeadInfo headInfo = new HeadInfo();

        public Holder(final PokedexEntry entry)
        {
            super(entry.model(), entry.texture(), entry.animation(), entry.getTrimmedName());
            this.entry = entry;
            this.texturer = new PokemobTexHelper(entry);

            if (Database.dummyMap.containsKey(entry.getPokedexNb()))
            {
                final PokedexEntry dummy = Database.dummyMap.get(entry.getPokedexNb());
                String newRes = entry.animation().toString().replace(entry.getTrimmedName(), dummy.getTrimmedName());
                this.backupAnimations.add(new ResourceLocation(newRes));
                newRes = entry.model().toString().replace(entry.getTrimmedName(), dummy.getTrimmedName());
                this.backupModels.add(new ResourceLocation(newRes));
            }
            if (entry.getBaseForme() != null)
            {
                String newRes = entry.animation().toString().replace(entry.getTrimmedName(),
                        entry.getBaseForme().getTrimmedName());
                this.backupAnimations.add(new ResourceLocation(newRes));
                newRes = entry.model().toString().replace(entry.getTrimmedName(),
                        entry.getBaseForme().getTrimmedName());
                this.backupModels.add(new ResourceLocation(newRes));
            }
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            final IAnimationHolder holder = this.getAnimationHolder();
            if (holder != null && holder.isFixed()) return holder.getAnimation(entityIn);
            if (this.overrideAnim) return this.anim;
            final String phase = this.getPhase((Mob) entityIn, PokemobCaps.getPokemobFor(entityIn));
            return phase;
        }

        @Override
        public IAnimationChanger getAnimationChanger()
        {
            return this.animator;
        }

        @Override
        public Map<String, List<Animation>> getAnimations()
        {
            return this.animations;
        }

        @Override
        public IAnimationHolder getAnimationHolder()
        {
            return this.currentHolder;
        }

        @Override
        public void setAnimationHolder(final IAnimationHolder holder)
        {
            this.currentHolder = holder;
            if (holder != null) holder.getHeadInfo().copyFrom(this.getHeadInfo());
            if (this.animator != null) this.animator.setAnimationHolder(holder);
            this.wrapper.imodel.setAnimationHolder(holder);
        }

        private String getPhase(final Mob entity, final IPokemob pokemob)
        {
            if (!this.wrapper.isLoaded()) return "not_loaded_yet!";
            final String phase = "idle";
            if (this.model == null || pokemob == null) return phase;
            final IAnimated anims = AnimatedCaps.getAnimated(entity);
            for (final String s : anims.getChoices()) if (this.hasAnimation(s, entity)) return s;
            return phase;
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
        public IPartTexturer getTexturer()
        {
            return this.texturer;
        }

        @Override
        public boolean hasAnimation(final String phase, final Entity entity)
        {
            if (this.animator != null && this.animator.hasAnimation(phase)) return true;
            return IModelRenderer.DEFAULTPHASE.equals(phase) || this.animations.containsKey(phase)
                    || this.wrapper.imodel.getBuiltInAnimations().contains(phase);
        }

        @Override
        public List<Animation> getAnimations(final Entity entity, final String phase)
        {
            this.toRun.clear();
            this.toRunNames.clear();
            if (this.animator != null)
                this.animator.getAlternates(this.toRunNames, this.animations.keySet(), entity, phase);
            for (final String name : this.toRunNames)
            {
                final List<Animation> anims = this.animations.get(name);
                if (anims != null) this.toRun.addAll(anims);
            }
            return this.toRun;
        }

        public void init()
        {
            boolean noUpdate = this.wrapper != null && this.wrapper.lastInit > Tracker.instance().getTick()
                    && this.wrapper.lastInit - Tracker.instance().getTick() < 100;
            if (noUpdate) return;
            if (ThutCore.conf.debug_models) PokecubeAPI.logDebug("Reloaded model for " + entry);
            RenderPokemob.holders.put(this.entry, this);
            this.toRun.clear();
            this.toRunNames.clear();
            this.parts.clear();
            this.initModel(new ModelWrapper<>(this, this));
            this.checkedAnims = false;
            this.failTimer = 0;
            this.wrapper.lastInit = Tracker.instance().getTick() + 50;
        }

        public void initModel(final ModelWrapper<Mob> model)
        {
            this.wrapper = model;
            ModelFactory.create(model.model, m -> {
                // Set this first in here, so that we can run parse properly.
                this.wrapper.imodel = m;
                AnimationLoader.parse(this, m, this);
            });
        }

        @Override
        public void scaleEntity(final PoseStack mat, final Entity entity, final IModel model, final float partialTick)
        {
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
            float s = 1;
            if (pokemob != null)
            {
                s = pokemob.getSize();
                if (pokemob.getGeneralState(GeneralStates.EXITINGCUBE))
                {
                    float scale = 1;
                    scale = Math.min(1,
                            (entity.tickCount + 1 + partialTick) / (float) LogicMiscUpdate.EXITCUBEDURATION);
                    s = Math.max(0.01f, s * scale);
                }
                else
                {
                    s = pokemob.getEntity().getScale();
                }
            }
            float sx = (float) this.getScale().x;
            float sy = (float) this.getScale().y;
            float sz = (float) this.getScale().z;
            sx *= s;
            sy *= s;
            sz *= s;
            this.rotPoint.set(this.getRotationOffset()).scalarMultBy(s);
            model.setOffset(this.rotPoint);
            if (!this.getScale().isEmpty()) mat.scale(sx, sy, sz);
            else mat.scale(s, s, s);
        }

        @Override
        public void setAnimationChanger(final IAnimationChanger changer)
        {
            this.animator = changer;
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
            this.texturer = texturer;
        }

        @Override
        public void updateModel(final Map<String, List<Vector5>> global, final ModelHolder model)
        {
            this.name = model.name;
            this.texture = model.texture;
            this.global = global;
        }

        @Override
        public HeadInfo getHeadInfo()
        {
            return this.headInfo;
        }
    }

    public static void reloadModel(final PokedexEntry entry)
    {
        if (RenderPokemob.holders.containsKey(entry))
        {
            var holder = RenderPokemob.holders.get(entry);
            if (holder.wrapper != null) holder.wrapper.lastInit = Long.MIN_VALUE;
            holder.init();
        }
        for (final Holder custom : RenderPokemob.customs.values()) if (custom.entry == entry)
        {
            if (custom.wrapper != null) custom.wrapper.lastInit = Long.MIN_VALUE;
            custom.init();
        }
    }

    public static final Map<ResourceLocation, Holder> customs = new Object2ObjectOpenHashMap<>();

    public static Map<PokemobType<?>, Holder> holderMap = new Object2ObjectOpenHashMap<>();
    public static Map<PokedexEntry, Holder> holders = new Object2ObjectOpenHashMap<>();

    public static void register()
    {
        if (ThutCore.conf.debug_models) PokecubeAPI.logInfo("Registering Models to the renderer.");
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (!entry.stock) continue;
            if (entry.generated) continue;
            final PokemobType<?> type = (PokemobType<?>) entry.getEntityType();
            final Holder holder = new Holder(entry);
            RenderPokemob.holderMap.put(type, holder);
            RenderPokemob.holders.put(entry, holder);
            // Always initialize starters, so the gui doesn't act a bit funny
            if (PokecubeCore.getConfig().preloadModels || entry.isStarter) holder.init();
        }
    }

    private static Holder MISSNGNO = new Holder(Database.missingno);

    private static Holder getMissingNo()
    {
        if (RenderPokemob.MISSNGNO.wrapper == null || !RenderPokemob.MISSNGNO.wrapper.isLoaded())
        {
            if (RenderPokemob.MISSNGNO.wrapper != null) RenderPokemob.MISSNGNO.wrapper.lastInit = Long.MIN_VALUE;
            RenderPokemob.MISSNGNO.init();
        }
        return RenderPokemob.MISSNGNO;
    }

    final Holder holder;
    Holder activeHolder = null;

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public RenderPokemob(final PokedexEntry entry, final EntityRendererProvider.Context p_i50961_1_)
    {
        super(p_i50961_1_, new ModelWrapper(RenderPokemob.getMissingNo(), RenderPokemob.getMissingNo()), 1);
        if (RenderPokemob.holders.containsKey(entry)) this.holder = RenderPokemob.holders.get(entry);
        else
        {
            this.holder = new Holder(entry);
            RenderPokemob.holders.put(entry, this.holder);
        }
    }

    @Override
    protected float getFlipDegrees(final Mob entityLivingBaseIn)
    {
        return 85.0f;
    }

    @Override
    public void render(final Mob entity, final float entityYaw, final float partialTicks, final PoseStack matrixStackIn,
            final MultiBufferSource bufferIn, final int packedLightIn)
    {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob == null) return;
        PokedexEntry entry = pokemob.getPokedexEntry();
        Holder holder = RenderPokemob.holders.getOrDefault(entry, this.holder);
        if (pokemob.getCustomHolder() != null)
        {
            final FormeHolder forme = pokemob.getCustomHolder();
            final ResourceLocation model = forme.key;
            Holder temp = RenderPokemob.customs.get(model);
            if (temp == null || temp.wrapper == null || !temp.wrapper.isValid())
            {
                if (temp == null) temp = new Holder(entry);
                if (forme.model != null) temp.model = forme.model;
                if (forme.animation != null) temp.animation = forme.animation;
                if (forme.texture != null) temp.texture = forme.texture;
                RenderPokemob.customs.put(model, temp);
                temp.init();
            }
            holder = temp;
        }
        if (holder.failTimer > 50) holder = MISSNGNO;
        if (holder.wrapper == null || !holder.wrapper.isLoaded())
        {
            holder.init();
        }
        if (holder.wrapper != null && !holder.wrapper.isLoaded())
        {
            if (!holder.wrapper.isLoaded() && holder.wrapper.lastInit < Tracker.instance().getTick()) holder.init();
            holder.failTimer++;
            return;
        }
        // This gives time for the model to actually finish loading in.
        if (holder.loadTimer-- > 0) return;
        holder.loadTimer = 0;
        holder.failTimer = 0;
        if (holder.wrapper == null || holder.wrapper.imodel == null || !holder.wrapper.isValid() || holder.model == null
                || holder.texture == null)
            holder = RenderPokemob.getMissingNo();

        this.model = holder.wrapper;
        this.shadowRadius = entity.getBbWidth();
        this.activeHolder = holder;

        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    protected RenderType getRenderType(final Mob entity, final boolean regular, final boolean model,
            final boolean glowing)
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
        return RenderType.create("pokecube:pokemob", DefaultVertexFormat.NEW_ENTITY, Mode.TRIANGLES, 256, true, true,
                rendertype$state);
    }

    @Override
    public ResourceLocation getTextureLocation(final Mob entity)
    {
        ResourceLocation texture = Database.missingno.texture;
        Holder holder = this.holder;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob == null) return texture;
        holder = RenderPokemob.holders.getOrDefault(pokemob.getPokedexEntry(), this.holder);
        if (pokemob.getCustomHolder() != null)
        {
            final FormeHolder forme = pokemob.getCustomHolder();
            final ResourceLocation model = forme.key;
            Holder temp = RenderPokemob.customs.get(model);
            if (temp == null || temp.wrapper == null || !temp.wrapper.isValid())
            {
                if (temp == null) temp = new Holder(pokemob.getPokedexEntry());
                if (forme.model != null) temp.model = forme.model;
                if (forme.animation != null) temp.animation = forme.animation;
                if (forme.texture != null) temp.texture = forme.texture;
                RenderPokemob.customs.put(model, temp);
                temp.init();
            }
            holder = temp;
        }
        if (holder.texture != null) texture = holder.texture;
        if (holder.getTexturer() == null) return texture;
        final ResourceLocation tex = holder.getTexturer().getTexture("", texture);
        return tex == null ? texture : tex;
    }

    private static float sleepDirectionToRotation(Direction bedDir)
    {
        switch (bedDir)
        {
        case SOUTH:
            return 90.0F;
        case WEST:
            return 0.0F;
        case NORTH:
            return 270.0F;
        case EAST:
            return 180.0F;
        default:
            return 0.0F;
        }
    }

    @Override
    protected void setupRotations(Mob entity, PoseStack stack, float ageInTicks, float rotationYaw, float partialTicks)
    {
        // See super implementation for default stuff.
        Pose pose = entity.getPose();
        boolean sleeping = pose == Pose.SLEEPING;

        if (!activeHolder.checkedAnims && this.activeHolder.wrapper.isLoaded())
        {
            activeHolder.checkedAnims = true;
            activeHolder.hasSleepAnim = this.activeHolder.hasAnimation("sleeping", entity);
            activeHolder.hasDeathAnim = this.activeHolder.hasAnimation("dead", entity);
        }

        if (!sleeping)
        {
            stack.mulPose(AxisAngles.YP.rotationDegrees(180.0F - rotationYaw));
        }

        if (this.isShaking(entity))
        {
            rotationYaw += (float) (Math.cos((double) entity.tickCount * 3.25D) * Math.PI * (double) 0.4F);
        }
        if (entity.deathTime > 0)
        {
            if (activeHolder.hasDeathAnim) return;
            float f = ((float) entity.deathTime + rotationYaw - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F)
            {
                f = 1.0F;
            }
            stack.mulPose(AxisAngles.ZP.rotationDegrees(f * this.getFlipDegrees(entity)));
        }
        else if (entity.isAutoSpinAttack())
        {
            stack.mulPose(AxisAngles.XP.rotationDegrees(-90.0F - entity.getXRot()));
            stack.mulPose(AxisAngles.YP.rotationDegrees(((float) entity.tickCount + partialTicks) * -75.0F));
        }
        else if (sleeping)
        {
            if (activeHolder.hasSleepAnim) return;
            Direction direction = entity.getBedOrientation();
            float f1 = direction != null ? sleepDirectionToRotation(direction) : rotationYaw;
            stack.mulPose(AxisAngles.YP.rotationDegrees(f1));
            stack.mulPose(AxisAngles.ZP.rotationDegrees(this.getFlipDegrees(entity)));
            stack.mulPose(AxisAngles.YP.rotationDegrees(270.0F));
        }
        else if (isEntityUpsideDown(entity))
        {
            stack.translate(0.0D, (double) (entity.getBbHeight() + 0.1F), 0.0D);
            stack.mulPose(AxisAngles.ZP.rotationDegrees(180.0F));
        }
    }
}
