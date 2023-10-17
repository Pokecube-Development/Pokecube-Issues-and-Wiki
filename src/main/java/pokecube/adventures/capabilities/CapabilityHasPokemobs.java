package pokecube.adventures.capabilities;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.adventures.Config;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.adventures.network.PacketTrainer;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasMessages;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.IHasRewards;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.entity.trainers.actions.ActionContext;
import pokecube.api.entity.trainers.actions.MessageState;
import pokecube.api.events.npcs.TrainerInteractEvent;
import pokecube.api.events.npcs.TrainerInteractEvent.CanInteract;
import pokecube.api.items.IPokecube;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.npc.Activities;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.DataSync;

public class CapabilityHasPokemobs
{
    public static class DefaultPokemobs implements IHasPokemobs
    {
        public static class DataParamHolder
        {
            public int TYPE;
            public int[] POKEMOBS = new int[6];
        }

        public static class DefeatEntry implements Comparable<DefeatEntry>
        {
            public static DefeatEntry createFromNBT(final CompoundTag nbt)
            {
                String defeater = nbt.getString("name");
                long time = nbt.getLong("time");
                short resetKey = nbt.getShort("resetKey");
                return new DefeatEntry(defeater, time, resetKey);
            }

            final String id;
            short resetKey = 0;
            long time;

            public DefeatEntry(final String id, final long time, short resetKey)
            {
                this.id = id;
                this.time = time;
                this.resetKey = resetKey;
            }

            @Override
            public int compareTo(final DefeatEntry o)
            {
                return this.id.compareTo(o.id) + Short.compare(resetKey, o.resetKey);
            }

            @Override
            public boolean equals(final Object other)
            {
                if (other instanceof DefeatEntry entry) return entry.id.equals(this.id) && resetKey == entry.resetKey;
                return false;
            }

            @Override
            public int hashCode()
            {
                return this.id.hashCode();
            }

            void writeToNBT(final CompoundTag nbt)
            {
                nbt.putString("name", this.id);
                nbt.putLong("time", this.time);
                nbt.putShort("resetKey", resetKey);
            }
        }

        public static class DefeatList implements INBTSerializable<CompoundTag>
        {
            private final Map<String, DefeatEntry> map = Maps.newHashMap();

            public void clear()
            {
                this.map.clear();
            }

            public boolean isValid(final Entity in, final long resetTime, short resetKey)
            {
                if (in == null) return false;
                if (!this.map.containsKey(in.getStringUUID())) return false;
                final var s = this.map.get(in.getStringUUID());
                // If reset key does not match, invalidate the entry.
                if (s.resetKey != resetKey)
                {
                    map.remove(in.getStringUUID());
                    return false;
                }
                // If this is the case, then this mob is not re-battleable.
                if (resetTime <= 0) return true;
                // Otherwise check the diff.
                final long diff = Tracker.instance().getTick() - s.time;
                if (diff > resetTime) return false;
                return true;
            }

            public void validate(final Entity in, short key)
            {
                if (in == null) return;
                final var s = this.map.getOrDefault(in.getStringUUID(), new DefeatEntry(in.getStringUUID(), 0, key));
                s.time = Tracker.instance().getTick();
                this.map.put(in.getStringUUID(), s);
            }

            public void load(final ListTag list)
            {
                this.clear();
                for (int i = 0; i < list.size(); i++)
                {
                    final DefeatEntry d = DefeatEntry.createFromNBT(list.getCompound(i));
                    if (d.id.isEmpty()) continue;
                    this.map.put(d.id, d);
                }
            }

            public ListTag save()
            {
                final ListTag list = new ListTag();
                for (final DefeatEntry entry : this.map.values())
                {
                    final CompoundTag CompoundNBT = new CompoundTag();
                    entry.writeToNBT(CompoundNBT);
                    list.add(CompoundNBT);
                }
                return list;
            }

