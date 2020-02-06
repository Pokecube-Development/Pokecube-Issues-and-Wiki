package pokecube.core.client.render.mobs.overlays;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
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

    public static class StatusTexturer implements IPartTexturer
    {
        private final ResourceLocation tex;
        public IPartTexturer           wrapped;
        public float                   time = 0;

        public StatusTexturer(final ResourceLocation tex)
        {
            this.tex = tex;
        }

        @Override
        public ResourceLocation getTexture(final String part, final ResourceLocation default_)
        {
            return this.tex;
        }

        @Override
        public boolean hasMapping(final String part)
        {
            return true;
        }

        @Override
        public boolean isFlat(final String part)
        {
            return this.wrapped == null ? true : this.wrapped.isFlat(part);
        }

        @Override
        public boolean shiftUVs(final String part, final double[] toFill)
        {
            toFill[0] += this.time;
            toFill[1] += this.time;
            return true;
        }

    }

    public static final StatusTexturer FRZTEX = new StatusTexturer(Status.FRZ);
    public static final StatusTexturer PARTEX = new StatusTexturer(Status.PAR);

    public static void render(final IModelRenderer<MobEntity> renderer, final MatrixStack mat,
            final IRenderTypeBuffer buf, final MobEntity mobEntity, final double d, final double d1, final double d2,
            final float f,
            final float partialTick)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mobEntity);
        if (pokemob == null) return;
        final byte status = pokemob.getStatus();
        if (status == IMoveConstants.STATUS_NON) return;
        if (!(status == IMoveConstants.STATUS_FRZ || status == IMoveConstants.STATUS_PAR)) return;
        final IPartTexturer oldTexturer = renderer.getTexturer();
        final StatusTexturer statusTexturer = status == IMoveConstants.STATUS_FRZ ? Status.FRZTEX : Status.PARTEX;
        statusTexturer.wrapped = oldTexturer;
        final float time = mobEntity.ticksExisted + partialTick;
        statusTexturer.time = time * (status == IMoveConstants.STATUS_FRZ ? 0.001f : 0.005f);
        mat.push();
        final IMobColourable colour = pokemob;
        final int[] col = colour.getRGBA();
        final int[] bak = col.clone();
        col[3] = 128;
        colour.setRGBA(col);
        renderer.setTexturer(statusTexturer);
        renderer.doRender(mobEntity, d, d1, d2, f, partialTick);
        renderer.setTexturer(oldTexturer);
        colour.setRGBA(bak);
        mat.pop();
    }
}
