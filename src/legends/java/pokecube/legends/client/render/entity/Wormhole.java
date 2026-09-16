package pokecube.legends.client.render.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import pokecube.legends.Reference;
import pokecube.legends.entity.WormholeEntity;
import thut.api.ModelHolder;
import thut.api.entity.IAnimated.HeadInfo;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.IAnimationChanger;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;

public class Wormhole extends LivingEntityRenderer<WormholeEntity, ModelWrapper<WormholeEntity>>
        implements IModelRenderer<WormholeEntity>
{
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.ID, "entity/textures/wormhole.png");
    static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Reference.ID, "entity/models/wormhole");

    final Vector3f rotPoint = new Vector3f();

    public Wormhole(final EntityRendererProvider.Context renderManager)
    {
        super(renderManager, null, 0.0f);
        this.model = this.makeModel();
    }

    private ModelWrapper<WormholeEntity> makeModel()
    {
        final ModelHolder holder = new ModelHolder(Wormhole.MODEL);
        final ModelWrapper<WormholeEntity> model = new ModelWrapper<>(holder, this);
        this.model = model;
        ModelFactory.create(model.model, m -> {
            model.setModel(m);
            AnimationLoader.parse(holder, model, this);
        });
        return model;
    }

    @Override
    protected RenderType getRenderType(final WormholeEntity entity, final boolean bool_a, final boolean bool_b,
            final boolean bool_c)
    {
        if (model.lastInit == -1)
        {
            this.model = this.makeModel();
            this.model.lastInit = 0;
        }
        final RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(this.getTextureLocation(entity), false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setCullState(new RenderStateShard.CullStateShard(false))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .setOverlayState(new RenderStateShard.OverlayStateShard(true)).createCompositeState(false);
        return RenderType.create("pokecube_legends:wormhole", DefaultVertexFormat.NEW_ENTITY, Mode.TRIANGLES, 256,
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
    public boolean hasAnimation(final String phase, final WormholeEntity entity, IModel model)
    {
        return model.getAnimationChanger().getAnimations().containsKey(phase);
    }

    @Override
    public String getAnimation(final WormholeEntity entityIn, IModel model)
    {
        return entityIn.isIdle() ? "stable"
                : entityIn.isClosing() ? "closing" : entityIn.isOpening() ? "opening" : "idle";
    }

    @Override
    public void scaleEntity(final PoseStack mat, final WormholeEntity entity, final IModel model, final float partialTick)
    {
        this.rotPoint.set(0);
        model.setOffset(this.rotPoint);
    }

    @Override
    public IAnimationChanger getAnimationChanger()
    {
        return this.getModel().getAnimationChanger();
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return HeadInfo.DUMMY;
    }

    @Override
    public void setTexturer(final IPartTexturer texturer)
    {
        this.getModel().texChangeHolder.set(texturer);
    }

    @Override
    public IPartTexturer getTexturer()
    {
        return this.getModel().texChangeHolder.get();
    }

    @Override
    public void setAnimationHolder(final IAnimationHolder holder)
    {
        this.getModel().setAnimationHolder(holder);
    }

    @Override
    public IAnimationHolder getAnimationHolder()
    {
        return this.getModel().animHolderHolder.get();
    }

}
