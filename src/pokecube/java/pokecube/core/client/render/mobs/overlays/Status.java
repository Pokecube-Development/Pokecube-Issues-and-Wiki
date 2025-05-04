package pokecube.core.client.render.mobs.overlays;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.utils.Resources;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class Status
{
    public static class StatusTexturer implements IPartTexturer
    {
        public ResourceLocation tex;

        public IPartTexturer wrapped = null;

        public float time = 0;
        public float rate = 1;
        public int alpha = 128;
        public int red = 255;
        public int green = 255;
        public int blue = 255;

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
                if (wrap == null)
                {
                    wrap = default_;
                }
                wrap = ResourceLocation.fromNamespaceAndPath(wrap.getNamespace(),
                        wrap.getPath() + "--sep--" + tex.getNamespace() + "--sep--" + tex.getPath() + "--sep--"
                                + alpha);
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
            this.time += rate * Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() / 1000;
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
            if (wrapped == null)
            {
                rgbaIn[0] = red;
                rgbaIn[1] = green;
                rgbaIn[2] = blue;
                rgbaIn[3] = this.alpha;
            }
        }

    }

    public static final Set<String> EXCLUDED_PARTS = Sets.newHashSet();

    public static record StatusOverlay(StatusTexturer texturer, float scale)
    {}

    public static final StatusOverlay FRZTEX = new StatusOverlay(new StatusTexturer(Resources.STATUS_FRZ), 0.05f);
    public static final StatusOverlay PARTEX = new StatusOverlay(new StatusTexturer(Resources.STATUS_PAR), 0.05f);

    public static final List<Function<IPokemob, StatusOverlay>> PROVIDERS = new ArrayList<>();

    static
    {
        PROVIDERS.add(pokemob -> {
            if (pokemob.getEntity().hasEffect(StatusEffects.PARALYSIS)) return PARTEX;
            if (pokemob.getEntity().hasEffect(StatusEffects.FREEZE)) return FRZTEX;
            return null;
        });
    }

    public static void render(RenderLivingEvent.Post<Mob, EntityModel<Mob>> event, final IPokemob pokemob)
    {
        var renderer = event.getRenderer();
        if (!(renderer.getModel() instanceof ModelWrapper<?> wrap)) return;
        final Mob mob = pokemob.getEntity();
        for (var func : PROVIDERS)
        {
            var effects = func.apply(pokemob);
            if (effects == null) continue;
            var mat = event.getPoseStack();
            mat.pushPose();

            float ds = effects.scale();
            float s = (1 + ds) / 1.73205081f;
            Vector3 scale = new Vector3(s, s, s);

            final StatusTexturer statusTexturer = effects.texturer();

            final ResourceLocation default_ = effects.texturer().tex;
            final IPartTexturer texer = wrap.renderer.getTexturer();
            if (texer == statusTexturer) return;
            wrap.renderer.setTexturer(statusTexturer);
            statusTexturer.bindObject(mob);

            var buf = event.getMultiBufferSource();
            wrap.getParts().forEach((n, p) -> {
                p.applyTexture(buf, default_, statusTexturer);
                if (EXCLUDED_PARTS.contains(p.getName())) p.setDisabled(true);
                p.setPostScale(scale);
            });

            boolean oldRenderOverlay = RenderMobOverlays.enabled;
            RenderMobOverlays.enabled = false;
            var accessor = renderer.entityRenderDispatcher;
            boolean oldShadow = accessor.shouldRenderShadow;
            accessor.setRenderShadow(false);
            float f = Mth.lerp(event.getPartialTick(), mob.yRotO, mob.getYRot());
            accessor.render(mob, 0, 0, 0, f, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight());
            accessor.setRenderShadow(oldShadow);
            RenderMobOverlays.enabled = oldRenderOverlay;

            if (texer != null)
            {
                final ResourceLocation orig_ = renderer.getTextureLocation(mob);
                texer.bindObject(mob);
                wrap.getParts().forEach((n, p) -> {
                    p.applyTexture(buf, orig_, texer);
                    if (EXCLUDED_PARTS.contains(p.getName())) p.setDisabled(false);
                });
            }
            scale.set(0, 0, 1);
            for (var p : wrap.getParts().values()) p.setPostScale(scale);
            wrap.renderer.setTexturer(texer);

            mat.popPose();
        }
    }

}
