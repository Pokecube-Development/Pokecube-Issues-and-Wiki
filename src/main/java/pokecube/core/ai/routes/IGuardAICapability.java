package pokecube.core.ai.routes;

import java.util.List;

import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TimePeriod;

public interface IGuardAICapability
{

    public static enum GuardState
    {
        IDLE, RUNNING, COOLDOWN
    }

    public static interface IGuardTask
    {
        void continueTask(MobEntity entity);

        void endTask(MobEntity entity);

        TimePeriod getActiveTime();

        BlockPos getPos();

        float getRoamDistance();

        default void load(final INBT tag)
        {
            final CompoundNBT nbt = (CompoundNBT) tag;
            if (nbt.contains("pos")) this.setPos(NBTUtil.readBlockPos(nbt.getCompound("pos")));
            this.setRoamDistance(nbt.getFloat("d"));
            this.setActiveTime(new TimePeriod((int) nbt.getLong("start"), (int) nbt.getLong("end")));
        }

        default INBT serialze()
        {
            final CompoundNBT tag = new CompoundNBT();
            if (this.getPos() != null) tag.put("pos", NBTUtil.writeBlockPos(this.getPos()));
            tag.putFloat("d", this.getRoamDistance());
            TimePeriod time;
            if ((time = this.getActiveTime()) != null)
            {
                tag.putLong("start", time.startTick);
                tag.putLong("end", time.endTick);
            }
            return tag;
        }

        void setActiveTime(TimePeriod active);

        void setPos(BlockPos pos);

        void setRoamDistance(float roam);

        void startTask(MobEntity entity);
    }

    public static class Provider extends GuardAICapability implements ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IGuardAICapability> holder = LazyOptional.of(() -> this);

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.setState(GuardState.values()[nbt.getInt("state")]);
            if (nbt.contains("tasks"))
            {
                final ListNBT tasks = (ListNBT) nbt.get("tasks");
                this.loadTasks(tasks);
            }
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return CapHolders.GUARDAI_CAP.orEmpty(capability, this.holder);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT ret = new CompoundNBT();
            ret.putInt("state", this.getState().ordinal());
            ret.put("tasks", this.serializeTasks());
            return ret;
        }
    }

    public static class Storage implements Capability.IStorage<IGuardAICapability>
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void readNBT(final Capability<IGuardAICapability> capability, final IGuardAICapability instance,
                final Direction side, final INBT nbt)
        {
            if (instance instanceof INBTSerializable<?>) ((INBTSerializable) instance).deserializeNBT(nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IGuardAICapability> capability, final IGuardAICapability instance,
                final Direction side)
        {
            if (instance instanceof INBTSerializable<?>) return ((INBTSerializable<?>) instance).serializeNBT();
            return null;
        }
    }

    static final ResourceLocation GUARDCAP = new ResourceLocation(PokecubeMod.ID, "guardai");

    public static void addCapability(final AttachCapabilitiesEvent<?> event)
    {
        if (event.getCapabilities().containsKey(IGuardAICapability.GUARDCAP)) return;
        event.addCapability(IGuardAICapability.GUARDCAP, new Provider());
    }

    IGuardTask getActiveTask();

    // This should be primary task to try, usually will just be
    // getTasks().get(0)
    IGuardTask getPrimaryTask();

    GuardState getState();

    List<IGuardTask> getTasks();

    default void setTask(final int index, final IGuardTask task)
    {
        this.getTasks().set(index, task);
    }

    // do we have a task with a location, and a position
    boolean hasActiveTask(long time, long daylength);

    void loadTasks(ListNBT list);

    ListNBT serializeTasks();

    void setState(GuardState state);
}
