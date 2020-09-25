package pokecube.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class Pokemake
{
    public static void setToArgs(final String[] args, final IPokemob mob, final int index, final Vector3 offset)
    {
        Pokemake.setToArgs(args, mob, index, offset, true);
    }

    /**
     * @param args
     * @param mob
     * @param command
     * @return owner name for pokemob if needed.
     */
    public static void setToArgs(String[] args, IPokemob mob, final int index, final Vector3 offset,
            final boolean initLevel)
    {
        final List<String> cleaned = Lists.newArrayList();
        mob.setHungerTime(-PokecubeCore.getConfig().pokemobLifeSpan / 4);
        for (int i = 0; i < index; i++)
            cleaned.add(args[i]);
        for (int i = index; i < args.length; i++)
        {
            String var = args[i];
            if (var.startsWith("\'")) for (int j = i + 1; i < args.length; j++)
            {
                var = var + " " + args[j];
                if (args[j].endsWith("\'"))
                {
                    var = var.replaceFirst("\'", "");
                    var = var.substring(0, var.length() - 1);
                    i = j;
                    break;
                }
            }
            cleaned.add(var);
        }
        args = cleaned.toArray(new String[0]);

        int red, green, blue;
        red = green = blue = 255;
        int exp = 10;
        int level = -1;
        final String[] moves = new String[4];
        int mindex = 0;
        boolean asWild = false;
        if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Making by Arguments: " + index + " " + Arrays.toString(args));
        ItemStack itemstack = ItemStack.EMPTY;

        if (index < args.length) for (int j = index; j < args.length; j++)
        {
            final String[] vals = args[j].split(":");
            String arg = vals[0];
            if (arg.startsWith("\'")) arg = arg.substring(1, arg.length());
            String val = "";
            if (vals.length > 1)
            {
                val = vals[1];
                for (int i = 2; i < vals.length; i++)
                    val = val + ":" + vals[i];
            }
            if (val.endsWith("\'")) val = val.substring(0, arg.length() - 1);
            if (arg.equalsIgnoreCase("s")) mob.setShiny(true);
            else if (arg.equalsIgnoreCase("item"))
            {
                try
                {
                    final ItemInput item = ItemArgument.item().parse(new StringReader(val));
                    itemstack = item.createStack(1, false);
                }
                catch (final Throwable e)
                {
                    PokecubeCore.LOGGER.error("Error with item for " + val, e);
                }
                /**
                 * Use this instead of isEmpty() to allow specifically setting
                 * an air itemstack for clearing held items.
                 */
                if (itemstack.isEmpty()) PokecubeCore.LOGGER.error("No Item found for " + val);
                else mob.setHeldItem(itemstack);
            }
            else if (arg.equalsIgnoreCase("l"))
            {
                level = Integer.parseInt(val);
                exp = Tools.levelToXp(mob.getExperienceMode(), level);
            }
            else if (arg.equalsIgnoreCase("f")) try
            {
                final ResourceLocation formetag = PokecubeItems.toPokecubeResource(val);
                final FormeHolder holder = Database.formeHolders.get(formetag);
                mob.setCustomHolder(holder);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error parsing forme tag: " + val);
            }
            else if (arg.equalsIgnoreCase("x"))
            {
                byte gender = -3;
                if (val.equalsIgnoreCase("f")) gender = IPokemob.FEMALE;
                if (val.equalsIgnoreCase("m")) gender = IPokemob.MALE;
                if (gender != -3) mob.setSexe(gender);
            }
            else if (arg.equalsIgnoreCase("r")) red = Integer.parseInt(val);
            else if (arg.equalsIgnoreCase("g")) green = Integer.parseInt(val);
            else if (arg.equalsIgnoreCase("b")) blue = Integer.parseInt(val);
            else if (arg.equalsIgnoreCase("a"))
            {
                String ability = null;
                ability = val;
                if (AbilityManager.abilityExists(ability)) mob.setAbility(AbilityManager.getAbility(ability));
            }
            else if (arg.equalsIgnoreCase("m") && mindex < 4)
            {
                moves[mindex] = val;
                mindex++;
            }
            else if (arg.equalsIgnoreCase("v") && offset != null)
            {
                final String[] vec = val.split(",");
                offset.x = Double.parseDouble(vec[0].trim());
                offset.y = Double.parseDouble(vec[1].trim());
                offset.z = Double.parseDouble(vec[2].trim());
            }
            else if (arg.equalsIgnoreCase("i"))
            {
                final String[] vec = val.split(",");
                final byte[] ivs = new byte[6];
                if (vec.length == 1)
                {
                    final byte iv = Byte.parseByte(vec[0]);
                    Arrays.fill(ivs, iv);
                }
                else for (int i = 0; i < 6; i++)
                    ivs[i] = Byte.parseByte(vec[i]);
            }
            else if (arg.equalsIgnoreCase("w")) asWild = true;
            else if (arg.equalsIgnoreCase("h")) mob.setSize(Float.parseFloat(val));
            else if (arg.equalsIgnoreCase("p"))
            {
                Nature nature = null;
                try
                {
                    nature = Nature.values()[Integer.parseInt(val)];
                }
                catch (final NumberFormatException e)
                {
                    nature = Nature.valueOf(val.toUpperCase(Locale.ENGLISH));
                }
                if (nature != null) mob.setNature(nature);
            }
            else if (arg.equalsIgnoreCase("n") && !val.isEmpty()) mob.setPokemonNickname(val);
        }
        mob.setHealth(mob.getMaxHealth());
        if (mob.getEntity() instanceof IMobColourable) ((IMobColourable) mob.getEntity()).setRGBA(red, green, blue,
                255);
        if (initLevel) if (asWild) mob = mob.setForSpawn(exp);
        else
        {
            mob = mob.setExp(exp, asWild);
            level = Tools.xpToLevel(mob.getPokedexEntry().getEvolutionMode(), exp);
            mob.levelUp(level);
        }

        for (int i1 = 0; i1 < 4; i1++)
            if (moves[i1] != null)
            {
                final String arg = moves[i1];
                if (!arg.isEmpty()) if (arg.equalsIgnoreCase("none")) mob.setMove(i1, null);
                else mob.setMove(i1, arg);
            }
    }

    private static int execute(final CommandSource source, final String name, final List<Object> args)
    {
        PokedexEntry entry = Database.getEntry(name);
        if (name.startsWith("random_"))
        {
            final ArrayList<PokedexEntry> entries = Lists.newArrayList(Database.getSortedFormes());
            Collections.shuffle(entries);
            final Iterator<PokedexEntry> iterator = entries.iterator();
            if (name.equalsIgnoreCase("random_normal"))
            {
                entry = iterator.next();
                while (entry.isLegendary() || entry.isMega)
                    entry = iterator.next();
            }
            else if (name.equalsIgnoreCase("random_all"))
            {
                entry = iterator.next();
                while (!entry.base)
                    entry = iterator.next();
            }
            else if (name.equalsIgnoreCase("random_legend"))
            {
                entry = iterator.next();
                while (!entry.isLegendary() || !entry.base)
                    entry = iterator.next();
            }
        }
        final Entity mob = PokecubeCore.createPokemob(entry, source.getWorld());
        if (mob == null)
        {
            CommandTools.sendError(source, "pokecube.command.makeinvalid");
            return 1;
        }
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);

        if (!args.isEmpty() && args.get(0) instanceof LivingEntity)
        {
            final LivingEntity owner = (LivingEntity) args.remove(0);
            pokemob.setOwner(owner.getUniqueID());
            PokecubeCore.LOGGER.debug("Creating " + pokemob.getPokedexEntry() + " for " + owner.getName());
            pokemob.setGeneralState(GeneralStates.TAMED, true);
        }

        final List<String> newArgs = Lists.newArrayList();
        for (final Object o : args)
        {
            final String[] split = o.toString().split(" ");
            for (final String s : split)
                newArgs.add(s);
        }
        final Vector3 offset = Vector3.getNewVector().set(0, 1, 0);
        Pokemake.setToArgs(newArgs.toArray(new String[0]), pokemob, 0, offset);
        pokemob.spawnInit();
        final Vector3 temp = Vector3.getNewVector();
        temp.set(source.getPos()).addTo(offset);
        temp.moveEntity(mob);
        GeneticsManager.initMob(mob);
        mob.getEntityWorld().addEntity(mob);

        final String text = TextFormatting.GREEN + "Spawned " + pokemob.getDisplayName().getString();
        final ITextComponent message = ITextComponent.Serializer.fromJson("[\"" + text + "\"]");
        source.sendFeedback(message, true);
        return 0;
    }

    private static SuggestionProvider<CommandSource> SUGGEST_OTHERS = (ctx,
            sb) -> net.minecraft.command.ISuggestionProvider.suggest(Lists.newArrayList("random_normal", "random_all",
                    "random_legend"), sb);

    private static SuggestionProvider<CommandSource> SUGGEST_POKEMOB = (ctx,
            sb) -> net.minecraft.command.ISuggestionProvider.suggest(Database.getSortedFormNames(), sb);

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final String perm = "command.pokemake";
        // Normal pokemake
        PermissionAPI.registerNode(perm, DefaultPermissionLevel.OP, "Is the player allowed to use /pokemake");

        LiteralArgumentBuilder<CommandSource> command;
        // Set a permission
        command = Commands.literal("pokemake").requires(cs -> CommandTools.hasPerm(cs, perm));
        // Plain command, no args besides name.
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokemake.SUGGEST_POKEMOB)
                .executes(ctx -> Pokemake.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mob"), Lists
                        .newArrayList())));
        commandDispatcher.register(command);

        command = Commands.literal("pokemake").requires(cs -> CommandTools.hasPerm(cs, perm));
        // command with player and no arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokemake.SUGGEST_POKEMOB)
                .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> Pokemake.execute(ctx
                        .getSource(), StringArgumentType.getString(ctx, "mob"), Lists.newArrayList(EntityArgument
                                .getPlayer(ctx, "player"))))));
        commandDispatcher.register(command);

        command = Commands.literal("pokemake").requires(cs -> CommandTools.hasPerm(cs, perm));
        // Command with player then string arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokemake.SUGGEST_POKEMOB)
                .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("args",
                        StringArgumentType.greedyString()).executes(ctx -> Pokemake.execute(ctx.getSource(),
                                StringArgumentType.getString(ctx, "mob"), Lists.newArrayList(EntityArgument.getPlayer(
                                        ctx, "player"), StringArgumentType.getString(ctx, "args")))))));
        commandDispatcher.register(command);

        command = Commands.literal("pokemake").requires(cs -> CommandTools.hasPerm(cs, perm));
        // Command string arguments
        command = command.then(Commands.argument("mob", StringArgumentType.string()).suggests(Pokemake.SUGGEST_POKEMOB)
                .then(Commands.argument("args", StringArgumentType.greedyString()).executes(ctx -> Pokemake.execute(ctx
                        .getSource(), StringArgumentType.getString(ctx, "mob"), Lists.newArrayList(StringArgumentType
                                .getString(ctx, "args"))))));
        commandDispatcher.register(command);

        // Random pokemake
        PermissionAPI.registerNode("command.pokemakerand", DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokemakerand");

        // Set a permission
        command = Commands.literal("pokemakerand").requires(cs -> CommandTools.hasPerm(cs, "command.pokemakerand"));
        // Plain command, no args besides name.
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokemake.SUGGEST_OTHERS)
                .executes(ctx -> Pokemake.execute(ctx.getSource(), StringArgumentType.getString(ctx, "mode"), Lists
                        .newArrayList())));
        commandDispatcher.register(command);

        // Set a permission
        command = Commands.literal("pokemakerand").requires(cs -> CommandTools.hasPerm(cs, "command.pokemakerand"));
        // command with player an no arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokemake.SUGGEST_OTHERS)
                .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> Pokemake.execute(ctx
                        .getSource(), StringArgumentType.getString(ctx, "mode"), Lists.newArrayList(EntityArgument
                                .getPlayer(ctx, "player"))))));
        commandDispatcher.register(command);

        // Set a permission
        command = Commands.literal("pokemakerand").requires(cs -> CommandTools.hasPerm(cs, "command.pokemakerand"));
        // Command with player then string arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokemake.SUGGEST_OTHERS)
                .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("args",
                        StringArgumentType.greedyString()).executes(ctx -> Pokemake.execute(ctx.getSource(),
                                StringArgumentType.getString(ctx, "mode"), Lists.newArrayList(EntityArgument.getPlayer(
                                        ctx, "player"), StringArgumentType.getString(ctx, "args")))))));
        commandDispatcher.register(command);

        // Set a permission
        command = Commands.literal("pokemakerand").requires(cs -> CommandTools.hasPerm(cs, "command.pokemakerand"));
        // Command string arguments
        command = command.then(Commands.argument("mode", StringArgumentType.string()).suggests(Pokemake.SUGGEST_OTHERS)
                .then(Commands.argument("args", StringArgumentType.greedyString()).executes(ctx -> Pokemake.execute(ctx
                        .getSource(), StringArgumentType.getString(ctx, "mode"), Lists.newArrayList(StringArgumentType
                                .getString(ctx, "args"))))));
        commandDispatcher.register(command);

    }
}
