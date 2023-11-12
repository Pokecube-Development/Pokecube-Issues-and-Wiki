package pokecube.compat.wearables.layers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ScreenEvent.InitScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.client.render.mobs.overlays.Status;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.entity.animation.IAnimationChanger.WornOffsets;
import thut.core.client.render.animation.AnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.parts.Part;
import thut.core.client.render.texturing.IRetexturableModel;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.lib.AxisAngles;
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
    public static final Map<EquipmentSlot, String> EQUIP_SLOTS = Maps.newHashMap();
    public static final List<String> WEAR_SLOTS = Lists.newArrayList();

    public static final List<String> ALL_SLOTS = Lists.newArrayList();

    static
    {
        for (var slot : EquipmentSlot.values())
        {
            String name = "__" + slot + "__";
            EQUIP_SLOTS.put(slot, name);
            ALL_SLOTS.add(name);
        }
        for (final EnumWearable wearable : EnumWearable.values())
        {

            final int num = wearable.slots;
            for (int i = 0; i < num; i++)
            {
                String ident = "__" + wearable + "__";
                if (num > 1) ident = i == 0 ? "__" + wearable + "_right__" : "__" + wearable + "_left__";
                WEAR_SLOTS.add(ident);
                ALL_SLOTS.add(ident);
            }
        }
        Status.EXCLUDED_PARTS.addAll(ALL_SLOTS);
    }

    private abstract static class WrapPart extends Part
    {
        public WrapPart(String name)
        {
            super(name);
        }

        @Override
        public String getType()
        {
            return "_internal_";
        }
    }

    private static class HeldItemWrapper extends WrapPart
    {
        public ItemStack stack;
        public LivingEntity mob;
        public WornOffsets offsets;

        public HeldItemWrapper(String name)
        {
            super(name);
        }

        public void setOffsets(WornOffsets offsets)
        {
            this.offsets = offsets;
            this.preTrans.set(offset);
            this.offsets = offsets;
        }

        @Override
        public void render(PoseStack mat, VertexConsumer buffer)
        {
            if (this.isDisabled()) return;
            if (stack.isEmpty() || mob == null) return;
            mat.pushPose();
            this.preRender(mat);
            mat.translate(this.offsets.offset.x, this.offsets.offset.y, this.offsets.offset.z);

            mat.scale(1, -1, -1);
            mat.mulPose(AxisAngles.YP.rotationDegrees(180));

            mat.mulPose(AxisAngles.ZP.rotationDegrees((float) offsets.angles.z));
            mat.mulPose(AxisAngles.YP.rotationDegrees((float) offsets.angles.y));
            mat.mulPose(AxisAngles.XP.rotationDegrees((float) offsets.angles.x));

            float sx = (float) this.offsets.scale.x;
            float sy = (float) this.offsets.scale.y;
            float sz = (float) this.offsets.scale.z;
            mat.scale(sx, -sy, -sz);

            final MultiBufferSource buff = Minecraft.getInstance().renderBuffers().bufferSource();
            Minecraft.getInstance().getItemInHandRenderer().renderItem(mob, stack, ItemTransforms.TransformType.GROUND,
                    false, mat, buff, this.brightness);
            this.postRender(mat);
            mat.popPose();
        }
    }

    private static class WearableRenderWrapper extends WrapPart
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
            if (this.isDisabled()) return;
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
            mat.mulPose(AxisAngles.YP.rotationDegrees(180));

            mat.mulPose(AxisAngles.ZP.rotationDegrees((float) offsets.angles.z));
            mat.mulPose(AxisAngles.YP.rotationDegrees((float) offsets.angles.y));
            mat.mulPose(AxisAngles.XP.rotationDegrees((float) offsets.angles.x));

            float sx = (float) this.offsets.scale.x;
            float sy = (float) this.offsets.scale.y;
            float sz = (float) this.offsets.scale.z;
            mat.scale(sx, -sy, -sz);

            this.wrapped.renderWearable(mat, buff, this.slot, this.subIndex, this.wearer, this.stack, pt, br, ol);
            this.postRender(mat);
            mat.popPose();
        }
    }

    private static IWearable getWearable(final ItemStack stack)
    {
        if (stack.getItem() instanceof IWearable wearable) return wearable;
        return ThutWearables.getWearable(stack);
    }

    public static WornOffsets getPartParent(final LivingEntity wearer, final IModelRenderer<?> renderer,
            final IModel imodel, final String identifier)
    {
        final IAnimationChanger temp = renderer.getAnimationChanger();
        if (temp instanceof AnimationChanger changer) return changer.wornOffsets.get(identifier);
        return null;
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
            int x = pokegui.getGuiLeft() + ThutWearables.config.buttonPos.get(0) - 18;
            int y = pokegui.getGuiTop() + ThutWearables.config.buttonPos.get(1) + 10;
            event.getScreen().addRenderableWidget(button = new GuiWearableButton(x, y, 9, 9,
                    TComponent.translatable("button.wearables.on"), b -> openPokemobWearables(pokegui), pokegui));
            button.stillVisible = () -> pokegui.moduleIndex == 0;
            button.setFGColor(0xFFFF00FF);
        }
        else if (event.getScreen() instanceof GuiWearables wear)
        {
            if (wear.getMenu().wearer.getUUID() == lastUUID)
            {
                int x = wear.getGuiLeft() + ThutWearables.config.buttonPos.get(0);
                int y = wear.getGuiTop() + ThutWearables.config.buttonPos.get(1);
                event.getScreen().addRenderableWidget(button = new GuiWearableButton(x, y, 9, 9,
                        TComponent.translatable("button.wearables.off"), b -> openPokemobGui(), wear));
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

        boolean debug = imodel instanceof ModelWrapper<?> wr && wr.debugMode;

        for (var slot : EquipmentSlot.values())
        {
            String ident = EQUIP_SLOTS.get(slot);
            offsets = WearableWrapper.getPartParent(wearer, renderer, imodel, ident);
            if (offsets != null && imodel.getParts().containsKey(offsets.parent))
            {
                ItemStack stack = wearer.getItemBySlot(slot);
                HeldItemWrapper wrapper;
                final IExtendedModelPart part = imodel.getParts().get(offsets.parent);
                if (imodel.getParts().get(ident) instanceof HeldItemWrapper wrap)
                {
                    wrapper = wrap;
                }
                else
                {
                    wrapper = new HeldItemWrapper(ident);
                    wrapper.setAnimationHolder(part.getAnimationHolder());
                    if (part instanceof IRetexturableModel p)
                    {
                        wrapper.setAnimationChanger(p.getAnimationChanger());
                        wrapper.setTexturerChanger(p.getTexturerChanger());
                    }
                    wrapper.setOffsets(offsets);
                    wrapper.setParent(part);
                    part.addChild(wrapper);
                    if (debug)
                    {
                        part.getRenderOrder().add(0, wrapper);
                        imodel.getParts().put(ident, wrapper);
                        imodel.getRenderOrder().add(0, wrapper);
                    }
                    else
                    {
                        part.getRenderOrder().add(wrapper);
                        imodel.getParts().put(ident, wrapper);
                        imodel.getRenderOrder().add(wrapper);
                    }
                }

                if (wrapper.stack != stack || wrapper.mob != wearer || offsets != wrapper.offsets)
                {
                    wrapper.setOffsets(offsets);
                    wrapper.stack = stack;
                    wrapper.mob = wearer;
                    imodel.getRenderOrder().remove(wrapper);

                    if (debug)
                    {
                        imodel.getParts().put(ident, wrapper);
                        imodel.getRenderOrder().add(0, wrapper);
                    }
                    else
                    {
                        imodel.getParts().put(ident, wrapper);
                        imodel.getRenderOrder().add(wrapper);
                    }
                    part.preProcess();
                }
            }
        }

        final PlayerWearables worn = ThutWearables.getWearables(wearer);
        if (worn == null) return;

        int index = 0;
        for (final EnumWearable wearable : EnumWearable.values())
        {
            final int num = wearable.slots;
            for (int i = 0; i < num; i++)
            {
                String ident = WEAR_SLOTS.get(index);
                index++;
                final IWearable w = WearableWrapper.getWearable(worn.getWearable(wearable, i));
                ItemStack stack = worn.getWearable(wearable);
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
                            part.getRenderOrder().add(0, wrapper);
                            imodel.getParts().put(ident, wrapper);
                            imodel.getRenderOrder().add(0, wrapper);
                        }
                        else
                        {
                            part.getRenderOrder().add(wrapper);
                            imodel.getParts().put(ident, wrapper);
                            imodel.getRenderOrder().add(wrapper);
                        }
                    }

                    if (wrapper.stack != stack || wrapper.wearer != wearer || offsets != wrapper.offsets)
                    {
                        wrapper.setOffsets(offsets);
                        wrapper.slot = wearable;
                        wrapper.wearer = wearer;
                        wrapper.stack = stack;
                        wrapper.wrapped = w;
                        wrapper.subIndex = i;
                        imodel.getRenderOrder().remove(wrapper);

                        if (debug)
                        {
                            imodel.getParts().put(ident, wrapper);
                            imodel.getRenderOrder().add(0, wrapper);
                        }
                        else
                        {
                            imodel.getParts().put(ident, wrapper);
                            imodel.getRenderOrder().add(wrapper);
                        }
                        part.preProcess();
                    }
                }
            }
        }
    }

}
