package pokecube.core.events.npc;

import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Cancelable;
import pokecube.core.entity.npc.NpcMob;

public class NpcSpawn extends NpcEvent
{
    private final BlockPos location;
    private final MobSpawnType reason;

    private NpcSpawn(final NpcMob trainer, final BlockPos location, final LevelAccessor world,
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

    @Cancelable
    public static class Check extends NpcSpawn
    {
        public final JsonObject args;

        public Check(final NpcMob trainer, final BlockPos location, final LevelAccessor world,
                final MobSpawnType reason, final JsonObject args)
        {
            super(trainer, location, world, reason);
            this.args = args;
        }
    }

    public static class Spawn extends NpcSpawn
    {
        public Spawn(final NpcMob trainer, final BlockPos location, final LevelAccessor world,
                final MobSpawnType reason)
        {
            super(trainer, location, world, reason);
        }
    }

}
