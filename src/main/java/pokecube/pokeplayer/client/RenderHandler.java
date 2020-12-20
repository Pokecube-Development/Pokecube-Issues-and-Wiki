package pokecube.pokeplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import pokecube.pokeplayer.PokeInfo;
import thut.core.common.handlers.PlayerDataHandler;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderHandler 
{
	@SubscribeEvent
    public static void renderHand(final RenderHandEvent event)
    {
        final PlayerEntity player = Minecraft.getInstance().player;
        final PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        if (info.getPokemob(player.world) != null) event.setCanceled(true);
    }
	
	@SubscribeEvent
    public static void renderPlayer(final RenderPlayerEvent.Pre event)
    {
        final PlayerEntity player = (PlayerEntity) event.getEntity();
        final PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        if (info.getPokemob(player.world) == null) return;
        final LivingEntity entity = info.getPokemob(player.world).getEntity();
        final boolean backup = event.getRenderer().getRenderManager().info.isValid();
        event.getRenderer().getRenderManager().setRenderShadow(false);
        event.getRenderer().getRenderManager().renderEntityStatic(
        		entity,
        		0, 
        		0, 
        		0, 
        		0, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
        event.getRenderer().getRenderManager().setRenderShadow(backup);
        event.setCanceled(true);
    }

}
