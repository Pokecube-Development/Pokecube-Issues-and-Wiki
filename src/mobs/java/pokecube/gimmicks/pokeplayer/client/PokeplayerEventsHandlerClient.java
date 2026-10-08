package pokecube.gimmicks.pokeplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.gimmicks.pokeplayer.init.PokeplayerClientSetupHandler;
import thut.api.ThutCaps;
import thut.api.entity.EntityProvider;
import thut.api.entity.ICopyMob;
import thut.core.common.ThutCore;


@EventBusSubscriber(modid = PokecubeCore.MODID, value = Dist.CLIENT)
/// Here we actually handle the keybinds stuff setup in PokeplayerClientSetupHandler
public class PokeplayerEventsHandlerClient
{
    /**
     * In here we register all of the methods for the event listening, this is to keep better track of what events we
     * listen for, and to include notes as to what each event is tracking.
     */
    public static void register()
    {
        // Here we handle the various keybindings for the gimmick
        ThutCore.FORGE_BUS.addListener(PokeplayerEventsHandlerClient::postClientTick);
    }

    @SubscribeEvent
    /// Makes neoforge stop complaining if you are using no other @SubscribeEvent functions
    public static void dummyRegister(RegisterEvent event) { }


    /// Where the key event handling actually happens.
    private static void postClientTick(ClientTickEvent.Post evt)
    {
        final Player player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null) return;
        final ICopyMob copy = ThutCaps.getCopyMob(player);
        if (copy == null) return;
        final LivingEntity pokemob = EntityProvider.getTracked(copy.getCopiedMob());
        if (pokemob == null) return;


        // Opens Pokemob inventory if the correct key is pressed.
        if (PokeplayerClientSetupHandler.openPokemobInv.consumeClick()) PacketPokemobGui.sendPagePacket((byte)0, pokemob.getId());

        PokeplayerClientSetupHandler.clearKeyUse();
    }

}
