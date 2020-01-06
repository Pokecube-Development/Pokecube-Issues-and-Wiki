package pokecube.adventures.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.Config;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.utils.MessageState;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.DataSync;

public class CapabilityHasPokemobs
{
    public static class DefaultPokemobs implements IHasPokemobs, ICapabilitySerializable<CompoundNBT>
    {
        public static class DataParamHolder
        {
            public int   TYPE;
            public int[] POKEMOBS = new int[6];
        }

        public static class DefeatEntry implements Comparable<DefeatEntry>
        {
            public static DefeatEntry createFromNBT(final CompoundNBT nbt)
            {
                final String defeater = nbt.getString("player");
                final long time = nbt.getLong("time");
                return new DefeatEntry(defeater, time);
            }

            final String defeater;
            long         defeatTime;

            public DefeatEntry(final String defeater, final long time)
            {
                this.defeater = defeater;
                this.defeatTime = time;
            }

            @Override
            public int compareTo(final DefeatEntry o)
            {
                return this.defeater.compareTo(o.defeater);
            }

            @Override
            public boolean equals(final Object other)
            {
                if (other instanceof DefeatEntry) return ((DefeatEntry) other).defeater.equals(this.defeater);
                return false;
            }

            @Override
            public int hashCode()
            {
                return this.defeater.hashCode();
            }

            void writeToNBT(final CompoundNBT nbt)
            {
                nbt.putString("player", this.defeater);
                nbt.putLong("time", this.defeatTime);
            }
        }

        private final LazyOptional<IHasPokemobs> cap_holder = LazyOptional.of(() -> this);

        public long                   resetTime        = 0;
        public int                    friendlyCooldown = 0;
        public ArrayList<DefeatEntry> defeaters        = new ArrayList<>();

        // Should the client be notified of the defeat via a packet?
        public boolean notifyDefeat = false;

        // This is the reference cooldown.
        public int              battleCooldown = -1;
        private byte            gender         = 1;
        private LivingEntity    user;
        private IHasNPCAIStates aiStates;
        private IHasMessages    messages;
        private IHasRewards     rewards;
        private int             nextSlot;
        // Cooldown between sending out pokemobs
        private int attackCooldown = 0;
        // Cooldown between agression
        private long                      cooldown      = 0;
        private int                       sight         = -1;
        private TypeTrainer               type;
        private LivingEntity              target;
        private UUID                      outID;
        private boolean                   canMegaEvolve = false;
        private IPokemob                  outMob;
        private List<ItemStack>           pokecubes;
        private LevelMode                 levelmode     = LevelMode.CONFIG;
        private final Set<ITargetWatcher> watchers      = Sets.newHashSet();

        public final DataParamHolder holder = new DataParamHolder();
        public DataSync              datasync;

        @Override
        public void addTargetWatcher(final ITargetWatcher watcher)
        {
            IHasPokemobs.super.addTargetWatcher(watcher);
            this.watchers.add(watcher);
        }

        @Override
        public boolean canBattle(final LivingEntity target)
        {
            return !this.hasDefeated(target);
        }

        @Override
        public boolean canMegaEvolve()
        {
            return this.canMegaEvolve;
        }

