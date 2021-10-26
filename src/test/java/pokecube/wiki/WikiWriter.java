package pokecube.wiki;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;

@Mod(value = "pokecube_wiki")
public class WikiWriter
{
    public WikiWriter()
    {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY, (in, net) -> true));
        MinecraftForge.EVENT_BUS.addListener(WikiWriter::onCommandRegister);
    }

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        final LiteralArgumentBuilder<CommandSource> command = Commands.literal("pokewiki");
        command.executes(ctx ->
        {
            JsonHelper.load(null);
            PokemobPageWriter.outputAll();
            return 0;
        });
        event.getDispatcher().register(command);
    }
}
