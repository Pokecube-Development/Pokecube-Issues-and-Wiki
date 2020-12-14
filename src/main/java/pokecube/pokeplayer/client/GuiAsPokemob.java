package pokecube.pokeplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackEntityHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackNothingHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.utils.Tools;
import pokecube.pokeplayer.data.PokeInfo;
import thut.api.maths.Vector3;
import thut.core.common.handlers.PlayerDataHandler;

public class GuiAsPokemob extends GuiDisplayPokecubeInfo
{

    @Override
    public IPokemob[] getPokemobsToDisplay()
    {
        final PlayerEntity player = Minecraft.getInstance().player;
        final PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        final IPokemob pokemob = info.getPokemob();
        if (pokemob != null) return new IPokemob[] { pokemob };
        return super.getPokemobsToDisplay();
    }

    @Override
    public IPokemob getCurrentPokemob()
    {
        final PlayerEntity player = Minecraft.getInstance().player;
        final PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        final IPokemob pokemob = info.getPokemob();
        if (pokemob != null) return pokemob;
        return super.getCurrentPokemob();
    }

    @Override
    public void pokemobAttack()
    {
        final PlayerEntity player = Minecraft.getInstance().player;
        final PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        final IPokemob pokemob = info.getPokemob();
        if (pokemob == null)
        {
            super.pokemobAttack();
            return;
        }
        if (pokemob.getAttackCooldown() > 0) return;
        final Entity target = Tools.getPointedEntity(player, 32);
        final Vector3 targetLocation = Tools.getPointedLocation(player, 32);
        boolean sameOwner = false;
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob != null) sameOwner = targetMob.getOwner() == player;
        if (pokemob != null)
        {
            if (pokemob.getMove(pokemob.getMoveIndex()) == null) return;
            if (pokemob.getMove(pokemob.getMoveIndex()).equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
            {
                if (!GuiTeleport.instance().getState())
                {
                    GuiTeleport.instance().setState(true);
                    return;
                }
                GuiTeleport.instance().setState(false);
                PacketCommand.sendCommand(pokemob, Command.TELEPORT, new TeleportHandler());
                return;
            }
        }
        if (target != null && !sameOwner && target instanceof LivingEntity) PacketCommand.sendCommand(pokemob,
                Command.ATTACKENTITY, new AttackEntityHandler(target.getEntityId()));
        else if (targetLocation != null) PacketCommand.sendCommand(pokemob, Command.ATTACKLOCATION,
                new AttackLocationHandler(targetLocation));
        else PacketCommand.sendCommand(pokemob, Command.ATTACKNOTHING, new AttackNothingHandler());
    }
}
