package pokecube.core.client.render.mobs.overlays;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;

public class Status
{
    public static final ResourceLocation FRZ = Resources.STATUS_FRZ;
    public static final ResourceLocation PAR = Resources.STATUS_PAR;

    public static class StatusTexturer implements IPartTexturer
    {
        private final ResourceLocation tex;
        public IPartTexturer           wrapped;
        public float                   time  = 0;
        public int                     alpha = 128;

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
        public void bindObject(final Object thing)
        {
            this.time += Minecraft.getInstance().getRenderPartialTicks() / 10000;
        }

        @Override
        public boolean shiftUVs(final String part, final double[] toFill)
        {
            toFill[0] += this.time;
            toFill[1] += this.time;
            return true;
        }

        @Override
        public void init(final CustomTex tex)
        {
            // Nope
        }

        @Override
        public void modifiyRGBA(final String part, final int[] rgbaIn)
        {
            rgbaIn[3] = this.alpha;
        }

    }

    public static final StatusTexturer FRZTEX = new StatusTexturer(Status.FRZ);
    public static final StatusTexturer PARTEX = new StatusTexturer(Status.PAR);

    public static void render(final LivingRenderer<MobEntity, EntityModel<MobEntity>> renderer, final MatrixStack mat,
            final IRenderTypeBuffer buf, final IPokemob pokemob, final float partialTicks, final int light)
    {
        byte status = pokemob.getStatus();
        if (status == IMoveConstants.STATUS_NON) return;

        final MobEntity mob = pokemob.getEntity();

        status = (status & IMoveConstants.STATUS_PAR) > 0 ? IMoveConstants.STATUS_PAR
                : (status & IMoveConstants.STATUS_FRZ) > 0 ? IMoveConstants.STATUS_FRZ : 0;
        if (status == 0) return;
        final boolean frz = status == IMoveConstants.STATUS_FRZ;

        final ModelWrapper<?> wrap = (ModelWrapper<?>) renderer.getEntityModel();

        mat.push();

        final float f = MathHelper.interpolateAngle(partialTicks, mob.prevRenderYawOffset, mob.renderYawOffset);
        final float f1 = MathHelper.interpolateAngle(partialTicks, mob.prevRotationYawHead, mob.rotationYawHead);
        final float f2 = f1 - f;

        final float f6 = MathHelper.lerp(partialTicks, mob.prevRotationPitch, mob.rotationPitch);

        final float f7 = mob.ticksExisted + partialTicks;
        float f8 = 0.0F;
        float f5 = 0.0F;
        {
            f8 = MathHelper.lerp(partialTicks, mob.prevLimbSwingAmount, mob.limbSwingAmount);
            f5 = mob.limbSwing - mob.limbSwingAmount * (1.0F - partialTicks);

            if (f8 > 1.0F) f8 = 1.0F;
        }
        mat.rotate(Vector3f.YP.rotationDegrees(180.0F - f));

        final float ds = frz ? 0.05f : 0.05f;

        final float s = 1 + ds;
        mat.scale(s, s, s);
        mat.scale(-1.0F, -1.0F, 1.0F);
        mat.translate(0.0D, -1.501F, 0.0D);
        final StatusTexturer statusTexturer = frz ? Status.FRZTEX : Status.PARTEX;

        final ResourceLocation default_ = frz ? Resources.STATUS_FRZ : Resources.STATUS_PAR;
        final IPartTexturer texer = wrap.renderer.getTexturer();
        wrap.renderer.setTexturer(statusTexturer);
        if (statusTexturer != null)
        {
            statusTexturer.bindObject(mob);
            wrap.getParts().forEach((n, p) ->
            {
                p.applyTexture(buf, default_, statusTexturer);
            });
        }
        renderer.getEntityModel().setLivingAnimations(mob, f5, f8, partialTicks);
        renderer.getEntityModel().render(mob, f5, f8, f7, f2, f6);
        renderer.getEntityModel().render(mat, buf.getBuffer(wrap.getRenderType(default_)), light,
                OverlayTexture.DEFAULT_LIGHT, 1, 1, 1, 0.5f);

        if (texer != null)
        {
            final ResourceLocation orig_ = renderer.getEntityTexture(mob);
            texer.bindObject(mob);
            wrap.getParts().forEach((n, p) ->
            {
                p.applyTexture(buf, orig_, texer);
            });
        }

        wrap.renderer.setTexturer(texer);
        mat.pop();
    }

}