            @Override
            public CompoundTag serializeNBT()
            {
                CompoundTag tag = new CompoundTag();
                tag.put("list", this.save());
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag nbt)
            {
                this.load(nbt.getList("list", 10));
            }
        }

        static
        {
            PokecubePlayerCustomData.registerDataType("npcs_defeated", DefeatList::new);
            PokecubePlayerCustomData.registerDataType("npcs_defeated_by", DefeatList::new);
        }

        private final LazyOptional<IHasPokemobs> cap_holder = LazyOptional.of(() -> this);

        public long resetTimeLose = 0;
        public long resetTimeWin = 0;
        public int friendlyCooldown = 0;

        // Should the client be notified of the defeat via a packet?
        public boolean notifyDefeat = false;

        // This is the reference cooldown.
        public int battleCooldown = -1;
        private byte gender = 0;
        private int number = Integer.MAX_VALUE;
        private LivingEntity user;
        private IHasNPCAIStates aiStates;
        private IHasMessages messages;
        private IHasRewards rewards;
        private int nextSlot;
        // Cooldown between sending out pokemobs
        private int attackCooldown = 0;

        // Cooldown between agression
        private long cooldown = 0;
        private int sight = -1;
        private TypeTrainer type;
        private short defeatResetKey = 0;
        private UUID outID;
        private boolean canMegaEvolve = false;
        private IPokemob outMob;
        private LevelMode levelmode = LevelMode.CONFIG;

        ActionContext context;

        private final Set<ITargetWatcher> watchers = Sets.newHashSet();

        public final DataParamHolder holder = new DataParamHolder();
        private DataSync datasync;

        public DefaultPokemobs()
        {}

        @Override
        public void addTargetWatcher(final ITargetWatcher watcher)
        {
            IHasPokemobs.super.addTargetWatcher(watcher);
            this.watchers.add(watcher);
        }

        @Override
        public AllowedBattle canBattle(final LivingEntity target, final boolean checkWatcher)
        {
            boolean ignoreBattled = false;
            if (checkWatcher && target != null) for (final ITargetWatcher w : this.watchers)
                ignoreBattled = ignoreBattled || w.ignoreHasBattled(target);

            // No battling if we have defeated or been defeated
            if (!ignoreBattled) if (this.defeatedBy(target) || this.defeated(target)) return AllowedBattle.NOTNOW;

            final IHasPokemobs trainer = TrainerCaps.getHasPokemobs(target);
            // No battling a target already battling something
            if (trainer != null && trainer.getTarget() != null) return AllowedBattle.NOTNOW;
            // Not checking watchers, return true
            if (!checkWatcher) return AllowedBattle.YES;
            // Valid if any watchers say so
            for (final ITargetWatcher w : this.watchers) if (w.isValidTarget(target)) return AllowedBattle.YES;
            // Otherwise false.
            return AllowedBattle.NOTNOW;
        }

        @Override
        public boolean canMegaEvolve()
        {
            return this.canMegaEvolve;
        }

