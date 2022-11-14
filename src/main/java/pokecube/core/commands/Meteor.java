package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.SpawnHandler;
import thut.api.maths.Vector3;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.commands.CommandTools;
import thut.lib.TComponent;

public class Meteor
{

    public static int execute(final CommandSourceStack source, final int power) throws CommandSyntaxException
    {
        final ServerLevel world = source.getLevel();
        final Vec3 pos = source.getPosition();

        final Vector3 v = new Vector3().set(pos);
        v.x = v.intX() + 0.5;
        v.y = v.intY() + 0.5;
        v.z = v.intZ() + 0.5;

        try
        {
            SpawnHandler.makeMeteor(world, v, power);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }

        // @formatter:off
        // /fill ~-19 ~-19 ~-19 ~19 ~ ~19 minecraft:dirt replace #pokecube:debug
        // /fill ~-11 ~-11 ~-11 ~11 ~11 ~11 minecraft:dirt replace air
        // @formatter:on

        source.sendSuccess(TComponent.translatable("pokecube.meteor.spawned", pos, power), true);
        return 0;
    }

    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        final String perm = "command.meteor";
        PermNodes.registerBooleanNode(PokecubeCore.MODID, perm, DefaultPermissionLevel.OP,
                "Is the player allowed to use /meteor");
        commandDispatcher.register(Commands.literal("meteor").requires(cs -> CommandTools.hasPerm(cs, perm))
                .then(Commands.argument("power", IntegerArgumentType.integer()).executes(
                        (ctx) -> Meteor.execute(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "power")))));
        commandDispatcher.register(Commands.literal("meteor").requires(cs -> CommandTools.hasPerm(cs, perm))
                .executes((ctx) -> Meteor.execute(ctx.getSource(), 100)));
    }
}
