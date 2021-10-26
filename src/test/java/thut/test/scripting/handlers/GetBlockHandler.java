package thut.test.scripting.handlers;

import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import thut.test.scripting.ICmdHandler;

public class GetBlockHandler implements ICmdHandler
{

    public GetBlockHandler()
    {
    }

    @Override
    public String handle(final MinecraftServer server, final String input)
    {
        JsonObject thing = new JsonObject();
        thing = PokedexEntryLoader.gson.fromJson(input.trim(), JsonObject.class);
        final String key = thing.get("key").getAsString();
        if ("get_block".equals(key))
        {
            final String worldName = thing.get("world").getAsString();
            final String[] posStr = thing.get("pos").getAsString().split(",");
            final RegistryKey<World> worldKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(
                    worldName));
            final ServerWorld world = server.getLevel(worldKey);
            final BlockPos pos = new BlockPos(Integer.parseInt(posStr[0]), Integer.parseInt(posStr[1]), Integer
                    .parseInt(posStr[2]));
            final BlockState block = world.getBlockState(pos);
            return block.toString();
        }
        return null;
    }

}
