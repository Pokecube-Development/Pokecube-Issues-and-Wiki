package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.handlers.events.SpawnHandler;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class Meteor
{

    public static int execute(final CommandSource source, final int power) throws CommandSyntaxException
    {
        final ServerWorld world = source.getWorld();
        final Vec3d pos = source.getPos();
        SpawnHandler.makeMeteor(world, Vector3.getNewVector().set(pos), power);
        source.sendFeedback(new TranslationTextComponent("pokecube.meteor.spawned", pos, power), true);
        return 0;
    }

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        PermissionAPI.registerNode("command.meteor", DefaultPermissionLevel.OP, "Is the player allowed to use /meteor");
        commandDispatcher.register(Commands.literal("meteor").requires(cs -> CommandTools.hasPerm(cs,
                "command.pokecount")).then(Commands.argument("power", IntegerArgumentType.integer()).executes((
                        ctx) -> Meteor.execute(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "power")))));

    }
}
