package pokecube.core.client.render.mobs.overlays;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.IMobColourable;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.texturing.IPartTexturer;

public class Status
{
    public static final ResourceLocation FRZ = Resources.STATUS_FRZ;
    public static final ResourceLocation PAR = Resources.STATUS_PAR;

    public static void render(final IModelRenderer<MobEntity> renderer, final Vec3d pos, final MobEntity mobEntity,
            final double d, final double d1, final double d2, final float f, final float partialTick)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mobEntity);
        if (pokemob == null) return;
        final byte status = pokemob.getStatus();
        if (status == IMoveConstants.STATUS_NON) return;
        if (!(status == IMoveConstants.STATUS_FRZ || status == IMoveConstants.STATUS_PAR)) return;

        final IPartTexturer oldTexturer = renderer.getTexturer();
        final IPartTexturer statusTexturer = new IPartTexturer()
        {

            @Override
            public void addCustomMapping(final String part, final String state, final String tex)
            {
            }

            @Override
            public void addMapping(final String part, final String tex)
            {
            }

            @Override
            public void applyTexture(final String part)
            {
                ResourceLocation texture = null;
                if (status == IMoveConstants.STATUS_FRZ) texture = Status.FRZ;
                else if (status == IMoveConstants.STATUS_PAR) texture = Status.PAR;
                texture = Status.PAR;
                Minecraft.getInstance().textureManager.bindTexture(texture);
            }

            @Override
            public void bindObject(final Object thing)
            {
            }

            @Override
            public boolean hasMapping(final String part)
            {
                return true;
            }

            @Override
            public boolean isFlat(final String part)
            {
                return true;
            }

            @Override
            public boolean shiftUVs(final String part, final double[] toFill)
            {
                return false;
            }
        };

        final float time = mobEntity.ticksExisted + partialTick;
        mat.push();
        final float speed = status == IMoveConstants.STATUS_FRZ ? 0.001f : 0.005f;
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        final float var5 = time * speed;
        final float var6 = time * speed;
        mat.translate(var5, var6, 0.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        float var7 = status == IMoveConstants.STATUS_FRZ ? 0.5f : 1F;
        GL11.glColor4f(var7, var7, var7, 0.5F);
        var7 = 1.5f;
        mat.scale(var7, var7, var7);
        final IMobColourable colour = pokemob;
        final int[] col = colour.getRGBA();
        final int[] bak = col.clone();
        col[3] = 255;
        colour.setRGBA(col);
        renderer.setTexturer(statusTexturer);
        renderer.doRender(mobEntity, d, d1, d2, f, partialTick);
        renderer.setTexturer(oldTexturer);
        colour.setRGBA(bak);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glColor4f(1, 1, 1, 1);
        mat.pop();
    }
}
