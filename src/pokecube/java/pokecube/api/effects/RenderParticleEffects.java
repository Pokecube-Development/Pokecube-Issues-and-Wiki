package pokecube.api.effects;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import pokecube.api.effects.IMoveAnimation.MovePacketInfo;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class RenderParticleEffects
{
    private static final Object lock = new Object();
    public static List<MovePacketInfo> TO_RENDER = new ArrayList<>();

    private static boolean tickMovePacketInfo(MovePacketInfo info)
    {
        info.onClientTick.accept(info);
        info.animation.spawnClientEntities(info, info.currentTick + 1);
        info.currentTick++;
        if(info.isFinished()) System.out.println("Removing Effect! "+info.currentTick);
        return info.isFinished();
    }

    public static void addParticleEffect(MovePacketInfo info)
    {
        synchronized (lock)
        {
            TO_RENDER.add(info);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event)
    {
        if (event.getLevel().isClientSide())
        {
            synchronized (lock)
            {
                TO_RENDER.clear();
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        if (event.getLevel() != Minecraft.getInstance().level) return;
        synchronized (lock)
        {
            TO_RENDER.removeIf(RenderParticleEffects::tickMovePacketInfo);
        }
    }
}
