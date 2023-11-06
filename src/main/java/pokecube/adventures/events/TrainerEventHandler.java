package pokecube.adventures.events;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.JsonSyntaxException;

import io.netty.buffer.Unpooled;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.network.NetworkHooks;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.capabilities.CapabilityHasTrades.DefaultTrades;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.capabilities.player.PlayerPokemobs;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
import pokecube.adventures.items.Linker;
import pokecube.adventures.network.PacketTrainer;
import pokecube.adventures.utils.DBLoader;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.trainers.IHasMessages;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.IHasPokemobs.AllowedBattle;
import pokecube.api.entity.trainers.IHasRewards.Reward;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.entity.trainers.actions.ActionContext;
import pokecube.api.entity.trainers.actions.MessageState;
import pokecube.api.events.CustomInteractEvent;
import pokecube.api.events.PCEvent;
import pokecube.api.events.combat.JoinBattleEvent;
import pokecube.api.events.npcs.NpcBreedEvent;
import pokecube.api.events.npcs.NpcEvent;
import pokecube.api.events.npcs.NpcSpawn;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.events.pokemobs.RecallEvent;
import pokecube.api.events.pokemobs.SpawnEvent.SendOut;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.events.pokemobs.ai.BrainInitEvent;
import pokecube.api.moves.Battle;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.moves.damage.TerrainDamageSource;
import pokecube.core.utils.EntityTools;
import thut.api.inventory.npc.NpcContainer;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.types.Data_ItemStack;
import thut.core.common.world.mobs.data.types.Data_String;
import thut.wearables.events.WearableDroppedEvent;
import thut.wearables.events.WearableUseEvent;

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
                    + ".named:" + names.get(ThutCore.newRandom().nextInt(names.size())));
        }

        @Override
        public void accept(final MerchantOffers t)
        {
            // We apply trades of last resort. If we got to here, then
            // profession based trades have already been applied if they exist.
            if (!t.isEmpty()) return;

            final Random rand = new Random(this.mob.getUUID().getLeastSignificantBits());
            final String type = this.mob.getNpcType() == NpcType.byType("professor") ? "professor" : "merchant";

            TrainerTrades trades = TypeTrainer.tradesMap.get(type);
            // first prioritise customTrades
            if (!this.mob.customTrades.isEmpty())
            {
                trades = TypeTrainer.tradesMap.get(this.mob.customTrades);
                if (trades != null) trades.addTrades(this.mob, this.mob.getOffers(), rand);
            }
            // Then per type.
            else if (trades != null) trades.addTrades(this.mob, this.mob.getOffers(), rand);
            // Then just add the defaults.
            else this.mob.getOffers().addAll(TypeTrainer.merchant.getRecipes(this.mob, rand));
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
            this.mob.getNpcType();
        }

    }

    public static final ResourceLocation POKEMOBSCAP = new ResourceLocation(PokecubeAdv.MODID, "pokemobs");
    public static final ResourceLocation AICAP = new ResourceLocation(PokecubeAdv.MODID, "ai");
    public static final ResourceLocation MESSAGECAP = new ResourceLocation(PokecubeAdv.MODID, "messages");
    public static final ResourceLocation REWARDSCAP = new ResourceLocation(PokecubeAdv.MODID, "rewards");
    public static final ResourceLocation DATASCAP = new ResourceLocation(PokecubeAdv.MODID, "data");
    public static final ResourceLocation TRADESCAP = new ResourceLocation(PokecubeAdv.MODID, "trades");

    public static void attach_guard(final AttachCapabilitiesEvent<Entity> event)
    {
        IGuardAICapability.addCapability(event);
    }

    public static void attach_pokemobs(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            PlayerPokemobs.register(event);
            return;
        }
        if (!(event.getObject() instanceof Mob mob)) return;
        if (TrainerEventHandler.hasCap(event)) return;

        if (TypeTrainer.get(mob, false) == null) return;

        final DefaultPokemobs mobs = new DefaultPokemobs();
        final DefaultRewards rewards = new DefaultRewards();
        ItemStack stack = ItemStack.EMPTY;
        try
        {
            stack = TrainerEventHandler.fromString(Config.instance.trainer_defeat_reward, event.getObject());
        }
        catch (final CommandRuntimeException e)
        {
            PokecubeAPI.LOGGER.warn("Error with default trainer rewards " + Config.instance.trainer_defeat_reward, e);
        }
        if (!stack.isEmpty()) rewards.getRewards().add(new Reward(stack));
        final DefaultAIStates aiStates = new DefaultAIStates();
        final DefaultMessager messages = new DefaultMessager();
        mobs.init(mob, aiStates, messages, rewards);
        event.addCapability(TrainerEventHandler.POKEMOBSCAP, mobs);
        event.addCapability(TrainerEventHandler.AICAP, aiStates);
        event.addCapability(TrainerEventHandler.MESSAGECAP, messages);
        event.addCapability(TrainerEventHandler.REWARDSCAP, rewards);

        if (mob instanceof TrainerBase) event.addCapability(TrainerEventHandler.TRADESCAP, new DefaultTrades());

        DataSync data = DataSync_Impl.getData(event);
        if (data == null)
        {
            data = new DataSync_Impl();
            event.addCapability(TrainerEventHandler.DATASCAP, (DataSync_Impl) data);
        }
        mobs.setDataSync(data);
        mobs.holder.TYPE = data.register(new Data_String(), "");

        for (int i = 0; i < 6; i++) mobs.holder.POKEMOBS[i] = data.register(new Data_ItemStack(), ItemStack.EMPTY);

        if (PokecubeCore.getConfig().debug_spawning)
            PokecubeAPI.logInfo("Initializing caps " + event.getObject() + " " + event.getObject().isAlive());
    }

    public static void onAttachMobCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        // Add capabilities for guard AI
        TrainerEventHandler.attach_guard(event);
        // Add pokemob holder caps
        TrainerEventHandler.attach_pokemobs(event);
    }

    public static ItemStack fromString(final String arg, final Entity sender) throws CommandRuntimeException
    {
        Drop drop;
        try
        {
            drop = JsonUtil.gson.fromJson(arg, Drop.class);
            return Tools.getStack(drop.getValues(), sender.getLevel() instanceof ServerLevel level ? level : null);
        }
        catch (final JsonSyntaxException e)
        {
            PokecubeAPI.LOGGER.error("Error loading drops from string {} for mob {}", arg, sender);
            PokecubeAPI.LOGGER.error(e);
            return ItemStack.EMPTY;
        }
    }

    private static boolean hasCap(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(TrainerEventHandler.POKEMOBSCAP)) return true;
        for (final ICapabilityProvider provider : event.getCapabilities().values())
            if (provider.getCapability(TrainerCaps.HASPOKEMOBS_CAP).isPresent()) return true;
        return false;
    }

    public static void onEntityInteract(final CustomInteractEvent evt)
    {
        TrainerEventHandler.processInteract(evt, evt.getTarget());
    }

    public static void onNpcBreedCheck(final NpcBreedEvent.Check evt)
    {
        final IHasNPCAIStates ai = TrainerCaps.getNPCAIStates(evt.getEntity());
        if (ai != null && !ai.getAIState(AIState.MATES)) evt.setCanceled(true);
    }

    /**
     * This manages invulnerability of npcs to pokemobs, as well as managing the
     * target allocation for trainers.
     *
     * @param evt
     */
    public static void onLivingHurt(final LivingHurtEvent evt)
    {
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(evt.getEntity());
        final IHasMessages messages = TrainerCaps.getMessages(evt.getEntity());

        if (evt.getEntity() instanceof Npc && !Config.instance.pokemobsHarmNPCs
                && (evt.getSource() instanceof PokemobDamageSource || evt.getSource() instanceof TerrainDamageSource))
            evt.setAmount(0);

        if (evt.getSource().getEntity() instanceof LivingEntity mob)
        {
            if (messages != null)
            {
                messages.sendMessage(MessageState.HURT, mob, evt.getEntity().getDisplayName(),
                        evt.getSource().getEntity().getDisplayName());
                messages.doAction(MessageState.HURT, new ActionContext(mob, evt.getEntityLiving(), evt.getSource()));
            }
            if (pokemobHolder != null && pokemobHolder.getTarget() == null) pokemobHolder.onSetTarget(mob);
        }
    }

    public static Function<LivingEntity, Integer> goodKill = (e) -> {
        // The VillagerEntity.sawMurder handles this case just fine.
        if (e instanceof Villager) return 0;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        if (pokemob != null)
            return pokemob.getGeneralState(GeneralStates.TAMED) ? PokecubeAdv.config.trainer_tame_kill_rep
                    : PokecubeAdv.config.trainer_wild_kill_rep;
        return 0;
    };

    public static void onLivingDeath(final LivingDeathEvent event)
    {
        final DamageSource source = event.getSource();
        final Entity user = source.getEntity();
        if (user instanceof ServerPlayer murderer)
        {
            final LivingEntity mob = event.getEntityLiving();
            // Check if the target was a wild pokemob.
            final int repGain = TrainerEventHandler.goodKill.apply(mob);
            if (repGain != 0 && mob.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
            {
                final GossipType type = repGain > 0 ? GossipType.MINOR_POSITIVE : GossipType.MINOR_NEGATIVE;
                final Optional<NearestVisibleLivingEntities> optional = mob.getBrain()
                        .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
                if (optional.isPresent())
                {
                    final Iterable<LivingEntity> mobs = optional.get().findAll(seen -> seen instanceof Villager);
                    mobs.forEach((gossipTarget) -> {
                        final Villager villager = (Villager) gossipTarget;
                        villager.getGossips().add(murderer.getUUID(), type, repGain);
                    });
                }
            }
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
        if (!(event.getWorld() instanceof ServerLevel)) return;
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
            TrainerEventHandler.initTrainer((LivingEntity) event.getEntity(), MobSpawnType.NATURAL);
            event.getEntity().getPersistentData().remove("__need__init___");
        }
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(event.getEntityLiving());
        if (pokemobHolder != null)
        {
            final LivingEntity npc = event.getEntityLiving();
            final Brain<?> brain = npc.getBrain();
            if (!brain.hasMemoryValue(MemoryTypes.BATTLETARGET.get()) && brain.isActive(Activities.BATTLE.get()))
                brain.setActiveActivityIfPossible(Activity.IDLE);
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
            if (npc instanceof Mob mob && npc.getLevel() instanceof ServerLevel)
            {
                TypeTrainer.addAI(mob);
                if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Added Tasks: " + npc);
            }
        }
    }

    public static void onBattleJoin(JoinBattleEvent event)
    {
        final IHasNPCAIStates holderA = TrainerCaps.getNPCAIStates(event.mobA);
        final IHasNPCAIStates holderB = TrainerCaps.getNPCAIStates(event.mobB);
        if (holderA != null && holderA.getAIState(AIState.PERMFRIENDLY)) event.setCanceled(true);
        if (holderB != null && holderB.getAIState(AIState.PERMFRIENDLY)) event.setCanceled(true);
    }

    private static void initTrainer(final LivingEntity mob, final MobSpawnType reason)
    {
        if (mob instanceof NpcMob)
        {
            final NpcMob npc = (NpcMob) mob;
            npc.setInitOffers(new NpcOffers(npc));
            npc.setUseOffers(new NpcOffer(npc));
        }

        final IHasPokemobs mobs = TrainerCaps.getHasPokemobs(mob);
        if (mobs == null || !(mob.getLevel() instanceof ServerLevel slevel) || mob instanceof Player) return;
        if (mob.getPersistentData().contains("pokeadv_join")
                && mob.getPersistentData().getLong("pokeadv_join") == mob.getLevel().getGameTime())
            return;
        mob.getPersistentData().putLong("pokeadv_join", mob.getLevel().getGameTime());

        if (mobs.countPokemon() != 0) return;
        final TypeTrainer newType = TypeTrainer.get(mob, true);
        if (newType == null) return;
        mobs.setType(newType);
        SpawnContext context = new SpawnContext(slevel, Database.missingno, new Vector3().set(mob));
        final int level = SpawnHandler.getSpawnLevel(context);
        if (mob instanceof TrainerBase npc) npc.initTeam(level);
        else TypeTrainer.getRandomTeam(mobs, mob, level, mob.getLevel());
        if (mob.isAddedToWorld()) EntityUpdate.sendEntityUpdate(mob);
    }

    /**
     * This deals with the interaction logic for trainers. It sends the messages
     * for MessageState.INTERACT, as well as applies the doAction. It also
     * handles opening the edit gui for the trainers when the player has the
     * trainer editor.
     *
     * @param evt
     * @param target
     */
    public static void processInteract(final PlayerInteractEvent evt, final Entity target)
    {
        LivingEntity living = EntityTools.getCoreLiving(target);
        if (living == null) return;

        Player player = evt.getPlayer();
        InteractionHand hand = evt.getHand();

        final IHasMessages messages = TrainerCaps.getMessages(target);
        final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(target);

//        // EXAMPLE OF SPLICING GENES FROM ITEMS INTO LIVING MOBS
//        var key = RegHelper.getKey(evt.getItemStack());
//        gene_splice:
//        if (key.toString().contains("dna_splicer"))
//        {
//            IMobGenetics newGenes = GeneticsManager.getGenes(evt.getItemStack());
//            if (player.isShiftKeyDown())
//            {
//                living = player;
//                if (newGenes.getAlleles().isEmpty())
//                {
//                    var g1 = new SpeciesGene();
//                    var g2 = new SpeciesGene();
//                    newGenes.setGenes(g1, g2);
//                }
//            }
//            IMobGenetics mobGenes = living.getCapability(ThutCaps.GENETICS_CAP, null).orElse(null);
//            if (newGenes == null || mobGenes == null) break gene_splice;
//            ItemStack tmp = new ItemStack(Items.BOOK);
//            tmp.setTag(new CompoundTag());
//            ListTag list = new ListTag();
//            list.add(StringTag.valueOf("all"));
//            tmp.getTag().put("Pages", list);
//            var selector = new ItemBasedSelector(tmp);
//            ItemStack bottle = new ItemStack(Items.POTION);
//            IMobGenetics bottleGenes = GeneticsManager.getGenes(bottle);
//            for (var _key : mobGenes.getKeys()) bottleGenes.getAlleles().put(_key, mobGenes.getAlleles().get(_key));
//            ClonerHelper.spliceGenes(newGenes, bottle, selector);
//            for (var _key : bottleGenes.getKeys())
//            {
//                var alleles = bottleGenes.getAlleles().get(_key);
//                alleles.setChangeListeners(mobGenes.getChangeListeners());
//                mobGenes.getAlleles().put(_key, alleles);
//                alleles.onChanged();
//            }
//            if (player instanceof ServerPlayer splayer)
//            {
//                for (final Alleles<?, ?> allele : mobGenes.getAlleles().values())
//                    PacketSyncGene.syncGeneToTracking(living, allele);
//                EntityUpdate.sendEntityUpdate(living);
//            }
//            evt.setCanceled(true);
//            return;
//        }

        InteractionResult succeed = InteractionResult.sidedSuccess(target.level.isClientSide);

        if (target instanceof Villager vill)
        {
            NpcEvent.OpenInventory event = new NpcEvent.OpenInventory(vill);
            ThutCore.FORGE_BUS.post(event);

            boolean creativeStick = player.isCreative() && player.getItemInHand(hand).getItem() == Items.STICK;

            if (event.getResult() == Result.ALLOW || creativeStick)
            {
                if (player instanceof ServerPlayer sp)
                {
                    final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
                    buffer.writeInt(vill.getId());
                    final SimpleMenuProvider provider = new SimpleMenuProvider(
                            (i, p, e) -> new NpcContainer(i, p, buffer), vill.getDisplayName());
                    NetworkHooks.openGui(sp, provider, buf -> {
                        buf.writeInt(vill.getId());
                    });
                }
                evt.setCanceled(true);
                evt.setCancellationResult(succeed);
            }
        }
        if (evt.getItemStack().getItem() instanceof Linker && player instanceof ServerPlayer sp
                && Linker.interact(sp, target, evt.getItemStack()))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(succeed);
            return;
        }
        if (target instanceof NpcMob npc
                && npc.getNpcType().getInteraction().processInteract(player, evt.getHand(), npc))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(succeed);
            return;
        }

        boolean filled_cube = PokecubeManager.isFilled(evt.getItemStack());

        if (messages != null)
        {
            MessageState state = MessageState.INTERACT;

            if (filled_cube)
            {
                final AllowedBattle test = pokemobs.canBattle(player, true);
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
            }
            // Check if a trade would have been possible, if so, and it is
            // no_battle, set it to interact instead. This prevents duplicated
            // "not want to battle right now" messages
            if (state == MessageState.INTERACT_NOBATTLE && target instanceof TrainerBase npc)
            {
                final boolean canTrade = npc.canTrade(player);
                if (canTrade) state = MessageState.INTERACT;
            }
            final int timer = player.tickCount;
            if (player.getPersistentData().getInt("__msg_sent_last_") == timer)
            {
                evt.setCanceled(true);
                evt.setCancellationResult(succeed);
            }
            else
            {
                if (player.getPersistentData().getInt("__msg_sent_last_") != timer)
                    messages.sendMessage(state, player, target.getDisplayName(), player.getDisplayName());
                player.getPersistentData().putInt("__msg_sent_last_", timer);
                if (messages.doAction(state,
                        pokemobs.setLatestContext(new ActionContext(player, living, evt.getItemStack()))))
                {
                    evt.setCanceled(true);
                    evt.setCancellationResult(succeed);
                }
            }

        }
    }

    private static final ResourceLocation BELT = new ResourceLocation(PokecubeAdv.MODID, "poke_belt");

    public static void onWearableUse(WearableUseEvent event)
    {
        if (ItemList.is(BELT, event.context.getItemInHand())
                && event.context.getPlayer() instanceof ServerPlayer player)
        {
            final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
            buffer.writeInt(player.getId());
            final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new ContainerTrainer(i, p, buffer),
                    player.getDisplayName());
            NetworkHooks.openGui(player, provider, buf -> {
                buf.writeInt(player.getId());
            });
        }
    }

    public static void dropBelt(final WearableDroppedEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(player);
        if (!ItemList.is(BELT, event.getToDrop())) return;
        if (pokemobs == null) return;
        LivingEntity mob = event.getParent().getEntityLiving();
        for (int i = 0; i < pokemobs.getContainerSize(); i++)
        {
            ItemStack stack = pokemobs.getItem(i);
            if (stack.isEmpty()) continue;
            final double d0 = mob.getY() - 0.3D + mob.getEyeHeight();
            final ItemEntity drop = new ItemEntity(mob.getLevel(), mob.getX(), d0, mob.getZ(), stack);
            final float f = mob.getRandom().nextFloat() * 0.5F;
            final float f1 = mob.getRandom().nextFloat() * ((float) Math.PI * 2F);
            drop.setDeltaMovement(-Mth.sin(f1) * f, Mth.cos(f1) * f, 0.2);
            pokemobs.setItem(i, ItemStack.EMPTY);
            event.getParent().getDrops().add(drop);
        }
    }

    public static void onPostServerStart(final ServerAboutToStartEvent event)
    {
        DBLoader.load();
        TypeTrainer.postInitTrainers();
    }

    public static void captureAttempt(final CaptureEvent.Pre event)
    {
        if (PokecubeAdv.config.canSnagTrainers) return;
        if (TrainerCaps.getHasPokemobs(event.mob) != null && event.getCaught() == null) event.setCanceled(true);
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
        if (PokecubeMod.fakeUUID.equals(evt.owner)) return;
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

            // If the npc was battling, we need to ensure that the target
            // pokemob has a cooldown set, otherwise it might auto-switch to us
            // directly.
            if (recalled.getMoveStats().targetEnemy != null)
            {
                IPokemob targetMob = PokemobCaps.getPokemobFor(recalled.getMoveStats().targetEnemy);
                if (targetMob != null)
                {
                    // If we have a new pokemob to send out, add an attack
                    // cooldown for the pokemob.
                    if (!pokemobHolder.getNextPokemob().isEmpty())
                    {
                        targetMob.setAttackCooldown(PokecubeAdv.config.trainerSendOutDelay);
                    }
                    else
                    {
                        // Otherwise, remove us from the battle.
                        Battle b = Battle.getBattle(owner);
                        if (b != null) b.removeFromBattle(owner);
                    }
                }
            }
            pokemobHolder.addPokemob(PokecubeManager.pokemobToItem(recalled));
            evt.setCanceled(true);
            recalled.markRemoved();
            recalled.getEntity().remove(RemovalReason.DISCARDED);
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
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(owner);
        if (owner == null || owner instanceof Player)
        {
            if (pokemobHolder != null)
            {
                pokemobHolder.setOutMob(evt.pokemob);
            }
            return;
        }
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
        if (!(event.getTarget() instanceof TrainerNpc trainer)) return;
        final IHasPokemobs mobs = TrainerCaps.getHasPokemobs(event.getEntity());
        if (!(mobs instanceof DefaultPokemobs pokemobs)) return;
        if (pokemobs.notifyDefeat && event.getPlayer() instanceof ServerPlayer player)
        {
            final PacketTrainer packet = new PacketTrainer(PacketTrainer.NOTIFYDEFEAT);
            packet.getTag().putInt("I", trainer.getId());
            packet.getTag().putBoolean("V", pokemobs.defeatedBy(player));
            PacketTrainer.ASSEMBLER.sendTo(packet, player);
        }
    }
}
