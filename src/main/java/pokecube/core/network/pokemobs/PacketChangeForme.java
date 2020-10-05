package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
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
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.megastuff.MegaCapability;
import thut.core.common.network.Packet;

public class PacketChangeForme extends Packet
{
    public static void sendPacketToServer(final Entity mob, final PokedexEntry forme)
    {
        final PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendPacketToTracking(final Entity mob, final PokedexEntry forme)
    {
        final PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeCore.packets.sendToTracking(packet, mob);
    }

    int entityId;

    PokedexEntry forme;

    public PacketChangeForme()
    {
    }

    public PacketChangeForme(final PacketBuffer buffer)
    {
        this.entityId = buffer.readInt();
        this.forme = Database.getEntry(buffer.readString(20));
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.setPokedexEntry(this.forme);
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final World world = player.getEntityWorld();
        final BlockPos pos = player.getPosition();
        final Entity mob = PokecubeCore.getEntityProvider().getEntity(world, this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        if (pokemob.getGeneralState(GeneralStates.EVOLVING)) return;
        final boolean hasRing = MegaCapability.canMegaEvolve(player, pokemob);
        if (!hasRing)
        {
            player.sendMessage(new TranslationTextComponent("pokecube.mega.noring", pokemob.getDisplayName()), Util.DUMMY_UUID);
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
            isDyna = isDyna || entry.isMega;
            PokedexEntry newEntry = entry.isMega ? pokemob.getMegaBase() : entry;

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
                final long dynatime = PokecubePlayerDataHandler.getCustomDataTag(player.getUniqueID()).getLong(
                        "pokecube:dynatime");
                final long time = player.getServer().getWorld(World.OVERWORLD).getGameTime();
                final long dynaagain = dynatime + PokecubeCore.getConfig().dynamax_cooldown;
                if (dynatime != 0 && time < dynaagain)
                {
                    player.sendMessage(new TranslationTextComponent("pokemob.dynamax.too_soon", pokemob
                            .getDisplayName()), Util.DUMMY_UUID);
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

        PokedexEntry newEntry = entry.isMega ? pokemob.getMegaBase() : entry;
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
        else player.sendMessage(new TranslationTextComponent("pokemob.megaevolve.failed", pokemob.getDisplayName()), Util.DUMMY_UUID);
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(this.entityId);
        if (this.forme != null) buffer.writeString(this.forme.getName());
        else buffer.writeString("");
    }

}
