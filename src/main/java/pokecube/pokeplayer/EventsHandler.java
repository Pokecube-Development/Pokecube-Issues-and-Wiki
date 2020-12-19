package pokecube.pokeplayer;

import java.util.HashSet;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.events.pokemob.EvolveEvent;
import pokecube.core.events.pokemob.RecallEvent;
import pokecube.core.events.pokemob.combat.AttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackEntityHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.pokeplayer.network.DataSyncWrapper;
import pokecube.pokeplayer.network.PacketTransform;
import thut.core.common.handlers.PlayerDataHandler;

public class EventsHandler
{
	public static final ResourceLocation DATACAP = new ResourceLocation(Reference.ID, "data");

    @SubscribeEvent
    public void pokemobAttack(AttackEvent evt)
    {
        if (evt.moveInfo.attacked instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) evt.moveInfo.attacked;
            IPokemob pokemob = PokePlayer.proxyProxy.getPokemob(player);
            if (pokemob != null)
            {
                evt.moveInfo.attacked = pokemob.getEntity();
            }
        }
    }

    @SubscribeEvent
    public void attack(AttackEntityEvent event)
    {
        PlayerEntity player = event.getPlayer();
        IPokemob pokemob = PokePlayer.proxyProxy.getPokemob(player);
        if (pokemob == null) return;
        if (player.getEntityWorld().isRemote) PacketCommand.sendCommand(pokemob, Command.ATTACKENTITY,
                new AttackEntityHandler(event.getTarget().getEntityId()).setFromOwner(true));
        event.setCanceled(true);
    }

    @SubscribeEvent
    /** Sync attacks to the players over to the pokemobs, and also notifiy the
     * pokeinfo that the pokemob was attacked.
     * 
     * @param event */
    public void attack(LivingAttackEvent event)
    {
        if (event.getEntity().getEntityWorld().isRemote) return;
        PlayerEntity player = null;
        if (event.getEntity() instanceof PlayerEntity)
        {
            player = (PlayerEntity) event.getEntity();
            IPokemob pokemob = PokePlayer.proxyProxy.getPokemob(player);
            if (pokemob != null)
            {
                pokemob.getEntity().attackEntityFrom(event.getSource(), event.getAmount());
            }
        }
        else if (event.getEntity().getEntity().getPersistentData().getBoolean("is_a_player"))
        {
            IPokemob evo = CapabilityPokemob.getPokemobFor(event.getEntity());
            if (evo != null)
            {
                UUID uuid = UUID.fromString(event.getEntity().getPersistentData().getString("playerID"));
                player = event.getEntity().getEntityWorld().getPlayerByUuid(uuid);
            }
        }
        if (player != null)
        {
            PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
            info.lastDamage = event.getSource();
        }
    }

    @SubscribeEvent
    public void doRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getPlayer() != null && !event.getPlayer().getEntityWorld().isRemote)
        {
            IPokemob pokemob = PokePlayer.proxyProxy.getPokemob(event.getPlayer());
            if (pokemob != null)
            {
                ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
                ItemStack stack = PokecubeManager.pokemobToItem(pokemob);
                PokecubeManager.heal(stack, event.getEntityLiving().world);
                pokemob = PokecubeManager.itemToPokemob(stack, event.getPlayer().getEntityWorld());
                pokemob.getEntity().isAlive();
                pokemob.getEntity().deathTime = -1;
                PokePlayer.proxyProxy.setPokemob(event.getPlayer(), pokemob);
                //PacketTransform.sendPacket(event.getPlayer(), player);
                if (!player.getEntityWorld().isRemote)
                {
                    EventsHandler.sendUpdate(player);
                    ((ServerPlayerEntity) player).sendAllContents(player.container,
                            player.container.inventoryItemStacks);
                     // Fixes the inventories appearing to vanish
                    player.getEntity().getPersistentData().putLong("_pokeplayer_evolved_",
                            player.getEntityWorld().getGameTime() + 50);
                }
            }
        }
    }

    @SubscribeEvent
    public void recall(RecallEvent.Pre evt)
    {
        if (evt.recalled.getEntity().getEntity().addTag("is_a_player")) evt.setCanceled(true);
    }

    @SubscribeEvent
    public void evolve(EvolveEvent.Post evt)
    {
        Entity entity = evt.mob.getEntity();
        if (entity.getEntity().getPersistentData().getBoolean("is_a_player"))
        {
            UUID uuid = UUID.fromString(entity.getEntity().getEntityString().concat("playerID"));
            PlayerEntity player = entity.getEntityWorld().getPlayerByUuid(uuid);
            IPokemob evo = evt.mob;
            PokePlayer.proxyProxy.setPokemob(player, evo);
            evt.setCanceled(true);
            if (!player.getEntityWorld().isRemote)
            {
                ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
                //PacketTransform.sendPacket(evt., playerMP);
                if (!player.getEntityWorld().isRemote)
                {
                    EventsHandler.sendUpdate(player);
                    ((ServerPlayerEntity) player).sendAllContents(player.container,
                            player.container.inventoryItemStacks);
                     // Fixes the inventories appearing to vanish
                    player.getEntity().getPersistentData().putLong("_pokeplayer_evolved_",
                            player.getEntityWorld().getGameTime() + 50);
                }
            }
            return;
        }
    }

    static HashSet<UUID> syncSchedule = new HashSet<UUID>();

    @SubscribeEvent
    public void PlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (Dist.DEDICATED_SERVER != null)
        {
            syncSchedule.add(event.getPlayer().getUniqueID());
        }
    }

    @SubscribeEvent
    public void PlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event)
    {
        syncSchedule.remove(event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public void startTracking(StartTracking event)
    {
        if (event.getTarget() instanceof PlayerEntity && event.getPlayer().isServerWorld())
        {
            //PacketTransform.sendPacket((PlayerEntity) event.getTarget(), (ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public void postPlayerTick(PlayerTickEvent event)
    {
        PlayerEntity player = event.player;
        if(player==null) return;
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        info.postPlayerTick(player);
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof PlayerEntity)
        {
            event.addCapability(DATACAP, new DataSyncWrapper());
        }
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent evt)
    {
        if (evt.getWorld().isRemote) return;
        if (evt.getEntity().getEntity().getPersistentData().getBoolean("is_a_player"))
        {
            IPokemob evo = CapabilityPokemob.getPokemobFor(evt.getEntity());
            if (evo != null)
            {
                UUID uuid = UUID.fromString(evt.getEntity().getEntity().getEntityString().concat("playerID"));
                PlayerEntity player = evt.getWorld().getPlayerByUuid(uuid);
                PokePlayer.proxyProxy.setPokemob(player, evo);
                evt.setCanceled(true);
                if (!player.getEntityWorld().isRemote)
                {
                    //PacketTransform.sendPacket(player, (ServerPlayerEntity) player);
                }
                return;
            }
        }
        else if (evt.getEntity() instanceof ServerPlayerEntity)
        {
            //sendUpdate((PlayerEntity) evt.getEntity());
        }
    }

    public static void sendUpdate(PlayerEntity player)
    {
        //PacketTransform.sendPacket(player, (ServerPlayerEntity) player);
    }
}
