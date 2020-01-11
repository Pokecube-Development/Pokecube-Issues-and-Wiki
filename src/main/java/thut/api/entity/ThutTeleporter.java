package thut.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector4;

public class ThutTeleporter
{
    private static class TransferTicker
    {
        private final Entity      entity;
        private final ServerWorld destWorld;
        private final Vector4     dest;

        public TransferTicker(ServerWorld destWorld, Entity entity, Vector4 dest)
        {
            this.entity = entity;
            this.dest = dest;
            this.destWorld = destWorld;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void TickEvent(WorldTickEvent event)
        {
            if (event.world == entity.getEntityWorld() && event.phase == Phase.END)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                transferMob(destWorld, dest, entity);
            }
        }
    }

    public static void transferTo(Entity entity, Vector4 dest)
    {
        if (entity.getEntityWorld() instanceof ServerWorld)
        {
            if (dest.w == entity.dimension.getId())
            {
                moveMob(entity, dest);
                return;
            }
            ServerWorld destWorld = entity.getServer().getWorld(DimensionType.getById((int) dest.w));
            if (entity instanceof ServerPlayerEntity)
            {

                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.teleport(destWorld, dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
            }
            else
            {
                // Schedule the transfer for end of tick.
                new TransferTicker(destWorld, entity, dest);
            }
        }
    }

    private static void transferMob(ServerWorld destWorld, Vector4 dest, Entity entity)
    {
        ServerWorld serverworld = (ServerWorld) entity.getEntityWorld();
        entity.dimension = destWorld.dimension.getType();
        removeMob(serverworld, entity, true); // Forge: The player
                                              // entity itself is moved,
                                              // and not cloned. So we
                                              // need to keep the data
                                              // alive with no matching
                                              // invalidate call later.
        entity.revive();
        entity.setLocationAndAngles(dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
        entity.setWorld(destWorld);
        addMob(destWorld, entity);
    }

    private static void addMob(ServerWorld world, Entity entity)
    {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.entity.EntityJoinWorldEvent(entity, world))) return;
        IChunk ichunk = world.getChunk(MathHelper.floor(entity.posX / 16.0D), MathHelper.floor(entity.posZ / 16.0D),
                ChunkStatus.FULL, true);
        if (ichunk instanceof Chunk)
        {
            ichunk.addEntity(entity);
        }
        world.addEntityIfNotDuplicate(entity);
    }

    private static void removeMob(ServerWorld world, Entity entity, boolean keepData)
    {
        entity.remove(keepData);
        world.removeEntity(entity, keepData);
    }

    private static void moveMob(Entity entity, Vector4 dest)
    {
        if (entity instanceof ServerPlayerEntity)
        {
            ((ServerPlayerEntity) entity).connection.setPlayerLocation(dest.x, dest.y, dest.z, entity.rotationYaw,
                    entity.rotationPitch);
            ((ServerPlayerEntity) entity).connection.captureCurrentPosition();
        }
        else
        {
            entity.setLocationAndAngles(dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
        }
    }
}
