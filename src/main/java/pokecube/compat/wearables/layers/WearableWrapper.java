package pokecube.compat.wearables.layers;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ScreenEvent.InitScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import thut.core.client.render.animation.AnimationChanger;
import thut.core.client.render.animation.IAnimationChanger;
import thut.core.client.render.animation.IAnimationChanger.WornOffsets;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.core.client.render.x3d.X3dPart;
import thut.lib.TComponent;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
import thut.wearables.client.gui.GuiWearableButton;
import thut.wearables.client.gui.GuiWearables;
import thut.wearables.inventory.PlayerWearables;
import thut.wearables.network.PacketGui;

public class WearableWrapper
{

    static final String helm_ident = "__helm__";
    static final String chest_ident = "__chest__";
    static final String legs_ident = "__legs__";
    static final String boots_ident = "__boots__";

    static final List<String> addedNames = Lists.newArrayList("__helm__", "__chest__", "__legs__", "__boots__");

    static
    {
        addedNames.addAll(EnumWearable.wearableNames.keySet());
    }

    private static class WearableRenderWrapper extends X3dPart
    {
        public IWearable wrapped;
        public EnumWearable slot;
        public int subIndex;
        public LivingEntity wearer;
        public ItemStack stack;
        public WornOffsets offsets;

        public WearableRenderWrapper(final String name, WornOffsets offsets)
        {
            super(name);
            this.setOffsets(offsets);
        }

        public void setOffsets(WornOffsets offsets)
        {
            this.offsets = offsets;
            this.preTrans.set(offset);
            this.offsets = offsets;
        }

        @Override
        public void render(final PoseStack mat, final VertexConsumer buffer)
        {
            // We had something, but took it off.
            if (wrapped == null) return;

            final MultiBufferSource buff = Minecraft.getInstance().renderBuffers().bufferSource();
            final float pt = 0;
            final int br = this.brightness;
            final int ol = this.overlay;
            mat.pushPose();
            this.preRender(mat);
            mat.translate(this.offsets.offset.x, this.offsets.offset.y, this.offsets.offset.z);

            mat.scale(1, -1, -1);
            mat.mulPose(Vector3f.YP.rotationDegrees(180));

            mat.mulPose(Vector3f.ZP.rotationDegrees((float) offsets.angles.z));
            mat.mulPose(Vector3f.YP.rotationDegrees((float) offsets.angles.y));
            mat.mulPose(Vector3f.XP.rotationDegrees((float) offsets.angles.x));

            float sx = (float) this.offsets.scale.x;
            float sy = (float) this.offsets.scale.y;
            float sz = (float) this.offsets.scale.z;
            mat.scale(sx, -sy, -sz);

            this.wrapped.renderWearable(mat, buff, this.slot, this.subIndex, this.wearer, this.stack, pt, br, ol);
            this.postRender(mat);
            mat.popPose();
        }

        @Override
        public String getType()
        {
            return "_internal_";
        }

    }

    private static IWearable getWearable(final ItemStack stack)
    {
        if (stack.getItem() instanceof IWearable wearable) return wearable;
        return stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
    }

    public static void removeWearables(final IModelRenderer<?> renderer, final IModel wrapper)
    {
        final List<IExtendedModelPart> added = Lists.newArrayList();
        for (final String name : WearableWrapper.addedNames)
            if (wrapper.getParts().containsKey(name)) added.add(wrapper.getParts().get(name));
        for (final IExtendedModelPart part : added)
            if (part instanceof WearableRenderWrapper wrapper2) wrapper2.wrapped = null;
    }

    public static WornOffsets getPartParent(final LivingEntity wearer, final IModelRenderer<?> renderer,
            final IModel imodel, final String identifier)
    {
        final IAnimationChanger temp = renderer.getAnimationChanger();
        if (temp instanceof AnimationChanger changer) return changer.wornOffsets.get(identifier);
        return null;
    }

    @SubscribeEvent
    public static void postRender(final RenderLivingEvent.Post<?, ?> event)
    {
        final EntityModel<?> model = event.getRenderer().getModel();

        if (model instanceof ModelWrapper<?> renderer) WearableWrapper.removeWearables(renderer.renderer, renderer);
    }

