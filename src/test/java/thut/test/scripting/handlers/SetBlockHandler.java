package thut.test.scripting.handlers;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import thut.test.scripting.ICmdHandler;

public class SetBlockHandler implements ICmdHandler
{

    public SetBlockHandler()
    {
    }

    @Override
    public String handle(final MinecraftServer server, final String input)
    {
        JsonObject thing = new JsonObject();
        thing = PokedexEntryLoader.gson.fromJson(input.trim(), JsonObject.class);
        final String key = thing.get("key").getAsString();
        if ("set_block".equals(key))
        {
            final String worldName = thing.get("world").getAsString();
            final String[] posStr = thing.get("pos").getAsString().split(",");
            final RegistryKey<World> worldKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(
                    worldName));
            final ServerWorld world = server.getWorld(worldKey);
            final BlockPos pos = new BlockPos(Integer.parseInt(posStr[0]), Integer.parseInt(posStr[1]), Integer
                    .parseInt(posStr[2]));
            final String block = thing.get("block").getAsString();
            try
            {
                final BlockState state = BlockStateArgument.blockState().parse(new StringReader(block)).getState();
                world.setBlockState(pos, state);
            }
            catch (final CommandSyntaxException e)
            {
                return e.toString();
            }
            return "block set";
        }
        return null;
    }

}
