package pokecube.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.effects.context.MoveEntryContext;
import pokecube.api.effects.context.PokemobContext;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.Permissions;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;

public class Effects
{
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_EFFECT = (ctx,
            sb) -> net.minecraft.commands.SharedSuggestionProvider.suggest(ParticleEffects.EFFECT_REGISTRY.keySet(), sb);

    public static int execute(final CommandSourceStack source, final ServerPlayer player, final String effect)
    {
        var function = ParticleEffects.getEffect(effect);
        var e = Tools.getPointedEntity(player, 10);
        if (e == null) return -1;
        // Pokemob effect, we will see if there is a pokemob in front of the player, and if so, apply it on that.
        if (effect.contains(".pokemob."))
        {
            var pokemob = PokemobCaps.getPokemobFor(e);
            if (pokemob == null) return -1;
            var context = new PokemobContext(pokemob);
            var record = function.apply(context);
            ParticleEffects.ADD_FOR_RENDER.accept(new EffectPacketInfo(record, e, ParticleEffects.EVO_ANCHORS).setContext(context));
            return 0;
        }
        // Move effect, we will just place it in front of the player
        else if (effect.contains(".move."))
        {
            var moveEntry = MoveEntry.get(effect.replaceFirst("pokecube.move.", ""));
            var context = new MoveEntryContext(moveEntry);
            var record = function.apply(context);
            var target = e.getEyePosition().toVector3f().add(e.getLookAngle().toVector3f().mul(5));
            var info = new EffectPacketInfo(record, e.level(), e, null, target).setContext(context);
            ParticleEffects.ADD_FOR_RENDER.accept(info);
            return 0;
        }
        return -1;
    }

    public static int execute(final CommandSourceStack source, final String effect) throws CommandSyntaxException
    {
        final ServerPlayer player = source.getPlayerOrException();
        return Effects.execute(source, player, effect);
    }

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        final String perm = "command.effect";
        PermNodes.registerBooleanNode(PokecubeCore.MODID, perm, DefaultPermissionLevel.OP,
                "Is the player allowed to use /effect");

        command.then(Commands.literal("effect").requires(Permissions.hasPerm(perm))
                .then(Commands.argument("effect", StringArgumentType.string()).suggests(Effects.SUGGEST_EFFECT)
                        .executes(
                                ctx -> Effects.execute(ctx.getSource(), StringArgumentType.getString(ctx, "effect")))));
    }
}
