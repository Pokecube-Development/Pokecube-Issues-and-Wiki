package pokecube.core.commands;

import java.util.Arrays;
import java.util.Locale;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.init.PokemakeArgumentEvent;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.arguments.PokemobArgument;
import pokecube.core.commands.arguments.PokemobArgument.PokemobInput;
import pokecube.core.impl.PokecubeMod;
import thut.api.ThutCaps;
import thut.api.entity.IMobColourable;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.commands.CommandTools;

public class Pokemake2
{
    public static IPokemob initFromNBT(IPokemob pokemob, CompoundTag nbt)
    {
        return initFromNBT(pokemob, pokemob.getEntity().position(), nbt);
    }

    public static IPokemob initFromNBT(IPokemob pokemob, Vec3 pos, CompoundTag nbt)
    {
        if (nbt == null) nbt = new CompoundTag();
        var mob = pokemob.getEntity();

        // first apply any event stuff
        var event = new PokemakeArgumentEvent(pokemob, pos, nbt);
        PokecubeAPI.POKEMOB_BUS.post(event);
        pos = event.getPos();
        nbt = event.getNbt();
        pokemob = event.getPokemob();

        // First process any custom nbt tags
        if (nbt.getTagType("tag") == Tag.TAG_COMPOUND)
        {
            CompoundTag tag = nbt.getCompound("tag");
            mob.deserializeNBT(tag);
        }

        mob.moveTo(pos.x(), pos.y(), pos.z());

        // Process moves
        if (nbt.contains("moves"))
        {
            var type = nbt.getTagType("moves");
            if (type == Tag.TAG_LIST)
            {
                ListTag moves = nbt.getList("moves", Tag.TAG_STRING);
                for (int i = 0; i < moves.size(); i++)
                {
                    pokemob.learn(moves.getString(i));
                }
            }
            else if (type == Tag.TAG_STRING)
            {
                String move = nbt.getString("moves");
                pokemob.learn(move);
            }
        }
        // Also handle single move case
        if (nbt.contains("move"))
        {
            String move = nbt.getString("move");
            pokemob.learn(move);
        }

        // Process Nature
        String _nature = nbt.getString("nature");
        if (!_nature.isBlank())
        {
            Nature nature = Nature.valueOf(_nature.toUpperCase(Locale.ENGLISH));
            if (nature != null) pokemob.setNature(nature);
        }

        // Process size
        float size = nbt.getFloat("size");
        if (size > 0) pokemob.setSize(size);

        // Process sex
        String sexe = nbt.getString("sex");
        if (!sexe.isBlank())
        {
            byte gender = -3;
            if (sexe.equalsIgnoreCase("female")) gender = IPokemob.FEMALE;
            if (sexe.equalsIgnoreCase("male")) gender = IPokemob.MALE;
            if (gender != -3) pokemob.setSexe(gender);
        }

        // Process shiny flag
        boolean shiny = nbt.getBoolean("shiny");
        if (nbt.contains("shiny")) pokemob.setShiny(shiny);

        // Process colours
        int[] colours = nbt.getIntArray("colour");
        colour:
        if (colours.length >= 3)
        {
            IMobColourable coloured = ThutCaps.getColourable(mob);
            if (coloured == null) break colour;
            int r = colours[0];
            int g = colours[1];
            int b = colours[2];
            int a = colours.length > 3 ? colours[3] : 255;
            coloured.setRGBA(r, g, b, a);
        }

        // Process ability
        if (nbt.contains("ability"))
        {
            var type = nbt.getTagType("ability");
            if (type == Tag.TAG_STRING)
            {
                String ability = nbt.getString("ability");
                if (!ability.isBlank())
                {
                    if (AbilityManager.abilityExists(ability))
                        pokemob.setAbilityRaw(AbilityManager.getAbility(ability));
                }
            }
            else if (type == Tag.TAG_INT)
            {
                pokemob.setAbilityIndex(nbt.getInt("ability"));
            }
        }

        // Process nickname
        String name = nbt.getString("name");
        if (!name.isBlank())
        {
            pokemob.setPokemonNickname(name);
            pokemob.setOriginalOwnerUUID(PokecubeMod.fakeUUID);
        }

        // Handle IVs
        if (nbt.contains("ivs"))
        {
            final byte[] ivs = new byte[6];
            var type = nbt.getTagType("ivs");
            if (type == Tag.TAG_INT)
            {
                byte iv = (byte) nbt.getInt("ivs");
                Arrays.fill(ivs, iv);
            }
            else if (type == Tag.TAG_INT_ARRAY)
            {
                int[] read = nbt.getIntArray("ivs");
                if (read.length == 6) for (int i = 0; i < 6; i++) ivs[i] = (byte) read[i];
            }
            pokemob.setIVs(ivs);
        }

        // Process if wild-like first
        boolean asWild = nbt.getBoolean("wild");

        // Then process level
        if (nbt.getTagType("level") == Tag.TAG_INT)
        {
            int level = nbt.getInt("level");
            int exp = Tools.levelToXp(pokemob.getExperienceMode(), level);
            if (asWild) pokemob = pokemob.setForSpawn(exp);
            else
            {
                pokemob = pokemob.setExp(exp, false);
                pokemob = pokemob.levelUp(level);
            }
        }

        return pokemob;
    }

    private static int execute(CommandSourceStack source, PokemobInput entry, ServerPlayer owner, Vec3 pos)
    {
        Entity mob = PokecubeCore.createPokemob(entry.entry, source.getLevel());
        if (mob == null)
        {
            CommandTools.sendError(source, "pokecube.command.makeinvalid");
            return 1;
        }
        IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        CompoundTag tag = null;
        if (entry.nbt instanceof CompoundTag nbt) tag = nbt;
        initFromNBT(pokemob, pos, tag);
        pokemob.spawnInit();
        mob.getLevel().addFreshEntity(mob);
        if (owner != null) pokemob.setOwner(owner);
        return 0;
    }

    public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
    {
        final String perm = "command.pokemake";
        // Normal pokemake
        PermNodes.registerBooleanNode(PokecubeCore.MODID, perm, DefaultPermissionLevel.OP,
                "Is the player allowed to use /pokemake");

        LiteralArgumentBuilder<CommandSourceStack> command;
        // Set a permission
        command = Commands.literal("pokemake").requires(cs -> CommandTools.hasPerm(cs, perm));
        // Plain command, no args besides name.
        command = command.then(Commands.argument("mob", PokemobArgument.pokemob())
                .suggests(PokemobArgument.SUMMONABLE_ENTITIES).executes(ctx ->
                {
                    return execute(ctx.getSource(), PokemobArgument.getEntry(ctx, "mob"), null,
                            ctx.getSource().getPosition());
                }).then(Commands.argument("pos", Vec3Argument.vec3()).executes((ctx) -> {
                    return execute(ctx.getSource(), PokemobArgument.getEntry(ctx, "mob"),
                            EntityArgument.getPlayer(ctx, "owner"), Vec3Argument.getVec3(ctx, "pos"));
                }).then(Commands.argument("owner", EntityArgument.player()).executes((ctx) -> {
                    return execute(ctx.getSource(), PokemobArgument.getEntry(ctx, "mob"),
                            EntityArgument.getPlayer(ctx, "owner"), ctx.getSource().getPosition());
                }))));
        commandDispatcher.register(command);

    }
}
