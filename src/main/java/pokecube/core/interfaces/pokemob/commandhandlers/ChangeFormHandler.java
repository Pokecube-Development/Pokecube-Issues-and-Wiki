package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
import thut.api.Tracker;

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
        final Level world = mob.getLevel();
        final BlockPos pos = mob.blockPosition();
        final MinecraftServer server = mob.getServer();

        if (pokemob.getGeneralState(GeneralStates.EVOLVING) || server == null || player == null) return;

        final boolean hasRing = !(player instanceof Player) || MegaCapability.canMegaEvolve(player, pokemob);
        if (!hasRing)
        {
            player.sendMessage(new TranslatableComponent("pokecube.mega.noring", pokemob.getDisplayName()),
                    Util.NIL_UUID);
            return;
        }
        final PokedexEntry entry = pokemob.getPokedexEntry();
        final Component oldName = pokemob.getDisplayName();

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
                Component mess = new TranslatableComponent("pokemob.dynamax.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = new TranslatableComponent("pokemob.dynamax.revert", oldName);
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
                    player.sendMessage(new TranslatableComponent("pokemob.dynamax.too_soon", pokemob
                            .getDisplayName()), Util.NIL_UUID);
                    return;
                }

                Component mess = new TranslatableComponent("pokemob.dynamax.command.evolve", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = new TranslatableComponent("pokemob.dynamax.success", oldName);
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
            Component mess = new TranslatableComponent("pokemob.dynamax.command.revert", oldName);
            pokemob.displayMessageToOwner(mess);
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            mess = new TranslatableComponent("pokemob.dynamax.revert", oldName);
            ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess, true);
            return;
        }

        newEntry = pokemob.getPokedexEntry().getMegaEvo(pokemob);
        if (newEntry != null && newEntry.getPokedexNb() == pokemob.getPokedexEntry().getPokedexNb())
        {
            if (pokemob.getPokedexEntry() == newEntry)
            {
                Component mess = new TranslatableComponent("pokemob.megaevolve.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = new TranslatableComponent("pokemob.megaevolve.revert", oldName, new TranslatableComponent(
                        newEntry.getUnlocalizedName()));
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
            else
            {
                Component mess = new TranslatableComponent("pokemob.megaevolve.command.evolve", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = new TranslatableComponent("pokemob.megaevolve.success", oldName, new TranslatableComponent(
                        newEntry.getUnlocalizedName()));
                pokemob.setCombatState(CombatStates.MEGAFORME, true);
                ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
            }
        }
        else if (pokemob.getCombatState(CombatStates.MEGAFORME))
        {
            Component mess = new TranslatableComponent("pokemob.megaevolve.command.revert", oldName);
            pokemob.displayMessageToOwner(mess);
            newEntry = pokemob.getMegaBase();
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            mess = new TranslatableComponent("pokemob.megaevolve.revert", oldName, new TranslatableComponent(
                    newEntry.getUnlocalizedName()));
            ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
        }
        else player.sendMessage(new TranslatableComponent("pokemob.megaevolve.failed", pokemob.getDisplayName()),
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
