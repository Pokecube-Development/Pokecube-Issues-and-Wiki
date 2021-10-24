package thut.api.entity;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class ThutTeleporter
{
    @Cancelable
    public static class TeleEvent extends EntityTeleportEvent
    {
        public TeleEvent(final Entity entity, final double targetX, final double targetY, final double targetZ)
        {
            super(entity, targetX, targetY, targetZ);
        }

        public static TeleEvent onUseTeleport(final LivingEntity entity, final double targetX, final double targetY,
                final double targetZ)
        {
            final TeleEvent event = new TeleEvent(entity, targetX, targetY, targetZ);
            MinecraftForge.EVENT_BUS.post(event);
            return event;
        }
    }

    public static class TeleDest
    {
        public static TeleDest readFromNBT(final CompoundTag nbt)
        {
            final Vector3 loc = Vector3.readFromNBT(nbt, "v");
            final String name = nbt.getString("name");
            final int index = nbt.getInt("i");
            final int version = nbt.getInt("_v_");
            GlobalPos pos = null;
            try
            {
                pos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt.get("pos")).result().get().getFirst();
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Error loading value", e);
                return null;
            }
            final TeleDest dest = new TeleDest().setLoc(pos, loc).setName(name).setIndex(index).setVersion(version);
            final TeleLoadEvent event = new TeleLoadEvent(dest);
            // This returns true if the event is cancelled.
            if (MinecraftForge.EVENT_BUS.post(event)) return null;
            // The event can override the destination, it defaults to dest.
            return event.getOverride();
        }

        public GlobalPos loc;

        private Vector3 subLoc;

        private String name;

        public int index;

        // This can be used for tracking things like if worlds update and
        // teledests need resetting, etc.
        public int version = 0;

        public TeleDest()
        {
        }

        public TeleDest setLoc(final GlobalPos loc, final Vector3 subLoc)
        {
            this.loc = loc;
            this.subLoc = subLoc;
            this.name = "";
            return this;
        }

        public TeleDest setPos(final GlobalPos pos)
        {
            if (pos != null)
            {
                this.loc = pos;
                this.subLoc = Vector3.getNewVector().set(this.loc.pos().getX() + 0.5, this.loc.pos().getY(), this.loc
                        .pos().getZ() + 0.5);
                this.name = "";
            }
            return this;
        }

        public TeleDest setVersion(final int version)
        {
            this.version = version;
            return this;
        }

        public GlobalPos getPos()
        {
            return this.loc;
        }

        public Vector3 getLoc()
        {
            return this.subLoc;
        }

        public String getName()
        {
            return this.name;
        }

        public TeleDest setIndex(final int index)
        {
            this.index = index;
            return this;
        }

        public TeleDest setName(final String name)
        {
            this.name = name;
            return this;
        }

        public void writeToNBT(final CompoundTag nbt)
        {
            if (this.subLoc == null) this.subLoc = Vector3.getNewVector().set(this.loc.pos()).add(0.5, 0, 0.5);
            this.subLoc.writeToNBT(nbt, "v");
            nbt.put("pos", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, this.loc).get().left().get());
            nbt.putString("name", this.name);
            nbt.putInt("i", this.index);
            nbt.putInt("_v_", this.version);
        }

        public void shift(final double dx, final int dy, final double dz)
        {
            this.subLoc.x += dx;
            this.subLoc.y += dy;
            this.subLoc.z += dz;
        }

        public Component getInfoName()
        {
            return new TranslatableComponent("teledest.location", this.loc.pos().getX(), this.loc.pos().getY(), this.loc
                    .pos().getZ(), this.loc.dimension().location());
        }

        public boolean withinDist(final TeleDest other, final double dist)
        {
            if (other.loc.dimension() == this.loc.dimension()) return other.loc.pos().closerThan(this.loc.pos(), dist);
            return false;
        }
    }

    private static class InvulnTicker
    {
        private final ServerLevel overworld;

        private final Entity entity;
        private final long   start;

        public InvulnTicker(final Entity entity)
        {
            this.entity = entity;
            this.overworld = entity.getServer().getLevel(Level.OVERWORLD);
            this.start = this.overworld.getGameTime();
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void damage(final LivingHurtEvent event)
        {
            if (!event.getEntity().getUUID().equals(this.entity.getUUID())) return;
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
        private final ServerLevel destWorld;
        private final TeleDest    dest;
        private final boolean     sound;

        public TransferTicker(final ServerLevel destWorld, final Entity entity, final TeleDest dest,
                final boolean sound)
        {
            this.entity = entity;
            this.dest = dest;
            this.sound = sound;
            this.destWorld = destWorld;
            final boolean inTick = destWorld.isHandlingTick() || ((ServerLevel) entity.getCommandSenderWorld())
                    .isHandlingTick();
            if (inTick) MinecraftForge.EVENT_BUS.register(this);
            else if (entity instanceof ServerPlayer)
            {
                final ServerPlayer player = (ServerPlayer) entity;
                player.isChangingDimension = true;
                player.teleportTo(destWorld, dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.yRot, entity.xRot);
                if (sound)
                {
                    destWorld.playLocalSound(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
                player.isChangingDimension = false;
            }
            else
            {
                ThutTeleporter.transferMob(this.destWorld, this.dest, this.entity);
                if (this.sound)
                {
                    this.destWorld.playLocalSound(this.dest.subLoc.x, this.dest.subLoc.y, this.dest.subLoc.z,
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    this.entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
        }

        @SubscribeEvent
        public void TickEvent(final WorldTickEvent event)
        {
            if (event.world == this.entity.getCommandSenderWorld() && event.phase == Phase.END)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                if (this.entity instanceof ServerPlayer)
                {
                    final ServerPlayer player = (ServerPlayer) this.entity;
                    player.isChangingDimension = true;
                    player.teleportTo(this.destWorld, this.dest.subLoc.x, this.dest.subLoc.y, this.dest.subLoc.z,
                            this.entity.yRot, this.entity.xRot);
                    if (this.sound)
                    {
                        this.destWorld.playLocalSound(this.dest.subLoc.x, this.dest.subLoc.y, this.dest.subLoc.z,
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
                        this.destWorld.playLocalSound(this.dest.subLoc.x, this.dest.subLoc.y, this.dest.subLoc.z,
                                SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
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
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void TickEvent(final WorldTickEvent event)
        {
            if (event.world != this.world) return;
            if (event.phase != Phase.END) return;
            if (this.n++ > 20) MinecraftForge.EVENT_BUS.unregister(this);
            final Entity mount = this.world.getEntity(this.mount);
            final Entity rider = this.world.getEntity(this.rider);
            if (mount != null && rider != null)
            {
                this.n--;
                final int num = mount.getPassengers().size();
                if (num == this.index)
                {
                    rider.startRiding(mount, true);
                    MinecraftForge.EVENT_BUS.unregister(this);
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
        if (entity.getCommandSenderWorld() instanceof ServerLevel)
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
        final ServerLevel serverworld = (ServerLevel) entity.getCommandSenderWorld();

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
        entity.moveTo(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.yRot, entity.xRot);
        entity.level = destWorld;
        ThutTeleporter.addMob(destWorld, entity);
        if (player != null)
        {
            player.isChangingDimension = false;
            player.connection.resetPosition();
            player.connection.teleport(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.yRot, entity.xRot);
        }
    }

    private static void addMob(final ServerLevel world, final Entity entity)
    {
        if (MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, world))) return;
        final ChunkAccess ichunk = world.getChunk(Mth.floor(entity.getX() / 16.0D), Mth.floor(entity.getZ() / 16.0D),
                ChunkStatus.FULL, true);
        if (ichunk instanceof LevelChunk) ichunk.addEntity(entity);
        world.addDuringTeleport(entity);
    }

    private static void removeMob(final ServerLevel world, final Entity entity, final boolean keepData)
    {
        world.removeEntity(entity, keepData);
    }

    private static void moveMob(final Entity entity, TeleDest dest)
    {
        if (entity instanceof LivingEntity)
        {
            double targetX = dest.getLoc().x + 0.5;
            double targetY = dest.getLoc().y;
            double targetZ = dest.getLoc().z + 0.5;
            final TeleEvent event = TeleEvent.onUseTeleport((LivingEntity) entity, targetX, targetY, targetZ);

            if (event.isCanceled()) return;

            targetX = event.getTargetX();
            targetY = event.getTargetY();
            targetZ = event.getTargetZ();

            dest = new TeleDest().setLoc(GlobalPos.of(dest.getPos().dimension(), new BlockPos(targetX, targetY,
                    targetZ)), Vector3.getNewVector().set(targetX, targetY, targetZ));
        }

        if (entity instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) entity;
            player.isChangingDimension = true;
            ((ServerPlayer) entity).connection.teleport(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.yRot,
                    entity.xRot);
            ((ServerPlayer) entity).connection.resetPosition();
            player.isChangingDimension = false;
        }
        else entity.teleportTo(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z);
    }
}
