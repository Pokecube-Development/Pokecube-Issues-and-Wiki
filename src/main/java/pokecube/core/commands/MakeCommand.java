package pokecube.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeCore;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;

public class MakeCommand
{
    public static String setToArgs(String[] args, IPokemob mob, int index, Vector3 offset)
    {
        return MakeCommand.setToArgs(args, mob, index, offset, true);
    }

    /**
     * @param args
     * @param mob
     * @param command
     * @return owner name for pokemob if needed.
     */
    public static String setToArgs(String[] args, IPokemob mob, int index, Vector3 offset, boolean initLevel)
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
        String ownerName = "";
        if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Making by Arguments: " + index + " " + Arrays.toString(args));

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
                // String[] args1 = val.split(" ");
                final ItemStack itemstack = ItemStack.EMPTY;
                try
                {
                    // TODO make item from name.
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error with item for " + val, e);
                }
                /**
                 * Use this instead of isEmpty() to allow specifically setting
                 * an air itemstack for clearing held items.
                 */
                if (itemstack == ItemStack.EMPTY) PokecubeCore.LOGGER.error("No Item found for " + val);
                else mob.setHeldItem(itemstack);
            }
            else if (arg.equalsIgnoreCase("l"))
            {
                level = Integer.parseInt(val);
                exp = Tools.levelToXp(mob.getExperienceMode(), level);
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
            else if (arg.equalsIgnoreCase("o")) ownerName = val;
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
            else ownerName = args[j];
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

        return ownerName;
    }

    private final List<String> aliases;

    public MakeCommand()
    {
        this.aliases = new ArrayList<>();
        this.aliases.add("pokemake");
    }
    //
    // @Override
    // public void execute(MinecraftServer server, ICommandSource sender,
    // String[] args) throws CommandException
    // {
    // String text = "";
    // ITextComponent message;
    // boolean deobfuscated = PokecubeMod.isDeobfuscated() ||
    // server.isDedicatedServer();
    // boolean commandBlock = !(sender instanceof PlayerEntity);
    // if (deobfuscated || commandBlock)
    // {
    // String name;
    // if (args.length > 0)
    // {
    // int index = 1;
    // PokedexEntry entry = null;
    // try
    // {
    // int id = Integer.parseInt(args[0]);
    // entry = Database.getEntry(id);
    // name = entry.getName();
    // }
    // catch (NumberFormatException e)
    // {
    // name = args[0];
    // if (name.startsWith("\'"))
    // {
    // for (int j = 1; j < args.length; j++)
    // {
    // name += " " + args[j];
    // if (args[j].contains("\'"))
    // {
    // index = j + 1;
    // break;
    // }
    // }
    // }
    // ArrayList<PokedexEntry> entries =
    // Lists.newArrayList(Database.getSortedFormes());
    // Collections.shuffle(entries);
    // Iterator<PokedexEntry> iterator = entries.iterator();
    // if (name.equalsIgnoreCase("random"))
    // {
    // entry = iterator.next();
    // while (entry.legendary || !entry.base)
    // {
    // entry = iterator.next();
    // }
    // }
    // else if (name.equalsIgnoreCase("randomall"))
    // {
    // entry = iterator.next();
    // while (!entry.base)
    // {
    // entry = iterator.next();
    // }
    // }
    // else if (name.equalsIgnoreCase("randomlegend"))
    // {
    // entry = iterator.next();
    // while (!entry.legendary || !entry.base)
    // {
    // entry = iterator.next();
    // }
    // }
    // else entry = Database.getEntry(name);
    // }
    // Entity mob = PokecubeCore.createPokemob(entry,
    // sender.getEntityWorld());
    // if (mob == null)
    // {
    // CommandTools.sendError(sender, "pokecube.command.makeinvalid");
    // return;
    // }
    // IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
    // pokemob.specificSpawnInit();
    // Vector3 offset = Vector3.getNewVector().set(0, 1, 0);
    // String ownerName = setToArgs(args, pokemob, index, offset);
    // GameProfile profile = null;
    // if (ownerName != null && !ownerName.isEmpty())
    // {
    // profile = getProfile(server, ownerName);
    // }
    // if (profile == null && ownerName != null && !ownerName.isEmpty())
    // {
    // PlayerEntity player = getPlayer(server, sender, ownerName);
    // profile = player.getGameProfile();
    // }
    // Vector3 temp = Vector3.getNewVector();
    // if (profile != null)
    // {
    // PokecubeCore.LOGGER.info(Level.INFO, "Creating " +
    // pokemob.getPokedexEntry() + "
    // for " + profile.getName());
    // pokemob.setPokemonOwner(profile.getId());
    // pokemob.setGeneralState(GeneralStates.TAMED, true);
    // }
    // temp.set(sender.getPosition()).addTo(offset);
    // temp.moveEntity(mob);
    // GeneticsManager.initMob(mob);
    // mob.getEntityWorld().spawnEntity(mob);
    // text = TextFormatting.GREEN + "Spawned " +
    // pokemob.getPokemonDisplayName().getFormattedText();
    // message = ITextComponent.Serializer.jsonToComponent("[\"" + text +
    // "\"]");
    // sender.sendMessage(message);
    // return;
    // }
    // CommandTools.sendError(sender, "pokecube.command.makeneedname");
    // return;
    // }
    // CommandTools.sendError(sender, "pokecube.command.makedeny");
    // return;
    // }
    //
    // @Override
    // public List<String> getAliases()
    // {
    // return this.aliases;
    // }
    //
    // @Override
    // public String getName()
    // {
    // return aliases.get(0);
    // }
    //
    // @Override
    // public String getUsage(ICommandSource sender)
    // {
    // return "/" + aliases.get(0) + "<pokemob name/number> <arguments>";
    // }
    //
    // @Override
    // /** Return the required permission level for this command. */
    // public int getRequiredPermissionLevel()
    // {
    // return 2;
    // }
    //
    // @Override
    // public List<String> getTabCompletions(MinecraftServer server,
    // ICommandSource sender, String[] args, BlockPos pos)
    // {
    // List<String> ret = new ArrayList<String>();
    // if (args.length == 1)
    // {
    // String text = args[0];
    // for (PokedexEntry entry : Database.getSortedFormes())
    // {
    // String check = entry.getName().toLowerCase(java.util.Locale.ENGLISH);
    // if (check.startsWith(text.toLowerCase(java.util.Locale.ENGLISH)))
    // {
    // String name = entry.getName();
    // if (name.contains(" "))
    // {
    // name = "\'" + name + "\'";
    // }
    // ret.add(name);
    // }
    // }
    // Collections.sort(ret, new Comparator<String>()
    // {
    // @Override
    // public int compare(String o1, String o2)
    // {
    // if (o1.contains("'") && !o2.contains("'")) return 1;
    // else if (o2.contains("'") && !o1.contains("'")) return -1;
    // return o1.compareToIgnoreCase(o2);
    // }
    // });
    // return getListOfStringsMatchingLastWord(args, ret);
    // }
    // return getListOfStringsMatchingLastWord(args,
    // server.getOnlinePlayerNames());
    // }
}
