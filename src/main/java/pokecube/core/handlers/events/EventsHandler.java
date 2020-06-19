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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.world.gen.jigsaw.JigsawPieces;
import thut.api.entity.ShearableCaps;
import thut.api.maths.Vector3;
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

    private static Map<DimensionType, List<IRunnable>> scheduledTasks = Maps.newConcurrentMap();

    public static void Schedule(final World world, final IRunnable task)
    {
        if (!(world instanceof ServerWorld)) return;
        final ServerWorld swrld = (ServerWorld) world;
        if (!swrld.tickingEntities)
        {
            try
            {
                task.run(swrld);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error running scheduled task!", e);
            }
            return;
        }
        final DimensionType dim = world.getDimension().getType();
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

    @SubscribeEvent
    /**
     * Used to make sure that when riding pokemobs underwater, you can mine at
     * a reasonable speed.
     *
     * @param evt
     */
    public static void breakSpeedCheck(final PlayerEvent.BreakSpeed evt)
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

    @SubscribeEvent
    public static void capabilityEntities(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof LivingEntity)) return;
        if (event.getObject() instanceof LivingEntity && !event.getCapabilities().containsKey(
                EventsHandler.AFFECTEDCAP))
        {
            final DefaultAffected affected = new DefaultAffected((LivingEntity) event.getObject());
            event.addCapability(EventsHandler.AFFECTEDCAP, affected);
        }
        if (PokecubeCore.getEntryFor(event.getObject().getType()) != null && !event.getCapabilities().containsKey(
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

    @SubscribeEvent
    public static void capabilityItemStacks(final AttachCapabilitiesEvent<ItemStack> event)
    {
        UsableItemEffects.registerCapabilities(event);
        if (!MegaCapability.isStoneOrWearable(event.getObject())) return;
        final ResourceLocation key = new ResourceLocation("pokecube:megawearable");
        if (event.getCapabilities().containsKey(key)) return;
        event.addCapability(key, new MegaCapability(event.getObject()));
    }

    @SubscribeEvent
    public static void capabilityTileEntities(final AttachCapabilitiesEvent<TileEntity> event)
    {
        final ResourceLocation key = new ResourceLocation("pokecube:tile_inventory");
        if (event.getCapabilities().containsKey(key)) return;
        if (event.getObject() instanceof TMTile) event.addCapability(key, new TMInventory((TMTile) event.getObject()));
        if (event.getObject() instanceof TraderTile) event.addCapability(key, new TradeInventory((TraderTile) event
                .getObject()));
        if (event.getObject() instanceof PCTile) event.addCapability(key, new PCWrapper((PCTile) event.getObject()));
    }

    @SubscribeEvent
    public static void EntityJoinWorld(final EntityJoinWorldEvent evt)
    {
        if (PokecubeCore.getConfig().disableVanillaMonsters && EventsHandler.MONSTERMATCHER.test(evt.getEntity()))
        {
            evt.getEntity().remove();
            // TODO maybe replace stuff here
            evt.setCanceled(true);
            return;
        }
        if (PokecubeCore.getConfig().disableVanillaAnimals && EventsHandler.ANIMALMATCHER.test(evt.getEntity()))
        {
            evt.getEntity().remove();
            // TODO maybe replace stuff here
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

    @SubscribeEvent
    public static void checkSpawns(final PotentialSpawns evt)
    {
        final boolean disabled = evt.getType() == EntityClassification.MONSTER ? PokecubeCore
                .getConfig().deactivateMonsters : PokecubeCore.getConfig().deactivateAnimals;
        if (disabled) evt.getList().removeIf(e -> e.entityType.getRegistryName().getNamespace().equals("minecraft"));
    }

    @SubscribeEvent
    public static void denySpawns(final LivingSpawnEvent.CheckSpawn event)
    {
        // Only deny them from these reasons.
        if (!(event.getSpawnReason() == SpawnReason.NATURAL || event.getSpawnReason() == SpawnReason.CHUNK_GENERATION
                || event.getSpawnReason() == SpawnReason.STRUCTURE)) return;

        if (EventsHandler.MONSTERMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateMonsters) event
                .setResult(Result.DENY);
        if (EventsHandler.ANIMALMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateAnimals) event
                .setResult(Result.DENY);
    }

    @SubscribeEvent
    public static void livingUpdate(final LivingUpdateEvent evt)
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

        // Handle refreshing the terrain for the player, assuming they have
        // moved out of their old terrain location
        if (evt.getEntity() instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) evt.getEntity();
            BlockPos here;
            BlockPos old;
            here = new BlockPos(MathHelper.floor(player.chasingPosX) >> 4, MathHelper.floor(player.chasingPosY) >> 4,
                    MathHelper.floor(player.chasingPosZ) >> 4);
            old = new BlockPos(MathHelper.floor(player.prevChasingPosX) >> 4, MathHelper.floor(
                    player.prevChasingPosY) >> 4, MathHelper.floor(player.prevChasingPosZ) >> 4);
            if (!here.equals(old)) SpawnHandler.refreshTerrain(Vector3.getNewVector().set(evt.getEntity()), evt
                    .getEntity().getEntityWorld());
        }
    }

    public static void sendInitInfo(final ServerPlayerEntity player)
    {
        PacketDataSync.sendInitPacket(player, "pokecube-data");
        PacketDataSync.sendInitPacket(player, "pokecube-stats");
        PacketPokedex.sendLoginPacket(player);
        if (PokecubeCore.getConfig().guiOnLogin) new ChooseFirst(player);
        else if (!PokecubeSerializer.getInstance().hasStarter(player)) player.sendMessage(new TranslationTextComponent(
                "pokecube.login.find_prof_or_config"));
    }

    @SubscribeEvent
    public static void PlayerLoggin(final PlayerLoggedInEvent evt)
    {
        final PlayerEntity player = evt.getPlayer();
        if (!player.isServerWorld()) return;
        EventsHandler.sendInitInfo((ServerPlayerEntity) player);
    }

    @SubscribeEvent
    public static void PokecubeWatchEvent(final StartTracking event)
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void serverAboutToStart(final FMLServerAboutToStartEvent event)
    {
        Database.swapManager(event.getServer());
        PokecubeCore.proxy.serverAboutToStart(event);
    }

    @SubscribeEvent
    public static void serverSopped(final FMLServerStoppedEvent event)
    {
        // Reset this.
        PokecubeSerializer.clearInstance();
        JigsawPieces.sent_events.clear();
        EventsHandler.scheduledTasks.clear();
    }

    @SubscribeEvent
    public static void serverStarted(final FMLServerStartedEvent event)
    {
        Database.postServerLoaded();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    /**
     * Register the commands.
     *
     * @param event
     */
    public static void serverStarting(final FMLServerStartingEvent event)
    {
        PokecubeCore.LOGGER.info("Server Starting, Registering Commands");
        PokecubeItems.init(event.getServer());
        CommandConfigs.register(PokecubeCore.getConfig(), event.getCommandDispatcher(), "pokesettings");
        CommandManager.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public static void capabilityWorld(final AttachCapabilitiesEvent<World> event)
    {
        if (event.getObject() instanceof ServerWorld && event.getObject().getDimension()
                .getType() == DimensionType.OVERWORLD) PokecubeSerializer.newInstance((ServerWorld) event.getObject());
    }

    @SubscribeEvent
    public static void tickEvent(final WorldTickEvent evt)
    {
        if (evt.phase != Phase.END || !(evt.world instanceof ServerWorld)) return;
        final DimensionType dim = evt.world.dimension.getType();
        final List<IRunnable> tasks = EventsHandler.scheduledTasks.getOrDefault(dim, Collections.emptyList());
        synchronized (tasks)
        {
            tasks.removeIf(r ->
            {
                try
                {
                    r.run(evt.world);
                    return true;
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error running scheduled task!", e);
                    return true;
                }
            });
        }
        // Call spawner tick at end of world tick.
        if (!Database.spawnables.isEmpty()) PokecubeCore.spawner.tick((ServerWorld) evt.world);
    }

    @SubscribeEvent
    public static void travelToDimension(final EntityTravelToDimensionEvent evt)
    {
        final Entity entity = evt.getEntity();
        if (entity.getEntityWorld().isRemote) return;
        // Recall the pokemobs if the player changes dimension.

        final List<Entity> pokemobs = new ArrayList<>(((ServerWorld) entity.getEntityWorld()).getEntities(null,
                e -> EventsHandler.validFollowing(entity, e)));
        PCEventsHandler.recallAll(pokemobs, false);
    }

    @SubscribeEvent
    public static void playerTick(final PlayerTickEvent event)
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

    @SubscribeEvent
    public static void WorldSave(final WorldEvent.Save evt)
    {
        // Save the pokecube data whenever the overworld saves.
        if (evt.getWorld().getDimension().getType() == DimensionType.OVERWORLD)
        {
            final long time = System.nanoTime();
            PokecubeSerializer.getInstance().save();
            final double dt = (System.nanoTime() - time) / 1000000d;
            if (dt > 20) System.err.println("Took " + dt + "ms to save pokecube data");
        }
    }
}
