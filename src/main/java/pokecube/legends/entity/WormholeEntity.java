package pokecube.legends.entity;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.EntityTools;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;

public class WormholeEntity extends LivingEntity
{

    private TeleDest dest = null;
    private TeleDest pos  = null;

    public int energy = 0;

    public WormholeEntity(final EntityType<? extends LivingEntity> type, final World level)
    {
        super(type, level);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
    }

    @Override
    public void readAdditionalSaveData(final CompoundNBT nbt)
    {
        if (nbt.contains("warp_dest"))
        {
            final CompoundNBT tag = nbt.getCompound("warp_dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        if (nbt.contains("anchor_pos"))
        {
            final CompoundNBT tag = nbt.getCompound("anchor_pos");
            this.pos = TeleDest.readFromNBT(tag);
        }
        this.energy = nbt.getInt("energy");
    }

    public TeleDest getDest()
    {
        if (this.dest == null) this.dest = new TeleDest().setPos(GlobalPos.of(this.level != null ? this.level
                .dimension() : World.OVERWORLD, this.getOnPos().above(20)));
        return this.dest;
    }

    public TeleDest getPos()
    {
        if (this.pos == null) this.pos = new TeleDest().setPos(GlobalPos.of(this.level != null ? this.level.dimension()
                : World.OVERWORLD, this.getOnPos()));
        return this.pos;
    }

    @Override
    public ActionResultType interact(final PlayerEntity p_184230_1_, final Hand p_184230_2_)
    {
        return super.interact(p_184230_1_, p_184230_2_);
    }

    @Override
    public void tick()
    {
        super.tick();
        this.getPos();
        this.getDest();
        this.setNoGravity(true);

        final BlockPos anchor = this.getPos().getPos().pos();
        final Vector3d origin = new Vector3d(anchor.getX(), anchor.getY(), anchor.getZ());
        final Vector3d here = this.position();
        final Vector3d diff = origin.subtract(here);
        final Vector3d v = this.getDeltaMovement();
        final double s = 0.01;
        this.setDeltaMovement(v.x + diff.x * s, v.y + diff.y * s, v.z + diff.z * s);

    }

    @Override
    protected void pushEntities()
    {
        final List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), e -> (e.getVehicle() == null
                && !e.level.isClientSide()));
        final Set<UUID> tpd = Sets.newHashSet();
        if (!list.isEmpty()) for (Entity entity : list)
        {
            entity = EntityTools.getCoreEntity(entity);
            final long lastTp = entity.getPersistentData().getLong("pokecube_legends:uwh_use") + 1000;
            final long now = Clock.systemUTC().millis();
            final UUID uuid = entity.getUUID();
            if (now < lastTp || tpd.contains(uuid)) continue;
            PokecubeCore.LOGGER.debug("Transfering {} through a wormhole!", entity);
            tpd.add(uuid);
            entity.getPersistentData().putLong("pokecube_legends:uwh_use", now);

            final boolean sameDim = this.getDest().getPos().dimension().equals(this.getPos().getPos().dimension());
            final List<Entity> passengers = entity.getPassengers();
            if (sameDim)
            {
                // Extra shenanigans needed for this to properly transfer the
                // mob if it has riders.
                entity.ejectPassengers();
                for (final Entity e : passengers)
                    e.setDeltaMovement(0, 0, 0);

            }
            ThutTeleporter.transferTo(entity, this.getDest());
            entity.setDeltaMovement(0, 0, 0);

            final Entity root = entity;
            final AtomicInteger counter = new AtomicInteger();
            if (sameDim) EventsHandler.Schedule(this.level, w ->
            {
                if (counter.getAndIncrement() < 5) return false;
                for (final Entity e : passengers)
                {
                    ThutTeleporter.transferTo(e, this.getDest());
                    e.startRiding(root, true);
                }
                return true;
            });

        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundNBT nbt)
    {
        CompoundNBT tag = new CompoundNBT();
        this.getDest().writeToNBT(tag);
        nbt.put("warp_dest", tag);
        tag = new CompoundNBT();
        this.getPos().writeToNBT(tag);
        nbt.put("anchor_pos", tag);
        nbt.putInt("energy", this.energy);
    }

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots()
    {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(final EquipmentSlotType p_184582_1_)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(final EquipmentSlotType p_184201_1_, final ItemStack p_184201_2_)
    {
    }

    @Override
    public HandSide getMainArm()
    {
        return HandSide.LEFT;
    }

}
