package pokecube.core.interfaces.pokemob.commandhandlers;

import thut.api.Tracker;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;

public class ChangeFormHandler extends DefaultHandler
{
    public ChangeFormHandler()
    {
    }

    @Override
    public void handleCommand(final IPokemob pokemob) throws Exception
    {
        final LivingEntity player = pokemob.getOwner();

        final Entity mob = pokemob.getEntity();
        final World world = mob.getCommandSenderWorld();
        final BlockPos pos = mob.blockPosition();
        final MinecraftServer server = mob.getServer();

        if (pokemob.getGeneralState(GeneralStates.EVOLVING) || server == null || player == null) return;

        final boolean hasRing = !(player instanceof PlayerEntity) || MegaCapability.canMegaEvolve(player, pokemob);
        if (!hasRing)
        {
            player.sendMessage(new TranslationTextComponent("pokecube.mega.noring", pokemob.getDisplayName()),
                    Util.NIL_UUID);
            return;
        }
        final PokedexEntry entry = pokemob.getPokedexEntry();
        final ITextComponent oldName = pokemob.getDisplayName();

        // Check dynamax/gigantamax first.
        final ForbidReason reason = SpawnHandler.getNoSpawnReason(world, pos);

        boolean gigant = pokemob.getCombatState(CombatStates.GIGANTAMAX);
        boolean isDyna = pokemob.getCombatState(CombatStates.DYNAMAX);
        if (reason == MaxTile.MAXSPOT)
        {
            isDyna = isDyna || entry.isMega();
            PokedexEntry newEntry = entry.isMega() ? pokemob.getMegaBase() : entry;

            if (gigant && !isDyna)
            {
                newEntry = Database.getEntry(newEntry.getTrimmedName() + "_gigantamax");
                if (newEntry == null) gigant = false;
            }

            if (isDyna)
            {
                ITextComponent mess = new TranslationTextComponent("pokemob.dynamax.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = new TranslationTextComponent("pokemob.dynamax.revert", oldName);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
                return;
            }
            else
            {
                final long dynatime = PokecubePlayerDataHandler.getCustomDataTag(player.getUUID()).getLong(
                        "pokecube:dynatime");
                final long time = Tracker.instance().getTick();
                final long dynaagain = dynatime + PokecubeCore.getConfig().dynamax_cooldown;
                if (dynatime != 0 && time < dynaagain)
                {
                    player.sendMessage(new TranslationTextComponent("pokemob.dynamax.too_soon", pokemob
                            .getDisplayName()), Util.NIL_UUID);
                    return;
                }

                ITextComponent mess = new TranslationTextComponent("pokemob.dynamax.command.evolve", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = new TranslationTextComponent("pokemob.dynamax.success", oldName);
                if (gigant) pokemob.setCombatState(CombatStates.MEGAFORME, true);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
                return;
            }
        }

        PokedexEntry newEntry = entry.isMega() ? pokemob.getMegaBase() : entry;
        if (gigant && !isDyna)
        {
            newEntry = Database.getEntry(newEntry.getTrimmedName() + "_gigantamax");
            if (newEntry == null) gigant = false;
        }
        if (isDyna || gigant)
        {
            ITextComponent mess = new TranslationTextComponent("pokemob.dynamax.command.revert", oldName);
            pokemob.displayMessageToOwner(mess);
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            mess = new TranslationTextComponent("pokemob.dynamax.revert", oldName);
            ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
            return;
        }

        newEntry = pokemob.getPokedexEntry().getEvo(pokemob);
        if (newEntry != null && newEntry.getPokedexNb() == pokemob.getPokedexEntry().getPokedexNb())
        {
            if (pokemob.getPokedexEntry() == newEntry)
            {
                ITextComponent mess = new TranslationTextComponent("pokemob.megaevolve.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = new TranslationTextComponent("pokemob.megaevolve.revert", oldName, new TranslationTextComponent(
                        newEntry.getUnlocalizedName()));
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
            else
            {
                ITextComponent mess = new TranslationTextComponent("pokemob.megaevolve.command.evolve", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = new TranslationTextComponent("pokemob.megaevolve.success", oldName, new TranslationTextComponent(
                        newEntry.getUnlocalizedName()));
                pokemob.setCombatState(CombatStates.MEGAFORME, true);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
        }
        else if (pokemob.getCombatState(CombatStates.MEGAFORME))
        {
            ITextComponent mess = new TranslationTextComponent("pokemob.megaevolve.command.revert", oldName);
            pokemob.displayMessageToOwner(mess);
            newEntry = pokemob.getMegaBase();
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            mess = new TranslationTextComponent("pokemob.megaevolve.revert", oldName, new TranslationTextComponent(
                    newEntry.getUnlocalizedName()));
            ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
        }
        else player.sendMessage(new TranslationTextComponent("pokemob.megaevolve.failed", pokemob.getDisplayName()),
                Util.NIL_UUID);
    }

    @Override
    public void readFromBuf(final ByteBuf buf)
    {
        super.readFromBuf(buf);
    }

    @Override
    public void writeToBuf(final ByteBuf buf)
    {
        super.writeToBuf(buf);
    }

}
