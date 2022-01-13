package pokecube.core.network.packets;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiChooseFirstPokemob;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.StarterEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.core.common.network.Packet;

public class PacketChoose extends Packet
{
    private static class GuiOpener
    {
        final PokedexEntry[] starters;
        final boolean        special;
        final boolean        pick;

        public GuiOpener(final PokedexEntry[] starters, final boolean special, final boolean pick)
        {
            this.special = special;
            this.starters = starters;
            this.pick = pick;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public void tick(final ClientTickEvent event)
        {
            pokecube.core.client.gui.GuiChooseFirstPokemob.special = this.special;
            pokecube.core.client.gui.GuiChooseFirstPokemob.pick = this.pick;
            pokecube.core.client.gui.GuiChooseFirstPokemob.starters = this.starters;
            net.minecraft.client.Minecraft.getInstance().setScreen(new GuiChooseFirstPokemob(this.starters));
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public static final byte OPENGUI = 0;
    public static final byte CHOOSE  = 1;

    public static PacketChoose createOpenPacket(final boolean special, final boolean pick, final PokedexEntry... starts)
    {
        final PacketChoose packet = new PacketChoose(PacketChoose.OPENGUI);
        packet.data.putBoolean("C", true);
        packet.data.putBoolean("S", special);
        packet.data.putBoolean("P", pick);
        final ListTag starters = new ListTag();
        for (final PokedexEntry e : starts)
            starters.add(StringTag.valueOf(e.getTrimmedName()));
        packet.data.put("L", starters);
        return packet;
    }

    byte               message;
    public CompoundTag data = new CompoundTag();

    public PacketChoose()
    {
    }

    public PacketChoose(final byte message)
    {
        this.message = message;
    }

    public PacketChoose(final FriendlyByteBuf buf)
    {
        this.message = buf.readByte();
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        this.data = buffer.readNbt();
    }

    @Override
    public void handleClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        if (player == null) throw new NullPointerException("Null Player while recieving starter packet");
        final boolean openGui = this.data.getBoolean("C");
        if (openGui)
        {
            final boolean special = this.data.getBoolean("S");
            final boolean pick = this.data.getBoolean("P");
            final ArrayList<PokedexEntry> starters = new ArrayList<>();
            final ListTag starterList = this.data.getList("L", 8);
            for (int i = 0; i < starterList.size(); i++)
            {
                final PokedexEntry entry = Database.getEntry(starterList.getString(i));
                if (entry != null) starters.add(entry);
            }
            new GuiOpener(starters.toArray(new PokedexEntry[0]), special, pick);
        }
        else PokecubeSerializer.getInstance(false).setHasStarter(player, this.data.getBoolean("H"));
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        /** Ignore this packet if the player already has a starter. */
        if (PokecubeSerializer.getInstance().hasStarter(player)) return;
        // Fire pre event to deny starters from being processed.
        final StarterEvent.Pre pre = new StarterEvent.Pre(player);
        MinecraftForge.EVENT_BUS.post(pre);
        if (pre.isCanceled()) return;
        final String entryName = this.data.getString("N");
        final PokedexEntry entry = Database.getEntry(entryName);
        // Did they also get contributor stuff.
        final List<ItemStack> items = Lists.newArrayList();
        // Copy main list from database.
        for (final ItemStack stack : Database.starterPack)
            items.add(stack.copy());

        // No Custom Starter. just gets this
        final ItemStack pokemobItemstack = PokecubeSerializer.getInstance().starter(entry, player);
        items.add(pokemobItemstack);

        // Fire pick event to add new starters or items
        final StarterEvent.Pick pick = new StarterEvent.Pick(player, items, entry);
        MinecraftForge.EVENT_BUS.post(pick);
        /**
         * If canceled, assume items were not needed, or canceller handled
         * giving them.
         */
        if (pick.isCanceled()) return;
        /** Update itemlist from the pick event. */
        items.clear();
        items.addAll(pick.starterPack);
        for (final ItemStack e : items)
        {
            if (e.isEmpty()) continue;
            /**
             * Run this before tools.give, as that invalidates the
             * itemstack.
             */
            if (PokecubeManager.isFilled(e))
            {
                final IPokemob pokemob = PokecubeManager.itemToPokemob(e, player.getLevel());
                /** First pokemob advancement on getting starter. */
                if (pokemob != null && pokemob.getPokedexEntry() == entry) StatsCollector.addCapture(pokemob);
            }
            Tools.giveItem(player, e);
        }
        /** Set starter status to prevent player getting more starters. */
        PokecubeSerializer.getInstance().setHasStarter(player);
        PokecubeSerializer.getInstance().save();

        // Send Packt to client to notifiy about having a starter now.
        final PacketChoose packet = new PacketChoose(PacketChoose.OPENGUI);
        packet.data.putBoolean("C", false);
        packet.data.putBoolean("H", true);
        PokecubeCore.packets.sendTo(packet, player);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeByte(this.message);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeNbt(this.data);
    }

}
