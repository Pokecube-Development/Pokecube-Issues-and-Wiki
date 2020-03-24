package pokecube.core.handlers.events;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
import pokecube.core.inventory.pc.PCSaveHandler;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.network.packets.PacketPC;
import thut.core.common.ThutCore;

public class PCEventsHandler
{
    public static final UUID THUTMOSE = UUID.fromString("f1dacdfd-42d6-4af0-8234-b2f180ecd6a8");

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
        return ((ServerWorld) player.getEntityWorld()).getEntities(null, c -> EventsHandler.validRecall(player, c, null,
                false, includeStay));
    }

    /**
     * If player tosses a pokecube item, it will be send to PC instead.
     *
     * @param evt
     */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void PCEvent(final pokecube.core.events.PCEvent evt)
    {
        if (evt.owner == null || evt.owner.getEntityWorld().isRemote) return;
        if (PokecubeManager.isFilled(evt.toPC))
        {
            PCInventory.addPokecubeToPC(evt.toPC, evt.owner.getEntityWorld());
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
    @SubscribeEvent
    public static void PCLoggin(final EntityJoinWorldEvent evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayerEntity)) return;

        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getEntity();

        if (player.getUniqueID().equals(PCEventsHandler.THUTMOSE)) for (final ServerPlayerEntity entity : player
                .getServer().getPlayerList().getPlayers())
        {
            final PacketPC packet = new PacketPC(PacketPC.PCINIT, entity.getUniqueID());
            packet.data.putBoolean("O", true);
            PokecubeCore.packets.sendTo(packet, entity);
        }
    }

    /**
     * Sends the packet with the player's PC data to that player.
     *
     * @param evt
     */
    @SubscribeEvent
    public static void PlayerLoggin(final PlayerLoggedInEvent evt)
    {
        final PlayerEntity PlayerEntity = evt.getPlayer();

        if (PlayerEntity.getUniqueID().equals(PCEventsHandler.THUTMOSE)) PCSaveHandler
                .getInstance().seenPCCreator = true;
        if (ThutCore.proxy.isClientSide()) return;
        PacketPC.sendInitialSyncMessage(evt.getPlayer());

    }

    /**
     * This sends pokecube to PC if the player has a full inventory and tries
     * to pick up a pokecube.
     *
     * @param evt
     */
    @SubscribeEvent
    public static void playerPickupItem(final EntityItemPickupEvent evt)
    {
        if (evt.getItem().getEntityWorld().isRemote) return;
        final PlayerInventory inv = evt.getPlayer().inventory;
        final int num = inv.getFirstEmptyStack();
        if (!PokecubeManager.isFilled(evt.getItem().getItem())) return;
        final String owner = PokecubeManager.getOwner(evt.getItem().getItem());
        if (evt.getPlayer().getCachedUniqueIdString().equals(owner))
        {
            if (num == -1)
            {
                PCInventory.addPokecubeToPC(evt.getItem().getItem(), evt.getPlayer().getEntityWorld());
                evt.getItem().remove();
            }
        }
        else
        {
            PCInventory.addPokecubeToPC(evt.getItem().getItem(), evt.getPlayer().getEntityWorld());
            evt.getItem().remove();
            evt.setCanceled(true);
        }
    }

    /**
     * If player tosses a pokecube item, it will be send to PC instead.
     *
     * @param evt
     */
    @SubscribeEvent
    public static void playerTossPokecubeToPC(final ItemTossEvent evt)
    {
        if (evt.getEntity().getEntityWorld().isRemote) return;
        if (PokecubeManager.isFilled(evt.getEntityItem().getItem()))
        {
            PCInventory.addPokecubeToPC(evt.getEntityItem().getItem(), evt.getEntity().getEntityWorld());
            evt.getEntity().remove();
            evt.setCanceled(true);
        }
    }

    public static void recallAll(final List<Entity> mobs, final boolean cubesToPC)
    {
        for (final Entity o : mobs)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(o);
            if (pokemob != null) pokemob.onRecall();
            else if (o instanceof EntityPokecube)
            {
                final EntityPokecube mob = (EntityPokecube) o;
                if (cubesToPC) PCInventory.addPokecubeToPC(mob.getItem(), mob.getEntityWorld());
                else
                {
                    final LivingEntity out = SendOutManager.sendOut(mob, true);
                    final IPokemob poke = CapabilityPokemob.getPokemobFor(out);
                    if (poke != null) poke.onRecall();
                }
                mob.remove();
            }
        }
    }

    /**
     * Attempts to send the pokecube to the PC whenever the ItemEntity it is in
     * expires. This prevents losing pokemobs if the cube is somehow left in the
     * world.
     *
     * @param evt
     */
    @SubscribeEvent
    public static void sendPokemobToPCOnItemExpiration(final ItemExpireEvent evt)
    {
        if (PokecubeManager.isFilled(evt.getEntityItem().getItem()))
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;
            PCInventory.addPokecubeToPC(evt.getEntityItem().getItem(), evt.getEntity().getEntityWorld());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void sendPokemobToPCPlayerDeath(final LivingDeathEvent evt)
    {
        if (evt.getEntity() instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) evt.getEntity();
            EventsHandler.recallAllPokemobsExcluding(player, null, false);
        }
    }

    /**
     * Tries to send pokecubes to PC when player dies.
     *
     * @param evt
     */
    @SubscribeEvent
    public static void sendPokemobToPCPlayerDrops(final LivingDropsEvent evt)
    {
        if (!(evt.getEntity() instanceof PlayerEntity) || !PokecubeCore.getConfig().pcOnDrop) return;
        if (evt.getEntity().getEntityWorld().isRemote) return;
        final List<ItemEntity> toRemove = Lists.newArrayList();
        for (final ItemEntity item : evt.getDrops())
            if (item != null && item.getItem() != null && PCContainer.isItemValid(item.getItem()))
            {
                PCInventory.addStackToPC(evt.getEntity().getUniqueID(), item.getItem().copy());
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
    @SubscribeEvent
    public static void sendPokemobToPCPlayerFull(final CaptureEvent.Post evt)
    {
        // Case for things like snag cubes
        if (evt.caught == null)
        {
            evt.pokecube.entityDropItem(evt.filledCube, 0.5f);
            return;
        }
        final Entity catcher = evt.caught.getOwner();
        if (evt.caught.isShadow()) return;
        if (catcher instanceof PlayerEntity && PokecubeManager.isFilled(evt.filledCube))
        {
            if (catcher.getEntityWorld().isRemote) return;

            final PlayerEntity player = (PlayerEntity) catcher;
            if (player instanceof FakePlayer) return;
            // Cancel it to stop the cube from processing itself.
            evt.setCanceled(true);

            final PlayerInventory inv = player.inventory;
            final UUID id = UUID.fromString(PokecubeManager.getOwner(evt.filledCube));
            final PCInventory pc = PCInventory.getPC(id);
            final int num = inv.getFirstEmptyStack();
            if (evt.filledCube == null || pc == null) System.err.println("Cube is null");
            else if (num == -1 || pc.autoToPC || !player.isAlive() || player.getHealth() <= 0) PCInventory
                    .addPokecubeToPC(evt.filledCube, catcher.getEntityWorld());
            else
            {
                player.inventory.addItemStackToInventory(evt.filledCube);
                if (player instanceof ServerPlayerEntity) ((ServerPlayerEntity) player).sendAllContents(
                        player.container, player.container.getInventory());
            }

            // Apply the same code that StatsHandler does, as it does not
            // get the cancelled event.
            final ResourceLocation cube_id = PokecubeItems.getCubeId(evt.filledCube);
            if (IPokecube.BEHAVIORS.containsKey(cube_id))
            {
                final PokecubeBehavior cube = IPokecube.BEHAVIORS.getValue(cube_id);
                cube.onPostCapture(evt);
            }
            StatsCollector.addCapture(evt.caught);
        }
    }
}
