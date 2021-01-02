package thut.test.scripting.handlers;

import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import thut.test.scripting.ICmdHandler;

public class CmdHandler implements ICmdHandler
{
    private static class SourceWrapper extends CommandSource
    {
        ITextComponent lastMsg = null;

        public SourceWrapper(final CommandSource wrapped)
        {
            super(wrapped.getServer(), wrapped.getPos(), wrapped.getRotation(), wrapped.getWorld(), 4, wrapped
                    .getName(), wrapped.getDisplayName(), wrapped.getServer(), wrapped.getEntity());
        }

        @Override
        public void sendFeedback(final ITextComponent message, final boolean allowLogging)
        {
            this.lastMsg = message;
            super.sendFeedback(message, allowLogging);
        }

        @Override
        public void sendErrorMessage(final ITextComponent message)
        {
            this.lastMsg = message;
            super.sendErrorMessage(message);
        }
    }

    public CmdHandler()
    {
    }

    @Override
    public String handle(final MinecraftServer server, final String input)
    {
        final SourceWrapper src = new SourceWrapper(server.getCommandSource());
        final int ret = server.getCommandManager().handleCommand(src, input);
        return ret == 0 ? null : src.lastMsg == null ? "command success" : src.lastMsg.getString();
    }

}