        public void checkDefeatAchievement(final PlayerEntity player)
        {
            // TODO advancements
            // if (!(this.user instanceof EntityTrainer)) return;
            // final boolean leader = this.user instanceof EntityLeader;
            // if (leader) Triggers.BEATLEADER.trigger((ServerPlayerEntity)
            // player, (EntityTrainer) this.user);
            // else Triggers.BEATTRAINER.trigger((ServerPlayerEntity) player,
            // (EntityTrainer) this.user);
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            if (nbt.contains("pokemobs", 9))
            {
                if (this.clearOnLoad()) this.clear();
                final ListNBT ListNBT = nbt.getList("pokemobs", 10);
                if (ListNBT.size() != 0) for (int i = 0; i < Math.min(ListNBT.size(), this.getMaxPokemobCount()); ++i)
                    this.setPokemob(i, ItemStack.read(ListNBT.getCompound(i)));
            }
            this.setType(TypeTrainer.getTrainer(nbt.getString("type")));
            this.setCooldown(nbt.getLong("nextBattle"));
            if (nbt.contains("outPokemob")) this.setOutID(UUID.fromString(nbt.getString("outPokemob")));
            this.setNextSlot(nbt.getInt("nextSlot"));
            this.setCanMegaEvolve(nbt.getBoolean("megaevolves"));
            if (nbt.contains("gender")) this.setGender(nbt.getByte("gender"));
            if (this.getNextSlot() >= 6) this.setNextSlot(0);
            this.sight = nbt.contains("sight") ? nbt.getInt("sight") : -1;
            if (nbt.contains("battleCD")) this.battleCooldown = nbt.getInt("battleCD");
            if (this.battleCooldown < 0) this.battleCooldown = Config.instance.trainerCooldown;

            this.defeaters.clear();
            if (nbt.contains("resetTime")) this.resetTime = nbt.getLong("resetTime");
            if (nbt.contains("DefeatList", 9))
            {
                final ListNBT ListNBT = nbt.getList("DefeatList", 10);
                for (int i = 0; i < ListNBT.size(); i++)
                    this.defeaters.add(DefeatEntry.createFromNBT(ListNBT.getCompound(i)));
            }
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
            return CapabilityHasPokemobs.HASPOKEMOBS_CAP.orEmpty(cap, this.cap_holder);
        }

        @Override
        public long getCooldown()
        {
            return this.cooldown;
        }

        @Override
        public byte getGender()
        {
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
            return this.outID;
        }

        @Override
        public IPokemob getOutMob()
        {
            return this.outMob;
        }

        @Override
        public ItemStack getPokemob(final int slot)
        {
            if (this.pokecubes != null) return this.pokecubes.get(slot);
            return this.datasync.get(this.holder.POKEMOBS[slot]);
        }

        @Override
        public LivingEntity getTarget()
        {
            return this.target;
        }

        @Override
        public Set<ITargetWatcher> getTargetWatchers()
        {
            return this.watchers;
        }

        @Override
        public TypeTrainer getType()
        {
            if (this.user.getEntityWorld().isRemote)
            {
                final String t = this.datasync.get(this.holder.TYPE);
                return t.isEmpty() ? this.type : TypeTrainer.getTrainer(t);
            }
            return this.type;
        }

        public boolean hasDefeated(final Entity e)
        {
            if (e == null) return false;
            final String name = e.getCachedUniqueIdString();
            for (final DefeatEntry s : this.defeaters)
                if (s.defeater.equals(name))
                {
                    // If this is the case, then this mob is not re-battleable.
                    if (this.resetTime <= 0) return true;
                    // Otherwise check the diff.
                    final long diff = this.user.getEntityWorld().getGameTime() - s.defeatTime;
                    if (diff > this.resetTime) return false;
                    return true;
                }
            return false;
        }

        public void init(final LivingEntity user, final IHasNPCAIStates aiStates, final IHasMessages messages,
                final IHasRewards rewards)
        {
            this.user = user;
            this.aiStates = aiStates;
            this.messages = messages;
            this.rewards = rewards;
            this.battleCooldown = Config.instance.trainerCooldown;
            this.resetTime = this.battleCooldown;
            if (!TypeTrainer.mobTypeMapper.shouldSync(user)) this.pokecubes = NonNullList.<ItemStack> withSize(6,
                    ItemStack.EMPTY);
        }

        @Override
        public boolean isAgressive()
        {
            return this.friendlyCooldown < 0;
        }

