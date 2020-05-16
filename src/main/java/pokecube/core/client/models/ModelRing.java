package pokecube.core.client.models;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;

public class ModelRing extends EntityModel<Entity>
{
    public static RenderType getType(final ResourceLocation loc, final boolean alpha)
    {
        return alpha ? RenderType.makeType("thutbling:bling_a", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true,
                false, RenderType.State.getBuilder().texture(new RenderState.TextureState(loc, true, false))
                        .diffuseLighting(new RenderState.DiffuseLightingState(true)).alpha(new RenderState.AlphaState(
                                0.003921569F)).cull(new RenderState.CullState(false)).lightmap(
                                        new RenderState.LightmapState(true)).overlay(new RenderState.OverlayState(true))
                        .build(false))
                : RenderType.makeType("thutbling:bling_b", DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, true, false,
                        RenderType.State.getBuilder().texture(new RenderState.TextureState(loc, true, false))
                                .diffuseLighting(new RenderState.DiffuseLightingState(true)).cull(
                                        new RenderState.CullState(false)).lightmap(new RenderState.LightmapState(true))
                                .overlay(new RenderState.OverlayState(true)).build(false));
    }

    public static IVertexBuilder makeBuilder(final IRenderTypeBuffer buff, final ResourceLocation loc)
    {
        return buff.getBuffer(ModelRing.getType(loc, true));
    }

    public static final ResourceLocation texture_1 = new ResourceLocation(PokecubeCore.MODID,
            "textures/worn/megaring_1.png");
    public static final ResourceLocation texture_2 = new ResourceLocation(PokecubeCore.MODID,
            "textures/worn/megaring_2.png");
    // fields
    ModelRenderer Shape2;
    ModelRenderer Shape1;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;

    public ItemStack stack;

    public int pass = 1;

    public ModelRing()
    {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.Shape2 = new ModelRenderer(this, 0, 11);
        this.Shape2.addBox(0F, 0F, 0F, 1, 1, 1);
        this.Shape2.setRotationPoint(8.4F, 9F, -0.4333333F);
        this.Shape2.setTextureSize(64, 32);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, 0F, 0F, 0F);
        this.Shape1 = new ModelRenderer(this, 0, 0);
        this.Shape1.addBox(0F, 0F, 0F, 1, 1, 4);
        this.Shape1.setRotationPoint(8F, 9F, -2F);
        this.Shape1.setTextureSize(64, 32);
        this.Shape1.mirror = true;
        this.setRotation(this.Shape1, 0F, 0F, 0F);
        this.Shape3 = new ModelRenderer(this, 11, 0);
        this.Shape3.addBox(0F, 0F, 0F, 6, 1, 1);
        this.Shape3.setRotationPoint(3F, 9F, -3F);
        this.Shape3.setTextureSize(64, 32);
        this.Shape3.mirror = true;
        this.setRotation(this.Shape3, 0F, 0F, 0F);
        this.Shape4 = new ModelRenderer(this, 31, 0);
        this.Shape4.addBox(0F, 0F, 0F, 1, 1, 5);
        this.Shape4.setRotationPoint(3F, 9F, -2F);
        this.Shape4.setTextureSize(64, 32);
        this.Shape4.mirror = true;
        this.setRotation(this.Shape4, 0F, 0F, 0F);
        this.Shape5 = new ModelRenderer(this, 17, 5);
        this.Shape5.addBox(0F, 0F, 0F, 5, 1, 1);
        this.Shape5.setRotationPoint(4F, 9F, 2F);
        this.Shape5.setTextureSize(64, 32);
        this.Shape5.mirror = true;
        this.setRotation(this.Shape5, 0F, 0F, 0F);
    }

    @Override
    public void setRotationAngles(final Entity entityIn, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float netHeadYaw, final float headPitch)
    {
    }

    @Override
    public void render(final MatrixStack matrixStackIn, final IVertexBuilder bufferIn, final int packedLightIn,
            final int packedOverlayIn, final float red, final float green, final float blue, final float alpha)
    {
        if (this.pass == 1)
        {
            matrixStackIn.push();
            matrixStackIn.scale(1f, 0.99f, 1.0f);
            matrixStackIn.translate(0, 0.005, 0);
            this.Shape2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, 1, 1, 1, 1);
            matrixStackIn.pop();
        }
        else
        {
            matrixStackIn.push();
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
            matrixStackIn.pop();
        }
    }

    private void setRotation(final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}