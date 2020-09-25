package thut.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector4;

public class ThutTeleporter
{
    private static class InvulnTicker
    {
        private final ServerWorld overworld;

        private final Entity entity;
        private final long   start;

        public InvulnTicker(final Entity entity)
        {
            this.entity = entity;
            this.overworld = entity.getServer().getWorld(DimensionType.OVERWORLD);
            this.start = this.overworld.getGameTime();
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void damage(final LivingHurtEvent event)
        {
            if (event.getEntity() != this.entity) return;
            final long time = this.overworld.getGameTime();
            if (time - this.start > 20)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }
            event.setCanceled(true);
        }

    }

    private static class TransferTicker
    {
        private final Entity      entity;
        private final ServerWorld destWorld;
        private final Vector4     dest;
        private final boolean     sound;

        public TransferTicker(final ServerWorld destWorld, final Entity entity, final Vector4 dest, final boolean sound)
        {
            this.entity = entity;
            this.dest = dest;
            this.sound = sound;
            this.destWorld = destWorld;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void TickEvent(final WorldTickEvent event)
        {
            if (event.world == this.entity.getEntityWorld() && event.phase == Phase.END)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                ThutTeleporter.transferMob(this.destWorld, this.dest, this.entity);
                if (this.sound)
                {
                    this.destWorld.playSound(this.dest.x, this.dest.y, this.dest.z,
                            SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    this.entity.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }

            }
        }
    }

    public static void transferTo(final Entity entity, final Vector4 dest)
    {
        ThutTeleporter.transferTo(entity, dest, false);
    }

    public static void transferTo(final Entity entity, final Vector4 dest, final boolean sound)
    {
        if (entity.getEntityWorld() instanceof ServerWorld)
        {
            new InvulnTicker(entity);
            if (dest.dim == entity.dimension)
            {
                ThutTeleporter.moveMob(entity, dest);
                return;
            }
            final ServerWorld destWorld = entity.getServer().getWorld(DimensionType.getById((int) dest.w));
            if (entity instanceof ServerPlayerEntity)
            {
                final ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.invulnerableDimensionChange = true;
                player.teleport(destWorld, dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
                if (sound)
                {
                    destWorld.playSound(dest.x, dest.y, dest.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                            SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
                player.invulnerableDimensionChange = false;
            }
            else // Schedule the transfer for end of tick.
                new TransferTicker(destWorld, entity, dest, sound);
        }
    }

    private static void transferMob(final ServerWorld destWorld, final Vector4 dest, final Entity entity)
    {
        ServerPlayerEntity player = null;
        if (entity instanceof ServerPlayerEntity)
        {
            player = (ServerPlayerEntity) entity;
            player.invulnerableDimensionChange = true;
        }
        final ServerWorld serverworld = (ServerWorld) entity.getEntityWorld();
        entity.dimension = destWorld.dimension.getType();
        ThutTeleporter.removeMob(serverworld, entity, true);
        entity.revive();
        entity.setLocationAndAngles(dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
        entity.setWorld(destWorld);
        ThutTeleporter.addMob(destWorld, entity);
        if (player != null)
        {
            player.invulnerableDimensionChange = false;
            player.connection.captureCurrentPosition();
            player.connection.setPlayerLocation(dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
        }
    }

    private static void addMob(final ServerWorld world, final Entity entity)
    {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.entity.EntityJoinWorldEvent(entity, world))) return;
        final IChunk ichunk = world.getChunk(MathHelper.floor(entity.getPosX() / 16.0D), MathHelper.floor(entity.getPosZ()
                / 16.0D), ChunkStatus.FULL, true);
        if (ichunk instanceof Chunk) ichunk.addEntity(entity);
        world.addEntityIfNotDuplicate(entity);
    }

    private static void removeMob(final ServerWorld world, final Entity entity, final boolean keepData)
    {
        entity.remove(keepData);
        world.removeEntity(entity, keepData);
    }

    private static void moveMob(final Entity entity, final Vector4 dest)
    {
        if (entity instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) entity;
            player.invulnerableDimensionChange = true;
            ((ServerPlayerEntity) entity).connection.setPlayerLocation(dest.x, dest.y, dest.z, entity.rotationYaw,
                    entity.rotationPitch);
            ((ServerPlayerEntity) entity).connection.captureCurrentPosition();
            player.invulnerableDimensionChange = false;
        }
        else entity.setLocationAndAngles(dest.x, dest.y, dest.z, entity.rotationYaw, entity.rotationPitch);
    }
}
