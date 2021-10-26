package thut.test.scripting;

import java.util.Random;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientBot
{
    static
    {
        MinecraftForge.EVENT_BUS.addListener(ClientBot::onClientTick);
    }

    private static void onClientTick(final ClientTickEvent event)
    {
        final World world = Minecraft.getInstance().level;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (world == null || player == null || world.isClientSide) return;
        final long timer = world.getGameTime() / 1000;
        final Random rng = new Random(timer * 213787354 + timer * timer * 123471753736l);
        final float yaw = rng.nextFloat() * 360f;
        player.yRot = yaw;
        Minecraft.getInstance().keyboardHandler.keyPress(Minecraft.getInstance().getWindow().getWindow(),
                GLFW.GLFW_KEY_W, 17, 1, 0);
    }

}
