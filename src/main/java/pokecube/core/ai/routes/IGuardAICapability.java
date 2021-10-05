package pokecube.core.ai.routes;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TimePeriod;

public interface IGuardAICapability extends INBTSerializable<CompoundTag>
{

    public static enum GuardState
    {
        IDLE, RUNNING, COOLDOWN
    }

    public static interface IGuardTask
    {
        void continueTask(Mob entity);

        void endTask(Mob entity);

        TimePeriod getActiveTime();

        BlockPos getPos();

        float getRoamDistance();

        default void load(final Tag tag)
        {
            final CompoundTag nbt = (CompoundTag) tag;
            if (nbt.contains("pos")) this.setPos(NbtUtils.readBlockPos(nbt.getCompound("pos")));
            this.setRoamDistance(nbt.getFloat("d"));
            this.setActiveTime(new TimePeriod((int) nbt.getLong("start"), (int) nbt.getLong("end")));
        }

        default Tag serialze()
        {
            final CompoundTag tag = new CompoundTag();
            if (this.getPos() != null) tag.put("pos", NbtUtils.writeBlockPos(this.getPos()));
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

        void startTask(Mob entity);
    }

    public static class Provider extends GuardAICapability implements ICapabilityProvider
    {
        private final LazyOptional<IGuardAICapability> holder = LazyOptional.of(() -> this);

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return CapHolders.GUARDAI_CAP.orEmpty(capability, this.holder);
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

    void loadTasks(ListTag list);

    ListTag serializeTasks();

    void setState(GuardState state);
}