    @SubscribeEvent
    public static void preRender(final RenderLivingEvent.Pre<?, ?> event)
    {
        final EntityModel<?> model = event.getRenderer().getModel();
        if (model instanceof ModelWrapper<?> renderer)
            WearableWrapper.applyWearables(event.getEntity(), renderer.renderer, renderer);
    }

    private static int lastID = -1;
    private static UUID lastUUID = null;

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public static void guiPostInit(final InitScreenEvent.Post event)
    {
        final GuiWearableButton button;
        if (event.getScreen() instanceof GuiPokemob pokegui)
        {
            int x = pokegui.getGuiLeft() + ThutWearables.config.buttonPos.get(0);
            int y = pokegui.getGuiTop() + ThutWearables.config.buttonPos.get(1) + 10;
            event.getScreen().addRenderableWidget(button = new GuiWearableButton(x, y, 9, 9,
                    TComponent.translatable("button.wearables.on"), b -> openPokemobWearables(pokegui)));
            button.setFGColor(0xFFFF00FF);
        }
        else if (event.getScreen() instanceof GuiWearables wear)
        {
            if (wear.getMenu().wearer.getUUID() == lastUUID)
            {
                int x = wear.getGuiLeft() + ThutWearables.config.buttonPos.get(0);
                int y = wear.getGuiTop() + ThutWearables.config.buttonPos.get(1);
                event.getScreen().addRenderableWidget(button = new GuiWearableButton(x, y, 9, 9,
                        TComponent.translatable("button.wearables.off"), b -> openPokemobGui()));
                button.setFGColor(0xFFFF00FF);
            }
            else
            {
                lastID = -1;
                lastUUID = null;
            }
        }
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void openPokemobWearables(final GuiPokemob pokegui)
    {
        IPokemob pokemob = pokegui.getMenu().pokemob;
        final PacketGui packet = new PacketGui();
        packet.data.putInt("w_open_target_", lastID = pokemob.getEntity().getId());
        lastUUID = pokemob.getEntity().getUUID();
        ThutWearables.packets.sendToServer(packet);
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void openPokemobGui()
    {
        Entity e = null;
        if (lastID != -1 && lastUUID != null)
        {
            e = Minecraft.getInstance().level.getEntity(lastID);
            if (e == null || e.getUUID() != lastUUID)
            {
                lastID = -1;
                lastUUID = null;
                return;
            }
        }
        else return;
        PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, e.getId());
        lastID = -1;
        lastUUID = null;
    }

    public static void applyWearables(final LivingEntity wearer, final IModelRenderer<?> renderer, final IModel imodel)
    {
        // No Render invisible.
        if (wearer.getEffect(MobEffects.INVISIBILITY) != null) return;
        WornOffsets offsets = null;
        final PlayerWearables worn = ThutWearables.getWearables(wearer);
        if (worn == null) return;

        boolean debug = imodel instanceof ModelWrapper<?> wr && wr.debugMode;

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
                    WearableRenderWrapper wrapper;
                    final IExtendedModelPart part = imodel.getParts().get(offsets.parent);
                    if (imodel.getParts().get(ident) instanceof WearableRenderWrapper wrap)
                    {
                        wrapper = wrap;
                    }
                    else
                    {
                        wrapper = new WearableRenderWrapper(ident, offsets);
                        wrapper.setParent(part);
                        part.addChild(wrapper);
                        if (debug)
                        {
                            part.getRenderOrder().add(0, ident);
                            imodel.getParts().put(ident, wrapper);
                            imodel.getRenderOrder().add(0, ident);
                        }
                        else
                        {
                            part.getRenderOrder().add(ident);
                            imodel.getParts().put(ident, wrapper);
                            imodel.getRenderOrder().add(ident);
                        }
                    }
                    wrapper.setOffsets(offsets);
                    wrapper.slot = wearable;
                    wrapper.wearer = wearer;
                    wrapper.stack = worn.getWearable(wearable);
                    wrapper.wrapped = w;
                    wrapper.subIndex = i;
                    imodel.getRenderOrder().remove(ident);

                    if (debug)
                    {
                        imodel.getParts().put(ident, wrapper);
                        imodel.getRenderOrder().add(0, ident);
                    }
                    else
                    {
                        imodel.getParts().put(ident, wrapper);
                        imodel.getRenderOrder().add(ident);
                    }

                    part.preProcess();
                }
            }
        }
    }

}
