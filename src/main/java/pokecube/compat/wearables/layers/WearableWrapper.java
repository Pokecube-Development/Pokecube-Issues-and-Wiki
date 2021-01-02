package pokecube.compat.wearables.layers;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationChanger;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.animation.IAnimationChanger.WornOffsets;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.core.client.render.x3d.X3dPart;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class WearableWrapper
{

    static final String helm_ident  = "__helm__";
    static final String chest_ident = "__chest__";
    static final String legs_ident  = "__legs__";
    static final String boots_ident = "__boots__";

    static final List<String> addedNames = Lists.newArrayList("__helm__", "__chest__", "__legs__", "__boots__");

    static
    {
        for (final EnumWearable wearable : EnumWearable.values())
            if (wearable.slots == 2)
            {
                WearableWrapper.addedNames.add("__" + wearable + "_right__");
                WearableWrapper.addedNames.add("__" + wearable + "_left__");
            }
            else WearableWrapper.addedNames.add("__" + wearable + "__");
    }

    private static class WearableRenderWrapper extends X3dPart
    {
        public IWearable    wrapped;
        public EnumWearable slot;
        public int          subIndex;
        public LivingEntity wearer;
        public ItemStack    stack;

        private final Vector4 angle;

        private final Vector3 translate;

        public WearableRenderWrapper(final String name, final Vector3 scale, final Vector3 offset, final Vector3 angles)
        {
            super(name);
            this.translate = offset.copy();
            this.preTrans.set(offset);
            this.scale.x = (float) scale.x;
            this.scale.y = (float) scale.y;
            this.scale.z = (float) scale.z;

            final float x = (float) angles.x;
            final float y = (float) angles.y;
            final float z = (float) angles.z;

            this.angle = Vector4.fromAngles(x, y, z);
        }

        @Override
        public void render(final MatrixStack mat, final IVertexBuilder buffer)
        {
            final IRenderTypeBuffer buff = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            final float pt = 0;
            final int br = this.brightness;
            final int ol = this.overlay;
            mat.push();
            mat.rotate(Vector3f.XP.rotationDegrees(-90));
            this.angle.glRotate(mat);
            mat.translate(this.translate.x, this.translate.y, this.translate.z);
            this.wrapped.renderWearable(mat, buff, this.slot, this.subIndex, this.wearer, this.stack, pt, br, ol);
            mat.pop();
        }

        @Override
        public String getType()
        {
            return "_internal_";
        }

    }

    private static IWearable getWearable(final ItemStack stack)
    {
        if (stack.getItem() instanceof IWearable) return (IWearable) stack.getItem();
        return stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
    }

    public static void removeWearables(final IModelRenderer<?> renderer, final IModel wrapper)
    {
        // TODO better way to determine whether we have these parts.
        final List<IExtendedModelPart> added = Lists.newArrayList();

        for (final String name : WearableWrapper.addedNames)
            if (wrapper.getParts().containsKey(name)) added.add(wrapper.getParts().get(name));
        for (final IExtendedModelPart part : added)
        {
            final String name = part.getName();
            part.getParent().getSubParts().remove(name);
            part.getParent().getRenderOrder().remove(name);
            wrapper.getParts().remove(name);
        }
    }

    public static WornOffsets getPartParent(final LivingEntity wearer, final IModelRenderer<?> renderer,
            final IModel imodel, final String identifier)
    {
        final IAnimationChanger temp = renderer.getAnimationChanger();
        if (temp instanceof AnimationChanger) return ((AnimationChanger) temp).wornOffsets.get(identifier);
        return null;
    }

    @SubscribeEvent
    public static void postRender(final RenderLivingEvent.Post<?, ?> event)
    {
        final EntityModel<?> model = event.getRenderer().getEntityModel();

        if (model instanceof ModelWrapper)
        {
            final ModelWrapper<?> renderer = (ModelWrapper<?>) model;
            WearableWrapper.removeWearables(renderer.renderer, renderer);
        }
    }

    @SubscribeEvent
    public static void preRender(final RenderLivingEvent.Pre<?, ?> event)
    {
        final EntityModel<?> model = event.getRenderer().getEntityModel();
        if (model instanceof ModelWrapper)
        {
            final ModelWrapper<?> renderer = (ModelWrapper<?>) model;
            WearableWrapper.applyWearables(event.getEntity(), renderer.renderer, renderer);
        }
    }

    public static void applyWearables(final LivingEntity wearer, final IModelRenderer<?> renderer, final IModel imodel)
    {
        // No Render invisible.
        if (wearer.getActivePotionEffect(Effects.INVISIBILITY) != null) return;
        WornOffsets offsets = null;

        final PlayerWearables worn = ThutWearables.getWearables(wearer);
        if (worn == null) return;
        for (final EnumWearable wearable : EnumWearable.values())
        {
            final int num = wearable.slots;
            for (int i = 0; i < num; i++)
            {
                String ident = "__" + wearable + "__";
                if (num > 1) ident = i == 0 ? "__" + wearable + "_right__" : "__" + wearable + "_left__";
                final IWearable w = WearableWrapper.getWearable(worn.getWearable(wearable, i));
                if (w == null) continue;
                offsets = WearableWrapper.getPartParent(wearer, renderer, imodel, ident);
                if (offsets != null && imodel.getParts().containsKey(offsets.parent))
                {
                    final WearableRenderWrapper wrapper = new WearableRenderWrapper(ident, offsets.scale,
                            offsets.offset, offsets.angles);
                    wrapper.slot = wearable;
                    wrapper.wearer = wearer;
                    wrapper.stack = worn.getWearable(wearable);
                    wrapper.wrapped = w;
                    wrapper.subIndex = i;
                    final IExtendedModelPart part = imodel.getParts().get(offsets.parent);
                    wrapper.setParent(part);
                    part.addChild(wrapper);
                    part.getRenderOrder().add(ident);
                    imodel.getParts().put(ident, wrapper);
                }
            }
        }
    }

}
