package thut.wearables.client.render;

import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketGui;

public class WearableEventHandler
{
    private final Set<IEntityRenderer<?, ?>> addedLayers = Sets.newHashSet();

    KeyBinding   toggleGui;
    KeyBinding[] keys = new KeyBinding[13];

    public WearableEventHandler()
    {
        this.toggleGui = new KeyBinding("Toggle Wearables Gui", InputMappings.UNKNOWN.getValue(), "Wearables");
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
            String name = "Activate ";
            if (slot.slots == 1) name = name + " " + slot;
            else name = name + " " + slot + " " + subIndex;

            final boolean defaulted = defaults.containsKey(i);
            final int key = defaulted ? defaults.get(i) : InputMappings.UNKNOWN.getValue();
            if (defaulted) this.keys[i] = new KeyBinding(name, KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
                    InputMappings.Type.KEYSYM.getOrCreate(key), "Wearables");
            else this.keys[i] = new KeyBinding(name, key, "Wearables");
            ClientRegistry.registerKeyBinding(this.keys[i]);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SubscribeEvent
    public void addWearableRenderLayer(final RenderLivingEvent.Post<?, ?> event)
    {
        // Only apply to model bipeds.
        if (!(event.getRenderer().getModel() instanceof BipedModel<?>)) return;
        // Only one layer per renderer.
        if (this.addedLayers.contains(event.getRenderer())) return;

        // Add the layer.
        event.getRenderer().addLayer(new WearablesRenderer(event.getRenderer()));
        this.addedLayers.add(event.getRenderer());
    }

    @SubscribeEvent
    public void keyPress(final KeyInputEvent event)
    {
        for (byte i = 0; i < 13; i++)
        {
            final KeyBinding key = this.keys[i];
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
        if (evt.getItemStack().getCapability(ThutWearables.WEARABLE_CAP, null).isPresent() || evt.getItemStack()
                .getItem() instanceof IWearable)
        {
            IWearable wear = evt.getItemStack().getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
            if (wear == null) wear = (IWearable) evt.getItemStack().getItem();
            final EnumWearable slot = wear.getSlot(evt.getItemStack());
            String key = this.keys[slot.index].getTranslatedKeyMessage().getString();
            String message = "";
            switch (slot.slots)
            {
            case 2:
                message = I18n.get("wearables.keyuse.left", key);
                evt.getToolTip().add(new StringTextComponent(message));
                key = this.keys[slot.index + 1].getTranslatedKeyMessage().getString();
                message = I18n.get("wearables.keyuse.right", key);
                evt.getToolTip().add(new StringTextComponent(message));
                break;
            default:
                message = I18n.get("wearables.keyuse.single", key);
                evt.getToolTip().add(new StringTextComponent(message));
                break;
            }
        }
    }
}
