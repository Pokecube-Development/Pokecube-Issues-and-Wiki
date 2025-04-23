package pokecube.adventures.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.player.PlayerPokemobs;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
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
import pokecube.core.database.Database;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.moves.damage.TerrainDamageSource;
import thut.api.inventory.npc.NpcContainer;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.wearables.events.WearableDroppedEvent;
import thut.wearables.events.WearableUseEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class TrainerEventHandler
{

    private record NpcOffers(NpcMob mob) implements Consumer<MerchantOffers>
    {
        private NpcOffers(final NpcMob mob)
        {
            this.mob = mob;

            // Check for blank name, and if so, randomize it.
            final List<String> names = mob.isMale() ? TypeTrainer.maleNames : TypeTrainer.femaleNames;
            if (!names.isEmpty() && mob.getNPCName().isEmpty()) mob.setNPCName(
                    "pokecube." + mob.getNpcType().getName() + ".named:" + names.get(
                            ThutCore.newRandom().nextInt(names.size())));
        }

        @Override
        public void accept(final MerchantOffers t)
        {
            // We apply trades of last resort. If we got to here, then
            // profession based trades have already been applied if they exist.
            if (!t.isEmpty()) return;

            final RandomSource rand = this.mob.getRandom();
            rand.setSeed(this.mob.getUUID().getLeastSignificantBits());
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

    private record NpcOffer(NpcMob mob) implements Consumer<MerchantOffer>
    {
        @Override
        public void accept(final MerchantOffer t)
        {
            this.mob.getNpcType();
        }
    }

    public static void entityLivingConstruct(EntityEvent.EntityConstructing event)
    {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living instanceof Player player)
        {
            if (player.level().isClientSide()) PlayerPokemobs.register(player);
            return;
        }

        if (!(living instanceof Mob mob)) return;
        if (TypeTrainer.get(mob, false) == null) return;

        var mobs = mob.getData(TrainerCaps.TRAINER);
        if (!(mobs instanceof DefaultPokemobs pmobs)) return;
        ItemStack stack = ItemStack.EMPTY;
        try
        {
            stack = TrainerEventHandler.fromString(Config.instance.trainer_defeat_reward, mob);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.warn("Error with default trainer rewards {}", Config.instance.trainer_defeat_reward, e);
        }
        pmobs.init(mob);
        var rewards = pmobs.rewards;
        if (!stack.isEmpty() && rewards.getRewards().isEmpty()) rewards.getRewards().add(new Reward(stack));

        mob.setData(TrainerCaps.TRAINER, mobs);

        DataSync data = DataSync_Impl.get(mob);
        mobs.setDataSync(data);

        if (PokecubeCore.getConfig().debug_spawning)
            PokecubeAPI.logInfo("Initializing caps " + mob + " " + mob.isAlive());
    }

    public static ItemStack fromString(final String arg, final Entity sender)
    {
        JsonElement drop;
        try
        {
            drop = JsonUtil.gson.fromJson(arg, JsonObject.class);
            return Tools.getStack(drop, sender.level() instanceof ServerLevel level ? level : null);
        }
        catch (final JsonSyntaxException e)
        {
            PokecubeAPI.LOGGER.error("Error loading drops from string {} for mob {}", arg, sender);
            PokecubeAPI.LOGGER.error(e);
            return ItemStack.EMPTY;
        }
    }

    public static void onNpcBreedCheck(final NpcBreedEvent.Check evt)
    {
        final IHasNPCAIStates ai = TrainerCaps.getNPCAIStates(evt.getEntity());
        if (ai != null && !ai.getAIState(AIState.MATES)) evt.setCanceled(true);
    }

    /**
     * This manages invulnerability of npcs to pokemobs, as well as managing the target allocation for trainers.
     */
    public static void onLivingHurt(final LivingDamageEvent.Pre evt)
    {
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(evt.getEntity());
        final IHasMessages messages = TrainerCaps.getMessages(evt.getEntity());

        if (evt.getEntity() instanceof Npc && !Config.instance.pokemobsHarmNPCs && (
                evt.getSource() instanceof PokemobDamageSource || evt.getSource() instanceof TerrainDamageSource))
            evt.setNewDamage(0);

        if (evt.getSource().getEntity() instanceof LivingEntity mob)
        {
            if (messages != null)
            {
                messages.sendMessage(MessageState.HURT, mob, evt.getEntity().getDisplayName(),
                        evt.getSource().getEntity().getDisplayName());
                messages.doAction(MessageState.HURT, new ActionContext(mob, evt.getEntity(), evt.getSource()));
            }
            if (pokemobHolder != null && pokemobHolder.getTarget() == null) pokemobHolder.onSetTarget(mob);
        }
    }

    public static Function<LivingEntity, Integer> goodKill = (e) -> {
        // The VillagerEntity.sawMurder handles this case just fine.
        if (e instanceof Villager) return 0;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        if (pokemob != null) return pokemob.getGeneralState(GeneralStates.TAMED)
                ? PokecubeAdv.config.trainer_tame_kill_rep
                : PokecubeAdv.config.trainer_wild_kill_rep;
        return 0;
    };

    public static void onLivingDeath(final LivingDeathEvent event)
    {
        final DamageSource source = event.getSource();
        final Entity user = source.getEntity();
        if (user instanceof ServerPlayer murderer)
        {
            final LivingEntity mob = event.getEntity();
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

    public static void onNpcSpawn(final NpcSpawn.Spawn event)
    {
        TrainerEventHandler.initTrainer(event.getNpcMob(), event.getReason());
    }

    public static void onNpcTick(final EntityTickEvent.Post event)
    {
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(event.getEntity());
        if (pokemobHolder != null && event.getEntity() instanceof LivingEntity npc)
        {
            final Brain<?> brain = npc.getBrain();
            if (!brain.hasMemoryValue(MemoryTypes.BATTLETARGET.get()) && brain.isActive(Activities.BATTLE.get()))
                brain.setActiveActivityIfPossible(Activity.IDLE);
            if (pokemobHolder.getTrainer() != npc) pokemobHolder.init(npc);
            pokemobHolder.onTick();
        }
    }

    public static void onBrainInit(final BrainInitEvent event)
    {
        final IHasPokemobs pokemobHolder = TrainerCaps.getHasPokemobs(event.getEntity());
        if (pokemobHolder != null)
        {
            final LivingEntity npc = event.getEntity();
            if (pokemobHolder.getTrainer() != npc) pokemobHolder.init(npc);
            // Add our task if the dummy not present, this can happen if the
            // brain has reset before
            if (npc instanceof Mob mob && npc.level() instanceof ServerLevel)
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

    public static void initTrainer(final LivingEntity mob, final MobSpawnType reason)
    {
        if (mob instanceof NpcMob npc)
        {
            npc.setInitOffers(new NpcOffers(npc));
            npc.setUseOffers(new NpcOffer(npc));
        }

        final IHasPokemobs mobs = TrainerCaps.getHasPokemobs(mob);
        if (mobs == null || !(mob.level() instanceof ServerLevel slevel) || mob instanceof Player) return;
        if (mob.getPersistentData().contains("pokeadv_join")
                && mob.getPersistentData().getLong("pokeadv_join") == mob.level().getGameTime()) return;
        mob.getPersistentData().putLong("pokeadv_join", mob.level().getGameTime());

        if (mobs.countPokemon() != 0) return;
        final TypeTrainer newType = TypeTrainer.get(mob, true);
        if (newType == null) return;
        mobs.setType(newType);
        SpawnContext context = new SpawnContext(slevel, Database.missingno, new Vector3().set(mob));
        final int level = SpawnHandler.getSpawnLevel(context);
        TrainerSpawnHandler.initTrainer(mobs, level);
        if (mob.isAddedToLevel()) EntityUpdate.sendEntityUpdate(mob);
    }

    /**
     * This deals with the interaction logic for trainers. It sends the messages for MessageState.INTERACT, as well as
     * applies the doAction. It also handles opening the edit gui for the trainers when the player has the trainer
     * editor.
     *
     * @param evt event
     */
    public static void processInteract(final PlayerInteractEvent.EntityInteract evt)
    {
        var target = evt.getTarget();
        if (!(target instanceof LivingEntity living)) return;

        Player player = evt.getEntity();
        InteractionHand hand = evt.getHand();

        final IHasMessages messages = TrainerCaps.getMessages(target);
        final IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(target);

        InteractionResult succeed = InteractionResult.sidedSuccess(target.level.isClientSide);

        if (target instanceof Villager vill)
        {
            NpcEvent.OpenInventory event = new NpcEvent.OpenInventory(vill);
            ThutCore.FORGE_BUS.post(event);

            boolean creativeStick = player.isCreative() && player.getItemInHand(hand).getItem() == Items.STICK;

            if (event.getResult() == TriState.TRUE || creativeStick)
            {
                if (player instanceof ServerPlayer sp)
                {
                    final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
                    buffer.writeInt(vill.getId());
                    final SimpleMenuProvider provider = new SimpleMenuProvider(
                            (i, p, e) -> new NpcContainer(i, p, buffer), vill.getDisplayName());
                    sp.openMenu(provider, buf -> buf.writeInt(vill.getId()));
                }
                evt.setCanceled(true);
                evt.setCancellationResult(succeed);
            }
        }
        if (target instanceof NpcMob npc && npc.getNpcType().getInteraction()
                .processInteract(player, evt.getHand(), npc))
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

    private static final ResourceLocation BELT = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID, "poke_belt");

    public static void onWearableUse(WearableUseEvent event)
    {
        if (ItemList.is(BELT, event.context.getItemInHand())
                && event.context.getPlayer() instanceof ServerPlayer player)
        {
            final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
            buffer.writeInt(player.getId());
            final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new ContainerTrainer(i, p, buffer),
                    player.getDisplayName());
            player.openMenu(provider, buf -> buf.writeInt(player.getId()));
        }
    }

    public static void dropBelt(final WearableDroppedEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        IHasPokemobs pokemobs = TrainerCaps.getHasPokemobs(player);
        if (!ItemList.is(BELT, event.getToDrop())) return;
        if (pokemobs == null) return;
        LivingEntity mob = event.getParent().getEntity();
        for (int i = 0; i < pokemobs.getContainerSize(); i++)
        {
            ItemStack stack = pokemobs.getItem(i);
            if (stack.isEmpty()) continue;
            final double d0 = mob.getY() - 0.3D + mob.getEyeHeight();
            final ItemEntity drop = new ItemEntity(mob.level(), mob.getX(), d0, mob.getZ(), stack);
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
                pokemobHolder.setOutMob(sent);
            }
            return;
        }
        if (pokemobHolder != null)
        {
            if (pokemobHolder.getOutMob() != null && pokemobHolder.getOutMob() != sent)
            {
                pokemobHolder.getOutMob().onRecall();
                pokemobHolder.setOutMob(sent);
            }
            else pokemobHolder.setOutMob(sent);
            final IHasNPCAIStates aiStates = TrainerCaps.getNPCAIStates(owner);
            if (aiStates != null) aiStates.setAIState(AIState.THROWING, false);
        }
    }

    /**
     * This manages making of trainers invisible if they have been defeated, if this is enabled for the given trainer.
     */
    public static void onWatchTrainer(final StartTracking event)
    {
        if (!(event.getTarget() instanceof TrainerNpc trainer)) return;
        final IHasPokemobs mobs = TrainerCaps.getHasPokemobs(event.getEntity());
        if (!(mobs instanceof DefaultPokemobs pokemobs)) return;
        if (pokemobs.notifyDefeat && event.getEntity() instanceof ServerPlayer player)
        {
            final PacketTrainer packet = new PacketTrainer(PacketTrainer.NOTIFYDEFEAT);
            packet.getTag().putInt("I", trainer.getId());
            packet.getTag().putBoolean("V", pokemobs.defeatedBy(player));
            PacketTrainer.ASSEMBLER.sendTo(packet.getTag(), player);
        }
    }
}
