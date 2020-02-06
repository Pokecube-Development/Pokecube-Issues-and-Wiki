package thut.core.client.render.particle;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector3;

public class ParticleHandler
{
    private static class ParticlePacket
    {
        final Vector3   location;
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
        if (particle == null || location == null || Minecraft
                .getInstance().gameSettings.particles == ParticleStatus.MINIMAL) return;
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
    public static void onRenderWorldPost(final RenderWorldLastEvent event)
    {
        try
        {
            synchronized (ParticleHandler.particles)
            {
                final MatrixStack mat = event.getMatrixStack();
                mat.push();
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
                    final PlayerEntity player = Minecraft.getInstance().player;
                    final Vector3 source = Vector3.getNewVector().set(player.lastTickPosX, player.lastTickPosY,
                            player.lastTickPosZ);
                    mat.push();
                    source.set(target.subtract(source));
                    mat.translate(source.x, source.y, source.z);
                    final double d0 = (-player.posX + player.lastTickPosX) * event.getPartialTicks();
                    final double d1 = (-player.posY + player.lastTickPosY) * event.getPartialTicks();
                    final double d2 = (-player.posZ + player.lastTickPosZ) * event.getPartialTicks();
                    source.set(d0, d1, d2);
                    mat.translate(source.x, source.y, source.z);
                    // particle.render(event.getRenderPartialTicks());
                    mat.pop();
                    if (particle.lastTick() != player.getEntityWorld().getGameTime())
                    {
                        particle.setDuration(particle.getDuration() - 1);
                        particle.setLastTick(player.getEntityWorld().getGameTime());
                    }
                    if (particle.getDuration() < 0)
                    {
                        packet.kill();
                        list.add(packet);
                    }
                }
                mat.pop();
                for (int i = 0; i < list.size(); i++)
                    ParticleHandler.particles.remove(list.get(i));
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
