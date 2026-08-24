package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
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
import org.joml.Vector3f;
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
import thut.api.ModelHolder;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.entity.IAnimated;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.IAnimationChanger;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.model.PartInfo;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.core.common.ThutCore;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RenderPokemob extends MobRenderer<Mob, ModelWrapper<Mob>>
{
    public static class PokemobTexHelper extends TextureHelper
    {
        static final QName male = new QName("male");
        static final QName female = new QName("female");

        final PokedexEntry entry;
        final Set<String> _custom_keys = new HashSet<>();

        public PokemobTexHelper(final PokedexEntry entry)
        {
            super();
            this.entry = entry;
        }

        @Override
        public void init(IModel model)
        {
            var holders = Database.customModels.get(entry);
            if(holders!=null) holders.forEach(forme->{
                if (forme.loaded_from == null) return;
                _custom_keys.addAll(forme.loaded_from._hide_);
                _custom_keys.addAll(forme.loaded_from._colourMap_.keySet());
                if (!forme.loaded_from._hide_.isEmpty())
                {
                    for (var p : model.getParts().values())
                    {
                        if (forme.loaded_from._hide_.contains(ThutCore.trim(p.getName())))
                        {
                            p.markAsAnimated();
                        }
                    }
                }
            });
        }

        @Override
        public boolean hasMapping(String part)
        {
            return super.hasMapping(part) || _custom_keys.contains(part);
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
            if (phase.values.containsKey(male))
            {
                this.entry.textureDetails[0] = this.fromValue(phase.values.get(male));
            }
            if (phase.values.containsKey(female))
            {
                this.entry.textureDetails[1] = this.fromValue(phase.values.get(female));
            }
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
        final Vector3f rotPoint = new Vector3f();

        public String name;
        public Map<String, PartInfo> parts = new Object2ObjectOpenHashMap<>();
        public Map<String, List<Animation>> animations = new Object2ObjectOpenHashMap<>();
        private final List<String> toRunNames = new ArrayList<>();
        private final List<Animation> toRun = new ArrayList<>();
        private Vector3f offset = new Vector3f();
        private Vector3f scale = new Vector3f(1);
        PokedexEntry entry;

        boolean checkedAnims = false;
        boolean hasSleepAnim = false;
        boolean hasDeathAnim = false;

        public boolean reload = false;
        public boolean overrideAnim = false;
        public String anim = "";

        // This is timer to wait until load to display a missingno, ms
        protected long loadTimer;

        // This is the game tick time when we will count as failed, ms
        protected long failTimer;

        HeadInfo headInfo = new HeadInfo();

        public Holder(final PokedexEntry entry)
        {
            super(entry.model(), entry.texture(), entry.animation(), entry.getTrimmedName());
            this.entry = entry;

            if (Database.dummyMap.containsKey(entry.getPokedexNb()))
            {
                final PokedexEntry dummy = Database.dummyMap.get(entry.getPokedexNb());
                String newRes = entry.animation().toString().replace(entry.getTrimmedName(), dummy.getTrimmedName());
                this.backupAnimations.add(ResourceLocation.parse(newRes));
                newRes = entry.model().toString().replace(entry.getTrimmedName(), dummy.getTrimmedName());
                this.backupModels.add(ResourceLocation.parse(newRes));
            }
            if (entry.getBaseForme() != null)
            {
                String newRes = entry.animation().toString()
                        .replace(entry.getTrimmedName(), entry.getBaseForme().getTrimmedName());
                this.backupAnimations.add(ResourceLocation.parse(newRes));
                newRes = entry.model().toString()
                        .replace(entry.getTrimmedName(), entry.getBaseForme().getTrimmedName());
                this.backupModels.add(ResourceLocation.parse(newRes));
            }
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            final IAnimationHolder holder = this.getAnimationHolder();
            if (holder != null && holder.isFixed()) return holder.getAnimation(entityIn);
            if (this.overrideAnim) return this.anim;
            return this.getPhase((Mob) entityIn, PokemobCaps.getPokemobFor(entityIn));
        }

        @Override
        public Map<String, List<Animation>> getAnimations()
        {
            return this.animations;
        }

        private String getPhase(final Mob entity, final IPokemob pokemob)
        {
            if (!this.wrapper.isLoaded()) return "not_loaded_yet!";
            final String phase = "idle";
            if (this.model == null || pokemob == null) return phase;
            final IAnimated anims = ThutCaps.getAnimated(entity);
            for (final String s : anims.getChoices()) if (this.hasAnimation(s, entity)) return s;
            return phase;
        }

        @Override
        public Vector3f getRotationOffset()
        {
            return this.offset;
        }

        @Override
        public Vector3f getScale()
        {
            return this.scale;
        }

        @Override
        public boolean hasAnimation(final String phase, final Entity entity)
        {
            var animator = this.getAnimationChanger();
            if (animator != null && animator.hasAnimation(phase)) return true;
            return IModelRenderer.DEFAULTPHASE.equals(phase) || this.animations.containsKey(phase)
                    || this.wrapper.getModel().getBuiltInAnimations().contains(phase);
        }

        @Override
        public List<Animation> getAnimations(final Entity entity, final String phase)
        {
            this.toRun.clear();
            this.toRunNames.clear();
            var animator = this.getAnimationChanger();
            if (animator != null) animator.getAlternates(this.toRunNames, this.animations.keySet(), entity, phase);
            for (final String name : this.toRunNames)
            {
                final List<Animation> anims = this.animations.get(name);
                if (anims != null) this.toRun.addAll(anims);
            }
            return this.toRun;
        }

        public void init()
        {
            long time = Tracker.instance().getTick();
            boolean noUpdate = this.wrapper != null && this.wrapper.lastInit > time;
            if (noUpdate) return;
            if (ThutCore.conf.debug_models) PokecubeAPI.logDebug("Reloaded model for " + entry);
            RenderPokemob.holders.put(this.entry, this);
            this.toRun.clear();
            this.toRunNames.clear();
            this.parts.clear();
            this.initModel(new ModelWrapper<>(this, this));
            this.checkedAnims = false;
            this.failTimer = time + 100;
            this.loadTimer = time + 5;
            this.wrapper.lastInit = time + 150;
        }

        public void initModel(final ModelWrapper<Mob> model)
        {
            this.wrapper = model;
            ModelFactory.create(model.model, m -> {
                // Set this first in here, so that we can run parse properly.
                this.wrapper.setModel(m);
                AnimationLoader.parse(this, m, this);
            });
        }

        @Override
        public void scaleEntity(final PoseStack mat, final Entity entity, final IModel model, final float partialTick)
        {
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
            float s = 1;
            if (pokemob != null && pokemob.getGeneralState(GeneralStates.EXITINGCUBE))
            {
                float scale = Math.min(1, (entity.tickCount + 1 + partialTick) / LogicMiscUpdate.EXITCUBEDURATION);
                s = Math.max(0.01f, s * scale);
            }
            float sx = this.getScale().x;
            float sy = this.getScale().y;
            float sz = this.getScale().z;
            sx *= s;
            sy *= s;
            sz *= s;
            this.rotPoint.set(this.getRotationOffset()).mul(s);
            model.setOffset(this.rotPoint);
            mat.scale(sx, sy, sz);
        }

        @Override
        public void setRotationOffset(final Vector3f offset)
        {
            this.offset = offset;
        }

        @Override
        public void setScale(final Vector3f scale)
        {
            this.scale = scale;
        }

        @Override
        public void updateModel(final ModelHolder model)
        {
            this.name = model.name;
            this.texture = model.texture;
        }

        @Override
        public HeadInfo getHeadInfo()
        {
            return this.headInfo;
        }

        @Override
        public void setAnimationChanger(final IAnimationChanger changer)
        {
            this.wrapper.animChangeHolder.set(changer);
        }

        @Override
        public IAnimationChanger getAnimationChanger()
        {
            return this.wrapper.animChangeHolder.get();
        }

        @Override
        public void setTexturer(final IPartTexturer texturer)
        {
            this.wrapper.texChangeHolder.set(texturer);
        }

        @Override
        public IPartTexturer getTexturer()
        {
            if (this.wrapper == null) return null;
            if (this.wrapper.texChangeHolder.get() == null) this.setTexturer(new PokemobTexHelper(entry));
            return this.wrapper.texChangeHolder.get();
        }

        @Override
        public void setAnimationHolder(final IAnimationHolder holder)
        {
            if (holder != null) holder.getHeadInfo().copyFrom(this.getHeadInfo());
            this.wrapper.setAnimationHolder(holder);
        }

        @Override
        public IAnimationHolder getAnimationHolder()
        {
            return this.wrapper.animHolderHolder.get();
        }
    }

    public static void reloadModel(final PokedexEntry entry)
    {
        if (RenderPokemob.holders.containsKey(entry))
        {
            var holder = RenderPokemob.holders.get(entry);
            if (holder.wrapper != null) holder.wrapper.lastInit = 0;
            holder.init();
        }
        for (final Holder custom : RenderPokemob.customs.values())
            if (custom.entry == entry)
            {
                if (custom.wrapper != null) custom.wrapper.lastInit = 0;
                custom.init();
            }
    }

    public static final Map<ResourceLocation, Holder> customs = new Object2ObjectOpenHashMap<>();

    public static Map<PokemobType<?>, Holder> holderMap = new Object2ObjectOpenHashMap<>();
    public static Map<PokedexEntry, Holder> holders = new Object2ObjectOpenHashMap<>();

    public static void register()
    {
        customs.clear();
        holderMap.clear();
        holders.clear();
        if (ThutCore.conf.debug_models) PokecubeAPI.logInfo("Registering Models to the renderer.");
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (!entry.stock) continue;
            final Holder holder = new Holder(entry);
            if (!entry.generated)
            {
                final PokemobType<?> type = (PokemobType<?>) entry.getEntityType();
                RenderPokemob.holderMap.put(type, holder);
            }
            RenderPokemob.holders.put(entry, holder);
            // Always initialize starters, so the gui doesn't act a bit funny
            if (PokecubeCore.getConfig().preloadModels || entry.isStarter) holder.init();
        }
    }

    private static final Holder MISSNGNO = new Holder(Database.missingno);

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RenderPokemob(final PokedexEntry entry, final EntityRendererProvider.Context p_i50961_1_)
    {
        super(p_i50961_1_, new ModelWrapper(RenderPokemob.getMissingNo(), RenderPokemob.getMissingNo()), 1);
        if (entry == Database.missingno) register();
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
        FormeHolder forme = pokemob.getCustomHolder();

        if (forme == null || forme == entry.default_holder)
        {
            byte sexe = pokemob.getSexe();
            if (entry.male != null && sexe == IPokemob.MALE)
            {
                holder = RenderPokemob.holders.getOrDefault(entry.male, this.holder);
                forme = entry.male_holder;
            }
            else if (entry.female != null && sexe == IPokemob.FEMALE)
            {
                holder = RenderPokemob.holders.getOrDefault(entry.female, this.holder);
                forme = entry.female_holder;
            }
            else forme = entry.default_holder;
        }

        if (forme != null)
        {
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
        long time = Tracker.instance().getTick();
        if (holder.wrapper == null)
        {
            holder.init();
        }
        if (holder.wrapper != null && !holder.wrapper.isLoaded() && holder.wrapper.lastInit < time)
        {
            holder.init();
            return;
        }
        if (holder.failTimer > 0 && holder.failTimer < time)
        {
            holder.init(); // Recall init again, incase it works
            holder = MISSNGNO;
        }
        // This gives time for the model to actually finish loading in.
        if (holder.loadTimer > time && !holder.wrapper.isLoaded())
        {
            return;
        }
        holder.loadTimer = 0;
        holder.failTimer = 0;
        if (holder.wrapper == null || holder.wrapper.getModel() == null || !holder.wrapper.isValid()
                || holder.model == null || holder.texture == null) holder = RenderPokemob.getMissingNo();

        this.model = holder.wrapper;
        this.activeHolder = holder;
        this.shadowRadius = 0;

        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    protected RenderType getRenderType(final Mob entity, final boolean regular, final boolean model,
            final boolean glowing)
    {
        final RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(this.getTextureLocation(entity), false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
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
        Holder holder;
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
        return switch (bedDir)
        {
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    @Override
    protected void setupRotations(Mob entity, PoseStack stack, float bob, float yBodyRot, float partialTicks,
            float scale)
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
            stack.mulPose(Axis.YP.rotationDegrees(180.0F - yBodyRot));
        }

        if (this.isShaking(entity))
        {
            yBodyRot += (float) (Math.cos(entity.tickCount * 3.25D) * Math.PI * 0.4F);
        }
        if (entity.deathTime > 0)
        {
            if (activeHolder.hasDeathAnim) return;
            // TODO decide on if to fix this?
            float f = (entity.deathTime + yBodyRot - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F)
            {
                f = 1.0F;
            }
            stack.mulPose(Axis.YP.rotationDegrees(f * this.getFlipDegrees(entity)));
        }
        else if (entity.isAutoSpinAttack())
        {
            stack.mulPose(Axis.XP.rotationDegrees(-90.0F - entity.getXRot()));
            stack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTicks) * -75.0F));
        }
        else if (sleeping)
        {
            if (activeHolder.hasSleepAnim) return;
            Direction direction = entity.getBedOrientation();
            if (direction != Direction.UP)
            {
                float f1 = sleepDirectionToRotation(direction);
                stack.mulPose(Axis.YP.rotationDegrees(f1));
                stack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees(entity)));
                stack.mulPose(Axis.YP.rotationDegrees(270.0F));
            }
            else
            {
                stack.translate(entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0);
                stack.mulPose(Axis.ZP.rotationDegrees(90));
            }
        }
        else if (isEntityUpsideDown(entity))
        {
            stack.translate(0.0D, (entity.getBbHeight() + 0.1F), 0.0D);
            stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }
}
