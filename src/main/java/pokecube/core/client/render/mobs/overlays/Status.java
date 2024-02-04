package pokecube.core.client.render.mobs.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.utils.Resources;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.lib.AxisAngles;

public class Status
{
    public static class StatusTexturer implements IPartTexturer
    {
        public ResourceLocation tex;

        public IPartTexturer wrapped = null;

        public float time = 0;
        public float rate = 1;
        public int alpha = 128;

        public boolean animated = true;

        public StatusTexturer(final ResourceLocation tex)
        {
            this.tex = tex;
        }

        @Override
        public ResourceLocation getTexture(final String part, final ResourceLocation default_)
        {
            if (wrapped != null)
            {
                ResourceLocation wrap = default_;
                if (wrapped.hasMapping(part)) wrap = wrapped.getTexture(part, default_);
                if (wrap != null)
                {
                    wrap = new ResourceLocation(wrap.getNamespace(), wrap.getPath() + "--sep--" + tex.getNamespace()
                            + "--sep--" + tex.getPath() + "--sep--" + alpha);
                }
                else
                {
                    wrap = default_;
                    wrap = new ResourceLocation(wrap.getNamespace(), wrap.getPath() + "--sep--" + tex.getNamespace()
                            + "--sep--" + tex.getPath() + "--sep--" + alpha);
                }
                return wrap;
            }
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
            this.time += rate * Minecraft.getInstance().getFrameTime() / 1000;
            if (wrapped != null) wrapped.bindObject(thing);
        }

        @Override
        public boolean shiftUVs(final String part, final double[] toFill)
        {
            if (!animated) return false;
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
            if (wrapped == null) rgbaIn[3] = this.alpha;
        }

    }

    public static final Set<String> EXCLUDED_PARTS = Sets.newHashSet();

    public static record StatusOverlay(StatusTexturer texturer, float scale)
    {
    };

    public static final StatusOverlay FRZTEX = new StatusOverlay(new StatusTexturer(Resources.STATUS_FRZ), 0.05f);
    public static final StatusOverlay PARTEX = new StatusOverlay(new StatusTexturer(Resources.STATUS_PAR), 0.05f);

    public static final List<Function<IPokemob, StatusOverlay>> PROVIDERS = new ArrayList<>();

    static
    {
        PROVIDERS.add(pokemob -> {
            int status = pokemob.getStatus();
            status = (status & IMoveConstants.STATUS_PAR) > 0 ? IMoveConstants.STATUS_PAR
                    : (status & IMoveConstants.STATUS_FRZ) > 0 ? IMoveConstants.STATUS_FRZ : 0;
            if (status == 0) return null;
            return status == IMoveConstants.STATUS_FRZ ? FRZTEX : PARTEX;
        });
    }

    public static void render(final LivingEntityRenderer<Mob, EntityModel<Mob>> renderer, final PoseStack mat,
            final MultiBufferSource buf, final IPokemob pokemob, final float partialTicks, final int light)
    {
        if (!(renderer.getModel() instanceof ModelWrapper<?> wrap)) return;
        final Mob mob = pokemob.getEntity();
        for (var func : PROVIDERS)
        {
            var effects = func.apply(pokemob);
            if (effects == null) continue;
            mat.pushPose();

            final float f = Mth.rotLerp(partialTicks, mob.yBodyRotO, mob.yBodyRot);
            final float f1 = Mth.rotLerp(partialTicks, mob.yHeadRotO, mob.yHeadRot);
            final float f2 = f1 - f;

            final float f6 = Mth.lerp(partialTicks, mob.xRotO, mob.xRot);

            final float f7 = mob.tickCount + partialTicks;
            float f8 = 0.0F;
            float f5 = 0.0F;
            {
                f8 = Mth.lerp(partialTicks, mob.animationSpeedOld, mob.animationSpeed);
                f5 = mob.animationPosition - mob.animationSpeed * (1.0F - partialTicks);

                if (f8 > 1.0F) f8 = 1.0F;
            }
            mat.mulPose(AxisAngles.YP.rotationDegrees(180.0F - f));

            float ds = effects.scale();

            final float s = (1 + ds) / 1.73205081f;

            Vector3 scale = new Vector3(s, s, s);
            float s2 = pokemob.getSize();
            mat.scale(-s2, -s2, s2);
            mat.translate(0.0D, -1.501F, 0.0d);
            final StatusTexturer statusTexturer = effects.texturer();

            final ResourceLocation default_ = effects.texturer().tex;
            final IPartTexturer texer = wrap.renderer.getTexturer();
            wrap.renderer.setTexturer(statusTexturer);
            statusTexturer.bindObject(mob);
            wrap.getParts().forEach((n, p) -> {
                p.applyTexture(buf, default_, statusTexturer);
                if (EXCLUDED_PARTS.contains(p.getName())) p.setDisabled(true);
            });
            renderer.getModel().prepareMobModel(mob, f5, f8, partialTicks);
            renderer.getModel().setupAnim(mob, f5, f8, f7, f2, f6);
            for (var p : wrap.getParts().values())
            {
                p.setPostScale(scale);
            }
            renderer.getModel().renderToBuffer(mat, buf.getBuffer(wrap.renderType(default_)), light,
                    OverlayTexture.NO_OVERLAY, 1, 1, 1, 0.5f);
            if (texer != null)
            {
                final ResourceLocation orig_ = renderer.getTextureLocation(mob);
                texer.bindObject(mob);
                wrap.getParts().forEach((n, p) -> {
                    p.applyTexture(buf, orig_, texer);
                    if (EXCLUDED_PARTS.contains(p.getName())) p.setDisabled(false);
                });
            }

            wrap.renderer.setTexturer(texer);
            mat.popPose();
        }
    }

}
