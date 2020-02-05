package pokecube.mobs.client.render;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.client.render.wrappers.ModelWrapper;

public class ModelWrapperSpinda<T extends Entity> extends ModelWrapper<T>
{
    private static final ResourceLocation normalh  = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaspotsh.png");
    private static final ResourceLocation normalhb = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaheadbase.png");
    private static final ResourceLocation shinyh   = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaspotshs.png");
    private static final ResourceLocation shinyhb  = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaheadbases.png");
    private static final ResourceLocation normale  = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaspotse.png");
    private static final ResourceLocation normaleb = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaearsbase.png");
    private static final ResourceLocation shinye   = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaspotses.png");
    private static final ResourceLocation shinyeb  = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaearsbases.png");

    public ModelWrapperSpinda(final ModelHolder model, final IModelRenderer<?> renderer)
    {
        // TODO call this
        super(model, renderer);
    }

    /** Sets the models various rotation angles then renders the model. */
    @Override
    public void render(final Entity entityIn, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale)
    {
        mat.push();
        GlStateManager.disableCull();
        final IPokemob spinda = CapabilityPokemob.getPokemobFor(entityIn);
        for (final String partName : this.imodel.getParts().keySet())
        {
            final IExtendedModelPart part = this.imodel.getParts().get(partName);
            if (part == null) continue;
            try
            {
                if (part.getParent() == null)
                {
                    final Random rand = new Random(spinda.getRNGValue());
                    ((IRetexturableModel) part).setTexturer(null);

                    // Render the base layer of the head and ears
                    mat.push();
                    Minecraft.getInstance().getTextureManager().bindTexture(spinda.isShiny()
                            ? ModelWrapperSpinda.shinyhb : ModelWrapperSpinda.normalhb);
                    part.renderOnly("Head");
                    mat.pop();
                    mat.push();
                    Minecraft.getInstance().getTextureManager().bindTexture(spinda.isShiny()
                            ? ModelWrapperSpinda.shinyeb : ModelWrapperSpinda.normaleb);
                    part.renderOnly("Left_ear");
                    mat.pop();
                    mat.push();
                    Minecraft.getInstance().getTextureManager().bindTexture(spinda.isShiny()
                            ? ModelWrapperSpinda.shinyeb : ModelWrapperSpinda.normaleb);
                    part.renderOnly("Right_ear");
                    mat.pop();

                    // Render the 4 spots
                    for (int i = 0; i < 4; i++)
                    {
                        float dx = rand.nextFloat();
                        float dy = rand.nextFloat() / 2 + 0.5f;
                        mat.push();
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        mat.translate(dx, dy, 0.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        Minecraft.getInstance().getTextureManager().bindTexture(spinda.isShiny()
                                ? ModelWrapperSpinda.shinyh : ModelWrapperSpinda.normalh);
                        part.renderOnly("Head");
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        mat.pop();
                        mat.push();
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        dx = rand.nextFloat();
                        dy = rand.nextFloat() / 2 + 0.5f;
                        mat.translate(dx, dy, 0.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        Minecraft.getInstance().getTextureManager().bindTexture(spinda.isShiny()
                                ? ModelWrapperSpinda.shinye : ModelWrapperSpinda.normale);
                        part.renderOnly("Left_ear");
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        mat.pop();
                        mat.push();
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        dx = rand.nextFloat();
                        dy = rand.nextFloat() / 2 + 0.5f;
                        mat.translate(dx, dy, 0.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        part.renderOnly("Right_ear");
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        mat.pop();
                    }
                    // Render the model normally.
                    if (this.renderer.getTexturer() != null && part instanceof IRetexturableModel)
                    {
                        this.renderer.getTexturer().bindObject(entityIn);
                        ((IRetexturableModel) part).setTexturer(this.renderer.getTexturer());
                    }
                    mat.push();
                    part.renderAll();
                    mat.pop();
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.enableCull();
        mat.pop();
    }
}
