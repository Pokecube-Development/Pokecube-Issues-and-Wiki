package pokecube.core.items.megastuff;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.impl.PokecubeMod;
import thut.api.ModelHolder;
import thut.bling.client.render.Finger;
import thut.bling.client.render.Hat;
import thut.bling.client.render.Util;
import thut.bling.client.render.Waist;
import thut.bling.client.render.Wrist;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.model.parts.Material;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;

public class WearablesCompat
{
    public static class WearableMega implements thut.wearables.IActiveWearable, ICapabilityProvider
    {
        private final LazyOptional<IActiveWearable> holder = LazyOptional.of(() -> this);

        @Override
        public boolean dyeable(final ItemStack stack)
        {
            return true;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return ThutWearables.WEARABLE_CAP.orEmpty(capability, this.holder);
        }

        @Override
        public thut.wearables.EnumWearable getSlot(final ItemStack stack)
        {
            return thut.wearables.EnumWearable.valueOf(this.getSlotSt(stack));
        }

        String getSlotSt(final ItemStack stack)
        {
            if (stack.getItem() instanceof ItemMegawearable wearable) return wearable.slot;
            return "";
        }

        String getVariant(final ItemStack stack)
        {
            if (stack.getItem() instanceof ItemMegawearable wearable) return wearable.name;
            return "";
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            final WearablesRenderer renderer = WearablesCompat.renderers.get(this.getVariant(stack));
            if (renderer != null)
                renderer.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

    public abstract static class WearablesRenderer
    {
        // 2 layers of belt rendering for the different colours.
        @OnlyIn(Dist.CLIENT)
        protected IModel model;

        final ResourceLocation _model;

        public WearablesRenderer(ResourceLocation model)
        {
            this._model = model;
        }

        public WearablesRenderer()
        {
            this._model = null;
        }

        @OnlyIn(Dist.CLIENT)
        public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                int brightness, int overlay)
        {
            boolean reload = Util.shouldReloadModel();
            if ((this.model == null || reload) && this._model != null)
                this.model = ModelFactory.create(new ModelHolder(this._model));
        }
    }

    public static class WearableWatch implements thut.wearables.IActiveWearable, ICapabilityProvider
    {
        private final LazyOptional<IActiveWearable> holder = LazyOptional.of(() -> this);

        @Override
        public boolean dyeable(final ItemStack stack)
        {
            return true;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return ThutWearables.WEARABLE_CAP.orEmpty(capability, this.holder);
        }

        @Override
        public thut.wearables.EnumWearable getSlot(final ItemStack stack)
        {
            return thut.wearables.EnumWearable.WRIST;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            final WearablesRenderer renderer = WearablesCompat.renderers.get("pokewatch");
            if (renderer != null)
                renderer.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

    private static final ResourceLocation WEARABLESKEY = new ResourceLocation("pokecube:wearable");

    public static Map<String, WearablesRenderer> renderers = Maps.newHashMap();

    @OnlyIn(Dist.CLIENT)
    public static Predicate<Material> IS_KEYSTONE = m -> (m.name.contains("stone")
            || m.tex != null && m.tex.getPath().contains("stone"));
    @OnlyIn(Dist.CLIENT)
    public static Predicate<Material> IS_OVERLAY = m -> (m.name.contains("_overlay")
            || m.tex != null && m.tex.getPath().contains("_overlay"));

    static
    {
        WearablesCompat.renderers.put("pokewatch",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/pokewatch"))
                {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.WRIST) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Wrist.renderWrist(mat, buff, wearer, stack, this.model, brightness, overlay, IS_OVERLAY);
                    }
                });
        WearablesCompat.renderers.put("ring",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/megaring"))
                {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.FINGER) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Finger.renderFinger(mat, buff, wearer, stack, this.model, brightness, overlay, IS_KEYSTONE);
                    }
                });
        WearablesCompat.renderers.put("belt",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/megabelt"))
                {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.WAIST) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Waist.renderWaist(mat, buff, wearer, stack, this.model, brightness, overlay, IS_KEYSTONE);
                    }
                });
        WearablesCompat.renderers.put("hat",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/hat"))
                {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.HAT) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Hat.renderHat(mat, buff, wearer, stack, this.model, brightness, overlay);
                    }
                });
    }

    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(WearablesCompat.WEARABLESKEY)) return;
        if (event.getObject().getItem() instanceof ItemMegawearable)
            event.addCapability(WearablesCompat.WEARABLESKEY, new WearableMega());
        else if (event.getObject().getItem() == PokecubeItems.POKEWATCH.get())
            event.addCapability(WearablesCompat.WEARABLESKEY, new WearableWatch());
    }
}
