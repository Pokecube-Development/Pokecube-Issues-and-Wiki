package thut.test.scripting.handlers;

import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import thut.test.scripting.ICmdHandler;

public class CmdHandler implements ICmdHandler
{
    private static class SourceWrapper extends CommandSourceStack
    {
        Component lastMsg = null;

        public SourceWrapper(final CommandSourceStack wrapped)
        {
            super(wrapped.getServer(), wrapped.getPosition(), wrapped.getRotation(), wrapped.getLevel(), 4, wrapped
                    .getTextName(), wrapped.getDisplayName(), wrapped.getServer(), wrapped.getEntity());
        }

        @Override
        public void sendFailure(final Component message)
        {
            this.lastMsg = message;
            super.sendFailure(message);
        }

        @Override
        public void sendSuccess(final Supplier<Component> message, final boolean b)
        {
            this.lastMsg = message.get();
            super.sendSuccess(message, b);
        }
    }

    public CmdHandler()
    {
    }

    @Override
    public String handle(final MinecraftServer server, final String input)
    {
        final SourceWrapper src = new SourceWrapper(server.createCommandSourceStack());
        final int ret = server.getCommands().performPrefixedCommand(src, input);
        return ret == 0 ? null : src.lastMsg == null ? "command success" : src.lastMsg.getString();
    }

}
