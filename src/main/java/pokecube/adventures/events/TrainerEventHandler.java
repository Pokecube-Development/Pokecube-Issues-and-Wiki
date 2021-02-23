package pokecube.adventures.events;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import com.google.gson.JsonSyntaxException;

import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs.AllowedBattle;
import pokecube.adventures.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.capabilities.CapabilityHasTrades.DefaultTrades;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates.AIState;
import pokecube.adventures.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.capabilities.player.PlayerPokemobs;
import pokecube.adventures.capabilities.utils.ActionContext;
import pokecube.adventures.capabilities.utils.MessageState;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.items.Linker;
import pokecube.adventures.network.PacketTrainer;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.events.BrainInitEvent;
import pokecube.core.events.CustomInteractEvent;
import pokecube.core.events.NpcSpawn;
import pokecube.core.events.PCEvent;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.pokemob.CaptureEvent;
import pokecube.core.events.pokemob.RecallEvent;
import pokecube.core.events.pokemob.SpawnEvent.SendOut;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.moves.damage.TerrainDamageSource;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.network.EntityUpdate;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.SyncHandler;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_String;

public class TrainerEventHandler
{

    private static class NpcOffers implements Consumer<MerchantOffers>
    {
        final NpcMob mob;

        public NpcOffers(final NpcMob mob)
        {
            this.mob = mob;

            // Check for blank name, and if so, randomize it.
            final List<String> names = mob.isMale() ? TypeTrainer.maleNames : TypeTrainer.femaleNames;
            if (!names.isEmpty() && mob.getNPCName().isEmpty()) mob.setNPCName("pokecube." + mob.getNpcType().getName()
                    + ".named:" + names.get(new Random().nextInt(names.size())));
        }

        @Override
        public void accept(final MerchantOffers t)
        {
            final Random rand = new Random(this.mob.getUniqueID().getLeastSignificantBits());
            final String type = this.mob.getNpcType() == NpcType.byType("professor") ? "professor" : "merchant";
            TrainerTrades trades = TypeTrainer.tradesMap.get(type);
            if (!this.mob.customTrades.isEmpty())
            {
                trades = TypeTrainer.tradesMap.get(this.mob.customTrades);
                if (trades != null) trades.addTrades(this.mob.getOffers(), rand);
            }
            else if (trades != null) trades.addTrades(this.mob.getOffers(), rand);
            else this.mob.getOffers().addAll(TypeTrainer.merchant.getRecipes(rand));
        }
    }

    private static class NpcOffer implements Consumer<MerchantOffer>
    {
        final NpcMob mob;

        public NpcOffer(final NpcMob mob)
        {
            this.mob = mob;
        }

        @Override
        public void accept(final MerchantOffer t)
        {
            // TODO decide if we want anything here
            this.mob.getNpcType();
        }

    }

    public static final ResourceLocation POKEMOBSCAP = new ResourceLocation(PokecubeAdv.MODID, "pokemobs");
    public static final ResourceLocation AICAP       = new ResourceLocation(PokecubeAdv.MODID, "ai");
    public static final ResourceLocation MESSAGECAP  = new ResourceLocation(PokecubeAdv.MODID, "messages");
    public static final ResourceLocation REWARDSCAP  = new ResourceLocation(PokecubeAdv.MODID, "rewards");
    public static final ResourceLocation DATASCAP    = new ResourceLocation(PokecubeAdv.MODID, "data");
    public static final ResourceLocation TRADESCAP   = new ResourceLocation(PokecubeAdv.MODID, "trades");

    public static void attach_guard(final AttachCapabilitiesEvent<Entity> event)
    {
        IGuardAICapability.addCapability(event);
    }

    public static void attach_pokemobs(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof PlayerEntity)
        {
            PlayerPokemobs.register(event);
            return;
        }
        if (!(event.getObject() instanceof MobEntity)) return;
        if (TrainerEventHandler.hasCap(event)) return;

        if (TypeTrainer.get((LivingEntity) event.getObject(), false) == null) return;

        final DefaultPokemobs mobs = new DefaultPokemobs();
        final DefaultRewards rewards = new DefaultRewards();
        final MobEntity mob = (MobEntity) event.getObject();
        ItemStack stack = ItemStack.EMPTY;
        try
        {
            stack = TrainerEventHandler.fromString(Config.instance.trainer_defeat_reward, event.getObject());
        }
        catch (final CommandException e)
        {
            PokecubeCore.LOGGER.warn("Error with default trainer rewards " + Config.instance.trainer_defeat_reward, e);
        }
        if (!stack.isEmpty()) rewards.getRewards().add(new Reward(stack));
        final DefaultAIStates aiStates = new DefaultAIStates();
        final DefaultMessager messages = new DefaultMessager();
        mobs.init((LivingEntity) event.getObject(), aiStates, messages, rewards);
        event.addCapability(TrainerEventHandler.POKEMOBSCAP, mobs);
        event.addCapability(TrainerEventHandler.AICAP, aiStates);
        event.addCapability(TrainerEventHandler.MESSAGECAP, messages);
        event.addCapability(TrainerEventHandler.REWARDSCAP, rewards);

