package pokecube.core.client.models;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import pokecube.core.database.PokedexEntry;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.PokeType;

/** egg - Undefined Created using Tabula 4.1.1 */
public class ModelPokemobEgg extends EntityModel<EntityPokemobEgg>
{
    public RendererModel Egg1;
    public RendererModel Egg2;
    public RendererModel Egg3;
    public RendererModel Egg4;
    public RendererModel Egg5;
    public RendererModel Egg6;
    public RendererModel Egg7;
    public RendererModel Egg8;
    public RendererModel Egg9;
    public RendererModel Egg10;
    public RendererModel Egg11;

    public ModelPokemobEgg()
    {
        this.textureWidth = 128;
        this.textureHeight = 128;
        this.Egg11 = new RendererModel(this, 0, 96);
        this.Egg11.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.Egg11.addBox(-2.5F, -20.0F, -2.5F, 5, 1, 5, 0.0F);
        this.Egg8 = new RendererModel(this, 56, 40);
        this.Egg8.setRotationPoint(0.0F, 22.0F, 0.0F);
        this.Egg8.addBox(-7.5F, -12.5F, -5.5F, 15, 11, 11, 0.0F);
        this.Egg5 = new RendererModel(this, 0, 70);
        this.Egg5.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.Egg5.addBox(-4.5F, -19.0F, -4.5F, 9, 1, 9, 0.0F);
        this.Egg4 = new RendererModel(this, 0, 57);
        this.Egg4.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.Egg4.addBox(-5.5F, -18.0F, -5.5F, 11, 1, 11, 0.0F);
        this.Egg7 = new RendererModel(this, 56, 13);
        this.Egg7.setRotationPoint(0.0F, 22.0F, 0.0F);
        this.Egg7.addBox(-5.5F, -12.5F, -7.5F, 11, 11, 15, 0.0F);
        this.Egg6 = new RendererModel(this, 0, 81);
        this.Egg6.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.Egg6.addBox(-6.5F, 0.0F, -6.5F, 13, 1, 13, 0.0F);
        this.Egg2 = new RendererModel(this, 0, 42);
        this.Egg2.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.Egg2.addBox(-6.5F, -17.0F, -6.5F, 13, 2, 13, 0.0F);
        this.Egg10 = new RendererModel(this, 56, 91);
        this.Egg10.setRotationPoint(0.0F, 22.0F, 0.0F);
        this.Egg10.addBox(-8.0F, -11.0F, -4.5F, 16, 9, 9, 0.0F);
        this.Egg3 = new RendererModel(this, 0, 13);
        this.Egg3.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.Egg3.addBox(-7.0F, -15.0F, -7.0F, 14, 15, 14, 0.0F);
        this.Egg1 = new RendererModel(this, 0, 0);
        this.Egg1.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.Egg1.addBox(-5.0F, 0.0F, -5.0F, 10, 3, 10, 0.0F);
        this.Egg9 = new RendererModel(this, 56, 64);
        this.Egg9.setRotationPoint(0.0F, 22.0F, 0.0F);
        this.Egg9.addBox(-4.5F, -11.0F, -8.0F, 9, 9, 16, 0.0F);
    }

    @Override
    public void render(final EntityPokemobEgg entity, final float f, final float f1, final float f2, final float f3,
            final float f4, final float f5)
    {

        mat.push();
        mat.scale(0.25, 0.25, 0.25);
        mat.translate(0, 4.45, 0);
        final PokedexEntry entry = ItemPokemobEgg.getEntry(entity.getHeldItemMainhand());

        Color colour = new Color(PokeType.unknown.colour);

        if (entry != null) if (entry.getType2() != PokeType.unknown) colour = new Color(entry.getType2().colour);
        else colour = new Color(entry.getType1().colour);

        float red = colour.getRed() / 255f;
        float green = colour.getGreen() / 255f;
        float blue = colour.getBlue() / 255f;

        GL11.glColor3f(red, green, blue);

        this.Egg10.render(f5);
        this.Egg9.render(f5);
        this.Egg7.render(f5);
        this.Egg8.render(f5);

        this.Egg11.render(f5);
        this.Egg3.render(f5);
        this.Egg1.render(f5);
        this.Egg5.render(f5);
        this.Egg4.render(f5);

        if (entry != null) colour = new Color(entry.getType1().colour);
        red = colour.getRed() / 255f;
        green = colour.getGreen() / 255f;
        blue = colour.getBlue() / 255f;

        GL11.glColor3f(red, green, blue);

        this.Egg6.render(f5);
        this.Egg2.render(f5);
        this.Egg3.render(f5);
        this.Egg1.render(f5);

        GL11.glColor3f(1, 1, 1);
        mat.pop();
    }

    /**
     * This is a helper function from Tabula to set the rotation of model
     * parts
     */
    public void setRotateAngle(final RendererModel modelRenderer, final float x, final float y, final float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
