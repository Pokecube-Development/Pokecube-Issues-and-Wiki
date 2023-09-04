package thut.api.particle;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector3;

public class ParticleHandler
{
    private static class ParticlePacket
    {
        final Vector3 location;
        final IParticle particle;

        public ParticlePacket(final Vector3 v, final IParticle p)
        {
            this.location = v;
            this.particle = p;
        }

        public void kill()
        {
            this.particle.kill();
        }
    }

    static List<ParticlePacket> particles = Lists.newArrayList();

    public static void addParticle(final Vector3 location, final IParticle particle)
    {
        if (particle == null || location == null || Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL)
            return;
        synchronized (ParticleHandler.particles)
        {
            ParticleHandler.particles.add(new ParticlePacket(location.copy(), particle));
        }
    }

    public static void clear()
    {
        ParticleHandler.particles.clear();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderWorldPost(final RenderLevelStageEvent event)
    {
        if (event.getStage() != Stage.AFTER_PARTICLES) return;
        try
        {
            synchronized (ParticleHandler.particles)
            {
                final PoseStack mat = event.getPoseStack();
                mat.pushPose();
                final List<ParticlePacket> list = Lists.newArrayList();
                for (int i = 0; i < ParticleHandler.particles.size(); i++)
                {
                    final ParticlePacket packet = ParticleHandler.particles.get(i);
                    final IParticle particle = packet.particle;
                    final Vector3 target = packet.location;
                    if (particle.getDuration() < 0)
                    {
                        packet.kill();
                        list.add(packet);
                        continue;
                    }
                    final Player player = Minecraft.getInstance().player;
                    final Vector3 source = new Vector3().set(player.xOld, player.yOld, player.zOld);
                    mat.pushPose();
                    source.set(target.subtract(source));
                    mat.translate(source.x, source.y, source.z);
                    final double d0 = (-player.getX() + player.xOld) * event.getPartialTick();
                    final double d1 = (-player.getY() + player.yOld) * event.getPartialTick();
                    final double d2 = (-player.getZ() + player.zOld) * event.getPartialTick();
                    source.set(d0, d1, d2);
                    mat.translate(source.x, source.y, source.z);
                    // particle.render(event.getRenderPartialTicks());
                    mat.popPose();
                    if (particle.lastTick() != player.level().getGameTime())
                    {
                        particle.setDuration(particle.getDuration() - 1);
                        particle.setLastTick(player.level().getGameTime());
                    }
                    if (particle.getDuration() < 0)
                    {
                        packet.kill();
                        list.add(packet);
                    }
                }
                mat.popPose();
                for (int i = 0; i < list.size(); i++) ParticleHandler.particles.remove(list.get(i));
            }
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void WorldUnloadEvent(final Unload evt)
    {
        ParticleHandler.clear();
    }
}