        public void checkDefeatAchievement(final Player player)
        {
            if (!(this.user instanceof TrainerBase trainer)) return;
            final boolean leader = this.user instanceof LeaderNpc;
            if (leader) Triggers.BEATLEADER.trigger((ServerPlayer) player, trainer);
            else Triggers.BEATTRAINER.trigger((ServerPlayer) player, trainer);
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            if (nbt.contains("pokemobs", 9))
            {
                if (this.clearOnLoad()) this.clearContent();
                final ListTag ListNBT = nbt.getList("pokemobs", 10);
                if (ListNBT.size() != 0) for (int i = 0; i < Math.min(ListNBT.size(), this.getMaxPokemobCount()); ++i)
                    this.setPokemob(i, ItemStack.of(ListNBT.getCompound(i)));
            }
            this.initCount();
            this.setType(TypeTrainer.getTrainer(nbt.getString("type"), true));
            this.setCooldown(nbt.getLong("nextBattle"));
            if (nbt.contains("outPokemob")) this.setOutID(UUID.fromString(nbt.getString("outPokemob")));
            this.setNextSlot(nbt.getInt("nextSlot"));
            this.setCanMegaEvolve(nbt.getBoolean("megaevolves"));
            if (nbt.contains("gender")) this.setGender(nbt.getByte("gender"));
            if (this.getNextSlot() >= this.getMaxPokemobCount()) this.setNextSlot(0);
            this.sight = nbt.contains("sight") ? nbt.getInt("sight") : -1;
            if (nbt.contains("battleCD")) this.battleCooldown = nbt.getInt("battleCD");
            if (this.battleCooldown < 0) this.battleCooldown = Config.instance.trainerCooldown;
            this.defeatResetKey = nbt.getShort("defeatResetKey");
            if (nbt.contains("resetTime")) this.resetTimeLose = nbt.getLong("resetTime");
            if (nbt.contains("resetTimeWin")) this.resetTimeWin = nbt.getLong("resetTimeWin");
            this.notifyDefeat = nbt.getBoolean("notifyDefeat");
            this.friendlyCooldown = nbt.getInt("friendly");
            if (nbt.contains("levelMode")) this.setLevelMode(LevelMode.valueOf(nbt.getString("levelMode")));
        }

        @Override
        public int getAgressDistance()
        {
            return this.sight <= 0 ? Config.instance.trainerSightRange : this.sight;
        }

        @Override
        public int getAttackCooldown()
        {
            return this.attackCooldown;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return TrainerCaps.HASPOKEMOBS_CAP.orEmpty(cap, this.cap_holder);
        }

        @Override
        public long getCooldown()
        {
            return this.cooldown;
        }

        @Override
        public byte getGender()
        {
            if (this.getType() == null)
            {
                // We only log this error if it was supposed to be a trainer,
                // other things can deal with their types however they feel
                // like.
                if (this.user instanceof TrainerBase) PokecubeAPI.LOGGER.error("Checking gender with no type!",
                        new IllegalStateException(new NullPointerException()));
                return 0;
            }
            final byte genders = this.getType().genders;
            if (genders == 1) this.setGender((byte) 1);
            if (genders == 2) this.setGender((byte) 2);
            if (this.gender == 0) if (genders == 3) this.setGender((byte) (Math.random() < 0.5 ? 1 : 2));
            return this.gender;
        }

        @Override
        public LevelMode getLevelMode()
        {
            return this.levelmode;
        }

        @Override
        public int getNextSlot()
        {
            return this.nextSlot;
        }

        @Override
        public UUID getOutID()
        {
            if (this.outID != null && this.outMob == null && this.user.level instanceof ServerLevel level)
            {
                this.outMob = PokemobCaps.getPokemobFor(level.getEntity(this.outID));
                if (this.outMob == null) this.outID = null;
            }
            if (this.outMob != null
                    && (this.outMob.getEntity().getHealth() <= 0 || !this.outMob.getEntity().isAddedToWorld()))
                this.setOutMob(null);
            return this.outID;
        }

        @Override
        public IPokemob getOutMob()
        {
            if (this.outMob != null && this.outMob.getEntity().getHealth() <= 0) this.setOutMob(null);
            return this.outMob;
        }

        @Override
        public ItemStack getPokemob(final int slot)
        {
            return this.datasync.get(this.holder.POKEMOBS[slot]);
        }

        @Override
        public LivingEntity getTarget()
        {
            final Brain<?> brain = this.user.getBrain();
            if (!brain.hasMemoryValue(MemoryTypes.BATTLETARGET.get())) return null;
            return brain.getMemory(MemoryTypes.BATTLETARGET.get()).get();
        }

        @Override
        public Set<ITargetWatcher> getTargetWatchers()
        {
            return this.watchers;
        }