        @Override
        public void lowerCooldowns()
        {
            if (this.aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY))
            {
                this.friendlyCooldown = 10;
                return;
            }
            if (this.friendlyCooldown-- >= 0) return;
            final boolean done = this.getAttackCooldown() <= 0;
            if (done)
            {
                this.setAttackCooldown(-1);
                this.setNextSlot(0);
            }
            else if (this.getOutMob() == null && !this.aiStates.getAIState(IHasNPCAIStates.THROWING)) this
                    .setAttackCooldown(this.getAttackCooldown() - 1);
            if (this.aiStates.getAIState(IHasNPCAIStates.INBATTLE)) return;
            if (!done && this.getTarget() != null) this.setTarget(null);
        }

        @Override
        public void onAddMob()
        {
            if (this.getTarget() == null || this.aiStates.getAIState(IHasNPCAIStates.THROWING) || this
                    .getOutMob() != null || !this.getNextPokemob().isEmpty()) return;
            this.aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
            if (this.getOutMob() == null && !this.aiStates.getAIState(IHasNPCAIStates.THROWING)) if (this
                    .getCooldown() <= this.user.getEntityWorld().getGameTime())
            {
                this.onDefeated(this.getTarget());
                this.setNextSlot(0);
            }
        }

        @Override
        public void onDefeated(final Entity defeater)
        {
            // Get this cleanup stuff done first.
            if (defeater instanceof PlayerEntity) this.setCooldown(this.user.getEntityWorld().getGameTime()
                    + this.battleCooldown);
            else this.setCooldown(this.user.getEntityWorld().getGameTime() + 10);
            this.setTarget(null);

            // Then parse if rewards and actions should be dealt with.
            final boolean reward = !(this.hasDefeated(defeater) || !this.user.isAlive() || this.user.getHealth() <= 0);

            // TODO possible have alternate message for invalid defeat?
            if (!reward) return;

            if (defeater instanceof PlayerEntity)
            {
                final DefeatEntry entry = new DefeatEntry(defeater.getCachedUniqueIdString(), this.user.getEntityWorld()
                        .getGameTime());
                if (this.defeaters.contains(entry)) this.defeaters.get(this.defeaters.indexOf(
                        entry)).defeatTime = entry.defeatTime;
                else this.defeaters.add(entry);
                if (this.rewards.getRewards() != null)
                {
                    final PlayerEntity player = (PlayerEntity) defeater;
                    this.rewards.giveReward(player, this.user);
                    this.checkDefeatAchievement(player);
                }
            }
            if (defeater != null)
            {
                this.messages.sendMessage(MessageState.DEFEAT, defeater, this.user.getDisplayName(), defeater
                        .getDisplayName());
                if (this.notifyDefeat && defeater instanceof ServerPlayerEntity)
                {
                    // TODO notify defeat packet.
                    // final PacketTrainer packet = new
                    // PacketTrainer(PacketTrainer.MESSAGENOTIFYDEFEAT);
                    // packet.data.putInt("I", this.user.getEntityId());
                    // packet.data.putLong("L",
                    // this.user.getEntityWorld().getGameTime() +
                    // this.resetTime);
                    // PokecubeMod.packetPipeline.sendTo(packet,
                    // (ServerPlayerEntity) defeater);
                }
                if (defeater instanceof LivingEntity) this.messages.doAction(MessageState.DEFEAT,
                        (LivingEntity) defeater);
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
            this.defeaters.clear();
        }

        @Override
        public void resetPokemob()
        {
            this.setNextSlot(0);
            EventsHandler.recallAllPokemobs(this.user);
            this.aiStates.setAIState(IHasNPCAIStates.THROWING, false);
            this.aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
            this.setOutMob(null);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();
            ListNBT ListNBT = new ListNBT();
            for (int index = 0; index < this.getMaxPokemobCount(); index++)
            {
                final ItemStack i = this.getPokemob(index);
                if (i.isEmpty()) continue;
                final CompoundNBT CompoundNBT = new CompoundNBT();
                ListNBT.add(CompoundNBT);
            }
            nbt.put("pokemobs", ListNBT);
            nbt.putInt("nextSlot", this.getNextSlot());
            if (this.getOutID() != null) nbt.putString("outPokemob", this.getOutID().toString());
            if (this.getType() != null) nbt.putString("type", this.getType().name);
            nbt.putLong("nextBattle", this.getCooldown());
            nbt.putByte("gender", this.getGender());

            if (this.battleCooldown < 0) this.battleCooldown = Config.instance.trainerCooldown;
            nbt.putInt("battleCD", this.battleCooldown);
            ListNBT = new ListNBT();
            for (final DefeatEntry entry : this.defeaters)
            {
                final CompoundNBT CompoundNBT = new CompoundNBT();
                entry.writeToNBT(CompoundNBT);
                ListNBT.add(CompoundNBT);
            }
            nbt.put("DefeatList", ListNBT);
            nbt.putBoolean("notifyDefeat", this.notifyDefeat);
            nbt.putLong("resetTime", this.resetTime);
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
            else this.outID = mob.getEntity().getUniqueID();
        }

        @Override
        public void setPokemob(final int slot, final ItemStack cube)
        {
            if (this.pokecubes != null)
            {
                this.pokecubes.set(slot, cube);
                return;
            }
            this.datasync.set(this.holder.POKEMOBS[slot], cube);
        }

        @Override
        public void setTarget(LivingEntity target)
        {
            final Set<ITargetWatcher> watchers = this.getTargetWatchers();
            if (target != null && !watchers.isEmpty())
            {
                boolean valid = false;
                for (final ITargetWatcher watcher : watchers)
                    if (watcher.validTargetSet(target))
                    {
                        valid = true;
                        break;
                    }
                if (!valid) target = null;
            }
            if (this.getPokemob(0).isEmpty())
            {
                target = null;
                this.aiStates.setAIState(IHasNPCAIStates.THROWING, false);
                this.aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
                return;
            }
            if (target != null && target != this.target && this.attackCooldown <= 0)
            {
                this.attackCooldown = Config.instance.trainerBattleDelay;
                this.messages.sendMessage(MessageState.AGRESS, target, this.user.getDisplayName(), target
                        .getDisplayName());
                this.messages.doAction(MessageState.AGRESS, target);
                this.aiStates.setAIState(IHasNPCAIStates.INBATTLE, true);
            }
            if (target == null)
            {
                if (this.target != null && this.aiStates.getAIState(IHasNPCAIStates.INBATTLE))
                {
                    this.messages.sendMessage(MessageState.DEAGRESS, this.target, this.user.getDisplayName(),
                            this.target.getDisplayName());
                    this.messages.doAction(MessageState.DEAGRESS, target);
                }
                this.aiStates.setAIState(IHasNPCAIStates.THROWING, false);
                this.aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
            }
            this.target = target;
        }

        @Override
        public void setType(final TypeTrainer type)
        {
            this.type = type;
            if (!this.user.getEntityWorld().isRemote) this.datasync.set(this.holder.TYPE, type == null ? ""
                    : type.name);
        }

        @Override
        public void throwCubeAt(final Entity target)
        {
            if (target == null || this.aiStates.getAIState(IHasNPCAIStates.THROWING)) return;
            final ItemStack i = this.getNextPokemob();
            if (!i.isEmpty())
            {
                this.aiStates.setAIState(IHasNPCAIStates.INBATTLE, true);
                final IPokecube cube = (IPokecube) i.getItem();
                final Vector3 here = Vector3.getNewVector().set(this.user);
                final Vector3 t = Vector3.getNewVector().set(target);
                t.set(t.subtractFrom(here).scalarMultBy(0.5).addTo(here));
                cube.throwPokecubeAt(this.user.getEntityWorld(), this.user, i, t, null);
                this.aiStates.setAIState(IHasNPCAIStates.THROWING, true);
                this.attackCooldown = Config.instance.trainerSendOutDelay;
                this.messages.sendMessage(MessageState.SENDOUT, target, this.user.getDisplayName(), i.getDisplayName(),
                        target.getDisplayName());
                if (target instanceof LivingEntity) this.messages.doAction(MessageState.SENDOUT, (LivingEntity) target);
                this.nextSlot++;
                if (this.nextSlot >= this.getMaxPokemobCount() || this.getNextPokemob() == null) this.nextSlot = -1;
                return;
            }
            this.nextSlot = -1;
        }
    }

