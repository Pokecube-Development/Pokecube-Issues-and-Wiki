package pokecube.core.handlers.events;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.pokemob.CaptureEvent;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pc.PCInventory;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.network.packets.PacketPC;
import pokecube.core.utils.PokemobTracker;

public class PCEventsHandler
{
    public static final UUID THUTMOSE = UUID.fromString("f1dacdfd-42d6-4af0-8234-b2f180ecd6a8");

    public static void register()
    {
        // This actually adds the cube to PC when sent, it can be prevented by
        // cancelling the event first, hence the lowest priority.
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, PCEventsHandler::onSendToPC);
        // This sends the pokecube to PC if the player captures on without
        // enough free inventory space. Otherwise it adds it to their inventory.
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, PCEventsHandler::onCapturePost);

        // This handler deals with changing the name of the PC from "Someone's
        // PC" to "Thutmose's PC" when the owner logs in. This is in reference
        // to "Bill's PC" in the pokemon games.
        MinecraftForge.EVENT_BUS.addListener(PCEventsHandler::onPlayerJoinWorld);
        // This syncs initial data to the player, like their PC box names, etc.
        MinecraftForge.EVENT_BUS.addListener(PCEventsHandler::onPlayerLogin);
        // This handles sending the pokecube to their PC if they had no room.
        MinecraftForge.EVENT_BUS.addListener(PCEventsHandler::onItemPickup);
        // This sends the pokecube to PC if tossed with Q or similar.
        MinecraftForge.EVENT_BUS.addListener(PCEventsHandler::onItemTossed);
        // This sends to PC if the pokecube item tries to despawn.
        MinecraftForge.EVENT_BUS.addListener(PCEventsHandler::onItemExpire);

        // This recalls the player's following pokemobs if they die.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, PCEventsHandler::onPlayerDeath);
        // This removes their pokecubes and important items from the drops list,
        // and instead sends them to PC.
        MinecraftForge.EVENT_BUS.addListener(PCEventsHandler::onPlayerDrops);
    }

    /**
     * If player tosses a pokecube item, it will be send to PC instead.
     *
     * @param evt
     */
    private static void onSendToPC(final pokecube.core.events.PCEvent evt)
    {
        if (evt.owner == null) return;
        if (PokecubeManager.isFilled(evt.toPC))
        {
            PCInventory.addPokecubeToPC(evt.toPC, evt.world);
            evt.setCanceled(true);
        }
    }

    /**
     * Used for changing name from "Someone's PC" to "Thutmose's PC". This is
     * done as all of the PC systems are named after whoever made them. See
     * Bill's PC for an example.
     *
     * @param evt
     */
    private static void onPlayerJoinWorld(final EntityJoinWorldEvent evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayer)) return;

        final ServerPlayer player = (ServerPlayer) evt.getEntity();

        if (player.getUUID().equals(PCEventsHandler.THUTMOSE)) for (final ServerPlayer entity : player.getServer()
                .getPlayerList().getPlayers())
        {
            final PacketPC packet = new PacketPC(PacketPC.PCINIT, entity.getUUID());
            packet.data.putBoolean("O", true);
            PokecubeCore.packets.sendTo(packet, entity);
        }
    }

    /**
     * Sends the packet with the player's PC data to that player.
     *
     * @param evt
     */
    private static void onPlayerLogin(final PlayerLoggedInEvent evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer)) return;
        PacketPC.sendInitialSyncMessage(evt.getPlayer());
    }

    /**
     * This sends pokecube to PC if the player has a full inventory and tries
     * to pick up a pokecube.
     *
     * @param evt
     */
    private static void onItemPickup(final EntityItemPickupEvent evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer)) return;
        final Inventory inv = evt.getPlayer().getInventory();
        final int num = inv.getFreeSlot();
        if (!PokecubeManager.isFilled(evt.getItem().getItem())) return;
        final String owner = PokecubeManager.getOwner(evt.getItem().getItem());
        if (evt.getPlayer().getStringUUID().equals(owner))
        {
            if (num == -1)
            {
                PCInventory.addPokecubeToPC(evt.getItem().getItem(), evt.getPlayer().getLevel());
                evt.getItem().discard();
            }
        }
        else
        {
            PCInventory.addPokecubeToPC(evt.getItem().getItem(), evt.getPlayer().getLevel());
            evt.getItem().discard();
            evt.setCanceled(true);
        }
    }

    /**
     * If player tosses a pokecube item, it will be send to PC instead.
     *
     * @param evt
     */
    private static void onItemTossed(final ItemTossEvent evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer)) return;
        if (PokecubeManager.isFilled(evt.getEntityItem().getItem()))
        {
            if (PokecubeManager.getOwner(evt.getEntityItem().getItem()).isEmpty()) return;
            PCInventory.addPokecubeToPC(evt.getEntityItem().getItem(), evt.getEntity().getLevel());
            evt.getEntity().discard();
            evt.setCanceled(true);
        }
    }

    /**
     * Attempts to send the pokecube to the PC whenever the ItemEntity it is in
     * expires. This prevents losing pokemobs if the cube is somehow left in the
     * world.
     *
     * @param evt
     */
    private static void onItemExpire(final ItemExpireEvent evt)
    {
        if (PokecubeManager.isFilled(evt.getEntityItem().getItem()))
        {
            if (evt.getEntity().getLevel().isClientSide) return;
            PCInventory.addPokecubeToPC(evt.getEntityItem().getItem(), evt.getEntity().getLevel());
        }
    }

    // @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    private static void onPlayerDeath(final LivingDeathEvent evt)
    {
        if (evt.getEntity() instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) evt.getEntity();
            EventsHandler.recallAllPokemobsExcluding(player, null, false);
        }
    }

    /**
     * Tries to send pokecubes to PC when player dies.
     *
     * @param evt
     */
    private static void onPlayerDrops(final LivingDropsEvent evt)
    {
        if (!(evt.getEntity() instanceof Player) || !PokecubeCore.getConfig().pcOnDrop) return;
        if (evt.getEntity().getLevel().isClientSide) return;
        final UUID id = evt.getEntity().getUUID();
        final List<ItemEntity> toRemove = Lists.newArrayList();
        for (final ItemEntity item : evt.getDrops())
            if (item != null && item.getItem() != null && PCContainer.isItemValid(item.getItem()))
            {
                PCInventory.addStackToPC(id, item.getItem().copy(), evt.getEntity().getLevel());
                toRemove.add(item);
            }
        evt.getDrops().removeAll(toRemove);
    }

    /**
     * Tries to send pokecube to PC if player has no room in inventory for it.
     * Otherwise, will add pokecube to player's inventory.
     *
     * @param evt
     */
    private static void onCapturePost(final CaptureEvent.Post evt)
    {
        // Case for things like snag cubes
        if (evt.getCaught() == null)
        {
            evt.pokecube.spawnAtLocation(evt.getFilledCube(), 0.5f);
            return;
        }
        final Entity catcher = evt.getCaught().getOwner();
        if (evt.getCaught().isShadow()) return;
        if (catcher instanceof ServerPlayer && PokecubeManager.isFilled(evt.getFilledCube()))
        {
            final Player player = (Player) catcher;
            if (player instanceof FakePlayer) return;
            // Cancel it to stop the cube from processing itself.
            evt.setCanceled(true);

            final Inventory inv = player.getInventory();
            final UUID id = UUID.fromString(PokecubeManager.getOwner(evt.getFilledCube()));
            final PCInventory pc = PCInventory.getPC(id);
            final int num = inv.getFreeSlot();
            if (evt.getFilledCube() == null || pc == null) System.err.println("Cube is null");
            else if (num == -1 || pc.autoToPC || !player.isAlive() || player.getHealth() <= 0) PCInventory
                    .addPokecubeToPC(evt.getFilledCube(), catcher.getLevel());
            else
            {
                player.getInventory().add(evt.getFilledCube());
                if (player instanceof ServerPlayer)
                {
                    player.inventoryMenu.sendAllDataToRemote();
                    ((ServerPlayer) player).resetSentInfo();
                }
            }

            // Apply the same code that StatsHandler does, as it does not
            // get the cancelled event.
            final ResourceLocation cube_id = PokecubeItems.getCubeId(evt.getFilledCube());
            if (IPokecube.PokecubeBehavior.BEHAVIORS.get().containsKey(cube_id))
            {
                final PokecubeBehavior cube = IPokecube.PokecubeBehavior.BEHAVIORS.get().getValue(cube_id);
                cube.onPostCapture(evt);
            }
            StatsCollector.addCapture(evt.getCaught());
        }
    }

    public static void recallAll(final List<Entity> mobs, final boolean cubesToPC)
    {
        if (mobs.isEmpty()) return;
        if (!(mobs.get(0).getLevel() instanceof ServerLevel)) return;
        final ServerLevel world = (ServerLevel) mobs.get(0).getLevel();
        EventsHandler.Schedule(world, w ->
        {
            for (final Entity o : mobs)
            {
                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(o);
                if (!o.isAddedToWorld()) continue;
                if (pokemob != null) pokemob.onRecall();
                else if (o instanceof EntityPokecube)
                {
                    final EntityPokecube mob = (EntityPokecube) o;
                    if (cubesToPC) PCInventory.addPokecubeToPC(mob.getItem(), mob.getLevel());
                    else
                    {
                        final LivingEntity out = SendOutManager.sendOut(mob, true);
                        final IPokemob poke = CapabilityPokemob.getPokemobFor(out);
                        if (poke != null) poke.onRecall();
                    }
                    mob.setRemoved(RemovalReason.DISCARDED);
                }
            }
            return true;
        });
    }

    /**
     * Gets a list of all pokemobs out of their cube belonging to the player in
     * the player's current world.
     *
     * @param player
     * @return
     */
    public static List<Entity> getOutMobs(final LivingEntity player, final boolean includeStay)
    {
        if (player == null) return Collections.emptyList();
        return PokemobTracker.getMobs(player, c -> EventsHandler.validRecall(player, c, null, false, includeStay));
    }
}