        @Override
        public TypeTrainer getType()
        {
            if (!(this.user.level instanceof ServerLevel))
            {
                final String t = this.datasync.get(this.holder.TYPE);
                // Handle possible null type for if things are called at wrong
                // times on client side.
                if (this.type == null) this.type = TypeTrainer.merchant;
                return t.isEmpty() ? this.type
                        : this.type.getName().equalsIgnoreCase(t) ? this.type
                                : (this.type = TypeTrainer.getTrainer(t, true));
            }
            return this.type;
        }

        public boolean defeated(final Entity e)
        {
            boolean defeated = false;
            if (e instanceof Player player)
            {
                DefeatList defeatedList = PokecubePlayerDataHandler.getCustomDataValue(e.getStringUUID(),
                        "npcs_defeated_by");
                defeated = defeated || defeatedList.isValid(this.user, resetTimeLose, this.defeatResetKey);
            }
            return defeated;
        }

        public boolean defeatedBy(final Entity e)
        {
            boolean defeated = false;
            if (e instanceof Player player)
            {
                DefeatList defeatedList = PokecubePlayerDataHandler.getCustomDataValue(e.getStringUUID(),
                        "npcs_defeated");
                defeated = defeated || defeatedList.isValid(this.user, resetTimeLose, this.defeatResetKey);
            }
            return defeated;
        }

        public void init(final LivingEntity user, final IHasNPCAIStates aiStates, final IHasMessages messages,
                final IHasRewards rewards)
        {
            this.user = user;
            this.aiStates = aiStates;
            this.messages = messages;
            this.rewards = rewards;
            this.battleCooldown = Config.instance.trainerCooldown;
            this.resetTimeWin = this.battleCooldown;
            this.resetTimeLose = this.battleCooldown;
        }

        @Override
        public boolean isAgressive()
        {
            return this.friendlyCooldown < 0;
        }

        @Override
        public void lowerCooldowns()
        {
            // If someone punches us, we will retaliate, so no permafriendly
            // then.
            if (this.aiStates.getAIState(AIState.PERMFRIENDLY) && this.user.getKillCredit() == null)
            {
                this.friendlyCooldown = 10;
                return;
            }
            if (this.friendlyCooldown-- >= 0) return;
            this.friendlyCooldown = -1;
            final boolean done = this.getAttackCooldown() <= 0;
            if (done)
            {
                this.setAttackCooldown(-1);
                this.setNextSlot(0);
            }
            else if (this.getOutMob() == null && !this.aiStates.getAIState(AIState.THROWING))
                this.setAttackCooldown(this.getAttackCooldown() - 1);
            if (this.isInBattle()) return;
            if (!done && this.getTarget() != null) this.onSetTarget(null);
        }

        @Override
        public void onAddMob()
        {
            if (this.getTarget() == null || this.aiStates.getAIState(AIState.THROWING) || this.getOutMob() != null
                    || !this.getNextPokemob().isEmpty())
                return;
            this.aiStates.setAIState(AIState.INBATTLE, false);
            if (this.getOutMob() == null && !this.aiStates.getAIState(AIState.THROWING))
                if (this.getCooldown() <= Tracker.instance().getTick())
            {
                this.onLose(this.getTarget());
                this.setNextSlot(0);
            }
        }

        @Override
        public void onWin(final Entity lost)
        {
            // Only store for players
            if (lost instanceof Player)
            {
                if (lost instanceof Player player)
                {
                    DefeatList defeatedList = PokecubePlayerDataHandler.getCustomDataValue(lost.getStringUUID(),
                            "npcs_defeated_by");
                    defeatedList.validate(this.user, defeatResetKey);
                }
                // If available, we will increase reputation out of pity
                if (this.user instanceof Villager villager)
                    villager.getGossips().add(lost.getUUID(), GossipType.MINOR_POSITIVE, 10);
            }
            if (lost == this.getTarget()) this.onSetTarget(null);
        }

