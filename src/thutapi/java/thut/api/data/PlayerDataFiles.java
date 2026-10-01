package thut.api.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import thut.api.ThutAPI;

import java.io.File;
import java.nio.file.Path;

public class PlayerDataFiles
{
    public static File getFileForUUID(final String uuid, final String fileName)
    {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Path path = server.getWorldPath(new LevelResource(ThutAPI.MODID));
        // This is to the uuid specific folder
        path = path.resolve(uuid);
        final File dir = path.toFile();
        // and this if the file itself
        path = path.resolve(fileName + ".dat");
        final File file = path.toFile();
        if (!file.exists()) dir.mkdirs();
        return file;
    }
}
