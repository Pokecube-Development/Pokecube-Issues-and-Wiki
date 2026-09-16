package pokecube.api.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import pokecube.api.effects.IMoveAnimation.MovePacketInfo;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class RenderParticleEffects
{
    private static final Object lock = new Object();
    private static final Object renderlock = new Object();
    private static final List<MovePacketInfo> TO_TICK = new ArrayList<>();
    private static final List<MovePacketInfo> TO_RENDER = new ArrayList<>();

    private static boolean tickMovePacketInfo(MovePacketInfo info)
    {
        info.onClientTick.accept(info);
        info.animation.spawnClientEntities(info, info.currentTick + 1);
        info.currentTick++;
        return info.isFinished();
    }

    public static void addParticleEffect(MovePacketInfo info)
    {
        synchronized (lock)
        {
            TO_TICK.add(info);
        }
        if (info.animation.hasComplexRender()) synchronized (renderlock)
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
                TO_TICK.clear();
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
            TO_TICK.removeIf(RenderParticleEffects::tickMovePacketInfo);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var stack = event.getPoseStack();
        var tick = event.getPartialTick();
        var delta = tick.getGameTimeDeltaTicks();
        List<MovePacketInfo> toRender;
        synchronized (renderlock)
        {
            TO_RENDER.removeIf(MovePacketInfo::isFinished);
            toRender = new ArrayList<>(TO_RENDER);
        }
        final Player player = Minecraft.getInstance().player;
        if (player == null) return;
        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        toRender.forEach(info -> {
            stack.pushPose();
            var target = info.getSource();
            var prev = info.getPrevSource();
            float x = target.x, y = target.y, z = target.z;
            var camera = event.getCamera().getPosition();
            var f = event.getCamera().getPartialTickTime();
            x = Mth.lerp(f, prev.x, x);
            y = Mth.lerp(f, prev.y, y);
            z = Mth.lerp(f, prev.z, z);
            stack.translate(x - camera.x, y - camera.y, z - camera.z);
            info.animation.clientAnimation(stack, buffers, info, delta, LightTexture.FULL_BLOCK);
            stack.popPose();
        });
    }
}