        @Override
        public void onLose(final Entity won)
        {
            final IHasPokemobs defeatingTrainer = TrainerCaps.getHasPokemobs(won);

            // Apply this first to remove any memory of the battle.
            // This clears the revenge/attacked values, to truely cancel
            // agression
            this.deAgro(defeatingTrainer);
            // If we were defeated by another trainer, lets forget about the
            // battle.
            if (defeatingTrainer != null)
            {
                defeatingTrainer.onWin(this.user);
                defeatingTrainer.onSetTarget(null);
            }

            // Get this cleanup stuff done first.
            this.setCooldown(Tracker.instance().getTick() + 100);
            this.onSetTarget(null);

            // Then parse if rewards and actions should be dealt with.
            final boolean reward = !(this.defeatedBy(won) || !this.user.isAlive() || this.user.getHealth() <= 0);

            // TODO possible have alternate message for invalid defeat?
            if (!reward) return;

            // Only store for players
            if (won instanceof Player player)
            {
                DefeatList defeatedList = PokecubePlayerDataHandler.getCustomDataValue(won.getStringUUID(),
                        "npcs_defeated");
                defeatedList.validate(this.user, defeatResetKey);

                if (this.rewards.getRewards() != null)
                {
                    this.rewards.giveReward(player, this.user);
                    this.checkDefeatAchievement(player);
                }

                // If applicable, increase reputation for winning the battle.
                if (this.user instanceof Villager villager)
                    villager.getGossips().add(won.getUUID(), GossipType.MINOR_POSITIVE, 20);
            }

            // Give the item rewards on defeat.
            if (this.rewards.getRewards() != null && won instanceof LivingEntity mob)
            {
                this.rewards.giveReward(mob, this.user);
            }

            if (won != null)
            {
                this.messages.sendMessage(MessageState.DEFEAT, won, this.user.getDisplayName(), won.getDisplayName());
                if (this.notifyDefeat && won instanceof ServerPlayer player)
                {
                    final PacketTrainer packet = new PacketTrainer(PacketTrainer.NOTIFYDEFEAT);
                    packet.getTag().putInt("I", this.user.getId());
                    packet.getTag().putLong("L", Tracker.instance().getTick() + this.resetTimeLose);
                    PacketTrainer.ASSEMBLER.sendTo(packet, player);
                }
                if (won instanceof LivingEntity living)
                    this.messages.doAction(MessageState.DEFEAT, new ActionContext(living, this.getTrainer()));
            }
        }

        @Override
        public void removeTargetWatcher(final ITargetWatcher watcher)
        {
            IHasPokemobs.super.removeTargetWatcher(watcher);
            this.watchers.remove(watcher);
        }

        @Override
        public void resetDefeatList()
        {
            this.defeatResetKey++;
        }

        @Override
        public void resetPokemob()
        {
            this.setNextSlot(0);
            this.aiStates.setAIState(AIState.THROWING, false);
            this.aiStates.setAIState(AIState.INBATTLE, false);
            EventsHandler.recallAllPokemobs(this.user);
            this.setOutMob(null);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();
            final ListTag ListNBT = new ListTag();
            for (int index = 0; index < this.getMaxPokemobCount(); index++)
            {
                final ItemStack i = this.getPokemob(index);
                if (i.isEmpty()) continue;
                final CompoundTag CompoundNBT = new CompoundTag();
                ListNBT.add(i.save(CompoundNBT));
            }

            nbt.put("pokemobs", ListNBT);
            nbt.putInt("nextSlot", this.getNextSlot());
            if (this.getOutID() != null) nbt.putString("outPokemob", this.getOutID().toString());
            if (this.getType() != null) nbt.putString("type", this.getType().getName());
            nbt.putLong("nextBattle", this.getCooldown());
            nbt.putByte("gender", this.getGender());
            if (this.battleCooldown < 0) this.battleCooldown = Config.instance.trainerCooldown;
            nbt.putInt("battleCD", this.battleCooldown);
            nbt.putBoolean("notifyDefeat", this.notifyDefeat);
            nbt.putShort("defeatResetKey", defeatResetKey);
            nbt.putLong("resetTime", this.resetTimeLose);
            nbt.putLong("resetTimeWin", this.resetTimeWin);
            if (this.sight != -1) nbt.putInt("sight", this.sight);
            nbt.putInt("friendly", this.friendlyCooldown);
            nbt.putString("levelMode", this.getLevelMode().name());
            return nbt;
        }

