package pokecube.core.network.packets;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.RecipePokeseals;
import thut.core.common.network.Packet;

public class PacketTrade extends Packet
{
    public CompoundNBT data = new CompoundNBT();

    public PacketTrade()
    {
    }

    public PacketTrade(final PacketBuffer buf)
    {
        this.data = buf.readCompoundTag();
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final Container cont = player.openContainer;
        if (!(cont instanceof TradeContainer)) return;
        final TradeContainer container = (TradeContainer) cont;
        if (this.data.contains("r"))
        {
            container.tile.confirmed[0] = false;
            container.tile.confirmed[1] = false;
            PokecubeCore.LOGGER.debug("Resetting trade status, users: " + container.tile.users);
            return;
        }
        if (this.data.contains("0"))
        {
            final byte slot = 0;
            container.tile.confirmed[slot] = this.data.getBoolean("0");
        }
        if (this.data.contains("1"))
        {
            final byte slot = 1;
            container.tile.confirmed[slot] = this.data.getBoolean("1");
        }
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final Container cont = player.openContainer;
        if (!(cont instanceof TradeContainer)) return;
        final TradeContainer container = (TradeContainer) cont;
        System.out.println(this.data);
        final InvWrapper inv = (InvWrapper) container.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElse(null);
        if (this.data.contains("r"))
        {
            container.tile.confirmed[0] = false;
            container.tile.confirmed[1] = false;
            PokecubeCore.packets.sendTo(this, player);
            return;
        }

        if (this.data.contains("s"))
        {
            final byte slot = this.data.getByte("s");
            final ItemStack stack = inv.getStackInSlot(slot);

            boolean canInteract = !stack.isEmpty();
            final boolean filled = PokecubeManager.isFilled(stack);
            if (filled) canInteract = PokecubeManager.getOwner(stack).equals(player.getCachedUniqueIdString());

            if (!canInteract)
            {
                // Reset status of selections.
                final PacketTrade trade = new PacketTrade();
                trade.data.putBoolean("r", true);
                container.tile.confirmed[0] = false;
                container.tile.confirmed[1] = false;
                for (final UUID id : container.tile.users)
                {
                    final ServerPlayerEntity user = player.getServer().getPlayerList().getPlayerByUUID(id);
                    if (user != null) PokecubeCore.packets.sendTo(trade, user);
                }
                return;
            }

            container.tile.confirmed[slot] = !container.tile.confirmed[slot];

            final boolean slot0 = container.tile.confirmed[0];
            final boolean slot1 = container.tile.confirmed[1];

            if (slot0 && slot1)
            {
                ItemStack pokecube0 = ItemStack.EMPTY;
                ItemStack pokecube1 = ItemStack.EMPTY;
                boolean toTrade = true;
                boolean pokeseal = false;
                boolean reskin = false;
                ItemStack seal = ItemStack.EMPTY;
                ItemStack skin = ItemStack.EMPTY;
                ItemStack cube = ItemStack.EMPTY;
                int cubeIndex = -1;

                if (PokecubeManager.isFilled(inv.getStackInSlot(0))) pokecube0 = inv.getStackInSlot(0);
                else toTrade = false;
                if (PokecubeManager.isFilled(inv.getStackInSlot(1))) pokecube1 = inv.getStackInSlot(1);
                else toTrade = false;

                /**
                 * We only work on filled cubes, so if none are filled, return
                 * here.
                 */
                if (pokecube0.isEmpty() && pokecube1.isEmpty())
                {
                    // Reset status of selections.
                    final PacketTrade trade = new PacketTrade();
                    trade.data.putBoolean("r", true);
                    container.tile.confirmed[0] = false;
                    container.tile.confirmed[1] = false;
                    for (final UUID id : container.tile.users)
                    {
                        final ServerPlayerEntity user = player.getServer().getPlayerList().getPlayerByUUID(id);
                        if (user != null) PokecubeCore.packets.sendTo(trade, user);
                    }
                    return;
                }

                /**
                 * Check if we are applying a pokeseal first.
                 */
                if (!toTrade)
                {
                    if (pokecube0.isEmpty())
                    {
                        pokeseal = (seal = inv.getStackInSlot(0)).getItem() == PokecubeItems.getEmptyCube(
                                PokecubeBehavior.POKESEAL);
                        cube = pokecube1;
                    }
                    if (pokecube1.isEmpty())
                    {
                        pokeseal = (seal = inv.getStackInSlot(1)).getItem() == PokecubeItems.getEmptyCube(
                                PokecubeBehavior.POKESEAL);
                        cube = pokecube0;
                    }
                }
                // Otherwise check if we are trying to re-skin the cube.
                if (!(toTrade || pokeseal))
                {
                    if (pokecube0.isEmpty())
                    {
                        reskin = PokecubeItems.getCubeId(skin = inv.getStackInSlot(0)) != null;
                        cube = pokecube1;
                        cubeIndex = 1;
                    }
                    if (pokecube1.isEmpty())
                    {
                        reskin = PokecubeItems.getCubeId(skin = inv.getStackInSlot(1)) != null;
                        cube = pokecube0;
                        cubeIndex = 0;
                    }
                }

                trade:
                if (toTrade)
                {
                    final IPokemob pokemob0 = PokecubeManager.itemToPokemob(pokecube0, player.getEntityWorld());
                    final IPokemob pokemob1 = PokecubeManager.itemToPokemob(pokecube1, player.getEntityWorld());
                    final UUID owner0 = pokemob0.getOwnerId();
                    final UUID owner1 = pokemob1.getOwnerId();
                    if (owner0 != null && owner0.equals(owner1)) break trade;
                    pokemob0.setOwner(owner1);
                    pokemob1.setOwner(owner0);
                    pokemob0.setTraded(true);
                    pokemob1.setTraded(true);
                    inv.setStackInSlot(0, PokecubeManager.pokemobToItem(pokemob0));
                    inv.setStackInSlot(1, PokecubeManager.pokemobToItem(pokemob1));
                }
                else if (pokeseal) RecipePokeseals.process(seal, cube);
                else if (reskin)
                {
                    final IPokemob pokemob = PokecubeManager.itemToPokemob(cube, player.getEntityWorld());
                    pokemob.setPokecube(skin);
                    inv.setStackInSlot(cubeIndex, PokecubeManager.pokemobToItem(pokemob));
                    inv.setStackInSlot(cubeIndex == 0 ? 1 : 0, ItemStack.EMPTY);
                }

                // Reset trade gui.
                final PacketTrade trade = new PacketTrade();
                trade.data.putBoolean("r", true);
                container.tile.confirmed[0] = false;
                container.tile.confirmed[1] = false;
                for (final UUID id : container.tile.users)
                {
                    final ServerPlayerEntity user = player.getServer().getPlayerList().getPlayerByUUID(id);
                    if (user != null) PokecubeCore.packets.sendTo(trade, user);
                    container.clearContainer(user, user.getEntityWorld(), inv.getInv());
                }
                return;

            }

            System.out.println(slot + " " + slot0 + " " + slot1 + " " + stack);
            final PacketTrade trade = new PacketTrade();
            trade.data.putBoolean("0", slot0);
            trade.data.putBoolean("1", slot1);
            PokecubeCore.packets.sendTo(trade, player);
        }

    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeCompoundTag(this.data);
    }
}