    public static interface IHasPokemobs
    {
        public static enum LevelMode
        {
            CONFIG, YES, NO;
        }

        /** Adds the pokemob back into the inventory, healing it as needed. */
        default boolean addPokemob(ItemStack mob)
        {
            long uuidLeast = 0;
            long uuidMost = 0;

            if (mob.hasTag()) if (mob.getTag().contains("Pokemob"))
            {
                final CompoundNBT nbt = mob.getTag().getCompound("Pokemob");
                uuidLeast = nbt.getLong("UUIDLeast");
                uuidMost = nbt.getLong("UUIDMost");
            }
            long uuidLeastTest = -1;
            long uuidMostTest = -1;
            boolean found = false;
            int foundID = -1;
            for (int i = 0; i < this.getMaxPokemobCount(); i++)
                if (!this.getPokemob(i).isEmpty()) if (this.getPokemob(i).hasTag()) if (this.getPokemob(i).getTag()
                        .contains("Pokemob"))
                {
                    final CompoundNBT nbt = this.getPokemob(i).getTag().getCompound("Pokemob");
                    uuidLeastTest = nbt.getLong("UUIDLeast");
                    uuidMostTest = nbt.getLong("UUIDMost");
                    if (uuidLeast == uuidLeastTest && uuidMost == uuidMostTest)
                    {
                        found = true;
                        foundID = i;
                        if (this.canLevel())
                        {
                            PokecubeManager.heal(mob);
                            this.setPokemob(i, mob.copy());
                        }
                        else mob = this.getPokemob(i);
                        break;
                    }
                }
            for (int i = 0; i < this.getMaxPokemobCount(); i++)
            {
                if (!found && this.getPokemob(i).isEmpty())
                {
                    this.setPokemob(i, mob.copy());
                    PokecubeManager.heal(this.getPokemob(i));
                    break;
                }
                if (found && foundID == i) if (this.getPokemob(i).isEmpty())
                {
                    PokecubeManager.heal(mob);
                    this.setPokemob(i, mob.copy());
                    break;
                }
                else if (!this.getPokemob(i).isEmpty()) PokecubeManager.heal(this.getPokemob(i));
            }
            for (int i = 0; i < this.getMaxPokemobCount(); i++)
            {
                final ItemStack stack = this.getPokemob(i);
                if (stack.isEmpty())
                {
                    found = true;
                    for (int j = i; j < this.getMaxPokemobCount() - 1; j++)
                    {
                        this.setPokemob(j, this.getPokemob(j + 1));
                        this.setPokemob(j + 1, ItemStack.EMPTY);
                    }
                }
            }
            this.onAddMob();
            return found;
        }