        @Override
        public void setAttackCooldown(final int value)
        {
            this.attackCooldown = value;
        }

        @Override
        public void setCanMegaEvolve(final boolean flag)
        {
            this.canMegaEvolve = flag;
        }

        @Override
        public void setCooldown(final long value)
        {
            this.cooldown = value;
        }

        @Override
        public void setGender(final byte value)
        {
            this.gender = value;
        }

        @Override
        public void setLevelMode(LevelMode type)
        {
            if (type == null) type = LevelMode.CONFIG;
            this.levelmode = type;
        }

        @Override
        public void setNextSlot(final int value)
        {
            this.nextSlot = value;
        }

        @Override
        public void setOutID(final UUID mob)
        {
            this.outID = mob;
            if (mob == null) this.outMob = null;
        }

        @Override
        public void setOutMob(final IPokemob mob)
        {
            this.outMob = mob;
            if (mob == null) this.outID = null;
            else this.outID = mob.getEntity().getUUID();
        }

        @Override
        public void setPokemob(final int slot, final ItemStack cube)
        {
            if (!cube.isEmpty())
            {
                final String owner = PokecubeManager.getOwner(cube);
                // Make trainer own it when place in.
                if (!this.getTrainer().getStringUUID().equals(owner))
                {
                    final IPokemob pokemob = PokecubeManager.itemToPokemob(cube, this.getTrainer().level());
                    if (pokemob != null)
                    {
                        pokemob.setOwner(this.getTrainer());
                        final ItemStack edited = PokecubeManager.pokemobToItem(pokemob);
                        cube.setTag(edited.getTag());
                    }
                }
            }
            this.datasync.set(this.holder.POKEMOBS[slot], cube);
        }

        @Override
        public void onSetTarget(final LivingEntity target, final boolean ignoreCanBattle)
        {
            final LivingEntity old = this.getTarget();
            // No calling this if we already have that target.
            if (old == target) return;
            // No calling this if we are the target.
            if (target == this.getTrainer()) return;

            if (!ignoreCanBattle && target != null && !this.canBattle(target, true).test()) return;

            final Set<ITargetWatcher> watchers = this.getTargetWatchers();
            // No next pokemob, so we shouldn't have a target in this case.

            // Set this here, before trying to validate other's target below.
            this.getTrainer().getBrain().eraseMemory(MemoryTypes.BATTLETARGET.get());
            if (target != null) this.getTrainer().getBrain().setMemory(MemoryTypes.BATTLETARGET.get(), target);

            final IHasPokemobs oldOther = TrainerCaps.getHasPokemobs(old);
            if (oldOther != null) oldOther.onSetTarget(null);

            if (target != null && this.getPokemob(0).isEmpty())
            {
                // Notify the watchers that a target was actually set.
                for (final ITargetWatcher watcher : watchers) watcher.onSet(null);
                this.aiStates.setAIState(AIState.THROWING, false);
                this.aiStates.setAIState(AIState.INBATTLE, false);
                BrainUtils.deagro(this.getTrainer());
                this.getTrainer().getBrain().eraseMemory(MemoryTypes.BATTLETARGET.get());
                this.getTrainer().getBrain().setActiveActivityIfPossible(Activity.IDLE);
                return;
            }

            final IHasPokemobs other = TrainerCaps.getHasPokemobs(target);
            if (other != null) other.onSetTarget(this.getTrainer(), true);

            if (target != null && this.getAttackCooldown() <= 0)
            {
                int cooldown = Config.instance.trainerBattleDelay;
                final LivingEntity hitBy = this.user.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)
                        ? this.user.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).get()
                        : null;
                final int hurtTimer = this.user.tickCount - this.user.getLastHurtMobTimestamp();
                // No cooldown if someone was punching is!
                if (hitBy == target && hurtTimer < 500) cooldown = 0;
                this.setAttackCooldown(cooldown);
                this.messages.sendMessage(MessageState.AGRESS, target, this.user.getDisplayName(),
                        target.getDisplayName());
                this.messages.doAction(MessageState.AGRESS, new ActionContext(target, this.getTrainer()));
                this.aiStates.setAIState(AIState.INBATTLE, true);
            }
            if (target == null)
            {
                if (old != null && this.aiStates.getAIState(AIState.INBATTLE))
                {
                    this.messages.sendMessage(MessageState.DEAGRESS, old, this.user.getDisplayName(),
                            old.getDisplayName());
                    this.messages.doAction(MessageState.DEAGRESS, new ActionContext(target, this.getTrainer()));
                }
                this.aiStates.setAIState(AIState.THROWING, false);
                this.aiStates.setAIState(AIState.INBATTLE, false);
            }
            // Notify the watchers that a target was actually set.
            for (final ITargetWatcher watcher : watchers) watcher.onSet(target);

