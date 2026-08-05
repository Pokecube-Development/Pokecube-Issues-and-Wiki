package thut.api.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.Tracker;
import thut.api.data.HolderProvider;
import thut.api.maths.Vector3;

import java.util.function.Supplier;

@EventBusSubscriber
public class PositionTracker
{
    public static interface ITrackedPosition extends INBTSerializable<CompoundTag>
    {
        /**
         * Units of m
         */
        Vec3 getPosition();
        /**
         * Units of m/t
         */
        Vec3 getVelocity();
        /**
         * Units of m/t^2
         */
        Vec3 getAcceleration();

        /**
         * Last tick we updated
         */
        long getTick();
        void update(Vec3 position, long tick);
    }

    private static class TrackedPosition implements ITrackedPosition
    {
        Vec3 position, velocity, acceleration=Vec3.ZERO;
        long last_tick = -1;

        @Override
        public Vec3 getPosition()
        {
            return position;
        }

        @Override
        public Vec3 getVelocity()
        {
            return velocity!=null?velocity:Vec3.ZERO;
        }

        @Override
        public Vec3 getAcceleration()
        {
            return acceleration;
        }

        @Override
        public long getTick()
        {
            return last_tick;
        }

        @Override
        public void update(Vec3 newPos, long tick)
        {
            long dTick = tick - last_tick;
            if (dTick<=0) return; // Don't apply multiple times a tick
            if (last_tick == -1)
            {
                last_tick = tick;
                position = newPos;
                return;
            }
            last_tick = tick;
            double dt = 1.0/dTick;
            if (position != null)
            {
                Vec3 newV = newPos.subtract(position).scale(dt);
                if (velocity != null)
                {
                    acceleration = newV.subtract(velocity).scale(dt);
                }
                velocity = newV;
            }
            position = newPos;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider)
        {
            CompoundTag tag = new CompoundTag();
            tag.putLong("t", last_tick);
            if(position!=null) new Vector3(position).writeToNBT(tag,"r");
            if(velocity!=null) new Vector3(velocity).writeToNBT(tag,"v");
            if(acceleration!=null) new Vector3(acceleration).writeToNBT(tag,"a");
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
        {
            if(nbt.contains("t")) last_tick = nbt.getLong("t");
            if(nbt.contains("rx")) position = Vector3.readFromNBT(nbt, "r").toVec3d();
            if(nbt.contains("vx")) velocity = Vector3.readFromNBT(nbt, "v").toVec3d();
            if(nbt.contains("ax")) acceleration = Vector3.readFromNBT(nbt, "a").toVec3d();
        }
    }

    public static final ResourceLocation ID = ResourceLocation.parse("thutcore:location_track");

    public static final HolderProvider<ITrackedPosition> _REGISTRY = new HolderProvider<>(ID);
    public static Supplier<AttachmentType<ITrackedPosition>> TYPE;

    @SubscribeEvent
    public static void preTickMobs(LevelTickEvent.Pre event)
    {
        if(event.getLevel() instanceof ServerLevel level)
        {
            for(var e: level.getEntities().getAll())
            {
                var track = e.getData(TYPE);
                long tick = Tracker.instance().getTick();
                if (tick == track.getTick()) continue;
                track.update(e.position(), tick);
            }
        }
    }

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register(ID.getPath(), () -> AttachmentType.serializable(_REGISTRY::make).build());
        _REGISTRY.register(new HolderProvider.Provider<>()
        {
            @Override
            public ITrackedPosition apply(IAttachmentHolder in)
            {
                return new TrackedPosition();
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });
    }
}
