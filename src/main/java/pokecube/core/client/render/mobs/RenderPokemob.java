package pokecube.core.client.render.mobs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.GenericPokemob;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.model.PartInfo;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.core.common.ThutCore;

public class RenderPokemob extends MobRenderer<GenericPokemob, ModelWrapper<GenericPokemob>>
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

    public static class Holder extends ModelHolder implements IModelRenderer<GenericPokemob>
    {
        public ModelWrapper<GenericPokemob>     wrapper;
        final Vector3                           rotPoint   = Vector3.getNewVector();
        HashMap<String, List<Animation>>        anims      = Maps.newHashMap();
        private IPartTexturer                   texturer;
        private IAnimationChanger               animator;
        public String                           name;
        public HashMap<String, PartInfo>        parts      = Maps.newHashMap();
        HashMap<String, ArrayList<Vector5>>     global;
        public HashMap<String, List<Animation>> animations = Maps.newHashMap();
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

        public boolean reload       = false;
        public boolean overrideAnim = false;
        public String  anim         = "";

        public Vector5 rotations = new Vector5();

        boolean blend;

        boolean light;

        int src;

        ///////////////////// IModelRenderer stuff below here//////////////////

        int dst;

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
        public void doRender(final GenericPokemob entity, final double d, final double d1, final double d2,
                final float f, final float partialTick)
        {
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            if (this.overrideAnim) return this.anim;
            return this.getPhase((MobEntity) entityIn, CapabilityPokemob.getPokemobFor(entityIn));
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

        private HashMap<String, PartInfo> getChildren(final IExtendedModelPart part)
        {
            final HashMap<String, PartInfo> partsList = new HashMap<>();
            for (final String s : part.getSubParts().keySet())
            {
                final PartInfo p = new PartInfo(s);
                final IExtendedModelPart subPart = (IExtendedModelPart) part.getSubParts().get(s);
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
            String phase = IModelRenderer.super.getAnimation(entity);
            if (this.model == null || pokemob == null) return phase;
            final Vec3d velocity = entity.getMotion();
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

            if (asleep && this.hasAnimation("sleeping", entity))
            {
                phase = "sleeping";
                return phase;
            }
            if (asleep && this.hasAnimation("asleep", entity))
            {
                phase = "asleep";
                return phase;
            }
            if (!entity.onGround && this.hasAnimation("flight", entity))
            {
                phase = "flight";
                return phase;
            }
            if (!entity.onGround && this.hasAnimation("flying", entity))
            {
                phase = "flying";
                return phase;
            }
            if (entity.isInWater() && this.hasAnimation("swimming", entity))
            {
                phase = "swimming";
                return phase;
            }
            if (entity.onGround && walkspeed > stationary && this.hasAnimation("walking", entity))
            {
                phase = "walking";
                return phase;
            }
            if (entity.onGround && walkspeed > stationary && this.hasAnimation("walk", entity))
            {
                phase = "walk";
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
            return IModelRenderer.DEFAULTPHASE.equals(phase) || this.animations.containsKey(phase)
                    || this.wrapper.imodel.getBuiltInAnimations().contains(phase);
        }

        public void init()
        {
            this.initModel(new ModelWrapper<>(this, this));
        }

        public void initModel(final ModelWrapper<GenericPokemob> model)
        {
            this.wrapper = model;
            model.imodel = ModelFactory.create(model.model);

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
        public void scaleEntity(final Entity entity, final IModel model, final float partialTick)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            float s = 1;
            if (pokemob != null && entity.addedToChunk) s = pokemob.getEntity().getRenderScale();
            float sx = (float) this.getScale().x;
            float sy = (float) this.getScale().y;
            float sz = (float) this.getScale().z;
            sx *= s;
            sy *= s;
            sz *= s;
            // TODO see if this is where things are going funny with tbl
            // offsets?
            this.rotPoint.set(this.getRotationOffset()).scalarMultBy(s);
            model.setOffset(this.rotPoint);
            if (!this.getScale().isEmpty()) GlStateManager.scalef(sx, sy, sz);
            else GlStateManager.scalef(s, s, s);
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
            final PokemobType<?> type = (PokemobType<?>) PokecubeCore.typeMap.get(entry);
            final Holder holder = new Holder(entry);
            RenderPokemob.holderMap.put(type, holder);
            RenderPokemob.holders.put(entry, holder);
        }
    }

    final Holder holder;

    public RenderPokemob(final PokedexEntry entry, final EntityRendererManager p_i50961_1_)
    {
        super(p_i50961_1_, null, 1);
        this.holder = new Holder(entry);
        this.holder.init();
        final ModelWrapper<GenericPokemob> model = new ModelWrapper<>(this.holder, this.holder);
        this.entityModel = model;
    }

    @Override
    public void doRender(final GenericPokemob entity, final double x, final double y, final double z,
            final float entityYaw, final float partialTicks)
    {
        final IPokemob pokemob = entity.pokemobCap;
        if (pokemob.getTransformedTo() != null)
        {
            this.renderManager.getRenderer(pokemob.getTransformedTo()).doRender(pokemob.getTransformedTo(), x, y, z,
                    entityYaw, partialTicks);
            return;
        }

        final PokemobType<?> type = (PokemobType<?>) entity.getType();
        Holder holder = RenderPokemob.holderMap.getOrDefault(type, this.holder);

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

        if (holder.wrapper == null) holder.init();
        holder.reload = false;
        if (holder.wrapper == null || holder.wrapper.imodel == null || !holder.wrapper.isValid() || holder.model == null
                || holder.texture == null)
        {
            this.holder.toString();
            holder = this.holder;
        }
        this.entityModel = holder.wrapper;
        this.shadowSize = entity.getWidth();
        try
        {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
            // holder.wrapper = null;
        }
        catch (final Exception e)
        {
            // holderMap.put(type, this.holder);
            PokecubeCore.LOGGER.error("Error rendering " + type.getEntry(), e);
        }
    }

    @Override
    public ResourceLocation getEntityTexture(final GenericPokemob entity)
    {
        return this.holder.texture;
    }
}
