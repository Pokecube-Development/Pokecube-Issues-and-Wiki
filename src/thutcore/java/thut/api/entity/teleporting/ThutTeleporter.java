package thut.api.entity.teleporting;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class ThutTeleporter
{
    private static class InvulnTicker
    {
        private final ServerLevel overworld;

        private final Entity entity;
        private final long start;

        public InvulnTicker(final Entity entity)
        {
            this.entity = entity;
            this.overworld = entity.getServer().getLevel(Level.OVERWORLD);
            this.start = this.overworld.getGameTime();
            ThutCore.FORGE_BUS.register(this);
        }

        @SubscribeEvent
        public void damage(final LivingHurtEvent event)
        {
            if (!event.getEntity().getUUID().equals(this.entity.getUUID())) return;
            final long time = this.overworld.getGameTime();
            if (time - this.start > 20)
            {
                ThutCore.FORGE_BUS.unregister(this);
                return;
            }
            event.setCanceled(true);
        }

    }

    private static class TransferTicker
    {
        private final Entity entity;
        private final ServerLevel destWorld;
        private final TeleDest dest;
        private final boolean sound;

        public TransferTicker(final ServerLevel destWorld, final Entity entity, final TeleDest dest,
                final boolean sound)
        {
            this.entity = entity;
            this.dest = dest;
            this.sound = sound;
            this.destWorld = destWorld;
            final boolean inTick = destWorld.isHandlingTick();
            if (inTick) ThutCore.FORGE_BUS.register(this);
            else if (entity instanceof ServerPlayer player)
            {
                player.isChangingDimension = true;
                player.teleportTo(destWorld, dest.getTeleLoc().x, dest.getTeleLoc().y, dest.getTeleLoc().z, entity.yRot,
                        entity.xRot);
                if (sound)
                {
                    destWorld.playLocalSound(dest.getTeleLoc().x, dest.getTeleLoc().y, dest.getTeleLoc().z,
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
                player.isChangingDimension = false;
            }
            else
            {
                ThutTeleporter.transferMob(this.destWorld, this.dest, this.entity);
                if (this.sound)
                {
                    this.destWorld.playLocalSound(this.dest.getTeleLoc().x, this.dest.getTeleLoc().y,
                            this.dest.getTeleLoc().z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F,
                            false);
                    this.entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
        }

        @SubscribeEvent
        public void tickEvent(final WorldTickEvent event)
        {
            if (event.world == this.entity.getLevel() && event.phase == Phase.END)
            {
                ThutCore.FORGE_BUS.unregister(this);
                if (this.entity instanceof ServerPlayer player)
                {
                    player.isChangingDimension = true;
                    player.teleportTo(this.destWorld, this.dest.getTeleLoc().x, this.dest.getTeleLoc().y,
                            this.dest.getTeleLoc().z, this.entity.yRot, this.entity.xRot);
                    if (this.sound)
                    {
                        this.destWorld.playLocalSound(this.dest.getTeleLoc().x, this.dest.getTeleLoc().y,
                                this.dest.getTeleLoc().z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F,
                                false);
                        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    }
                    player.isChangingDimension = false;
                }
                else
                {
                    ThutTeleporter.transferMob(this.destWorld, this.dest, this.entity);
                    if (this.sound)
                    {
                        this.destWorld.playLocalSound(this.dest.getTeleLoc().x, this.dest.getTeleLoc().y,
                                this.dest.getTeleLoc().z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F,
                                false);
                        this.entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    private static class RemountTicker
    {
        private final UUID mount;
        private final UUID rider;

        final int index;

        private final ServerLevel world;

        int n = 0;

        public RemountTicker(final UUID mount, final UUID rider, final int index, final ServerLevel world)
        {
            this.mount = mount;
            this.rider = rider;
            this.world = world;
            this.index = index;
            ThutCore.FORGE_BUS.register(this);
        }

        @SubscribeEvent
        public void TickEvent(final WorldTickEvent event)
        {
            if (event.world != this.world) return;
            if (event.phase != Phase.END) return;
            if (this.n++ > 20) ThutCore.FORGE_BUS.unregister(this);
            final Entity mount = this.world.getEntity(this.mount);
            final Entity rider = this.world.getEntity(this.rider);
            if (mount != null && rider != null)
            {
                this.n--;
                final int num = mount.getPassengers().size();
                if (num == this.index)
                {
                    rider.startRiding(mount, true);
                    ThutCore.FORGE_BUS.unregister(this);
                }
            }
        }
    }

    public static void transferTo(final Entity entity, final TeleDest dest)
    {
        ThutTeleporter.transferTo(entity, dest, false);
    }

    public static void transferTo(final Entity entity, final TeleDest dest, final boolean sound)
    {
        if (entity.getLevel() instanceof ServerLevel)
        {
            new InvulnTicker(entity);
            if (dest.loc.dimension() == entity.level.dimension())
            {
                ThutTeleporter.moveMob(entity, dest);
                return;
            }
            final ServerLevel destWorld = entity.getServer().getLevel(dest.loc.dimension());
            // Schedule the transfer for end of tick.
            if (destWorld != null) new TransferTicker(destWorld, entity, dest, sound);
        }
    }

    private static void transferMob(final ServerLevel destWorld, final TeleDest dest, final Entity entity)
    {
        ServerPlayer player = null;
        if (entity instanceof ServerPlayer)
        {
            player = (ServerPlayer) entity;
            player.isChangingDimension = true;
        }
        final ServerLevel serverworld = (ServerLevel) entity.getLevel();

        final List<Entity> passengers = entity.getPassengers();
        entity.ejectPassengers();
        for (int i = 0; i < passengers.size(); i++)
        {
            final Entity e = passengers.get(i);
            e.getPersistentData().putBoolean("thutcore:dimtp", true);
            ThutTeleporter.transferTo(e, dest);
            e.getPersistentData().remove("thutcore:dimtp");
            new RemountTicker(entity.getUUID(), e.getUUID(), i, destWorld);
        }

        ThutTeleporter.removeMob(serverworld, entity, true);
        entity.revive();
        entity.moveTo(dest.getTeleLoc().x, dest.getTeleLoc().y, dest.getTeleLoc().z, entity.yRot, entity.xRot);
        entity.level = destWorld;
        ThutTeleporter.addMob(destWorld, entity);
        if (player != null)
        {
            player.isChangingDimension = false;
            player.connection.resetPosition();
            player.connection.teleport(dest.getTeleLoc().x, dest.getTeleLoc().y, dest.getTeleLoc().z, entity.yRot,
                    entity.xRot);
        }
    }

    private static void addMob(final ServerLevel world, final Entity entity)
    {
        var event = new EntityJoinWorldEvent(entity, world);
        ThutCore.FORGE_BUS.post(event);
        if (event.isCanceled()) return;
        final ChunkAccess ichunk = world.getChunk(Mth.floor(entity.getX() / 16.0D), Mth.floor(entity.getZ() / 16.0D),
                ChunkStatus.FULL, true);
        if (ichunk instanceof LevelChunk) ichunk.addEntity(entity);
        world.addDuringTeleport(entity);
    }

    private static void removeMob(final ServerLevel world, final Entity entity, final boolean keepData)
    {
        entity.setRemoved(RemovalReason.CHANGED_DIMENSION);
    }

    private static void moveMob(final Entity entity, TeleDest dest)
    {
        if (entity instanceof LivingEntity living)
        {
            double targetX = dest.getTeleLoc().x;
            double targetY = dest.getTeleLoc().y;
            double targetZ = dest.getTeleLoc().z;
            final TeleEvent event = TeleEvent.onUseTeleport(living, targetX, targetY, targetZ);

            if (event.isCanceled()) return;

            targetX = event.getTargetX();
            targetY = event.getTargetY();
            targetZ = event.getTargetZ();

            dest = new TeleDest().setLoc(
                    GlobalPos.of(dest.getPos().dimension(), new BlockPos(targetX, targetY, targetZ)),
                    new Vector3().set(targetX, targetY, targetZ));
        }

        if (entity instanceof ServerPlayer player)
        {
            player.isChangingDimension = true;
            player.connection.teleport(dest.getTeleLoc().x, dest.getTeleLoc().y, dest.getTeleLoc().z, entity.yRot,
                    entity.xRot);
            player.connection.resetPosition();
            player.isChangingDimension = false;
        }
        else entity.teleportTo(dest.getTeleLoc().x, dest.getTeleLoc().y, dest.getTeleLoc().z);
    }
}
