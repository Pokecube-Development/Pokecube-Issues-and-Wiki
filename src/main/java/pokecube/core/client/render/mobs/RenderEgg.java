package pokecube.core.client.render.mobs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.utils.PokeType;
import thut.api.ModelHolder;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.Animation;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;

public class RenderEgg extends LivingRenderer<EntityPokemobEgg, ModelWrapper<EntityPokemobEgg>> implements
        IModelRenderer<EntityPokemobEgg>
{
    static final ResourceLocation TEXTURE = new ResourceLocation(PokecubeCore.MODID, "entity/textures/egg.png");
    static final ResourceLocation MODEL   = new ResourceLocation(PokecubeCore.MODID, "entity/models/egg.x3d");
    static final ResourceLocation ANIM    = new ResourceLocation(PokecubeCore.MODID, "entity/animations/egg.xml");

    private static class EggColourer implements IAnimationChanger
    {

        @Override
        public void addChild(final IAnimationChanger animationRandomizer)
        {
        }

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
        {
        }

        @Override
        public void parseDyeables(final Set<String> set)
        {
        }

        @Override
        public void parseShearables(final Set<String> set)
        {
        }

        @Override
        public void parseWornOffsets(final Map<String, WornOffsets> map)
        {
        }

        @Override
        public void setAnimationHolder(final IAnimationHolder holder)
        {
        }

        @Override
        public IAnimationHolder getAnimationHolder()
        {
            return null;
        }

    }

    private final HashMap<String, List<Animation>> anims = Maps.newHashMap();

    private IAnimationChanger changer = null;
    private IPartTexturer     texer   = null;

    private final Vector3 scale = Vector3.getNewVector();

    public RenderEgg(final EntityRendererManager manager)
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
        final RenderType.State rendertype$state = RenderType.State.builder().setTextureState(
                new RenderState.TextureState(this.getTextureLocation(entity), false, false)).setTransparencyState(
                        new RenderState.TransparencyState("translucent_transparency", () ->
                        {
                            RenderSystem.enableBlend();
                            RenderSystem.defaultBlendFunc();
                        }, () ->
                        {
                            RenderSystem.disableBlend();
                        })).setDiffuseLightingState(new RenderState.DiffuseLightingState(true)).setAlphaState(
                                new RenderState.AlphaState(0.003921569F)).setCullState(new RenderState.CullState(false))
                .setLightmapState(new RenderState.LightmapState(true)).setOverlayState(new RenderState.OverlayState(
                        true)).createCompositeState(false);
        return RenderType.create("pokecube:pokemob_egg", DefaultVertexFormats.NEW_ENTITY, GL11.GL_TRIANGLES, 256,
                bool_a, bool_b, rendertype$state);
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
    public void scaleEntity(final MatrixStack mat, final Entity entity, final IModel model, final float partialTick)
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
    {
    }

    @Override
    public void setRotations(final thut.core.client.render.model.IModelRenderer.Vector5 rotations)
    {
    }

    @Override
    public void setScale(final Vector3 scale)
    {
    }

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
    }

    @Override
    public IAnimationHolder getAnimationHolder()
    {
        return null;
    }
}
