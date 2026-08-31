package pokecube.api.events.npcs;

import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.ICancellableEvent;
import pokecube.core.entity.npc.NpcMob;

public class NpcSpawn extends NpcEvent
{
    private final BlockPos location;
    private final MobSpawnType reason;

    private NpcSpawn(final NpcMob trainer, final BlockPos location,
            final MobSpawnType reason)
    {
        super(trainer);
        this.location = location;
        this.reason = reason;
    }

    public BlockPos getLocation()
    {
        return this.location;
    }

    public MobSpawnType getReason()
    {
        return this.reason;
    }

    public static class Check extends NpcSpawn implements ICancellableEvent
    {
        public final JsonObject args;

        public Check(final NpcMob trainer, final BlockPos location,
                final MobSpawnType reason, final JsonObject args)
        {
            super(trainer, location, reason);
            this.args = args;
        }
    }

    public static class Spawn extends NpcSpawn implements ICancellableEvent
    {
        public Spawn(final NpcMob trainer, final BlockPos location, final MobSpawnType reason)
        {
            super(trainer, location, reason);
        }
    }

}
