package pokecube.gimmicks.pokeplayer.init;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.gimmicks.pokeplayer.client.PokeplayerEventsHandlerClient;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = PokecubeCore.MODID, value = Dist.CLIENT)
/// Here we setup the keybinds for pokeplayer stuff
public class PokeplayerClientSetupHandler
{
    public static KeyMapping openPokemobInv;

    static
    {
        openPokemobInv = new KeyMapping("key.pokeplayer.openpokemobinv", KeyConflictContext.IN_GAME, KeyModifier.ALT, Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT, "key.categories.pokecube");
    }

    private static final List<KeyMapping> KEYS = new ArrayList<>();

    private static void registerKey(KeyMapping key, RegisterKeyMappingsEvent event)
    {
        KEYS.add(key);
        event.register(key);
    }

    public static void clearKeyUse()
    {
        KEYS.forEach(k -> {while (k.consumeClick()) ;});
    }

    @SubscribeEvent
    public static void registerKeybinds(RegisterKeyMappingsEvent event)
    {
        PokecubeAPI.logDebug("Init Pokeplayer Keybinds");
        registerKey(PokeplayerClientSetupHandler.openPokemobInv, event);
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo("Pokeplayer Client Setup");

        // Register event handler
        PokeplayerEventsHandlerClient.register();
    }
}
