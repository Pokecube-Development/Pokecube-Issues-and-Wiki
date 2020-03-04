package pokecube.core.commands;

import java.util.stream.Stream;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.commands.CommandTools;

public class KillCommand
{

    public static int execute(final CommandSource source, final boolean tame, final boolean cull)
            throws CommandSyntaxException
    {
        final ServerWorld world = source.getWorld();
        final Stream<Entity> mobs = world.getEntities();
        final Vec3d pos = source.getPos();

        int count1 = 0;
        final double threshold = PokecubeCore.getConfig().maxSpawnRadius * PokecubeCore.getConfig().maxSpawnRadius;
        for (final Object o : mobs.toArray())
        {
            final IPokemob e = CapabilityPokemob.getPokemobFor((ICapabilityProvider) o);
            if (e != null)
            {
                if (cull && ((Entity) o).getDistanceSq(pos.x, pos.y, pos.z) < threshold) continue;
                if (!tame && e.getOwnerId() != null) continue;
                e.onRecall();
                count1++;

            }
        }
        source.sendFeedback(new TranslationTextComponent("pokecube.command." + (cull ? "cull" : "kill"), count1), true);
        return 0;
    }

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        PermissionAPI.registerNode("command.pokecull", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokecull");
        PermissionAPI.registerNode("command.pokekill", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokekill");
        PermissionAPI.registerNode("command.pokekill_all", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokekill_all");
        // Wild only version
        commandDispatcher.register(Commands.literal("pokekill").requires(cs -> CommandTools.hasPerm(cs,
                "command.pokekill")).executes((ctx) -> KillCommand.execute(ctx.getSource(), false, false)));
        // Also tamed ones
        commandDispatcher.register(Commands.literal("pokekill_all").requires(cs -> CommandTools.hasPerm(cs,
                "command.pokekill_all")).executes((ctx) -> KillCommand.execute(ctx.getSource(), true, false)));

        // cull command
        commandDispatcher.register(Commands.literal("pokecull").requires(cs -> CommandTools.hasPerm(cs,
                "command.pokecull")).executes((ctx) -> KillCommand.execute(ctx.getSource(), false, true)));

    }
}
