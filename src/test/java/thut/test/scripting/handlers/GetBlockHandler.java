package thut.test.scripting.handlers;

import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
            final ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY,
                    new ResourceLocation(worldName));
            final ServerLevel world = server.getLevel(worldKey);
            final BlockPos pos = new BlockPos(Integer.parseInt(posStr[0]), Integer.parseInt(posStr[1]), Integer
                    .parseInt(posStr[2]));
            final BlockState block = world.getBlockState(pos);
            return block.toString();
        }
        return null;
    }

}
