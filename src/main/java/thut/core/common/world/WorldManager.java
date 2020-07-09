package thut.core.common.world;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.world.DimensionType;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.world.World;

public class WorldManager
{
    private static WorldManager instance = new WorldManager();

    public static WorldManager instance()
    {
        return WorldManager.instance;
    }

    Map<DimensionType, World> worldDimMap = Maps.newHashMap();

    @Nullable
    public World getWorld(DimensionType dimension)
    {
        return this.worldDimMap.get(dimension);
    }

    @SubscribeEvent
    public void WorldLoadEvent(Load evt)
    {
        this.worldDimMap.put(evt.getWorld().getDimension().getType(), new World_Impl(evt.getWorld()));
    }

    @SubscribeEvent
    public void WorldUnLoadEvent(Unload evt)
    {
        this.worldDimMap.remove(evt.getWorld().getDimension().getType());
    }
}
