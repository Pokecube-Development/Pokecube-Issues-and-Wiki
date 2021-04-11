package thut.api.entity;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class ThutTeleporter
{
    public static class TeleDest
    {
        public static TeleDest readFromNBT(final CompoundNBT nbt)
        {
            final Vector3 loc = Vector3.readFromNBT(nbt, "v");
            final String name = nbt.getString("name");
            final int index = nbt.getInt("i");
            final int version = nbt.getInt("_v_");
            GlobalPos pos = null;
            try
            {
                pos = GlobalPos.CODEC.decode(NBTDynamicOps.INSTANCE, nbt.get("pos")).result().get().getFirst();
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
                this.subLoc = Vector3.getNewVector().set(this.loc.pos().getX() + 0.5, this.loc.pos().getY(), this.loc.pos()
                        .getZ() + 0.5);
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

        public void writeToNBT(final CompoundNBT nbt)
        {
            if (this.subLoc == null) this.subLoc = Vector3.getNewVector().set(this.loc.pos()).add(0.5, 0, 0.5);
            this.subLoc.writeToNBT(nbt, "v");
            nbt.put("pos", GlobalPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, this.loc).get().left().get());
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

        public ITextComponent getInfoName()
        {
            return new TranslationTextComponent("teledest.location", this.loc.pos().getX(), this.loc.pos().getY(),
                    this.loc.pos().getZ(), this.loc.dimension().location());
        }

        public boolean withinDist(final TeleDest other, final double dist)
        {
            if (other.loc.dimension() == this.loc.dimension()) return other.loc.pos().closerThan(this.loc.pos(), dist);
            return false;
        }
    }

    private static class InvulnTicker
    {
        private final ServerWorld overworld;

        private final Entity entity;
        private final long   start;

        public InvulnTicker(final Entity entity)
        {
            this.entity = entity;
            this.overworld = entity.getServer().getLevel(World.OVERWORLD);
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
        private final ServerWorld destWorld;
        private final TeleDest    dest;
        private final boolean     sound;

        public TransferTicker(final ServerWorld destWorld, final Entity entity, final TeleDest dest,
                final boolean sound)
        {
            this.entity = entity;
            this.dest = dest;
            this.sound = sound;
            this.destWorld = destWorld;
            final boolean inTick = destWorld.tickingEntities || ((ServerWorld) entity
                    .getCommandSenderWorld()).tickingEntities;
            if (inTick) MinecraftForge.EVENT_BUS.register(this);
            else
            {
                ThutTeleporter.transferMob(this.destWorld, this.dest, this.entity);
                if (this.sound)
                {
                    this.destWorld.playLocalSound(this.dest.subLoc.x, this.dest.subLoc.y, this.dest.subLoc.z,
                            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
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
                ThutTeleporter.transferMob(this.destWorld, this.dest, this.entity);
                if (this.sound)
                {
                    this.destWorld.playLocalSound(this.dest.subLoc.x, this.dest.subLoc.y, this.dest.subLoc.z,
                            SoundEvents.ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    this.entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
        }
    }

    private static class RemountTicker
    {
        private final UUID mount;
        private final UUID rider;

        final int index;

        private final ServerWorld world;

        int n = 0;

        public RemountTicker(final UUID mount, final UUID rider, final int index, final ServerWorld world)
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
        if (entity.getCommandSenderWorld() instanceof ServerWorld)
        {
            new InvulnTicker(entity);
            if (dest.loc.dimension() == entity.level.dimension())
            {
                ThutTeleporter.moveMob(entity, dest);
                return;
            }
            final ServerWorld destWorld = entity.getServer().getLevel(dest.loc.dimension());
            if (entity instanceof ServerPlayerEntity)
            {
                final ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.isChangingDimension = true;
                player.teleportTo(destWorld, dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.yRot, entity.xRot);
                if (sound)
                {
                    destWorld.playLocalSound(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, SoundEvents.ENDERMAN_TELEPORT,
                            SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
                player.isChangingDimension = false;
            }
            else // Schedule the transfer for end of tick.
                new TransferTicker(destWorld, entity, dest, sound);
        }
    }

    private static void transferMob(final ServerWorld destWorld, final TeleDest dest, final Entity entity)
    {
        ServerPlayerEntity player = null;
        if (entity instanceof ServerPlayerEntity)
        {
            player = (ServerPlayerEntity) entity;
            player.isChangingDimension = true;
        }
        final ServerWorld serverworld = (ServerWorld) entity.getCommandSenderWorld();

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
        entity.setLevel(destWorld);
        ThutTeleporter.addMob(destWorld, entity);
        if (player != null)
        {
            player.isChangingDimension = false;
            player.connection.resetPosition();
            player.connection.teleport(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.yRot, entity.xRot);
        }
    }

    private static void addMob(final ServerWorld world, final Entity entity)
    {
        if (MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, world))) return;
        final IChunk ichunk = world.getChunk(MathHelper.floor(entity.getX() / 16.0D), MathHelper.floor(entity.getZ()
                / 16.0D), ChunkStatus.FULL, true);
        if (ichunk instanceof Chunk) ichunk.addEntity(entity);
        world.loadFromChunk(entity);
    }

    private static void removeMob(final ServerWorld world, final Entity entity, final boolean keepData)
    {
        entity.remove(keepData);
        world.removeEntity(entity, keepData);
    }

    private static void moveMob(final Entity entity, final TeleDest dest)
    {
        if (entity instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) entity;
            player.isChangingDimension = true;
            ((ServerPlayerEntity) entity).connection.teleport(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.yRot,
                    entity.xRot);
            ((ServerPlayerEntity) entity).connection.resetPosition();
            player.isChangingDimension = false;
        }
        else entity.moveTo(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.yRot, entity.xRot);
    }
}
