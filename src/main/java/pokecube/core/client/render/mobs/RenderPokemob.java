package pokecube.core.client.render.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Node;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;

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
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.model.PartInfo;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;

public class RenderPokemob extends MobRenderer<GenericPokemob, ModelWrapper<GenericPokemob>>
{
    public static class Holder extends ModelHolder implements IModelRenderer<GenericPokemob>
    {
        ModelWrapper<GenericPokemob>            wrapper;
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
        }

        @Override
        public void doRender(final GenericPokemob entity, final double d, final double d1, final double d2,
                final float f, final float partialTick)
        {
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
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
                final String anim = state.toString().toLowerCase(Locale.ENGLISH);
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
                final String anim = state.toString().toLowerCase(Locale.ENGLISH);
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
        public void handleCustomTextures(final Node node)
        {
            this.setTextureDetails(node);
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
            this.name = model.model.name;
            this.texture = model.model.texture;
            model.imodel = ModelFactory.create(model.model);
            AnimationLoader.parse(model.model, model, this);
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

        protected void postRenderStatus()
        {
            if (this.light) GL11.glEnable(GL11.GL_LIGHTING);
            if (!this.blend) GL11.glDisable(GL11.GL_BLEND);
            GL11.glBlendFunc(this.src, this.dst);
        }

        protected void preRenderStatus()
        {
            this.blend = GL11.glGetBoolean(GL11.GL_BLEND);
            this.light = GL11.glGetBoolean(GL11.GL_LIGHTING);
            this.src = GL11.glGetInteger(GL11.GL_BLEND_SRC);
            this.dst = GL11.glGetInteger(GL11.GL_BLEND_DST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        @Override
        public void renderStatus(final GenericPokemob entity, final double d, final double d1, final double d2,
                final float f, final float partialTick)
        {

        }

        @Override
        public void scaleEntity(final Entity entity, final IModel model, final float partialTick)
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

        private void setTextureDetails(final Node node)
        {
            if (node.getAttributes() == null) return;
            String[] male = null, female = null;
            if (node.getAttributes().getNamedItem("male") != null)
            {
                String shift;
                shift = node.getAttributes().getNamedItem("male").getNodeValue();
                male = shift.split(",");
                for (int i = 0; i < male.length; i++)
                    male[i] = Database.trim(male[i]);
            }
            if (node.getAttributes().getNamedItem("female") != null)
            {
                String shift;
                shift = node.getAttributes().getNamedItem("female").getNodeValue();
                female = shift.split(",");
                for (int i = 0; i < female.length; i++)
                    female[i] = Database.trim(female[i]);
            }
            if (female == null && male != null || this.entry.textureDetails == null) female = male;
            if (male != null)
            {
                this.entry.textureDetails[0] = male;
                this.entry.textureDetails[1] = female;
            }
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

    public static boolean reload_models = false;

    public static Map<PokemobType<?>, Holder> holderMap = Maps.newHashMap();

    public static void register()
    {
        PokecubeCore.LOGGER.info("Registering Models to the renderer.");
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            final PokemobType<?> type = (PokemobType<?>) PokecubeCore.typeMap.get(entry);
            RenderPokemob.holderMap.put(type, new Holder(entry));
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
        final PokemobType<?> type = (PokemobType<?>) entity.getType();
        Holder holder = RenderPokemob.holderMap.getOrDefault(type, this.holder);
        if (holder.wrapper == null || RenderPokemob.reload_models) holder.init();
        RenderPokemob.reload_models = false;
        if (holder.wrapper == null || holder.wrapper.imodel == null || !holder.wrapper.isValid() || holder.entry != type
                .getEntry() || holder.model == null || holder.texture == null) holder = this.holder;
        this.entityModel = holder.wrapper;
        try
        {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
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
        final PokemobType<?> type = (PokemobType<?>) entity.getType();
        final Holder holder = RenderPokemob.holderMap.getOrDefault(type, this.holder);
        return holder.texture;
    }
}
