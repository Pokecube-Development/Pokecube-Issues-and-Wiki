package pokecube.wiki;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import thut.core.common.ThutCore;

@Mod(value = "pokecube_wiki")
public class WikiWriter
{
    public WikiWriter()
    {
//        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
//                () -> new IExtensionPoint.DisplayTest(() -> "pokecube_wiki", (incoming, isNetwork) -> true));
        ThutCore.FORGE_BUS.addListener(WikiWriter::onCommandRegister);
    }

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pokewiki");
        command.executes(ctx ->
        {
            JsonHelper.load(null);
            PokemobPageWriter.outputAll();
            return 0;
        });
        event.getDispatcher().register(command);
    }
}
