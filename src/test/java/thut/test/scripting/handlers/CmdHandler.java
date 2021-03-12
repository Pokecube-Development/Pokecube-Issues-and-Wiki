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
            super(wrapped.getServer(), wrapped.getPosition(), wrapped.getRotation(), wrapped.getLevel(), 4, wrapped
                    .getTextName(), wrapped.getDisplayName(), wrapped.getServer(), wrapped.getEntity());
        }

        @Override
        public void sendFailure(final ITextComponent message)
        {
            this.lastMsg = message;
            super.sendFailure(message);
        }

        @Override
        public void sendSuccess(final ITextComponent message, final boolean p_197030_2_)
        {
            this.lastMsg = message;
            super.sendSuccess(message, p_197030_2_);
        }
    }

    public CmdHandler()
    {
    }

    @Override
    public String handle(final MinecraftServer server, final String input)
    {
        final SourceWrapper src = new SourceWrapper(server.createCommandSourceStack());
        final int ret = server.getCommands().performCommand(src, input);
        return ret == 0 ? null : src.lastMsg == null ? "command success" : src.lastMsg.getString();
    }

}