        default void addTargetWatcher(final ITargetWatcher watcher)
        {
            watcher.onAdded(this);
        }

        /** If we are agressive, is this a valid target? */
        boolean canBattle(LivingEntity target);

        default boolean canLevel()
        {
            final LevelMode type = this.getLevelMode();
            if (type == LevelMode.CONFIG) return Config.instance.trainerslevel;
            return type == LevelMode.YES ? true : false;
        }

        boolean canMegaEvolve();

        default void clear()
        {
            for (int i = 0; i < this.getMaxPokemobCount(); i++)
                this.setPokemob(i, ItemStack.EMPTY);
        }

        default boolean clearOnLoad()
        {
            return true;
        }

        default int countPokemon()
        {
            int ret = 0;
            for (int i = 0; i < this.getMaxPokemobCount(); i++)
                if (PokecubeManager.isFilled(this.getPokemob(i))) ret++;
            return ret;
        }

        /** The distance to see for attacking players */
        default int getAgressDistance()
        {
            return Config.instance.trainerSightRange;
        }

        /**
         * This is the cooldown for whether a pokemob can be sent out, it ticks
         * downwards, when less than 0, a mob may be thrown out as needed.
         */
        int getAttackCooldown();

        /**
         * This is the time when the next battle can start. it is in world
         * ticks.
         */
        long getCooldown();

