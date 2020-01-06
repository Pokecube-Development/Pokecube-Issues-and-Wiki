package pokecube.core.client.models;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;

public class ModelRing extends EntityModel<Entity>
{
    ResourceLocation texture_1 = new ResourceLocation(PokecubeCore.MODID, "textures/worn/megaring_1.png");
    ResourceLocation texture_2 = new ResourceLocation(PokecubeCore.MODID, "textures/worn/megaring_2.png");
    // fields
    RendererModel Shape2;
    RendererModel Shape1;
    RendererModel Shape3;
    RendererModel Shape4;
    RendererModel Shape5;

    public ItemStack stack;

    public ModelRing()
    {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.Shape2 = new RendererModel(this, 0, 11);
        this.Shape2.addBox(0F, 0F, 0F, 1, 1, 1);
        this.Shape2.setRotationPoint(8.4F, 9F, -0.4333333F);
        this.Shape2.setTextureSize(64, 32);
        this.Shape2.mirror = true;
        this.setRotation(this.Shape2, 0F, 0F, 0F);
        this.Shape1 = new RendererModel(this, 0, 0);
        this.Shape1.addBox(0F, 0F, 0F, 1, 1, 4);
        this.Shape1.setRotationPoint(8F, 9F, -2F);
        this.Shape1.setTextureSize(64, 32);
        this.Shape1.mirror = true;
        this.setRotation(this.Shape1, 0F, 0F, 0F);
        this.Shape3 = new RendererModel(this, 11, 0);
        this.Shape3.addBox(0F, 0F, 0F, 6, 1, 1);
        this.Shape3.setRotationPoint(3F, 9F, -3F);
        this.Shape3.setTextureSize(64, 32);
        this.Shape3.mirror = true;
        this.setRotation(this.Shape3, 0F, 0F, 0F);
        this.Shape4 = new RendererModel(this, 31, 0);
        this.Shape4.addBox(0F, 0F, 0F, 1, 1, 5);
        this.Shape4.setRotationPoint(3F, 9F, -2F);
        this.Shape4.setTextureSize(64, 32);
        this.Shape4.mirror = true;
        this.setRotation(this.Shape4, 0F, 0F, 0F);
        this.Shape5 = new RendererModel(this, 17, 5);
        this.Shape5.addBox(0F, 0F, 0F, 5, 1, 1);
        this.Shape5.setRotationPoint(4F, 9F, 2F);
        this.Shape5.setTextureSize(64, 32);
        this.Shape5.mirror = true;
        this.setRotation(this.Shape5, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        if (this.stack == null) return;
        Minecraft.getInstance().getTextureManager().bindTexture(this.texture_1);
        GL11.glPushMatrix();
        GL11.glRotated(90, 0, 1, 0);
        GL11.glTranslated(-0.08, -0.3, 0);
        this.setRotationAngles(entity, f, f1, f2, f3, f4, f5);
        GL11.glScaled(0.5, 0.5, 0.5);
        this.Shape2.render(f5);
        GL11.glScaled(1.01, 1.01, 1.01);
        Minecraft.getInstance().getTextureManager().bindTexture(this.texture_2);
        DyeColor ret = DyeColor.GRAY;
        if (this.stack.hasTag() && this.stack.getTag().contains("dyeColour"))
        {
            final int damage = this.stack.getTag().getInt("dyeColour");
            ret = DyeColor.byId(damage);
        }
        final Color colour = new Color(ret.func_218388_g() + 0xFF000000);
        GL11.glColor3f(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f);
        this.Shape1.render(f5);
        this.Shape3.render(f5);
        this.Shape4.render(f5);
        this.Shape5.render(f5);
        GL11.glColor3f(1f, 1f, 1f);

        GL11.glPopMatrix();
    }

    private void setRotation(RendererModel model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}