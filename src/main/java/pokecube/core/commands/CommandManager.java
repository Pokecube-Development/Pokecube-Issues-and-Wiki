package pokecube.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.arguments.PokemobArgument;
import pokecube.core.utils.Permissions;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;

public class CommandManager
{
    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        final String perm = "command.pokecube";
        PermNodes.registerBooleanNode(PokecubeCore.MODID, perm, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the root pokecube command.");
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("pokecube")
                .requires(Permissions.hasPerm(perm));

        Kill.register(command);
        Count.register(command);
        Restore.register(command);
        Reset.register(command);
        Reload.register(command);
        ReloadMoves.register(command);

        commandDispatcher.register(command);
        TM.register(commandDispatcher);
        SecretBase.register(commandDispatcher);
        Pokemake.register(commandDispatcher);
        Pokemake2.register(commandDispatcher);
        Meteor.register(commandDispatcher);
        Pokerecall.register(commandDispatcher);
        Pokeegg.register(commandDispatcher);
    }

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGS;

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> POKEMAKE;

    static
    {
        ARGS = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, PokecubeCore.MODID);
        POKEMAKE = ARGS.register("pokemob", () -> ArgumentTypeInfos.registerByClass(PokemobArgument.class,
                SingletonArgumentInfo.contextFree(PokemobArgument::pokemob)));
    }

    public static void init(IEventBus bus)
    {
        ARGS.register(bus);
    }
}
