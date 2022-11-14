package pokecube.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.utils.Permissions;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.lib.TComponent;

public class Reload
{

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        final String perm = "command.pokecube.reload";
        PermNodes.registerBooleanNode(PokecubeCore.MODID, perm, DefaultPermissionLevel.OP,
                "Is the player allowed to reload pokecube datapacks");
        command.then(Commands.literal("reload").requires(Permissions.hasPerm(perm))
                .executes((ctx) -> Reload.execute(ctx.getSource())));
    }

    public static int execute(final CommandSourceStack source) throws CommandSyntaxException
    {
        source.sendSuccess(TComponent.translatable("pokecube.command.reloading_packs.start"), true);
        Database.listener.loaded = true;
        Database.needs_reload = true;
        Database.onResourcesReloaded();
        source.sendSuccess(TComponent.translatable("pokecube.command.reloading_packs.end"), true);
        return 0;
    }

}
