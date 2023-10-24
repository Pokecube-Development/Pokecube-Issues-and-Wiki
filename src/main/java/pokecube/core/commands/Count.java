package pokecube.core.commands;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.Vec3;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.Permissions;
import thut.api.entity.ai.RootTask;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.lib.TComponent;

public class Count
{

    public static int execute(final CommandSourceStack source) throws CommandSyntaxException
    {
        final ServerLevel world = source.getLevel();
        final LevelEntityGetter<Entity> mobs = world.getEntities();
        final Vec3 pos = source.getPosition();
        int count1 = 0;
        int count2 = 0;
        final Map<PokedexEntry, Integer> counts = Maps.newHashMap();
        final double threshold = PokecubeCore.getConfig().maxSpawnRadius * PokecubeCore.getConfig().maxSpawnRadius;
        final Set<UUID> found = Sets.newHashSet();
        for (final Entity o : mobs.getAll())
        {
            final IPokemob e = PokemobCaps.getPokemobFor(o);
            if (e != null)
            {
                if (!found.add(e.getEntity().getUUID())) continue;
                if (o.distanceToSqr(pos.x, pos.y, pos.z) > threshold) count2++;
                else count1++;
                Integer i = counts.get(e.getPokedexEntry());
                if (i == null) i = 0;
                counts.put(e.getPokedexEntry(), i + 1);
            }
        }
        final List<Map.Entry<PokedexEntry, Integer>> entries = Lists.newArrayList(counts.entrySet());
        Collections.sort(entries, (o1, o2) -> o2.getValue() - o1.getValue());
        source.sendSuccess(TComponent.translatable("pokecube.command.count", count1, count2), true);
        source.sendSuccess(TComponent.literal(entries.toString()), true);
        if (RootTask.doLoadThrottling) source.sendSuccess(TComponent.literal("Load Factor: " + RootTask.runRate), true);
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        final String perm = "command.pokecube.count";
        PermNodes.registerBooleanNode(PokecubeCore.MODID, perm, DefaultPermissionLevel.OP,
                "Is the player allowed to check the number of pokemobs in the world");
        command.then(Commands.literal("count").requires(Permissions.hasPerm(perm))
                .executes((ctx) -> Count.execute(ctx.getSource())));
    }
}
