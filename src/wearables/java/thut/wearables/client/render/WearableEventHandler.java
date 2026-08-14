package thut.wearables.client.render;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.Reference;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketGui;

public class WearableEventHandler
{
    static KeyMapping toggleGui;
    static KeyMapping[] keys = new KeyMapping[13];

    @EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
    public static class RegistryEvents
    {
        @SuppressWarnings(
        { "unchecked", "rawtypes" })
        @SubscribeEvent
        public static void registerLayers(final EntityRenderersEvent.AddLayers event)
        {
            try
            {
                Field f = event.getClass().getDeclaredField("renderers");
                f.setAccessible(true);
                Map<EntityType<?>, EntityRenderer<?>> renderers = (Map<EntityType<?>, EntityRenderer<?>>) f.get(event);
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE.stream().toList())
                {
                    EntityRenderer<?> render = renderers.get(type);
                    if (render instanceof LivingEntityRenderer livingRender
                            && livingRender.getModel() instanceof HumanoidModel)
                    {
                        livingRender.addLayer(new WearablesRenderer(livingRender));
                    }
                }

                EntityRenderer<? extends Player> renderer = event.getSkin(PlayerSkin.Model.SLIM);
                if (renderer instanceof LivingEntityRenderer livingRenderer)
                {
                    livingRenderer.addLayer(new WearablesRenderer<>(livingRenderer));
                }

                renderer = event.getSkin(PlayerSkin.Model.WIDE);
                if (renderer instanceof LivingEntityRenderer livingRenderer)
                {
                    livingRenderer.addLayer(new WearablesRenderer<>(livingRenderer));
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event)
        {
            WearableEventHandler.toggleGui = new KeyMapping("key.wearables.toggle_gui",
                    InputConstants.UNKNOWN.getValue(), "key.categories.wearables");
            event.register(WearableEventHandler.toggleGui);

            final Map<Integer, Integer> defaults = Maps.newHashMap();
            // Back
            defaults.put(7, GLFW.GLFW_KEY_E);
            // Left and right wrists
            defaults.put(2, GLFW.GLFW_KEY_Z);
            defaults.put(3, GLFW.GLFW_KEY_X);

            for (int i = 0; i < 13; i++)
            {
                final EnumWearable slot = EnumWearable.getWearable(i);
                final int subIndex = EnumWearable.getSubIndex(i);
                String name = "Activate ";
                if (slot.slots == 1) name = name + " " + slot + " Wearable";
                else name = name + " " + slot + " " + subIndex + " Wearable";

                final boolean defaulted = defaults.containsKey(i);
                final int key = defaulted ? defaults.get(i) : InputConstants.UNKNOWN.getValue();
                if (defaulted) WearableEventHandler.keys[i] = new KeyMapping(name, KeyConflictContext.IN_GAME,
                        KeyModifier.CONTROL, InputConstants.Type.KEYSYM.getOrCreate(key), "key.categories.wearables");
                else WearableEventHandler.keys[i] = new KeyMapping(name, key, "key.categories.wearables");
                event.register(WearableEventHandler.keys[i]);
            }
        }
    }

    public WearableEventHandler()
    {}

    @SubscribeEvent
    public void keyPress(final Key event)
    {
        for (byte i = 0; i < 13; i++)
        {
            final KeyMapping key = WearableEventHandler.keys[i];
            if (key.consumeClick())
            {
                final PacketGui packet = new PacketGui();
                packet.data.putByte("S", i);
                ThutWearables.packets.sendToServer(packet);
            }
        }
        if (WearableEventHandler.toggleGui.consumeClick())
        {
            final PacketGui packet = new PacketGui();
            ThutWearables.packets.sendToServer(packet);
        }
    }

    @SubscribeEvent
    public void onToolTip(final ItemTooltipEvent evt)
    {
        if (ThutWearables.getWearable(evt.getItemStack()) != null || evt.getItemStack().getItem() instanceof IWearable)
        {
            IWearable wear = ThutWearables.getWearable(evt.getItemStack());
            if (wear == null) wear = (IWearable) evt.getItemStack().getItem();
            final EnumWearable slot = wear.getSlot(evt.getItemStack());
            String key = WearableEventHandler.keys[slot.index].getTranslatedKeyMessage().getString();
            String message = "";
            switch (slot.slots)
            {
            case 2:
                message = I18n.get("wearables.keyuse.left", key.formatted(ChatFormatting.GRAY));
                evt.getToolTip().add(Component.literal(message));
                key = WearableEventHandler.keys[slot.index + 1].getTranslatedKeyMessage().getString()
                        .formatted(ChatFormatting.GRAY);
                message = I18n.get("wearables.keyuse.right", key);
                evt.getToolTip().add(Component.literal(message));
                break;
            default:
                message = I18n.get("wearables.keyuse.single", key.formatted(ChatFormatting.GRAY));
                evt.getToolTip().add(Component.literal(message));
                break;
            }
        }
    }
}
