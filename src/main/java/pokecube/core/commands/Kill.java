package pokecube.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.init.Config;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.PermNodes.DefaultPermissionLevel;
import pokecube.core.utils.Tools;
import thut.lib.TComponent;

public class Kill
{
    @Cancelable
    /**
     * This is fired on the PokecubeAPI.POKEMOB_BUS. If cancelled, the kill
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
            final IPokemob e = PokemobCaps.getPokemobFor((ICapabilityProvider) o);
            if (e != null && !e.getEntity().isInvulnerable())
            {
                try
                {
                    if (cull && world.getNearestPlayer(e.getEntity(), Config.Rules.despawnDistance(world)) != null)
                        continue;
                    if (!tame && e.getOwnerId() != null) continue;
                    final KillCommandEvent event = new KillCommandEvent(e.getEntity());
                    if (PokecubeAPI.POKEMOB_BUS.post(event)) continue;
                    e.onRecall();
                    count1++;
                }
                catch (Exception e1)
                {
                    PokecubeAPI.LOGGER.error("Error in kill command!", e1);
                }
            }
        }
        source.sendSuccess(TComponent.translatable("pokecube.command." + (cull ? "cull" : "kill"), count1), true);
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        final String killPerm = "command.pokecube.kill";
        final String killAllPerm = "command.pokecube.kill_all";
        final String cullPerm = "command.pokecube.cull";

        PermNodes.registerNode(cullPerm, DefaultPermissionLevel.OP, "Is the player allowed to cull pokemobs");
        PermNodes.registerNode(killPerm, DefaultPermissionLevel.OP, "Is the player allowed to kill wild pokemobs");
        PermNodes.registerNode(killAllPerm, DefaultPermissionLevel.OP,
                "Is the player allowed to force all pokemobs to recall");

        command.then(Commands.literal("kill").requires(Tools.hasPerm(killPerm))
                .executes((ctx) -> Kill.execute(ctx.getSource(), false, false)));
        command.then(Commands.literal("kill_all").requires(Tools.hasPerm(killAllPerm))
                .executes((ctx) -> Kill.execute(ctx.getSource(), true, false)));
        command.then(Commands.literal("cull").requires(Tools.hasPerm(cullPerm))
                .executes((ctx) -> Kill.execute(ctx.getSource(), false, true)));
    }
}