            if (target == null)
            {
                BrainUtils.deagro(this.getTrainer());
                this.resetPokemob();
                this.getTrainer().getBrain().setActiveActivityIfPossible(Activity.IDLE);
            }
            else this.getTrainer().getBrain().setActiveActivityIfPossible(Activities.BATTLE.get());
        }

        @Override
        public void setType(final TypeTrainer type)
        {
            this.type = type;
            if (!this.user.level.isClientSide) this.datasync.set(this.holder.TYPE, type == null ? "" : type.getName());
        }

        @Override
        public void throwCubeAt(final Entity target)
        {
            if (target == null || this.aiStates.getAIState(AIState.THROWING)
                    || !(target.level() instanceof ServerLevel))
                return;
            final ItemStack i = this.getNextPokemob();
            if (!i.isEmpty())
            {
                this.aiStates.setAIState(AIState.INBATTLE, true);
                final IPokecube cube = (IPokecube) i.getItem();
                final Vector3 here = new Vector3().set(this.user);
                final Vector3 t = new Vector3().set(target);
                t.set(t.subtractFrom(here).scalarMultBy(0.5).addTo(here));
                PokecubeManager.heal(i, user.level, false);

                LivingEntity mob = PokecubeManager.itemToMob(i, this.user.level);

                // Evolve the pokemob if needed, ie we spawned with it, but not
                // checked yet.
                IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
                if (pokemob != null && mob.getPersistentData().getBoolean("__need_init_evos__"))
                {
                    mob.getPersistentData().remove("__need_init_evos__");
                    if (pokemob.canEvolve(pokemob.getHeldItem()))
                        for (final EvolutionData evo : pokemob.getPokedexEntry().getEvolutions())
                            if (evo.shouldEvolve(pokemob))
                    {
                        final IPokemob temp = PokemobCaps.getPokemobFor(evo.getEvolution(user.level));
                        if (temp != null)
                        {
                            pokemob = temp;
                            break;
                        }
                    }
                }
                mob = pokemob.getEntity();
                PokecubeManager.addToCube(i, mob);

                this.setItem(getNextSlot(), i);

                final EntityPokecubeBase thrown = cube.throwPokecubeAt(this.user.level, this.user, i, t, null);
                if (thrown != null)
                {
                    thrown.autoRelease = 20;
                    thrown.canBePickedUp = false;
                    this.aiStates.setAIState(AIState.THROWING, true);
                    this.setAttackCooldown(Config.instance.trainerSendOutDelay);
                    this.messages.sendMessage(MessageState.SENDOUT, target, this.user.getDisplayName(),
                            i.getHoverName(), target.getDisplayName());
                    if (target instanceof LivingEntity) this.messages.doAction(MessageState.SENDOUT,
                            new ActionContext((LivingEntity) target, this.getTrainer()));
                }
                this.nextSlot++;
                if (this.nextSlot >= this.getMaxPokemobCount() || this.getNextPokemob() == null) this.nextSlot = -1;
                return;
            }
            this.nextSlot = -1;
        }

        @Override
        public void setDataSync(final DataSync sync)
        {
            this.datasync = sync;
        }

        @Override
        public LivingEntity getTrainer()
        {
            return this.user;
        }

        @Override
        public LivingEntity getTargetRaw()
        {
            final Brain<?> brain = this.user.getBrain();
            if (!brain.hasMemoryValue(MemoryTypes.BATTLETARGET.get())) return null;
            return brain.getMemory(MemoryTypes.BATTLETARGET.get()).get();
        }

        @Override
        public int countPokemon()
        {
            if (this.number > this.getMaxPokemobCount()) this.initCount();
            return this.number;
        }

        @Override
        public void initCount()
        {
            this.number = 0;
            for (int i = 0; i < this.getMaxPokemobCount(); i++)
                if (PokecubeManager.isFilled(this.getPokemob(i))) this.number++;
        }

        @Override
        public boolean isInBattle()
        {
            return this.aiStates.getAIState(AIState.INBATTLE);
        }

        // Things below are from IInventory

        @Override
        public int getContainerSize()
        {
            return 6;
        }

        @Override
        public boolean isEmpty()
        {
            return this.getItem(0).isEmpty();
        }

        @Override
        public ItemStack getItem(final int index)
        {
            return this.getPokemob(index);
        }

        @Override
        public ItemStack removeItem(final int index, final int count)
        {
            // We should only ever have counts of 1 anyway.
            return this.removeItemNoUpdate(index);
        }

        @Override
        public ItemStack removeItemNoUpdate(final int index)
        {
            final ItemStack stack = this.getPokemob(index);
            this.setPokemob(index, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(final int index, final ItemStack stack)
        {
            this.setPokemob(index, stack);
        }

        @Override
        public void setChanged()
        {
            // NOOP
        }

        Player usingPlayer = null;

        @Override
        public boolean stillValid(final Player player)
        {
            if (this.usingPlayer == player) return true;
            if (this.getLatestContext() == null || this.getLatestContext().target != player)
                this.setLatestContext(new ActionContext(player, this.getTrainer(), player.getMainHandItem()));

            // No item in action, no allow.
            if (this.getLatestContext().playerStack.isEmpty()) return false;
            // Only allow if it is a pokedex.
            if (this.getLatestContext().playerStack.getItem() != PokecubeItems.POKEDEX.get()) return false;

            final TrainerInteractEvent.CanInteract event = new CanInteract(player, this.getLatestContext());
            final Result result = player == this.user || player.isCreative() ? Result.ALLOW : Result.DENY;
            event.setResult(result);
            MinecraftForge.EVENT_BUS.post(event);
            final boolean allow = event.getResult() == Result.ALLOW;
            return allow;
        }

        @Override
        public void startOpen(final Player player)
        {
            IHasPokemobs.super.startOpen(player);
            this.usingPlayer = player;
        }

        @Override
        public void stopOpen(final Player player)
        {
            IHasPokemobs.super.stopOpen(player);
            this.usingPlayer = null;
        }

        @Override
        public int getMaxStackSize()
        {
            return 1;
        }

        // End of IInventory stuff

        @Override
        public ActionContext getLatestContext()
        {
            return this.context;
        }

        @Override
        public ActionContext setLatestContext(final ActionContext context)
        {
            this.context = context;
            return context;
        }
    }
}