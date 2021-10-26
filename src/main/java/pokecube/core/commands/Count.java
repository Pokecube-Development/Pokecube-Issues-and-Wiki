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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;

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
        for (final Object o : mobs.getAll())
        {
            final IPokemob e = CapabilityPokemob.getPokemobFor((ICapabilityProvider) o);
            if (e != null)
            {
                if (!found.add(e.getEntity().getUUID())) continue;
                if (((Entity) o).distanceToSqr(pos.x, pos.y, pos.z) > threshold) count2++;
                else count1++;
                Integer i = counts.get(e.getPokedexEntry());
                if (i == null) i = 0;
                counts.put(e.getPokedexEntry(), i + 1);
            }
        }
        final List<Map.Entry<PokedexEntry, Integer>> entries = Lists.newArrayList(counts.entrySet());
        Collections.sort(entries, (o1, o2) -> o2.getValue() - o1.getValue());
        source.sendSuccess(new TranslatableComponent("pokecube.command.count", count1, count2), true);
        source.sendSuccess(new TextComponent(entries.toString()), true);
        if (RootTask.doLoadThrottling) source.sendSuccess(new TextComponent("Load Factor: " + RootTask.runRate),
                true);
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        final String perm = "command.pokecube.count";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP,
                "Is the player allowed to check the number of pokemobs in the world");
        command.then(Commands.literal("count").requires(Tools.hasPerm(perm)).executes((ctx) -> Count.execute(ctx
                .getSource())));
    }
}
