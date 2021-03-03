package pokecube.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.database.Database;
import pokecube.core.utils.Tools;

public class Reload
{

    public static void register(final LiteralArgumentBuilder<CommandSource> command)
    {
        final String perm = "command.pokecube.reload";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP,
                "Is the player allowed to reload pokecube datapacks");
        command.then(Commands.literal("reload").requires(Tools.hasPerm(perm)).executes((ctx) -> Reload.execute(ctx
                .getSource())));
    }

    public static int execute(final CommandSource source) throws CommandSyntaxException
    {
        source.sendFeedback(new TranslationTextComponent("pokecube.command.reloading_packs.start"), true);
        Database.listener.loaded = true;
        Database.onResourcesReloaded();
        source.sendFeedback(new TranslationTextComponent("pokecube.command.reloading_packs.end"), true);
        return 0;
    }

}