        if (mob instanceof TrainerBase) event.addCapability(TrainerEventHandler.TRADESCAP, new DefaultTrades());

        DataSync data = TrainerEventHandler.getData(event);
        if (data == null)
        {
            data = new DataSync_Impl();
            event.addCapability(TrainerEventHandler.DATASCAP, (DataSync_Impl) data);
        }
        mobs.setDataSync(data);
        mobs.holder.TYPE = data.register(new Data_String(), "");

        for (int i = 0; i < 6; i++)
            mobs.holder.POKEMOBS[i] = data.register(new Data_ItemStack(), ItemStack.EMPTY);

        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Initializing caps " + event.getObject() + " " + event
                .getObject().isAlive());
    }

    public static void onAttachMobCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        // Add capabilities for guard AI
        TrainerEventHandler.attach_guard(event);
        // Add pokemob holder caps
        TrainerEventHandler.attach_pokemobs(event);
    }

    public static ItemStack fromString(final String arg, final Entity sender) throws CommandException
    {
        Drop drop;
        try
        {
            drop = PokedexEntryLoader.gson.fromJson(arg, Drop.class);
            return Tools.getStack(drop.getValues(), sender.getEntityWorld() instanceof ServerWorld
                    ? (ServerWorld) sender.getEntityWorld()
                    : null);
        }
        catch (final JsonSyntaxException e)
        {
            PokecubeCore.LOGGER.error("Error loading drops from string {} for mob {}", arg, sender);
            PokecubeCore.LOGGER.error(e);
            return ItemStack.EMPTY;
        }
    }

    public static DataSync getData(final AttachCapabilitiesEvent<Entity> event)
    {
        for (final ICapabilityProvider provider : event.getCapabilities().values())
            if (provider.getCapability(SyncHandler.CAP).isPresent()) return provider.getCapability(SyncHandler.CAP)
                    .orElse(null);
        return null;
    }

    private static boolean hasCap(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(TrainerEventHandler.POKEMOBSCAP)) return true;
        for (final ICapabilityProvider provider : event.getCapabilities().values())
            if (provider.getCapability(TrainerCaps.HASPOKEMOBS_CAP).isPresent()) return true;
        return false;
    }

    public static void onEntityAttributes(final EntityAttributeCreationEvent event)
    {
        final AttributeModifierMap.MutableAttribute attribs = LivingEntity.registerAttributes().createMutableAttribute(
                Attributes.FOLLOW_RANGE, 16.0D).createMutableAttribute(Attributes.ATTACK_KNOCKBACK)
                .createMutableAttribute(Attributes.MAX_HEALTH, 20.0D);
        event.put(TrainerNpc.TYPE, attribs.create());
        event.put(LeaderNpc.TYPE, attribs.create());
    }

    public static void onEntityInteract(final CustomInteractEvent evt)
    {
        TrainerEventHandler.processInteract(evt, evt.getTarget());
    }

    /**
     * This manages invulnerability of npcs to pokemobs, as well as managing
     * the target allocation for trainers.
     *
     * @param evt
     */
    public static void onLivingHurt(final LivingHurtEvent evt)
    {
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(evt.getEntity());
        final IHasMessages messages = TrainerCaps.getMessages(evt.getEntity());

        if (evt.getEntity() instanceof INPC && !Config.instance.pokemobsHarmNPCs && (evt
                .getSource() instanceof PokemobDamageSource || evt.getSource() instanceof TerrainDamageSource)) evt
                        .setAmount(0);

        if (evt.getSource().getTrueSource() instanceof LivingEntity)
        {
            if (messages != null)
            {
                messages.sendMessage(MessageState.HURT, evt.getSource().getTrueSource(), evt.getEntity()
                        .getDisplayName(), evt.getSource().getTrueSource().getDisplayName());
                messages.doAction(MessageState.HURT, new ActionContext((LivingEntity) evt.getSource().getTrueSource(),
                        evt.getEntityLiving(), evt.getSource()));
            }
            if (pokemobHolder != null && pokemobHolder.getTarget() == null) pokemobHolder.onSetTarget((LivingEntity) evt
                    .getSource().getTrueSource());
        }
    }

    /**
     * Initializes the AI for the trainers when they join the world.
     *
     * @param event
     */
    public static void onJoinWorld(final EntityJoinWorldEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getWorld() instanceof ServerWorld)) return;
        // Schedule the update for the next time this ticks, otherwise we can
        // get race conditions from block checks...
        event.getEntity().getPersistentData().putBoolean("__need__init___", true);
    }

    public static void onNpcSpawn(final NpcSpawn.Spawn event)
    {
        TrainerEventHandler.initTrainer(event.getNpcMob(), event.getReason());
    }

    public static void onNpcTick(final LivingUpdateEvent event)
    {
        if (event.getEntity().getPersistentData().contains("__need__init___"))
        {
            TrainerEventHandler.initTrainer((LivingEntity) event.getEntity(), SpawnReason.NATURAL);
            event.getEntity().getPersistentData().remove("__need__init___");
        }
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(event.getEntityLiving());
        if (pokemobHolder != null)
        {
            final LivingEntity npc = event.getEntityLiving();
            final Brain<?> brain = npc.getBrain();
            if (!brain.hasMemory(MemoryTypes.BATTLETARGET) && brain.hasActivity(Activities.BATTLE)) brain.switchTo(
                    Activity.IDLE);
            pokemobHolder.onTick();
        }
    }

    public static void onBrainInit(final BrainInitEvent event)
    {
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(event.getEntityLiving());
        if (pokemobHolder != null)
        {
            final LivingEntity npc = event.getEntityLiving();
            // Add our task if the dummy not present, this can happen if the
            // brain has reset before
            if (npc instanceof MobEntity && npc.getEntityWorld() instanceof ServerWorld)
            {
                TypeTrainer.addAI((MobEntity) npc);
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Added Tasks: " + npc);
            }
        }
    }

    private static void initTrainer(final LivingEntity mob, final SpawnReason reason)
    {
        if (mob instanceof NpcMob)
        {
            final NpcMob npc = (NpcMob) mob;
            npc.setInitOffers(new NpcOffers(npc));
            npc.setUseOffers(new NpcOffer(npc));
        }

        final IHasPokemobs mobs = TrainerCaps.getHasPokemobs(mob);
        if (mobs == null || !(mob.getEntityWorld() instanceof ServerWorld) || mob instanceof PlayerEntity) return;
        if (mob.getPersistentData().contains("pokeadv_join") && mob.getPersistentData().getLong("pokeadv_join") == mob
                .getEntityWorld().getGameTime()) return;
        mob.getPersistentData().putLong("pokeadv_join", mob.getEntityWorld().getGameTime());

        if (mobs.countPokemon() != 0) return;
        final TypeTrainer newType = TypeTrainer.get(mob, true);
        if (newType == null) return;
        mobs.setType(newType);
        final int level = SpawnHandler.getSpawnLevel(mob.getEntityWorld(), Vector3.getNewVector().set(mob),
                Database.missingno);
        if (mob instanceof TrainerBase) ((TrainerBase) mob).initTeam(level);
        else TypeTrainer.getRandomTeam(mobs, mob, level, mob.getEntityWorld());
        if (mob.addedToChunk) EntityUpdate.sendEntityUpdate(mob);
    }

    /**
     * This deals with the interaction logic for trainers. It sends the
     * messages for MessageState.INTERACT, as well as applies the doAction. It
     * also handles opening the edit gui for the trainers when the player has
     * the trainer editor.
     *
     * @param evt
     * @param target
     */
    public static void processInteract(final PlayerInteractEvent evt, final Entity target)
    {
        if (!(target instanceof LivingEntity)) return;

        final IHasMessages messages = TrainerCaps.getMessages(target);
        final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(target);

        if (evt.getItemStack().getItem() instanceof Linker && Linker.interact((ServerPlayerEntity) evt.getPlayer(),
                target, evt.getItemStack()))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }
        if (target instanceof NpcMob && ((NpcMob) target).getNpcType().getInteraction().processInteract(evt.getPlayer(),
                evt.getHand(), (NpcMob) target))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

        if (messages != null)
        {
            MessageState state = MessageState.INTERACT;
            final AllowedBattle test = pokemobs.canBattle(evt.getPlayer(), true);
            switch (test)
            {
            case NO:
                state = MessageState.INTERACT;
                break;
            case NOTNOW:
                state = MessageState.INTERACT_NOBATTLE;
                break;
            case YES:
                state = MessageState.INTERACT_YESBATTLE;
                break;
            default:
                break;
            }

            // Check if a trade would have been possible, if so, and it is
            // no_battle, set it to interact instead. This prevents duplicated
            // "not want to battle right now" messages
            if (state == MessageState.INTERACT_NOBATTLE && target instanceof TrainerBase)
            {
                final boolean canTrade = ((TrainerBase) target).canTrade(evt.getPlayer());
                if (canTrade) state = MessageState.INTERACT;
            }
            final int timer = evt.getPlayer().ticksExisted;
            if (evt.getPlayer().getPersistentData().getInt("__msg_sent_last_") != timer) messages.sendMessage(state, evt
                    .getPlayer(), target.getDisplayName(), evt.getPlayer().getDisplayName());
            evt.getPlayer().getPersistentData().putInt("__msg_sent_last_", timer);
            if (messages.doAction(state, pokemobs.setLatestContext(new ActionContext(evt.getPlayer(),
                    (LivingEntity) target, evt.getItemStack()))))
            {
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
            }
        }
    }

    public static void onPostDatabaseLoad(final InitDatabase.Post event)
    {
        DBLoader.load();
    }

    public static void onPostServerStart(final FMLServerStartedEvent event)
    {
        TypeTrainer.postInitTrainers();
    }

    public static void captureAttempt(final CaptureEvent.Pre event)
    {
        if (PokecubeAdv.config.canSnagTrainers) return;
        if (TrainerCaps.getHasPokemobs(event.mob) != null && event.caught == null) event.setCanceled(true);
    }

    /**
     * This prevents trainer's pokemobs going to PC
     *
     * @param evt
     */
    public static void onSentToPC(final PCEvent evt)
    {
        final boolean isPlayerOrUnknown = evt.owner == null || evt.players;
        if (isPlayerOrUnknown) return;
        // This prevents the cube from ending up on the ground when recalled
        evt.setCanceled(true);
    }

    /**
     * This sends pokemobs back to their NPC trainers when they are recalled.
     *
     * @param evt
     */
    public static void onRecalledPokemob(final RecallEvent.Pre evt)
    {
        if (evt.recalled.isPlayerOwned()) return;
        final IPokemob recalled = evt.recalled;
        final LivingEntity owner = recalled.getOwner();
        if (owner == null) return;
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(owner);
        if (pokemobHolder != null)
        {
            if (recalled == pokemobHolder.getOutMob()) pokemobHolder.setOutMob(null);
            pokemobHolder.addPokemob(PokecubeManager.pokemobToItem(recalled));
            evt.setCanceled(true);
            recalled.markRemoved();
            recalled.getEntity().remove(false);
        }
    }

    /**
     * This links the pokemob to the trainer when it is sent out.
     *
     * @param evt
     */
    public static void onPostSendOut(final SendOut.Post evt)
    {
        final IPokemob sent = evt.pokemob;
        final LivingEntity owner = sent.getOwner();
        if (owner == null || owner instanceof PlayerEntity) return;
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(owner);
        if (pokemobHolder != null)
        {
            if (pokemobHolder.getOutMob() != null && pokemobHolder.getOutMob() != evt.pokemob)
            {
                pokemobHolder.getOutMob().onRecall();
                pokemobHolder.setOutMob(evt.pokemob);
            }
            else pokemobHolder.setOutMob(evt.pokemob);
            final IHasNPCAIStates aiStates = TrainerCaps.getNPCAIStates(owner);
            if (aiStates != null) aiStates.setAIState(AIState.THROWING, false);
        }
    }

    /**
     * This manages making of trainers invisible if they have been defeated, if
     * this is enabled for the given trainer.
     *
     * @param event
     */
    public static void onWatchTrainer(final StartTracking event)
    {
        if (!(event.getTarget() instanceof TrainerNpc)) return;
        final IHasPokemobs mobs = TrainerCaps.getHasPokemobs(event.getEntity());
        if (!(mobs instanceof DefaultPokemobs)) return;
        final DefaultPokemobs pokemobs = (DefaultPokemobs) mobs;
        if (event.getPlayer() instanceof ServerPlayerEntity)
        {
            final TrainerNpc trainer = (TrainerNpc) event.getTarget();
            if (pokemobs.notifyDefeat)
            {
                final PacketTrainer packet = new PacketTrainer(PacketTrainer.NOTIFYDEFEAT);
                packet.getTag().putInt("I", trainer.getEntityId());
                packet.getTag().putBoolean("V", pokemobs.defeatedBy(event.getPlayer()));
                PacketTrainer.ASSEMBLER.sendTo(packet, (ServerPlayerEntity) event.getPlayer());
            }
        }
    }
}
