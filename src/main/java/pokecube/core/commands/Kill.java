package pokecube.core.commands;

import java.util.stream.Stream;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;

public class Kill
{

    public static int execute(final CommandSource source, final boolean tame, final boolean cull)
            throws CommandSyntaxException
    {
        final ServerWorld world = source.getLevel();
        final Stream<Entity> mobs = world.getEntities();
        int count1 = 0;
        for (final Object o : mobs.toArray())
        {
            final IPokemob e = CapabilityPokemob.getPokemobFor((ICapabilityProvider) o);
            if (e != null)
            {
                if (cull && world.getNearestPlayer(e.getEntity(), PokecubeCore.getConfig().cullDistance) != null)
                    continue;
                if (!tame && e.getOwnerId() != null) continue;
                e.onRecall();
                count1++;
            }
        }
        source.sendSuccess(new TranslationTextComponent("pokecube.command." + (cull ? "cull" : "kill"), count1), true);
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSource> command)
    {
        final String killPerm = "command.pokecube.kill";
        final String killAllPerm = "command.pokecube.kill_all";
        final String cullPerm = "command.pokecube.cull";

        PermissionAPI.registerNode(cullPerm, DefaultPermissionLevel.OP, "Is the player allowed to cull pokemobs");
        PermissionAPI.registerNode(killPerm, DefaultPermissionLevel.OP, "Is the player allowed to kill wild pokemobs");
        PermissionAPI.registerNode(killAllPerm, DefaultPermissionLevel.OP,
                "Is the player allowed to force all pokemobs to recall");

        command.then(Commands.literal("kill").requires(Tools.hasPerm(killPerm)).executes((ctx) -> Kill.execute(ctx
                .getSource(), false, false)));
        command.then(Commands.literal("kill_all").requires(Tools.hasPerm(killAllPerm)).executes((ctx) -> Kill.execute(
                ctx.getSource(), true, false)));
        command.then(Commands.literal("cull").requires(Tools.hasPerm(cullPerm)).executes((ctx) -> Kill.execute(ctx
                .getSource(), false, true)));
    }
}
