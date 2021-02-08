package pokecube.core.client.render.mobs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader.DefaultFormeHolder.TexColours;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.TextureableCaps.PokemobCap;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.moves.MovesUtils;
import thut.api.ModelHolder;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.model.PartInfo;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.core.common.ThutCore;

public class RenderPokemob extends MobRenderer<MobEntity, ModelWrapper<MobEntity>>
{
    public static class PokemobTexHelper extends TextureHelper
    {
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
            if (this.mob instanceof PokemobCap) mob = ((PokemobCap) this.mob).pokemob;
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
            if (this.mob instanceof PokemobCap) mob = ((PokemobCap) this.mob).pokemob;
            if (mob == null) return false;
            final FormeHolder holder = mob.getCustomHolder();
            if (holder == null || holder.loaded_from == null) return false;
            return holder.loaded_from._hide_.contains(part);
        }

        @Override
        public void applyTexturePhase(final Phase phase)
        {
            final QName male = new QName("male");
            final QName female = new QName("female");
            if (phase.values.containsKey(male)) this.entry.textureDetails[0] = this.fromValue(phase.values.get(male));
            if (phase.values.containsKey(female)) this.entry.textureDetails[1] = this.fromValue(phase.values.get(
                    female));
        }

        private String[] fromValue(final String string)
        {
            final String[] ret = string.split(",");
            for (int i = 0; i < ret.length; i++)
                ret[i] = ThutCore.trim(ret[i]);
            return ret;
        }
    }

    public static class Holder extends ModelHolder implements IModelRenderer<MobEntity>
    {
        public ModelWrapper<MobEntity>          wrapper;
        final Vector3                           rotPoint   = Vector3.getNewVector();
        HashMap<String, List<Animation>>        anims      = Maps.newHashMap();
        private IPartTexturer                   texturer;
        private IAnimationChanger               animator;
        public String                           name;
        public HashMap<String, PartInfo>        parts      = Maps.newHashMap();
        HashMap<String, ArrayList<Vector5>>     global;
        public HashMap<String, List<Animation>> animations = Maps.newHashMap();
        private final List<String>              toRunNames = Lists.newArrayList();
        private final List<Animation>           toRun      = Lists.newArrayList();
        public Vector3                          offset     = Vector3.getNewVector();;
        public Vector3                          scale      = Vector3.getNewVector();
        ResourceLocation                        texture;
        PokedexEntry                            entry;
        // Used to check if it has a custom sleeping animation.
        private boolean checkedForContactAttack   = false;
        private boolean hasContactAttackAnimation = false;

        // Used to check if it has a custom sleeping animation.
        private boolean checkedForRangedAttack   = false;
        private boolean hasRangedAttackAnimation = false;

        // Used to check if it has a custom sleeping animation.
        private boolean checkedForDead   = false;
        private boolean hasDeadAnimation = false;

        public boolean reload       = false;
        public boolean overrideAnim = false;
        public String  anim         = "";

        public Vector5 rotations = new Vector5();

        // This will decrement if above 0, and if so, we don't render, this
        // gives some time to actually load the model.
        protected int loadTimer = 3;

        IAnimationHolder currentHolder = null;

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
                String newRes = entry.animation().toString().replace(entry.getTrimmedName(), entry.getBaseForme()
                        .getTrimmedName());
                this.backupAnimations.add(new ResourceLocation(newRes));
                newRes = entry.model().toString().replace(entry.getTrimmedName(), entry.getBaseForme()
                        .getTrimmedName());
                this.backupModels.add(new ResourceLocation(newRes));
            }
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            if (this.overrideAnim) return this.anim;
            final String phase = this.getPhase((MobEntity) entityIn, CapabilityPokemob.getPokemobFor(entityIn));
            return phase;
        }

        @Override
        public IAnimationChanger getAnimationChanger()
        {
            return this.animator;
        }

        @Override
        public HashMap<String, List<Animation>> getAnimations()
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
            if (this.animator != null) this.animator.setAnimationHolder(holder);
            this.wrapper.imodel.setAnimationHolder(holder);
        }

        private HashMap<String, PartInfo> getChildren(final IExtendedModelPart part)
        {
            final HashMap<String, PartInfo> partsList = new HashMap<>();
            for (final String s : part.getSubParts().keySet())
            {
                final PartInfo p = new PartInfo(s);
                final IExtendedModelPart subPart = part.getSubParts().get(s);
                p.children = this.getChildren(subPart);
                partsList.put(s, p);
            }
            return partsList;
        }

        private PartInfo getPartInfo(final String partName)
        {
            PartInfo ret = null;
            for (final PartInfo part : this.parts.values())
            {
                if (part.name.equalsIgnoreCase(partName)) return part;
                ret = this.getPartInfo(partName, part);
                if (ret != null) return ret;
            }
            for (final IExtendedModelPart part : this.wrapper.getParts().values())
                if (part.getName().equals(partName))
                {
                    final PartInfo p = new PartInfo(part.getName());
                    p.children = this.getChildren(part);
                    boolean toAdd = true;
                    IExtendedModelPart parent = part.getParent();
                    while (parent != null && toAdd)
                    {
                        toAdd = !this.parts.containsKey(parent.getName());
                        parent = parent.getParent();
                    }
                    if (toAdd) this.parts.put(partName, p);
                    return p;
                }

            return ret;
        }

        private PartInfo getPartInfo(final String partName, final PartInfo parent)
        {
            PartInfo ret = null;
            for (final PartInfo part : parent.children.values())
            {
                if (part.name.equalsIgnoreCase(partName)) return part;
                ret = this.getPartInfo(partName, part);
                if (ret != null) return ret;
            }

            return ret;
        }

        private String getPhase(final MobEntity entity, final IPokemob pokemob)
        {
            if (!this.wrapper.isLoaded()) return "not_loaded_yet!";
            String phase = "idle";
            if (this.model == null || pokemob == null) return phase;
            final Vector3d velocity = entity.getMotion();
            final float dStep = entity.limbSwingAmount - entity.prevLimbSwingAmount;
            final float walkspeed = (float) (velocity.x * velocity.x + velocity.z * velocity.z + dStep * dStep);
            final float stationary = 0.00001f;
            final boolean asleep = pokemob.getStatus() == IMoveConstants.STATUS_SLP || pokemob.getLogicState(
                    LogicStates.SLEEPING);

            if (!this.checkedForContactAttack)
            {
                this.hasContactAttackAnimation = this.hasAnimation("attack_contact", entity);
                this.checkedForContactAttack = true;
            }
            if (!this.checkedForRangedAttack)
            {
                this.hasRangedAttackAnimation = this.hasAnimation("attack_ranged", entity);
                this.checkedForRangedAttack = true;
            }
            if (!this.checkedForDead)
            {
                this.hasDeadAnimation = this.hasAnimation("dead", entity);
                this.checkedForDead = true;
            }

            if (entity.deathTime > 0) return this.hasDeadAnimation ? "dead" : phase;
            if (pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
            {
                final int index = pokemob.getMoveIndex();
                Move_Base move;
                if (index < 4 && (move = MovesUtils.getMoveFromName(pokemob.getMove(index))) != null)
                {
                    if (this.hasContactAttackAnimation && (move.getAttackCategory()
                            & IMoveConstants.CATEGORY_CONTACT) > 0)
                    {
                        phase = "attack_contact";
                        return phase;
                    }
                    if (this.hasRangedAttackAnimation && (move.getAttackCategory()
                            & IMoveConstants.CATEGORY_DISTANCE) > 0)
                    {
                        phase = "attack_ranged";
                        return phase;
                    }
                }
            }

            for (final LogicStates state : LogicStates.values())
            {
                final String anim = ThutCore.trim(state.toString());
                if (pokemob.getLogicState(state) && this.hasAnimation(anim, entity)) return anim;
            }

            BlockPos pos;
            final boolean flying = !entity.isOnGround() && !(entity.getPosY() - (int) entity.getPosY() < 0.01f && entity
                    .getEntityWorld().getBlockState(pos = entity.getPosition().down()).isTopSolid(entity
                            .getEntityWorld(), pos, entity, Direction.UP));
            final boolean walking = entity.isOnGround() && walkspeed > stationary;
            final boolean swimming = entity.isInWater();

            if (asleep && this.hasAnimation("sleeping", entity))
            {
                phase = "sleeping";
                return phase;
            }
            if (flying && this.hasAnimation("flying", entity))
            {
                phase = "flying";
                return phase;
            }
            if (swimming && this.hasAnimation("swimming", entity))
            {
                phase = "swimming";
                return phase;
            }
            if (walking && this.hasAnimation("walking", entity))
            {
                phase = "walking";
                return phase;
            }

            for (final CombatStates state : CombatStates.values())
            {
                final String anim = ThutCore.trim(state.toString());
                if (pokemob.getCombatState(state) && this.hasAnimation(anim, entity)) return anim;
            }
            return phase;
        }

        @Override
        public Vector3 getRotationOffset()
        {
            return this.offset;
        }

        @Override
        public Vector5 getRotations()
        {
            return this.rotations;
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
            if (this.animator != null) this.animator.getAlternates(this.toRunNames, this.animations.keySet(), entity,
                    phase);
            for (final String name : this.toRunNames)
            {
                final List<Animation> anims = this.animations.get(name);
                if (anims != null) this.toRun.addAll(anims);
            }
            return this.toRun;
        }

        public void init()
        {
            RenderPokemob.holders.put(this.entry, this);
            this.toRun.clear();
            this.toRunNames.clear();
            this.parts.clear();
            this.initModel(new ModelWrapper<>(this, this));
        }

        public void initModel(final ModelWrapper<MobEntity> model)
        {
            this.wrapper = model;
            ModelFactory.create(model.model, m ->
            {
                model.imodel = m;
                // Check if an animation file exists.
                try
                {
                    Minecraft.getInstance().getResourceManager().getResource(this.animation);
                }
                catch (final IOException e)
                {
                    // No animation here, lets try to use the base one.
                }

                AnimationLoader.parse(this, model, this);
                this.initModelParts();
            });
        }

        private void initModelParts()
        {
            if (this.wrapper == null) return;

            for (final String s : this.wrapper.getParts().keySet())
                if (this.wrapper.getParts().get(s).getParent() == null && !this.parts.containsKey(s))
                {
                    final PartInfo p = this.getPartInfo(s);
                    this.parts.put(s, p);
                }
        }

        @Override
        public void scaleEntity(final MatrixStack mat, final Entity entity, final IModel model, final float partialTick)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            float s = 1;
            if (pokemob != null) s = pokemob.getEntity().getRenderScale();
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
        public void setRotations(final Vector5 rotations)
        {
            this.rotations = rotations;
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
        public void updateModel(final HashMap<String, ArrayList<Vector5>> global, final ModelHolder model)
        {
            this.name = model.name;
            this.texture = model.texture;
            this.initModelParts();
            this.global = global;
        }
    }

    public static void reloadModel(final PokedexEntry entry)
    {
        if (RenderPokemob.holders.containsKey(entry)) RenderPokemob.holders.get(entry).init();
        for (final Holder custom : RenderPokemob.customs.values())
            if (custom.entry == entry) custom.init();
    }

    public static final Map<ResourceLocation, Holder> customs = Maps.newHashMap();

    public static Map<PokemobType<?>, Holder> holderMap = Maps.newHashMap();
    public static Map<PokedexEntry, Holder>   holders   = Maps.newHashMap();

    public static void register()
    {
        PokecubeCore.LOGGER.info("Registering Models to the renderer.");
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (!entry.stock) continue;
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
        if (RenderPokemob.MISSNGNO.wrapper == null) RenderPokemob.MISSNGNO.init();
        return RenderPokemob.MISSNGNO;
    }

    final Holder holder;

    public RenderPokemob(final PokedexEntry entry, final EntityRendererManager p_i50961_1_)
    {
        super(p_i50961_1_, null, 1);
        if (RenderPokemob.holders.containsKey(entry)) this.holder = RenderPokemob.holders.get(entry);
        else
        {
            this.holder = new Holder(entry);
            RenderPokemob.holders.put(entry, this.holder);
        }
    }

    @Override
    protected float getDeathMaxRotation(final MobEntity entityLivingBaseIn)
    {
        return 85.0f;
    }

    @Override
    public void render(final MobEntity entity, final float entityYaw, final float partialTicks,
            final MatrixStack matrixStackIn, final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return;
        transform:
        if (pokemob.getTransformedTo() != null)
        {
            final Entity to = pokemob.getTransformedTo();
            final IPokemob other = CapabilityPokemob.getPokemobFor(to);
            // No transform loops!
            if (other != null && other.getTransformedTo() != null) break transform;
            this.renderManager.getRenderer(pokemob.getTransformedTo()).render(pokemob.getTransformedTo(), entityYaw,
                    partialTicks, matrixStackIn, bufferIn, packedLightIn);
            return;
        }

        Holder holder = RenderPokemob.holders.getOrDefault(pokemob.getPokedexEntry(), this.holder);

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

        if (holder.wrapper == null)
        {
            holder.init();
            PokecubeMod.LOGGER.debug("Reloaded model for " + pokemob.getPokedexEntry());
        }
        if (holder.wrapper != null && !holder.wrapper.isLoaded()) return;

        // This gives time for the model to actually finish loading in.
        if (holder.loadTimer-- > 0) return;
        holder.loadTimer = 0;

        if (holder.wrapper == null || holder.wrapper.imodel == null || !holder.wrapper.isValid() || holder.model == null
                || holder.texture == null) holder = RenderPokemob.getMissingNo();

        this.entityModel = holder.wrapper;
        this.shadowSize = entity.getWidth();
        try
        {
            final IPartTexturer texer = holder.wrapper.renderer.getTexturer();
            final ResourceLocation default_ = this.getEntityTexture(entity);
            if (texer != null)
            {
                texer.bindObject(entity);
                holder.wrapper.getParts().forEach((n, p) ->
                {
                    p.applyTexture(bufferIn, default_, texer);
                });
            }
            holder.wrapper.SetEntity(entity);
            super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        }
        catch (final Exception e)
        {
            // holderMap.put(type, this.holder);
            PokecubeCore.LOGGER.error("Error rendering " + pokemob.getPokedexEntry(), e);
        }
    }

    @Override
    protected RenderType func_230496_a_(final MobEntity entity, final boolean bool_a, final boolean bool_b,
            final boolean bool_c)
    {
        final RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(
                this.getEntityTexture(entity), false, false)).transparency(new RenderState.TransparencyState(
                        "translucent_transparency", () ->
                        {
                            RenderSystem.enableBlend();
                            RenderSystem.defaultBlendFunc();
                        }, () ->
                        {
                            RenderSystem.disableBlend();
                        })).diffuseLighting(new RenderState.DiffuseLightingState(true)).alpha(
                                new RenderState.AlphaState(0.003921569F)).cull(new RenderState.CullState(false))
                .lightmap(new RenderState.LightmapState(true)).overlay(new RenderState.OverlayState(true)).build(false);
        return RenderType.makeType("pokecube:pokemob", DefaultVertexFormats.ENTITY, GL11.GL_TRIANGLES, 256, bool_a,
                bool_b, rendertype$state);
    }

    @Override
    public ResourceLocation getEntityTexture(final MobEntity entity)
    {
        final ResourceLocation texture = Database.missingno.texture;
        Holder holder = this.holder;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
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
        if (holder.getTexturer() == null) return texture;
        final ResourceLocation tex = holder.getTexturer().getTexture("", texture);
        return tex == null ? texture : tex;
    }
}