        /** 1 = male 2= female */
        byte getGender();

        LevelMode getLevelMode();

        default int getMaxPokemobCount()
        {
            return 6;
        }

        /** The next pokemob to be sent out */
        default ItemStack getNextPokemob()
        {
            if (this.getNextSlot() < 0) return ItemStack.EMPTY;
            for (int i = 0; i < this.getMaxPokemobCount(); i++)
            {
                final ItemStack stack = this.getPokemob(i);
                if (stack.isEmpty()) for (int j = i; j < this.getMaxPokemobCount() - 1; j++)
                {
                    this.setPokemob(j, this.getPokemob(j + 1));
                    this.setPokemob(j + 1, ItemStack.EMPTY);
                }
            }
            return this.getPokemob(this.getNextSlot());
        }

        /** The next slot to be sent out. */
        int getNextSlot();

        UUID getOutID();

        /** If we have a mob out, this should be it. */
        IPokemob getOutMob();

        ItemStack getPokemob(int slot);

        LivingEntity getTarget();

        default Set<ITargetWatcher> getTargetWatchers()
        {
            return Collections.emptySet();
        }

        TypeTrainer getType();

        /** Whether we should look for their target to attack. */
        default boolean isAgressive()
        {
            return true;
        }

        default boolean isAgressive(final Entity target)
        {
            return this.isAgressive();
        }

        void lowerCooldowns();

        void onAddMob();

        void onDefeated(Entity defeater);

        default void removeTargetWatcher(final ITargetWatcher watcher)
        {
            watcher.onRemoved(this);
        }

        void resetDefeatList();

        /** Resets the pokemobs; */
        void resetPokemob();

        void setAttackCooldown(int value);

        void setCanMegaEvolve(boolean flag);

        void setCooldown(long value);

        /** 1 = male 2= female */
        void setGender(byte value);

        void setLevelMode(LevelMode type);

        void setNextSlot(int value);

        void setOutID(UUID mob);

        void setOutMob(IPokemob mob);

        void setPokemob(int slot, ItemStack cube);

        void setTarget(LivingEntity target);

        void setType(TypeTrainer type);

        void throwCubeAt(Entity target);
    }

    public static interface ITargetWatcher
    {
        default void onAdded(final IHasPokemobs pokemobs)
        {
        }

        default void onRemoved(final IHasPokemobs pokemobs)
        {
        }

        boolean validTargetSet(LivingEntity target);
    }

    public static class Storage implements Capability.IStorage<IHasPokemobs>
    {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void readNBT(final Capability<IHasPokemobs> capability, final IHasPokemobs instance,
                final Direction side, final INBT base)
        {
            if (instance instanceof INBTSerializable) ((INBTSerializable) instance).deserializeNBT(base);
        }

        @Override
        public INBT writeNBT(final Capability<IHasPokemobs> capability, final IHasPokemobs instance,
                final Direction side)
        {
            if (instance instanceof INBTSerializable<?>) return ((INBTSerializable<?>) instance).serializeNBT();
            return null;
        }

    }

    @CapabilityInject(IHasPokemobs.class)
    public static final Capability<IHasPokemobs> HASPOKEMOBS_CAP = null;

    public static Storage storage;

    public static IHasPokemobs getHasPokemobs(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IHasPokemobs holder = entityIn.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasPokemobs) return (IHasPokemobs) entityIn;
        return holder;
    }
}