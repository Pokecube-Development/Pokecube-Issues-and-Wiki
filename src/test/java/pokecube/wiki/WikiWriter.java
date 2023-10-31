package pokecube.wiki;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import thut.core.common.ThutCore;

@Mod(value = "pokecube_wiki")
public class WikiWriter
{
    public WikiWriter()
    {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> "pokecube_wiki", (incoming, isNetwork) -> true));
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
