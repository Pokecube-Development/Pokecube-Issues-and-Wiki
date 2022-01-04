package pokecube.core.client.render.mobs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.utils.PokeType;
import thut.api.ModelHolder;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.CapabilityAnimation;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;

public class RenderEgg extends LivingEntityRenderer<EntityPokemobEgg, ModelWrapper<EntityPokemobEgg>>
        implements IModelRenderer<EntityPokemobEgg>
{
    static final ResourceLocation TEXTURE = new ResourceLocation(PokecubeCore.MODID, "entity/textures/egg.png");
    static final ResourceLocation MODEL = new ResourceLocation(PokecubeCore.MODID, "entity/models/egg.x3d");
    static final ResourceLocation ANIM = new ResourceLocation(PokecubeCore.MODID, "entity/animations/egg.xml");

    private static class EggColourer implements IAnimationChanger
    {
        IAnimationHolder anims = new CapabilityAnimation.DefaultImpl();

        @Override
        public void addChild(final IAnimationChanger animationRandomizer)
        {}

        @Override
        public boolean modifyColourForPart(final String partIdentifier, final Entity entity, final int[] rgba)
        {
            final IPokemob poke = ((EntityPokemobEgg) entity).getPokemob(false);
            if (poke == null) return false;
            final PokeType t1 = poke.getType1();
            final PokeType t2 = poke.getType2();
            final int rgb = partIdentifier.contains("spot") ? t2.colour : t1.colour;
            final Color c = new Color(rgb);
            rgba[0] = c.getRed();
            rgba[1] = c.getGreen();
            rgba[2] = c.getBlue();
            rgba[3] = 255;
            return true;
        }

        @Override
        public WornOffsets getOffsets(final String part)
        {
            return null;
        }

        @Override
        public void init(final Collection<Animation> anims)
        {}

        @Override
        public void parseDyeables(final Set<String> set)
        {}

        @Override
        public void parseShearables(final Set<String> set)
        {}

        @Override
        public void parseWornOffsets(final Map<String, WornOffsets> map)
        {}

        @Override
        public void setAnimationHolder(final IAnimationHolder holder)
        {
            this.anims = holder;
        }

        @Override
        public IAnimationHolder getAnimationHolder()
        {
            return this.anims;
        }

    }

    private final HashMap<String, List<Animation>> anims = Maps.newHashMap();

    private IAnimationChanger changer = null;
    private IPartTexturer texer = null;

    private final Vector3 scale = Vector3.getNewVector();

    IAnimationHolder animHolder = new CapabilityAnimation.DefaultImpl();

    public RenderEgg(final EntityRendererProvider.Context manager)
    {
        super(manager, null, 0.1f);
        this.model = this.makeModel();
    }

    private ModelWrapper<EntityPokemobEgg> makeModel()
    {
        final ModelHolder holder = new ModelHolder(RenderEgg.MODEL, RenderEgg.TEXTURE, RenderEgg.ANIM, "pokemob_egg");
        final ModelWrapper<EntityPokemobEgg> model = new ModelWrapper<>(holder, this);
        model.imodel = ModelFactory.create(holder);
        AnimationLoader.parse(model.model, model, this);
        this.changer = new EggColourer();
        return model;
    }

    @Override
    protected RenderType getRenderType(final EntityPokemobEgg entity, final boolean bool_a, final boolean bool_b,
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
        return RenderType.create("pokecube:pokemob_egg", DefaultVertexFormat.NEW_ENTITY, Mode.TRIANGLES, 256, bool_a,
                bool_b, rendertype$state);
    }

    @Override
    public Vector3 getScale()
    {
        return this.scale;
    }

    @Override
    protected boolean shouldShowName(final EntityPokemobEgg entity)
    {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(final EntityPokemobEgg entity)
    {
        return RenderEgg.TEXTURE;
    }

    @Override
    public IAnimationChanger getAnimationChanger()
    {
        return this.changer;
    }

    @Override
    public HashMap<String, List<Animation>> getAnimations()
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
        return false;
    }

    @Override
    public void scaleEntity(final PoseStack mat, final Entity entity, final IModel model, final float partialTick)
    {
        final float s = 0.15f;
        float sx = (float) this.getScale().x;
        float sy = (float) this.getScale().y;
        float sz = (float) this.getScale().z;
        sx *= s;
        sy *= s;
        sz *= s;
        if (!this.getScale().isEmpty()) mat.scale(sx, sy, sz);
        else mat.scale(s, s, s);
    }

    @Override
    public void setAnimationChanger(final IAnimationChanger changer)
    {
        this.changer = changer;
    }

    @Override
    public void setRotationOffset(final Vector3 offset)
    {}

    @Override
    public void setRotations(final thut.core.client.render.model.IModelRenderer.Vector5 rotations)
    {}

    @Override
    public void setScale(final Vector3 scale)
    {}

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        this.texer = texturer;
    }

    @Override
    public void updateModel(
            final HashMap<String, ArrayList<thut.core.client.render.model.IModelRenderer.Vector5>> phaseList,
            final ModelHolder model)
    {

    }

    @Override
    public void setAnimationHolder(final IAnimationHolder holder)
    {
        this.animHolder = holder;
    }

    @Override
    public IAnimationHolder getAnimationHolder()
    {
        return this.animHolder;
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return HeadInfo.DUMMY;
    }
}
