package pokecube.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;

public class Kill
{
    @Cancelable
    /**
     * This is fired on the PokecubeCore.POKEMOB_BUS. If cancelled, the kill
     * command will not apply to the requested pokemob!
     */
    public static class KillCommandEvent extends LivingEvent
    {
        public KillCommandEvent(final LivingEntity entity)
        {
            super(entity);
        }
    }

    public static int execute(final CommandSourceStack source, final boolean tame, final boolean cull)
            throws CommandSyntaxException
    {
        final ServerLevel world = source.getLevel();
        final LevelEntityGetter<Entity> mobs = world.getEntities();
        int count1 = 0;
        for (final Object o : mobs.getAll())
        {
            final IPokemob e = CapabilityPokemob.getPokemobFor((ICapabilityProvider) o);
            if (e != null)
            {
                if (cull && world.getNearestPlayer(e.getEntity(), PokecubeCore.getConfig().cullDistance) != null)
                    continue;
                if (!tame && e.getOwnerId() != null) continue;
                final KillCommandEvent event = new KillCommandEvent(e.getEntity());
                if (PokecubeCore.POKEMOB_BUS.post(event)) continue;
                e.onRecall();
                count1++;
            }
        }
        source.sendSuccess(new TranslatableComponent("pokecube.command." + (cull ? "cull" : "kill"), count1), true);
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
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
