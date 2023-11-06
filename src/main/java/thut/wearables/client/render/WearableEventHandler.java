package thut.wearables.client.render;

import java.lang.reflect.Field;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import thut.lib.TComponent;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.Reference;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketGui;

public class WearableEventHandler
{
    KeyMapping toggleGui;
    KeyMapping[] keys = new KeyMapping[13];

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.MODID, value = Dist.CLIENT)
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
                for (EntityType<?> type : ForgeRegistries.ENTITIES.getValues())
                {
                    EntityRenderer<?> render = renderers.get(type);
                    if (render instanceof LivingEntityRenderer livingRender
                            && livingRender.getModel() instanceof HumanoidModel)
                    {
                        livingRender.addLayer(new WearablesRenderer(livingRender));
                    }
                }

                EntityRenderer<? extends Player> renderer = event.getSkin("slim");
                if (renderer instanceof LivingEntityRenderer livingRenderer)
                {
                    livingRenderer.addLayer(new WearablesRenderer<>(livingRenderer));
                }

                renderer = event.getSkin("default");
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
    }

    public WearableEventHandler()
    {
        this.toggleGui = new KeyMapping("key.wearables.toggle_gui", InputConstants.UNKNOWN.getValue(), "key.categories.wearables");
        ClientRegistry.registerKeyBinding(this.toggleGui);

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
            String name = "Activate";
            if (slot.slots == 1) name = name + " " + slot + " Wearable";
            else name = name + " " + slot + " " + subIndex + " Wearable";

            final boolean defaulted = defaults.containsKey(i);
            final int key = defaulted ? defaults.get(i) : InputConstants.UNKNOWN.getValue();
            if (defaulted) this.keys[i] = new KeyMapping(name, KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
                    InputConstants.Type.KEYSYM.getOrCreate(key), "key.categories.wearables");
            else this.keys[i] = new KeyMapping(name, key, "key.categories.wearables");
            ClientRegistry.registerKeyBinding(this.keys[i]);
        }
    }

    @SubscribeEvent
    public void keyPress(final KeyInputEvent event)
    {
        for (byte i = 0; i < 13; i++)
        {
            final KeyMapping key = this.keys[i];
            if (key.consumeClick())
            {
                final PacketGui packet = new PacketGui();
                packet.data.putByte("S", i);
                ThutWearables.packets.sendToServer(packet);
            }
        }
        if (this.toggleGui.consumeClick())
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
            String key = this.keys[slot.index].getTranslatedKeyMessage().getString();
            String message = "";
            switch (slot.slots)
            {
            case 2:
                message = I18n.get("wearables.keyuse.left", key.formatted(ChatFormatting.GRAY));
                evt.getToolTip().add(TComponent.literal(message));
                key = this.keys[slot.index + 1].getTranslatedKeyMessage().getString().formatted(ChatFormatting.GRAY);
                message = I18n.get("wearables.keyuse.right", key);
                evt.getToolTip().add(TComponent.literal(message));
                break;
            default:
                message = I18n.get("wearables.keyuse.single", key.formatted(ChatFormatting.GRAY));
                evt.getToolTip().add(TComponent.literal(message));
                break;
            }
        }
    }
}
