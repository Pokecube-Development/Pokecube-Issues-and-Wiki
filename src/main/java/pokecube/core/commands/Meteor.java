package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.vector.Vector3d;
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
        final Vector3d pos = source.getPos();

        final Vector3 v = Vector3.getNewVector().set(pos);
        v.x = v.intX() + 0.5;
        v.y = v.intY() + 0.5;
        v.z = v.intZ() + 0.5;

        try
        {
            SpawnHandler.makeMeteor(world, v, power);
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // @formatter:off
        // /fill ~-19 ~-19 ~-19 ~19 ~ ~19 minecraft:dirt replace #pokecube:debug
        // /fill ~-11 ~-11 ~-11 ~11 ~11 ~11 minecraft:dirt replace air
        // @formatter:on

        source.sendFeedback(new TranslationTextComponent("pokecube.meteor.spawned", pos, power), true);
        return 0;
    }

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final String perm = "command.meteor";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP, "Is the player allowed to use /meteor");
        commandDispatcher.register(Commands.literal("meteor").requires(cs -> CommandTools.hasPerm(cs, perm)).then(
                Commands.argument("power", IntegerArgumentType.integer()).executes((ctx) -> Meteor.execute(ctx
                        .getSource(), IntegerArgumentType.getInteger(ctx, "power")))));
        commandDispatcher.register(Commands.literal("meteor").requires(cs -> CommandTools.hasPerm(cs, perm)).executes((
                ctx) -> Meteor.execute(ctx.getSource(), 100)));
    }
}
