package thut.crafts.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Node;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
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
import thut.crafts.Reference;
import thut.crafts.entity.EntityTest;

public class RenderEntity extends MobRenderer<EntityTest, EntityModel<EntityTest>>
{
    private static ResourceLocation TEXTURE   = new ResourceLocation(Reference.MODID, "entity/missingno.png");
    private static ResourceLocation MODEL     = new ResourceLocation(Reference.MODID, "entity/missingno.x3d");
    private static ResourceLocation ANIMATION = new ResourceLocation(Reference.MODID, "entity/missingno.xml");

    public static class Holder extends ModelHolder implements IModelRenderer<EntityTest>
    {
        ModelWrapper<EntityTest>                wrapper;
        final Vector3                           rotPoint     = Vector3.getNewVector();
        HashMap<String, List<Animation>>        anims        = Maps.newHashMap();
        private IPartTexturer                   texturer;
        private IAnimationChanger               animator;
        public String                           name;
        public HashMap<String, PartInfo>        parts        = Maps.newHashMap();
        HashMap<String, ArrayList<Vector5>>     global;
        public HashMap<String, List<Animation>> animations   = Maps.newHashMap();
        public Vector3                          offset       = Vector3.getNewVector();;
        public Vector3                          scale        = Vector3.getNewVector();
        ResourceLocation                        texture;

        public boolean                          overrideAnim = false;
        public String                           anim         = "";

        public Vector5                          rotations    = new Vector5();

        boolean                                 blend;

        boolean                                 light;

        int                                     src;

        ///////////////////// IModelRenderer stuff below here//////////////////

        int                                     dst;

        public Holder()
        {
            super(RenderEntity.MODEL, RenderEntity.TEXTURE, RenderEntity.ANIMATION, "missingno");
        }

        @Override
        public void doRender(final EntityTest entity, final double d, final double d1, final double d2, final float f,
                final float partialTick)
        {
        }

        @Override
        public String getAnimation(final Entity entityIn)
        {
            return "idle";
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

        public void initModel(final ModelWrapper<EntityTest> model)
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
        public void scaleEntity(final MatrixStack mat, final Entity entity, final IModel model, final float partialTick)
        {
            //
            // float s = 1;
            // // TODO see if this is where things are going funny with tbl
            // // offsets?
            // this.rotPoint.set(this.getRotationOffset()).scalarMultBy(s);
            // model.setOffset(this.rotPoint);
            // if (!this.getScale().isEmpty()) GlStateManager.scalef(s, s, s);
            // else GlStateManager.scalef(s, s, s);
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

    public static class TestModel extends EntityModel<EntityTest>
    {
        private final ModelRenderer box;

        public TestModel()
        {
            this.box = new ModelRenderer(this, 0, 0);
            this.box.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);
        }

        @Override
        public void render(final EntityTest entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks,
                final float netHeadYaw, final float headPitch)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void render(final MatrixStack matrixStackIn, final IVertexBuilder bufferIn, final int packedLightIn, final int packedOverlayIn,
                final float red, final float green, final float blue, final float alpha)
        {
            // System.out.println(matrixStackIn.getLast().getPositionMatrix());
            this.box.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }

    }

    final Holder             holder;
    EntityModel<EntityTest>  vanilla;
    ModelWrapper<EntityTest> custom;

    public RenderEntity(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, null, 1);
        this.holder = new Holder();
        this.holder.init();
        this.entityModel = this.custom = new ModelWrapper<>(this.holder, this.holder);
        this.vanilla = new TestModel();
    }

    @Override
    public void render(final EntityTest entityIn, final float entityYaw, final float partialTicks, final MatrixStack matrixStackIn,
            final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        final IPartTexturer texer = this.custom.renderer.getTexturer();
        final ResourceLocation default_ = this.getEntityTexture(entityIn);
        if (texer != null && this.custom.imodel != null)
        {
            texer.bindObject(entityIn);
            this.custom.imodel.getParts().forEach((n, p) ->
            {
                // Get the default texture for this part.
                final ResourceLocation tex_part = texer.getTexture(n, default_);
                p.applyTexture(bufferIn, tex_part, texer);
            });
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    protected RenderType func_230042_a_(final EntityTest p_230042_1_, final boolean p_230042_2_, final boolean p_230042_3_)
    {
        final RenderType.State rendertype$state = RenderType.State.builder()
                .texture(new RenderState.TextureState(RenderEntity.TEXTURE, true, false))
                .transparency(new RenderState.TransparencyState("translucent_transparency", () ->
                {
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                }, () ->
                {
                    RenderSystem.disableBlend();
                })).diffuseLighting(new RenderState.DiffuseLightingState(true))
                .alpha(new RenderState.AlphaState(0.003921569F)).cull(new RenderState.CullState(false))
                .lightmap(new RenderState.LightmapState(true)).overlay(new RenderState.OverlayState(true)).build(false);

        return RenderType.get("thutmob_tranas", DefaultVertexFormats.ITEM, GL11.GL_TRIANGLES, 256, true, false,
                rendertype$state);
    }

    @Override
    public ResourceLocation getEntityTexture(final EntityTest entity)
    {
        return RenderEntity.TEXTURE;
    }

}
