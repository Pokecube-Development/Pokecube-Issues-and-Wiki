package thut.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
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
            this.name = loc.getPos().toString() + " " + loc.getDimension().getRegistryName();
            return this;
        }

        public TeleDest setPos(final GlobalPos pos)
        {
            if (pos != null)
            {
                this.loc = pos;
                this.subLoc = Vector3.getNewVector().set(this.loc.getPos().getX(), this.loc.getPos().getY(), this.loc
                        .getPos().getZ());
                this.name = this.loc.getPos().toString() + " " + this.loc.getDimension().getRegistryName();
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

        public boolean withinDist(final TeleDest other, final double dist)
        {
            if (other.loc.getDimension() == this.loc.getDimension()) return other.loc.getPos().withinDistance(this.loc
                    .getPos(), dist);
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
            this.overworld = entity.getServer().getWorld(World.OVERWORLD);
            this.start = this.overworld.getGameTime();
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void damage(final LivingHurtEvent event)
        {
            if (!event.getEntity().getUniqueID().equals(this.entity.getUniqueID())) return;
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
                    this.destWorld.playSound(this.dest.subLoc.x, this.dest.subLoc.y, this.dest.subLoc.z,
                            SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    this.entity.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
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
        if (entity.getEntityWorld() instanceof ServerWorld)
        {
            new InvulnTicker(entity);
            if (dest.loc.getDimension() == entity.world.getDimensionKey())
            {
                ThutTeleporter.moveMob(entity, dest);
                return;
            }
            final ServerWorld destWorld = entity.getServer().getWorld(dest.loc.getDimension());
            if (entity instanceof ServerPlayerEntity)
            {
                final ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.invulnerableDimensionChange = true;
                player.teleport(destWorld, dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.rotationYaw,
                        entity.rotationPitch);
                if (sound)
                {
                    destWorld.playSound(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z,
                            SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
                player.invulnerableDimensionChange = false;
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
            player.invulnerableDimensionChange = true;
        }
        final ServerWorld serverworld = (ServerWorld) entity.getEntityWorld();
        // TODO did we need to update the mob for what dim it was in?
        ThutTeleporter.removeMob(serverworld, entity, true);
        entity.revive();
        entity.setLocationAndAngles(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.rotationYaw,
                entity.rotationPitch);
        entity.setWorld(destWorld);
        ThutTeleporter.addMob(destWorld, entity);
        if (player != null)
        {
            player.invulnerableDimensionChange = false;
            player.connection.captureCurrentPosition();
            player.connection.setPlayerLocation(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.rotationYaw,
                    entity.rotationPitch);
        }
    }

    private static void addMob(final ServerWorld world, final Entity entity)
    {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.entity.EntityJoinWorldEvent(entity, world))) return;
        final IChunk ichunk = world.getChunk(MathHelper.floor(entity.getPosX() / 16.0D), MathHelper.floor(entity
                .getPosZ() / 16.0D), ChunkStatus.FULL, true);
        if (ichunk instanceof Chunk) ichunk.addEntity(entity);
        world.addEntityIfNotDuplicate(entity);
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
            player.invulnerableDimensionChange = true;
            ((ServerPlayerEntity) entity).connection.setPlayerLocation(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z,
                    entity.rotationYaw, entity.rotationPitch);
            ((ServerPlayerEntity) entity).connection.captureCurrentPosition();
            player.invulnerableDimensionChange = false;
        }
        else entity.setLocationAndAngles(dest.subLoc.x, dest.subLoc.y, dest.subLoc.z, entity.rotationYaw,
                entity.rotationPitch);
    }
}
