package pokecube.core.commands;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;

public class CountCommand
{

    public static int execute(final CommandSource source) throws CommandSyntaxException
    {
        final ServerWorld world = source.getWorld();
        final Stream<Entity> mobs = world.getEntities();
        final Vec3d pos = source.getPos();
        // CommandGenStuff.execute(source.asPlayer(), new String[] { "" });
        int count1 = 0;
        int count2 = 0;
        final Map<PokedexEntry, Integer> counts = Maps.newHashMap();
        final double threshold = PokecubeCore.getConfig().maxSpawnRadius * PokecubeCore.getConfig().maxSpawnRadius;
        for (final Object o : mobs.toArray())
        {
            final IPokemob e = CapabilityPokemob.getPokemobFor((ICapabilityProvider) o);
            if (e != null)
            {
                if (((Entity) o).getDistanceSq(pos.x, pos.y, pos.z) > threshold) count2++;
                else count1++;
                Integer i = counts.get(e.getPokedexEntry());
                if (i == null) i = 0;
                counts.put(e.getPokedexEntry(), i + 1);
            }
        }
        final List<Map.Entry<PokedexEntry, Integer>> entries = Lists.newArrayList(counts.entrySet());
        Collections.sort(entries, (o1, o2) -> o2.getValue() - o1.getValue());
        source.sendFeedback(new TranslationTextComponent("pokecube.command.count", count1, count2), true);
        source.sendFeedback(new StringTextComponent(entries.toString()), true);
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSource> command)
    {
        final String perm = "command.pokecube.count";
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP,
                "Is the player allowed to check the number of pokemobs in the world");
        command.then(Commands.literal("count").requires(Tools.hasPerm(perm)).executes((ctx) -> CountCommand.execute(ctx
                .getSource())));
    }
}
