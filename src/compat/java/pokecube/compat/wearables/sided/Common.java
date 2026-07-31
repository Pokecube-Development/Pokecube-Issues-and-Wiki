package pokecube.compat.wearables.sided;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
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
import thut.wearables.impl.WearableData;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class Common
{
    public static class WearableMega implements IActiveWearable
    {
        @Override
        public EnumWearable getSlot(final ItemStack stack)
        {
            return EnumWearable.valueOf(this.getSlotSt(stack));
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

    public static class WearableWatch implements IActiveWearable
    {
        @Override
        public EnumWearable getSlot(final ItemStack stack)
        {
            return EnumWearable.WRIST;
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

    public static class WearableBag implements IActiveWearable
    {
        @Override
        public EnumWearable getSlot(final ItemStack stack)
        {
            return EnumWearable.BACK;
        }

        @Override
        public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            final WearablesRenderer renderer = Client.renderers.get("bag");
            if (renderer != null)
                renderer.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

    private static final ResourceLocation WEARABLESKEY = ResourceLocation.parse("pokecube:wearable");

    public static void registerWearable()
    {
        ThutWearables.REGISTRY.put(ResourceLocation.parse("pokecube:mega"), stack -> new WearableMega());
        ThutWearables.REGISTRY.put(ResourceLocation.parse("pokecube:watch"), stack -> new WearableWatch());
        ThutWearables.REGISTRY.put(ResourceLocation.parse("pokecube_adventures:bag"), stack -> new WearableBag());
    }

    @SubscribeEvent
    public static void onWearablesDrop(WearableDroppedEvent event)
    {
        IPokemob mob = PokemobCaps.getPokemobFor(event.getEntity());
        if (mob != null && mob.getOwnerId() != null) event.setCanceled(true);
    }

    @EventBusSubscriber(modid = PokecubeCore.MODID)
    public static class Regster
    {
        @SubscribeEvent
        public static void modifyComponents(ModifyDefaultComponentsEvent event)
        {
            event.modify(PokecubeItems.POKEWATCH, builder -> builder.set(ThutWearables.WEARABLE_DATA.get(),
                    new WearableData(ResourceLocation.parse("pokecube:watch"))));
            event.modify(PokecubeAdv.BAG, builder -> builder.set(ThutWearables.WEARABLE_DATA.get(),
                    new WearableData(ResourceLocation.parse("pokecube_adventures:bag"))));
            event.modifyMatching(i -> i instanceof ItemMegawearable,
                    builder -> builder.set(ThutWearables.WEARABLE_DATA.get(),
                            new WearableData(ResourceLocation.parse("pokecube:mega"))));
        }
    }
}
