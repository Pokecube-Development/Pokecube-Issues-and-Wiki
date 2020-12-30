package thut.test.scripting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.server.MinecraftServer;

public interface ICmdHandler
{
    @Nullable
    /**
     * This should return the feedback message if input is a properly formattred
     * command, and the command has been handled. If this fail to handle, it
     * should return null instead.
     *
     * @param input
     * @return
     */
    String handle(MinecraftServer server, @Nonnull String input);
}
