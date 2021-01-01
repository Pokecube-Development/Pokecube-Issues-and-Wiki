package pokecube.core.handlers.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.PotentialSpawns;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.blocks.pc.PCTile;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.commands.CommandManager;
import pokecube.core.database.Database;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.events.CustomInteractEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityAffected.DefaultAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.capabilities.TextureableCaps;
import pokecube.core.interfaces.capabilities.TextureableCaps.NPCCap;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.inventory.pc.PCWrapper;
import pokecube.core.inventory.tms.TMInventory;
import pokecube.core.inventory.trade.TradeInventory;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.items.megastuff.WearablesCompat;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.world.gen.jigsaw.CustomJigsawPiece;
import pokecube.nbtedit.NBTEdit;
import thut.api.entity.ShearableCaps;
import thut.core.common.commands.CommandConfigs;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class EventsHandler
{
    public static class ChooseFirst
    {
        final PlayerEntity player;

        final long start;

        public ChooseFirst(final PlayerEntity player)
        {
            this.player = player;
            this.start = player.getEntityWorld().getGameTime();
            if (!SpawnHandler.canSpawnInWorld(player.getEntityWorld(), false)) return;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(final TickEvent.PlayerTickEvent event)
        {
            if (event.player.getEntityWorld().getGameTime() - this.start < 20) return;
            if (event.player == this.player)
            {
                PacketChoose packet;
                packet = new PacketChoose(PacketChoose.OPENGUI);
                final boolean hasStarter = PokecubeSerializer.getInstance().hasStarter(this.player);
                if (hasStarter)
                {
                    packet.data.putBoolean("C", false);
                    packet.data.putBoolean("H", hasStarter);
                }
                else
                {
                    final boolean special = false;
                    final boolean pick = false;
                    packet = PacketChoose.createOpenPacket(special, pick, Database.getStarters());
                }
                PokecubeCore.packets.sendTo(packet, (ServerPlayerEntity) event.player);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    public static final ResourceLocation POKEMOBCAP  = new ResourceLocation(PokecubeMod.ID, "pokemob");
    public static final ResourceLocation AFFECTEDCAP = new ResourceLocation(PokecubeMod.ID, "affected");
    public static final ResourceLocation DATACAP     = new ResourceLocation(PokecubeMod.ID, "data");
    public static final ResourceLocation TEXTURECAP  = new ResourceLocation(PokecubeMod.ID, "textured");

    static double max = 0;

    /**
     * This returns true if the given entity is not a vanilla entity, or is not
     * a mob-like entity, it returns false for modded mobs, as well as players,
     * armour stands, boats, etc
     */
    private static Predicate<Entity> NOTVANILLAANIMALORMOB;

    /**
     * This returns true if the given entity is ia "vanilla" monster, but not a
     * boss
     */
    public static Predicate<Entity> MONSTERMATCHER;

    /**
     * This returns true if the given entity is ia "vanilla" animal
     */
    public static Predicate<Entity> ANIMALMATCHER;

    static
    {
        // This deals with making sure it is actually a mob, as well as not an
        // npc, or a pokemob
        EventsHandler.NOTVANILLAANIMALORMOB = e ->
        {
            boolean canSpawn = false;
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
            // This includes players, armour stands, effects, etc
            final boolean noSpawnBlock = !(e instanceof MobEntity);
            // We don't want to block something if we have made it a pokemob
            final boolean isPokemob = pokemob != null;
            // Simple check for vanillaness, via the entity type registry name
            final boolean isVanilla = e.getType().getRegistryName().getNamespace().equals("minecraft");
            // Lets not block villagers/merchants/pillagers
            final boolean isNpc = e instanceof INPC || e instanceof IMerchant || e instanceof WitherEntity;
            // Lets also not block the ender dragon/parts
            final boolean isDragon = e instanceof EnderDragonEntity || e instanceof EnderDragonPartEntity;
            canSpawn = noSpawnBlock || isDragon || isNpc || isPokemob || !isVanilla;
            return !canSpawn;
        };

        // IMob -> monster
        EventsHandler.MONSTERMATCHER = e -> (e instanceof IMob);
        // Not IMob -> animal
        EventsHandler.ANIMALMATCHER = e -> !(e instanceof IMob);

        EventsHandler.ANIMALMATCHER = EventsHandler.NOTVANILLAANIMALORMOB.and(EventsHandler.ANIMALMATCHER);
        EventsHandler.MONSTERMATCHER = EventsHandler.NOTVANILLAANIMALORMOB.and(EventsHandler.MONSTERMATCHER);
    }

    private static Map<RegistryKey<World>, List<IRunnable>> scheduledTasks = Maps.newConcurrentMap();

    public static void Schedule(final World world, final IRunnable task)
    {
        if (!(world instanceof ServerWorld)) return;
        final ServerWorld swrld = (ServerWorld) world;

        // If we are tickingEntities, do not do this, as it can cause
        // concurrent modification exceptions.
        if (!swrld.tickingEntities)
        {
            // This will either run it now, or run it on main thread soon
            swrld.getServer().execute(() -> task.run(swrld));
            return;
        }
        final RegistryKey<World> dim = world.getDimensionKey();
        final List<IRunnable> tasks = EventsHandler.scheduledTasks.getOrDefault(dim, Lists.newArrayList());
        synchronized (tasks)
        {
            tasks.add(task);
        }
        EventsHandler.scheduledTasks.put(dim, tasks);
    }

    static int count = 0;

    static int     countAbove = 0;
    static double  mean       = 0;
    static long    starttime  = 0;
    static boolean notified   = false;

    // 4 = 1 per 10mins, 2 = 1 per 10s, 5 = 1 per 48 hours
    public static double candyChance = 4.5;

    public static double juiceChance = 3.5;

    public static void register()
    {
        // Allows mining properly while riding dive pokemobs.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onBreakSpeedCheck);

        // This adds: AffectCapability, PokemobCapability + supporting (data,
        // genetics, textures, shearable) as well as NPCMob capabilities for
        // textures.
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, EventsHandler::onEntityCaps);
        // This one does the mega wearable caps for worn items
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, EventsHandler::onItemCaps);
        // This does inventory capabilities for:
        // TM machine, Trading Maching and PCs
        MinecraftForge.EVENT_BUS.addGenericListener(TileEntity.class, EventsHandler::onTileCaps);
        // This is being used as an earlier "world load" like event, for
        // re-setting the pokecube serializer for the overworld.
        MinecraftForge.EVENT_BUS.addGenericListener(World.class, EventsHandler::onWorldCaps);

        // This handles preventing blacklisted mobs from joining a world, for
        // the disable<thing> configs. It also adds the creepers avoid psychic
        // types AI, and does some cleanup for shoulder mobs.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onMobJoinWorld);
        // This handles one part of preventing natural spawns for the
        // deactivate<thing> configs.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onCheckSpawnPotential);
        // This is another stage of the above.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onCheckSpawnCheck);

        // Here we handle bed healing if enabled in configs
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPlayerWakeUp);
        // This ticks ongoing effects (like burn, poison, etc)
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onLivingUpdate);
        // This synchronizes stats and data for the player, and sends the
        // GuiOnLogin if enabled and required.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPlayerLogin);
        // This one handles not sending "hidden" pokecubes to the player, for
        // loot-pokecubes which the player is not allowed to pick up yet.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPokecubeWatch);

        // This initializes some things in the Database, is HIGHEST to ensure
        // that is finished before addons do their own things. It also does some
        // cleanup in the ClientProxy. TODO move that cleanup elsewhere!
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, EventsHandler::onServerAboutToStart);
        // Does some debug output in pokecube tags if enabled.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onServerStarting);
        // Cleans up some things for when server next starts.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onServerStopped);
        // Initialises or reloads some datapack dependent values in Database
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onResourcesReloaded);
        // This does similar to the above, but on dedicated servers only.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onServerStarted);
        // Registers our commands.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onCommandRegister);
        // This deals with running the tasks scheduled via
        // EventsHandler::shedule, as well as ticking the pokemob spawner.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onWorldTick);
        // This attempts to recall the mobs following the player when they
        // change dimension.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onChangeDimension);
        // This handles preventing players from being kicked for flying, if they
        // are riding a pokemob that can fly.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPlayerTick);
        // This saves the pokecube Serializer
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onWorldSave);

        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onEntityInteractSpecific);
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onItemRightClick);
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onEmptyRightClick);

        // now let our other handlers register their stuff

        MoveEventsHandler.register();
        PCEventsHandler.register();
        PokemobEventsHandler.register();
        SpawnEventsHandler.register();
        StatsHandler.register();
        MoveQueuer.register();

        // Here we register the onWorldLoad for pokemob tracker, this handles
        // initializing the tracked pokemob maps, etc.
        MinecraftForge.EVENT_BUS.addListener(PokemobTracker::onWorldLoad);

    }

    private static void onEntityInteract(final PlayerInteractEvent.EntityInteract evt)
    {
        if (evt instanceof CustomInteractEvent) return;
        if (!(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == player.getEntityWorld().getGameTime())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
        if (!evt.isCanceled())
        {
            final CustomInteractEvent event = new CustomInteractEvent(evt.getPlayer(), evt.getHand(), evt.getTarget());
            MinecraftForge.EVENT_BUS.post(event);
            if (event.getResult() == Result.ALLOW)
            {
                player.getPersistentData().putLong("__poke_int_c_", player.getEntityWorld().getGameTime());
                player.getPersistentData().putLong(ID, player.getEntityWorld().getGameTime());
            }
        }
    }

    private static void onEntityInteractSpecific(final PlayerInteractEvent.EntityInteractSpecific evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == player.getEntityWorld().getGameTime())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
        if (!evt.isCanceled())
        {
            final CustomInteractEvent event = new CustomInteractEvent(evt.getPlayer(), evt.getHand(), evt.getTarget());
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled())
            {
                player.getPersistentData().putLong("__poke_int_c_", player.getEntityWorld().getGameTime());
                player.getPersistentData().putLong(ID, player.getEntityWorld().getGameTime());
            }
        }
    }

    private static void onItemRightClick(final PlayerInteractEvent.RightClickItem evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == player.getEntityWorld().getGameTime())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
    }

    private static void onEmptyRightClick(final PlayerInteractEvent.RightClickEmpty evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == player.getEntityWorld().getGameTime())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
    }

    private static void onBreakSpeedCheck(final PlayerEvent.BreakSpeed evt)
    {
        final Entity ridden = evt.getEntity().getRidingEntity();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(ridden);
        if (pokemob != null)
        {
            boolean aqua = evt.getEntity().isInWater();
            if (aqua) aqua = !EnchantmentHelper.hasAquaAffinity((LivingEntity) evt.getEntity());
            if (aqua) evt.setNewSpeed(evt.getOriginalSpeed() / 0.04f);
            else evt.setNewSpeed(evt.getOriginalSpeed() / 0.2f);
        }
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof LivingEntity)) return;
        if (event.getObject() instanceof LivingEntity && !event.getCapabilities().containsKey(
                EventsHandler.AFFECTEDCAP))
        {
            final DefaultAffected affected = new DefaultAffected((LivingEntity) event.getObject());
            event.addCapability(EventsHandler.AFFECTEDCAP, affected);
        }
        if (event.getObject() instanceof EntityPokemob && !event.getCapabilities().containsKey(
                EventsHandler.POKEMOBCAP))
        {
            final DefaultPokemob pokemob = new DefaultPokemob((MobEntity) event.getObject());
            final GeneticsProvider genes = new GeneticsProvider();
            final DataSync_Impl data = new DataSync_Impl();
            final TextureableCaps.PokemobCap tex = new TextureableCaps.PokemobCap((EntityPokemob) event.getObject());
            pokemob.setDataSync(data);
            pokemob.genes = genes.wrapped;
            event.addCapability(GeneticsManager.POKECUBEGENETICS, genes);
            event.addCapability(EventsHandler.POKEMOBCAP, pokemob);
            event.addCapability(EventsHandler.DATACAP, data);
            event.addCapability(EventsHandler.TEXTURECAP, tex);
            event.addCapability(ShearableCaps.LOC, new ShearableCaps.Wrapper(pokemob));
            IGuardAICapability.addCapability(event);
        }

        if (event.getObject() instanceof NpcMob)
        {
            final NpcMob prof = (NpcMob) event.getObject();
            event.addCapability(EventsHandler.TEXTURECAP, new NPCCap<>(prof, e -> e.getTex(), e -> !e.isMale()));
            IGuardAICapability.addCapability(event);
        }
    }

    private static void onItemCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        UsableItemEffects.registerCapabilities(event);
        GeneticsManager.registerCapabilities(event);
        WearablesCompat.registerCapabilities(event);
        if (!MegaCapability.isStoneOrWearable(event.getObject())) return;
        final ResourceLocation key = new ResourceLocation("pokecube:megawearable");
        if (event.getCapabilities().containsKey(key)) return;
        event.addCapability(key, new MegaCapability(event.getObject()));
    }

    private static void onTileCaps(final AttachCapabilitiesEvent<TileEntity> event)
    {
        final ResourceLocation key = new ResourceLocation("pokecube:tile_inventory");
        if (event.getCapabilities().containsKey(key)) return;
        if (event.getObject() instanceof TMTile) event.addCapability(key, new TMInventory((TMTile) event.getObject()));
        if (event.getObject() instanceof TraderTile) event.addCapability(key, new TradeInventory((TraderTile) event
                .getObject()));
        if (event.getObject() instanceof PCTile) event.addCapability(key, new PCWrapper((PCTile) event.getObject()));
    }

    private static void onWorldCaps(final AttachCapabilitiesEvent<World> event)
    {
        if (event.getObject() instanceof ServerWorld && event.getObject().getDimensionKey().equals(World.OVERWORLD))
            PokecubeSerializer.newInstance((ServerWorld) event.getObject());
    }

    private static void onMobJoinWorld(final EntityJoinWorldEvent evt)
    {
        if (PokecubeCore.getConfig().disableVanillaMonsters && EventsHandler.MONSTERMATCHER.test(evt.getEntity()))
        {
            evt.getEntity().remove();
            evt.setCanceled(true);
            return;
        }
        if (PokecubeCore.getConfig().disableVanillaAnimals && EventsHandler.ANIMALMATCHER.test(evt.getEntity()))
        {
            evt.getEntity().remove();
            evt.setCanceled(true);
            return;
        }

        if (evt.getEntity() instanceof IPokemob && evt.getEntity().getPersistentData().getBoolean("onShoulder"))
        {
            ((IPokemob) evt.getEntity()).setLogicState(LogicStates.SITTING, false);
            evt.getEntity().getPersistentData().remove("onShoulder");
        }
        if (evt.getEntity() instanceof CreeperEntity)
        {
            final CreeperEntity creeper = (CreeperEntity) evt.getEntity();
            final AvoidEntityGoal<?> avoidAI = new AvoidEntityGoal<>(creeper, EntityPokemob.class, 6.0F, 1.0D, 1.2D,
                    e -> CapabilityPokemob.getPokemobFor(e).isType(PokeType.getType("psychic")));
            creeper.goalSelector.addGoal(3, avoidAI);
        }
    }

    private static void onCheckSpawnPotential(final PotentialSpawns evt)
    {
        final boolean disabled = evt.getType() == EntityClassification.MONSTER ? PokecubeCore
                .getConfig().deactivateMonsters : PokecubeCore.getConfig().deactivateAnimals;
        if (disabled) evt.getList().removeIf(e -> e.type.getRegistryName().getNamespace().equals("minecraft"));
    }

    private static void onCheckSpawnCheck(final LivingSpawnEvent.CheckSpawn event)
    {
        // Only deny them from these reasons.
        if (!(event.getSpawnReason() == SpawnReason.NATURAL || event.getSpawnReason() == SpawnReason.CHUNK_GENERATION
                || event.getSpawnReason() == SpawnReason.STRUCTURE)) return;

        if (EventsHandler.MONSTERMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateMonsters) event
                .setResult(Result.DENY);
        if (EventsHandler.ANIMALMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateAnimals) event
                .setResult(Result.DENY);
    }

    private static void onPlayerWakeUp(final PlayerWakeUpEvent evt)
    {
        if (!PokecubeCore.getConfig().bedsHeal) return;
        for (int i = 0; i < evt.getPlayer().inventory.getSizeInventory(); i++)
        {
            final ItemStack stack = evt.getPlayer().inventory.getStackInSlot(i);
            if (PokecubeManager.isFilled(stack)) PokecubeManager.heal(stack, evt.getPlayer().getEntityWorld());
        }
    }

    private static void onLivingUpdate(final LivingUpdateEvent evt)
    {
        final IPokemob poke = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (poke != null) poke.onTick();

        if (evt.getEntity().getEntityWorld().isRemote || !evt.getEntity().isAlive()) return;
        final int tick = Math.max(PokecubeCore.getConfig().attackCooldown, 1);
        // Handle ongoing effects for this mob.
        if (evt.getEntity().ticksExisted % tick == 0)
        {
            final IOngoingAffected affected = CapabilityAffected.getAffected(evt.getEntity());
            if (affected != null) affected.tick();
        }
    }

    private static void onPlayerLogin(final PlayerLoggedInEvent evt)
    {
        final PlayerEntity player = evt.getPlayer();
        if (!player.isServerWorld()) return;
        EventsHandler.sendInitInfo((ServerPlayerEntity) player);
    }

    private static void onPokecubeWatch(final StartTracking event)
    {
        // Check if the pokecube is loot, and is not collectable by the player,
        // if this is the case, it should be set invisible.
        if (event.getTarget() instanceof EntityPokecube && event.getEntity() instanceof ServerPlayerEntity)
        {
            final EntityPokecube pokecube = (EntityPokecube) event.getTarget();
            if (pokecube.isLoot && pokecube.cannotCollect(event.getEntity())) PacketPokecube.sendMessage(
                    (PlayerEntity) event.getEntity(), pokecube.getEntityId(), pokecube.world.getGameTime()
                            + pokecube.resetTime);
        }
    }

    private static void onServerAboutToStart(final FMLServerAboutToStartEvent event)
    {
        PokecubeCore.proxy.serverAboutToStart(event);
    }

    private static void onServerStarting(final FMLServerStartingEvent event)
    {
        PokecubeCore.LOGGER.info("Server Starting");
        PokecubeItems.init(event.getServer());
    }

    private static void onServerStopped(final FMLServerStoppedEvent event)
    {
        // Reset this.
        PokecubeSerializer.clearInstance();
        CustomJigsawPiece.sent_events.clear();
        EventsHandler.scheduledTasks.clear();
    }

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        PokecubeCore.LOGGER.info("Registering Commands");
        CommandConfigs.register(PokecubeCore.getConfig(), event.getDispatcher(), "pokesettings");
        CommandManager.register(event.getDispatcher());
        NBTEdit.registerCommands(event);
    }

    private static void onWorldTick(final WorldTickEvent evt)
    {
        if (evt.phase != Phase.END || !(evt.world instanceof ServerWorld)) return;
        final RegistryKey<World> dim = evt.world.getDimensionKey();
        final List<IRunnable> tasks = EventsHandler.scheduledTasks.getOrDefault(dim, Collections.emptyList());
        synchronized (tasks)
        {
            tasks.removeIf(r ->
            {
                // This ensures it is executed on the main thread.
                evt.world.getServer().execute(() -> r.run(evt.world));
                return true;
            });
        }
        // Call spawner tick at end of world tick.
        if (!Database.spawnables.isEmpty()) PokecubeCore.spawner.tick((ServerWorld) evt.world);
    }

    private static void onChangeDimension(final EntityTravelToDimensionEvent evt)
    {
        final Entity entity = evt.getEntity();
        if (entity.getEntityWorld().isRemote) return;
        // Recall the pokemobs if the player changes dimension.
        final List<Entity> pokemobs = new ArrayList<>(((ServerWorld) entity.getEntityWorld()).getEntities(null,
                e -> EventsHandler.validFollowing(entity, e)));
        PCEventsHandler.recallAll(pokemobs, false);
    }

    private static void onPlayerTick(final PlayerTickEvent event)
    {
        if (event.side == LogicalSide.SERVER && event.player instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) event.player;
            final IPokemob ridden = CapabilityPokemob.getPokemobFor(player.getRidingEntity());
            if (ridden != null && ridden.canUseFly())
            {
                player.connection.floatingTickCount = 0;
                player.connection.vehicleFloatingTickCount = 0;
            }
        }
    }

    private static void onWorldSave(final WorldEvent.Save evt)
    {
        if (!(evt.getWorld() instanceof ServerWorld)) return;
        // Save the pokecube data whenever the overworld saves.
        if (((World) evt.getWorld()).getDimensionKey().equals(World.OVERWORLD))
        {
            final long time = System.nanoTime();
            PokecubeSerializer.getInstance().save();
            final double dt = (System.nanoTime() - time) / 1000000d;
            if (dt > 20) System.err.println("Took " + dt + "ms to save pokecube data");
        }
    }

    private static void onServerStarted(final FMLServerStartedEvent event)
    {
        if (event.getServer().isDedicatedServer()) Database.onResourcesReloaded();
    }

    private static void onResourcesReloaded(final AddReloadListenerEvent event)
    {
        event.addListener(Database.ReloadListener.INSTANCE);
    }

    public static void sendInitInfo(final ServerPlayerEntity player)
    {
        PacketDataSync.sendInitPacket(player, "pokecube-data");
        PacketDataSync.sendInitPacket(player, "pokecube-stats");
        PacketPokedex.sendLoginPacket(player);
        if (PokecubeCore.getConfig().guiOnLogin) new ChooseFirst(player);
        else if (!PokecubeSerializer.getInstance().hasStarter(player)) player.sendMessage(new TranslationTextComponent(
                "pokecube.login.find_prof_or_config"), Util.DUMMY_UUID);
    }

    public static void recallAllPokemobs(final LivingEntity user)
    {
        if (!user.isServerWorld()) return;
        final List<Entity> pokemobs = PokemobTracker.getMobs(user, e -> EventsHandler.validRecall(user, e, null,
                false));
        PCEventsHandler.recallAll(pokemobs, true);
    }

    public static void recallAllPokemobsExcluding(final ServerPlayerEntity player, final IPokemob excluded,
            final boolean includeStaying)
    {
        final List<Entity> pokemobs = PokemobTracker.getMobs(player, e -> EventsHandler.validRecall(player, e, excluded,
                includeStaying));
        PCEventsHandler.recallAll(pokemobs, true);
    }

    /**
     * Checks if Entity is owned by owner, it checks if it is a pokemob, or a
     * filled pokecube. If it is a pokemob, it also confirms that it is not set
     * to stay.
     *
     * @param owner
     * @param toRecall
     * @return
     */
    public static boolean validFollowing(final Entity owner, final Entity toRecall)
    {
        if (!toRecall.isAlive()) return false;
        if (!toRecall.isAddedToWorld()) return false;
        final IPokemob mob = CapabilityPokemob.getPokemobFor(toRecall);
        if (mob == null)
        {
            if (toRecall instanceof EntityPokecube)
            {
                final EntityPokecube cube = (EntityPokecube) toRecall;
                if (!cube.getItem().isEmpty())
                {
                    final String name = PokecubeManager.getOwner(cube.getItem());
                    if (name != null && name.equals(owner.getCachedUniqueIdString())) return true;
                }
            }
            return false;
        }
        if (!mob.getGeneralState(GeneralStates.TAMED)) return false;
        if (mob.getGeneralState(GeneralStates.STAYING)) return false;
        return mob.getOwner() == owner;
    }

    public static boolean validRecall(final LivingEntity player, final Entity toRecall, final IPokemob excluded,
            final boolean includeStay)
    {
        return EventsHandler.validRecall(player, toRecall, excluded, true, includeStay);
    }

    public static boolean validRecall(final LivingEntity player, final Entity toRecall, final IPokemob excluded,
            final boolean includeCubes, final boolean includeStay)
    {
        if (!toRecall.isAlive()) return false;
        if (!toRecall.isAddedToWorld()) return false;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(toRecall);
        if (pokemob != null)
        {
            if (pokemob != excluded && pokemob.getOwner() == player && (includeStay || !pokemob.getGeneralState(
                    GeneralStates.STAYING))) return true;
        }
        else if (toRecall instanceof EntityPokecube)
        {
            final EntityPokecube mob = (EntityPokecube) toRecall;
            if (!mob.getItem().isEmpty())
            {
                final String name = PokecubeManager.getOwner(mob.getItem());
                if (name != null && name.equals(player.getCachedUniqueIdString())) return true;
            }
        }
        return false;
    }
}
