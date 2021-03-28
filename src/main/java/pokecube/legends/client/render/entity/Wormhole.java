package pokecube.legends.client.render.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import pokecube.legends.Reference;
import pokecube.legends.entity.WormholeEntity;
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

public class Wormhole extends LivingRenderer<WormholeEntity, ModelWrapper<WormholeEntity>> implements
        IModelRenderer<WormholeEntity>
{
    static final ResourceLocation TEXTURE = new ResourceLocation(Reference.ID, "entity/textures/wormhole.png");
    static final ResourceLocation MODEL   = new ResourceLocation(Reference.ID, "entity/models/wormhole.x3d");
    static final ResourceLocation ANIM    = new ResourceLocation(Reference.ID, "entity/animations/wormhole.xml");

    private final HashMap<String, List<Animation>> anims = Maps.newHashMap();

    private IAnimationChanger changer = null;
    private IPartTexturer     texer   = null;

    private final Vector3 scale = Vector3.getNewVector();

    public Wormhole(final EntityRendererManager manager)
    {
        super(manager, null, 0f);
        this.model = this.makeModel();
    }

    private ModelWrapper<WormholeEntity> makeModel()
    {
        final ModelHolder holder = new ModelHolder(Wormhole.MODEL, Wormhole.TEXTURE, Wormhole.ANIM, "ultra_wormhole");
        final ModelWrapper<WormholeEntity> model = new ModelWrapper<>(holder, this);
        model.imodel = ModelFactory.create(holder);
        AnimationLoader.parse(model.model, model, this);
        return model;
    }

    @Override
    protected RenderType getRenderType(final WormholeEntity entity, final boolean bool_a, final boolean bool_b,
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
        return RenderType.create("pokecube_legends:wormhole", DefaultVertexFormats.NEW_ENTITY, GL11.GL_TRIANGLES, 256,
                bool_a, bool_b, rendertype$state);
    }

    @Override
    public ResourceLocation getTextureLocation(final WormholeEntity mob)
    {
        return Wormhole.TEXTURE;
    }

    @Override
    protected boolean shouldShowName(final WormholeEntity entity)
    {
        return false;
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
    public Vector3 getScale()
    {
        return this.scale;
    }

    @Override
    public void scaleEntity(final MatrixStack mat, final Entity entity, final IModel model, final float partialTick)
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
    public void setRotations(final Vector5 rotations)
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
    public void updateModel(final HashMap<String, ArrayList<Vector5>> phaseList, final ModelHolder model)
    {
    }

}
