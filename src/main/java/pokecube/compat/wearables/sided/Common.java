package pokecube.compat.wearables.sided;

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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeItems;
import pokecube.core.items.megastuff.ItemMegawearable;
import thut.api.ModelHolder;
import thut.bling.client.render.Util;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.ModelFactory;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;
import thut.wearables.events.WearableDroppedEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Common
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
            final WearablesRenderer renderer = Client.renderers.get(this.getVariant(stack));
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
                this.model = ModelFactory.createScaled(new ModelHolder(this._model));
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
            final WearablesRenderer renderer = Client.renderers.get("pokewatch");
            if (renderer != null)
                renderer.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

    private static final ResourceLocation WEARABLESKEY = new ResourceLocation("pokecube:wearable");

    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(Common.WEARABLESKEY)) return;
        if (event.getObject().getItem() instanceof ItemMegawearable)
            event.addCapability(Common.WEARABLESKEY, new WearableMega());
        else if (event.getObject().getItem() == PokecubeItems.POKEWATCH.get())
            event.addCapability(Common.WEARABLESKEY, new WearableWatch());
    }

    @SubscribeEvent
    public static void onWearablesDrop(WearableDroppedEvent event)
    {
        IPokemob mob = PokemobCaps.getPokemobFor(event.getEntity());
        if (mob != null && mob.getOwnerId() != null) event.setCanceled(true);
    }
}
