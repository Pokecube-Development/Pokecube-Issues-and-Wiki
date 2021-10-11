package pokecube.core.client.models;

import java.awt.Color;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import pokecube.core.PokecubeCore;

public class ModelRing extends EntityModel<Entity>
{
    public static RenderType getType(final ResourceLocation loc, final boolean alpha)
    {
        // FIXME shaders
        return alpha ? RenderType.create("thutbling:bling_a", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true,
                false, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(loc,
                        true, false)).setCullState(new RenderStateShard.CullStateShard(false)).setLightmapState(
                                new RenderStateShard.LightmapStateShard(true)).setOverlayState(
                                        new RenderStateShard.OverlayStateShard(true)).createCompositeState(false))
                : RenderType.create("thutbling:bling_b", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false,
                        RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(loc,
                                true, false)).setCullState(new RenderStateShard.CullStateShard(false)).setLightmapState(
                                        new RenderStateShard.LightmapStateShard(true)).setOverlayState(
                                                new RenderStateShard.OverlayStateShard(true)).createCompositeState(
                                                        false));
    }

    public static VertexConsumer makeBuilder(final MultiBufferSource buff, final ResourceLocation loc)
    {
        return buff.getBuffer(ModelRing.getType(loc, true));
    }

    public static final ResourceLocation texture_1 = new ResourceLocation(PokecubeCore.MODID,
            "textures/worn/megaring_1.png");
    public static final ResourceLocation texture_2 = new ResourceLocation(PokecubeCore.MODID,
            "textures/worn/megaring_2.png");
    // fields
    ModelPart root;
    ModelPart Shape2;
    ModelPart Shape1;
    ModelPart Shape3;
    ModelPart Shape4;
    ModelPart Shape5;

    public ItemStack stack;

    public int pass = 1;

    public static LayerDefinition createBodyLayer()
    {
        final MeshDefinition meshdefinition = new MeshDefinition();
        final PartDefinition partdefinition = meshdefinition.getRoot();
        // @formatter:off
        partdefinition.addOrReplaceChild("Shape1", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 4).mirror(), PartPose.offset(8F, 9F, -2F));
        partdefinition.addOrReplaceChild("Shape2", CubeListBuilder.create().texOffs(0, 11).addBox(0F, 0F, 0F, 1, 1, 1).mirror(), PartPose.offset(8.4F, 9F, -0.433f));
        partdefinition.addOrReplaceChild("Shape3", CubeListBuilder.create().texOffs(11, 0).addBox(0F, 0F, 0F, 6, 1, 1).mirror(), PartPose.offset(3F, 9F, -3F));
        partdefinition.addOrReplaceChild("Shape4", CubeListBuilder.create().texOffs(31, 0).addBox(0F, 0F, 0F, 1, 1, 5).mirror(), PartPose.offset(3F, 9F, -2F));
        partdefinition.addOrReplaceChild("Shape5", CubeListBuilder.create().texOffs(17, 5).addBox(0F, 0F, 0F, 5, 1, 1).mirror(), PartPose.offset(4F, 9F, 2F));
        // @formatter:on
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public ModelRing()
    {
        final LayerDefinition def = ModelRing.createBodyLayer();
        this.root = def.bakeRoot();

        this.Shape1 = this.root.getChild("Shape1");
        this.Shape2 = this.root.getChild("Shape2");
        this.Shape3 = this.root.getChild("Shape3");
        this.Shape4 = this.root.getChild("Shape4");
        this.Shape5 = this.root.getChild("Shape5");

        // this.Shape1 = new ModelPart(this, 0, 0);
        // this.Shape1.addBox(0F, 0F, 0F, 1, 1, 4);
        // this.Shape1.setPos(8F, 9F, -2F);
        // this.Shape1.setTexSize(64, 32);
        // this.Shape1.mirror = true;
        // this.setRotation(this.Shape1, 0F, 0F, 0F);

        // this.Shape2 = new ModelPart(this, 0, 11);
        // this.Shape2.addBox(0F, 0F, 0F, 1, 1, 1);
        // this.Shape2.setPos(8.4F, 9F, -0.4333333F);
        // this.Shape2.setTexSize(64, 32);
        // this.Shape2.mirror = true;
        // this.setRotation(this.Shape2, 0F, 0F, 0F);

        // this.Shape3 = new ModelPart(this, 11, 0);
        // this.Shape3.addBox(0F, 0F, 0F, 6, 1, 1);
        // this.Shape3.setPos(3F, 9F, -3F);
        // this.Shape3.setTexSize(64, 32);
        // this.Shape3.mirror = true;
        // this.setRotation(this.Shape3, 0F, 0F, 0F);

        // this.Shape4 = new ModelPart(this, 31, 0);
        // this.Shape4.addBox(0F, 0F, 0F, 1, 1, 5);
        // this.Shape4.setPos(3F, 9F, -2F);
        // this.Shape4.setTexSize(64, 32);
        // this.Shape4.mirror = true;
        // this.setRotation(this.Shape4, 0F, 0F, 0F);

        // this.Shape5 = new ModelPart(this, 17, 5);
        // this.Shape5.addBox(0F, 0F, 0F, 5, 1, 1);
        // this.Shape5.setPos(4F, 9F, 2F);
        // this.Shape5.setTexSize(64, 32);
        // this.Shape5.mirror = true;
        // this.setRotation(this.Shape5, 0F, 0F, 0F);
    }

    @Override
    public void setupAnim(final Entity entityIn, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float netHeadYaw, final float headPitch)
    {
    }

    @Override
    public void renderToBuffer(final PoseStack matrixStackIn, final VertexConsumer bufferIn, final int packedLightIn,
            final int packedOverlayIn, final float red, final float green, final float blue, final float alpha)
    {
        if (this.pass == 1)
        {
            matrixStackIn.pushPose();
            matrixStackIn.scale(1f, 0.99f, 1.0f);
            matrixStackIn.translate(0, 0.005, 0);
            this.Shape2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, 1, 1, 1, 1);
            matrixStackIn.popPose();
        }
        else
        {
            matrixStackIn.pushPose();
            DyeColor ret = DyeColor.GRAY;
            if (this.stack.hasTag() && this.stack.getTag().contains("dyeColour"))
            {
                final int damage = this.stack.getTag().getInt("dyeColour");
                ret = DyeColor.byId(damage);
            }
            final Color colour = new Color(ret.getTextColor() + 0xFF000000);
            this.Shape1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, colour.getRed() / 255f, colour
                    .getGreen() / 255f, colour.getBlue() / 255f, 1);
            this.Shape3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, colour.getRed() / 255f, colour
                    .getGreen() / 255f, colour.getBlue() / 255f, 1);
            this.Shape4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, colour.getRed() / 255f, colour
                    .getGreen() / 255f, colour.getBlue() / 255f, 1);
            this.Shape5.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, colour.getRed() / 255f, colour
                    .getGreen() / 255f, colour.getBlue() / 255f, 1);
            matrixStackIn.popPose();
        }
    }

}