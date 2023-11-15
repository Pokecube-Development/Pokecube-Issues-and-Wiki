/**
 *
 */
package pokecube.api.entity.pokemob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.moves.Battle;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import thut.api.ModelHolder;
import thut.api.Tracker;
import thut.api.entity.ICopyMob;
import thut.api.entity.IHungrymob;
import thut.api.entity.IMobColourable;
import thut.api.entity.IShearable;
import thut.api.entity.ai.IAIRunnable;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.DataSync;
import thut.lib.TComponent;

/** @author Manchou */
public interface IPokemob extends IHasMobAIStates, IHasMoves, ICanEvolve, IHasOwner, IHasStats, IHungrymob,
        IHasCommands, IMobColourable, IShearable, ICopyMob
{
    /**
     * Holder object for custom models/textures/etc for a pokemob.
     */
    public static class FormeHolder extends ModelHolder
    {
        public static HashMap<ResourceLocation, FormeHolder> formeHolders = new HashMap<>();

        public static FormeHolder load(final CompoundTag nbt)
        {
            final String name = nbt.getString("key");
            final String anim = nbt.contains("anim") ? nbt.getString("anim") : null;
            final String model = nbt.contains("model") ? nbt.getString("model") : null;
            final String tex = nbt.contains("tex") ? nbt.getString("tex") : null;
            String entry_name = nbt.getString("entry");

            final ResourceLocation _model = model != null ? PokecubeItems.toPokecubeResource(model) : null;
            final ResourceLocation _tex = tex != null ? PokecubeItems.toPokecubeResource(tex) : null;
            final ResourceLocation _anim = anim != null ? PokecubeItems.toPokecubeResource(anim) : null;
            PokedexEntry entry = Database.getEntry(entry_name);
            if (entry == null) entry = Database.missingno;

            final FormeHolder holder = FormeHolder.get(entry, _model, _tex, _anim,
                    PokecubeItems.toPokecubeResource(name));
            return holder;
        }

        public static FormeHolder get(PokedexEntry entry, final ResourceLocation model, final ResourceLocation texture,
                final ResourceLocation animation, final ResourceLocation name)
        {
            if (FormeHolder.formeHolders.containsKey(name)) return FormeHolder.formeHolders.get(name);
            final FormeHolder holder = new FormeHolder(entry, model, texture, animation, name);
            FormeHolder.formeHolders.put(name, holder);
            return holder;
        }

        // This key is used for unique identifier for lookup in renderer for
        // initialization.
        public ResourceLocation key;
        public DefaultFormeHolder loaded_from;

        public boolean _is_item_forme = false;
        public PokedexEntry _entry = Database.missingno;

        // Icons for the entry, ordering is male/maleshiny, female/female shiny.
        // genderless fills the male slot.
        private final ResourceLocation[][] icons =
        {
                { null, null },
                { null, null } };

        private FormeHolder(PokedexEntry entry, final ResourceLocation model, final ResourceLocation texture,
                final ResourceLocation animation, final ResourceLocation name)
        {
            super(model, texture, animation, name.toString());
            this.key = name;
            this.setEntry(entry);
        }

        /**
         * Returns the inventory icon for the pokemob.
         * 
         * @param male  - Whether to look for male icon for gendered mobs
         * @param shiny - whether to look for shiny icon
         * @param base  - The base pokedex entry to get the icons for
         * @return the icon
         */
        public ResourceLocation getIcon(final boolean male, final boolean shiny, final PokedexEntry base)
        {
            if (this.icons[0][0] == null)
            {
                final String path = base.texturePath.replace("entity", "entity_icon");
                final String texture = path + this.key.getPath();
                this.icons[0][0] = new ResourceLocation(texture + ".png");
                this.icons[0][1] = new ResourceLocation(texture + "_s.png");
                this.icons[1][0] = new ResourceLocation(texture + ".png");
                this.icons[1][1] = new ResourceLocation(texture + "_s.png");
            }
            final int i = male ? 0 : 1;
            final int j = shiny ? 1 : 0;
            return this.icons[i][j];
        }

        /**
         * Serialises to NBT
         * 
         * @return the nbt containing our data.
         */
        public CompoundTag save()
        {
            final CompoundTag ret = new CompoundTag();
            ret.putString("key", this.key.toString());
            if (this.model != null) ret.putString("model", this.model.toString());
            if (this.animation != null) ret.putString("anim", this.animation.toString());
            if (this.texture != null) ret.putString("tex", this.texture.toString());
            ret.putString("entry", this._entry.getTrimmedName());
            return ret;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof FormeHolder holder) return this.key.equals(holder.key);
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.key.hashCode();
        }

        public void setEntry(PokedexEntry entry)
        {
            this._entry = entry;
            if (entry != Database.missingno) entry.default_holder = this;
        }
    }

    public static enum HappinessType
    {
        /**
         * Happiness gain over time
         */
        TIME(2, 2, 1),
        /**
         * Happiness gain from levelling
         */
        LEVEL(5, 3, 2),
        /**
         * Happiness gain from eating a berry while not in combat
         */
        BERRY(3, 2, 1),
        /**
         * Happiness gain from eating an EV reducing berry
         */
        EVBERRY(10, 5, 2),
        /**
         * Happiness gain (loss) from faining
         */
        FAINT(-1, -1, -1),
        /**
         * Happiness gain from trading.
         */
        TRADE(0, 0, 0);

        public static void applyHappiness(final IPokemob mob, final HappinessType type)
        {
            final int current = mob.getHappiness();
            if (type == BERRY && mob.getStatus() != IMoveConstants.STATUS_NON) return;
            if (type != TRADE)
            {
                if (current < 100) mob.addHappiness(type.low);
                else if (current < 200) mob.addHappiness(type.mid);
                else mob.addHappiness(type.high);
            }
            else mob.addHappiness(-(current - mob.getPokedexEntry().getHappiness()));
        }

        public final int low;
        public final int mid;
        public final int high;

        private HappinessType(final int low, final int mid, final int high)
        {
            this.low = low;
            this.mid = mid;
            this.high = high;
        }
    }

    /**
     * This is the interface which an AI which is used to locate combat targets
     * for the pokemob implements.
     *
     */
    public static interface ITargetFinder
    {
        /**
         * Clears any tracking for our combat target.
         */
        void clear();
    }

    public static enum Stats
    {
        /**
         * Stat responsibly for pokemob's maximum health
         */
        HP,
        /**
         * Stat responsible for damage caused by physical attacks
         */
        ATTACK,
        /**
         * Stat responsible for reducing damage by physical attacks
         */
        DEFENSE,
        /**
         * Stat responsible for damage caused by special attacks
         */
        SPATTACK,
        /**
         * Stat responsible for reducing damage caused by special attacks
         */
        SPDEFENSE,
        /**
         * Stat responsible for some aspects of move use order in combat
         */
        VIT,
        /**
         * Stat responsible for whether our attacks hit
         */
        ACCURACY,
        /**
         * Stat responsible for whether we can dodge attacks.
         */
        EVASION,
    }

    static final UUID FLYSPEEDFACTOR_ID = UUID.fromString("662A6B8D-DA3E-4C1C-1235-96EA6097278D");
    static final AttributeModifier FLYSPEEDFACTOR = new AttributeModifier(IPokemob.FLYSPEEDFACTOR_ID, "flying boost",
            0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL);

    static final UUID SWIMSPEEDFACTOR_ID = UUID.fromString("662A6B8D-DA3E-4C1C-1236-96EA6097278D");
    static final AttributeModifier SWIMSPEEDFACTOR = new AttributeModifier(IPokemob.SWIMSPEEDFACTOR_ID, "swimmig boost",
            0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL);

    /*
     * Genders of pokemobs
     */
    byte MALE = 1;

    byte FEMALE = 2;

    byte NOSEXE = -1;

    byte SEXLEGENDARY = -2;

    int TYPE_CRIT = 2;

    /**
     * Sets our {@link ITargetFinder} instance
     * 
     * @param tracker
     */
    void setTargetFinder(ITargetFinder tracker);

    /**
     * @return our {@link ITargetFinder} instance
     */
    ITargetFinder getTargetFinder();

    /**
     * Called each tick of the mob, the default implementation ticks
     * {@link #timeSinceCombat()} and {@link #updateBattleInfo()}
     */
    default void onTick()
    {
        this.tickTimeSinceCombat();
        this.updateBattleInfo();
    }

    /**
     * Whether this mob can use the item HMDive to be ridden underwater.
     *
     * @return whether this mob can be ridden with HMDive
     */
    default boolean canUseDive()
    {
        return this.getPokedexEntry().shouldDive && PokecubeCore.getConfig().diveEnabled && this.canUseSurf();
    }

    /**
     * Whether this mob can use the item HMFly to be ridden in the air.
     *
     * @return whether this mob can be ridden with HMFly
     */
    default boolean canUseFly()
    {
        return (this.getPokedexEntry().shouldFly || this.getPokedexEntry().flys()) && !this.isGrounded();
    }

    /**
     * Whether this mob can use the item HMSurf to be ridden on water.
     *
     * @return whether this mob can be ridden with HMSurf
     */
    default boolean canUseSurf()
    {
        return this.getPokedexEntry().shouldSurf || this.getPokedexEntry().shouldDive || this.getPokedexEntry().swims()
                || this.isType(PokeType.getType("water"));
    }

    /**
     * @return our {@link DataSync} object used to synchronize values between
     *         client and server
     */
    DataSync dataSync();

    /**
     * @return Whether we float above the ground.
     */
    default boolean floats()
    {
        return this.getPokedexEntry().floats() && !this.isGrounded();
    }

    /**
     * @return Whether we can fly.
     */
    default boolean flys()
    {
        // TODO also include effects from external float reasons here
        return (this.getPokedexEntry().flys() || this.canUseFly() && this.getEntity().isVehicle())
                && !this.isGrounded();
    }

    /** If this is larger than 0, the pokemob shouldn't be allowed to attack. */
    @Override
    int getAttackCooldown();

    /**
     * See IMultiplePassengerEntity.getPitch()
     *
     * @return
     */
    float getPitch();

    /**
     * Returns the name to display in any GUI. Can be the nickname or the
     * Pokemob translated name.
     *
     * @return the name to display
     */
    default Component getDisplayName()
    {
        if (this.getPokemonNickname().isEmpty()) return this.getPokedexEntry().getTranslatedName();
        return TComponent.literal(this.getPokemonNickname());
    }

    /**
     * The evolution tick will be set when the mob evolves and then is decreased
     * each tick. It is used to render a special effect.
     *
     * @return the evolutionTicks
     */
    int getEvolutionTicks();

    /**
     * 1 for about to explode, -1 for not exploding, this should probably be
     * changed to a boolean.
     */
    int getExplosionState();

    /**
     * @return number of ticks since the last time we noticed we were in combat
     *         If this is negative or zero, we are in combat, otherwise we are
     *         not
     */
    int timeSinceCombat();

    /**
     * Flags us in combat, should set timeSinceCombat to 0
     */
    void resetCombatTime();

    /**
     * Increments us being in combat, should increase resetCombatTime if angry,
     * and decrease it if not
     */
    void tickTimeSinceCombat();

    /**
     * 
     * @return whether we are in combat.
     */
    default boolean inCombat()
    {
        return this.timeSinceCombat() > -20;
    }

    /**
     * @param index
     * @return the value of the flavour amount for this mob, this will be used
     *         for particle effects, and possibly for boosts based on how much
     *         the mob likes the flavour
     */
    int getFlavourAmount(int index);

    /**
     * @return how far above the ground we float, if {@link #floats()} is true.
     */
    default double getFloatHeight()
    {
        return this.getPokedexEntry().preferedHeight;
    }

    /** @return how happy is the pokemob, see {@link HappinessType} */
    int getHappiness();

    /**
     * @return our held item.
     */
    default ItemStack getHeldItem()
    {
        if (this.getEntity() == null) return ItemStack.EMPTY;
        return this.getEntity().getMainHandItem();
    }

    /**
     * @return the location we consider to be "home"
     */
    BlockPos getHome();

    /**
     * @return How far we should wander from {@link #getHome()}
     */
    float getHomeDistance();

    /**
     * Slots go as follows:<br>
     * <ul>
     *  <li>0 - saddle</li>
     *  <li>1 - held item</li>
     *  <li>2-6 - general inventory</li>
     *  <li>rest - armour followed by offhand item</li>
     * </ul>
     * 
     * @return the Container holding our inventory
     */
    Container getInventory();

    /**
     * @return the length/width/height of our mob.
     */
    Vector3 getMobSizes();

    /**
     * @return How fast we should move
     */
    default double getMovementSpeed()
    {
        final AttributeInstance iattributeinstance = this.getEntity().getAttribute(Attributes.MOVEMENT_SPEED);
        final boolean swimming = this.getEntity().isInWater()
                || this.getEntity().isInLava() && this.getEntity().fireImmune();
        final boolean flying = !swimming && !this.getEntity().isOnGround();

        final boolean hasFlyBoost = iattributeinstance.getModifier(IPokemob.FLYSPEEDFACTOR_ID) != null;
        final boolean hasSwimBoost = iattributeinstance.getModifier(IPokemob.SWIMSPEEDFACTOR_ID) != null;

        if (flying && !hasFlyBoost) iattributeinstance.addTransientModifier(IPokemob.FLYSPEEDFACTOR);
        else if (hasFlyBoost && !flying) iattributeinstance.removeModifier(IPokemob.FLYSPEEDFACTOR_ID);

        if (swimming && !hasSwimBoost) iattributeinstance.addTransientModifier(IPokemob.SWIMSPEEDFACTOR);
        else if (hasSwimBoost && !swimming) iattributeinstance.removeModifier(IPokemob.SWIMSPEEDFACTOR_ID);

        final double speed = iattributeinstance.getValue();
        return speed;
    }

    /**
     * @return All of our loaded AI tasks, this can be used to edit/adjust AI
     *         for combat, etc.
     */
    List<IAIRunnable> getTasks();
    
    Map<String, IAIRunnable> getNamedTaskes();

    /**
     * Note: This only returns a unique number for player owned pokemobs. All
     * other pokemobs will return -1
     *
     * @return
     */
    int getPokemonUID();

    /**
     * The personality value for the pokemob, used to determine nature, ability,
     * etc.<br>
     * http://bulbapedia.bulbagarden.net/wiki/Personality_value
     *
     * @return
     */
    int getRNGValue();

    /**
     * {@link #MALE} or {@link #FEMALE} or {@link #NOSEXE}
     *
     * @return the byte sexe
     */
    byte getSexe();

    /**
     * @return the sound we should play while idle.
     */
    default SoundEvent getSound()
    {
        return this.getPokedexEntry().getSoundEvent();
    }

    /**
     * Statuses: {@link IMoveConstants#STATUS_PSN} for example.
     *
     * @return the status
     */
    int getStatus();

    /**
     * The timer for SLP. When reach 0, the mob wakes up.
     *
     * @return the actual value of the timer.
     */
    short getStatusTimer();

    /**
     * @return whether we have a valid {@link #getHome()}
     */
    boolean hasHomeArea();

    /** Removes the current status. */
    void healStatus();

    /**
     * @return whether we are forced to not be able to fly or float.
     */
    boolean isGrounded();

    /**
     * @return whether we are presently on the ground.
     */
    boolean isOnGround();

    /**
     * Returns the texture path.
     *
     * @return
     */
    @OnlyIn(Dist.CLIENT)
    ResourceLocation getTexture();

    /**
     * Returns modified texture to account for shininess, animation, etc.
     *
     * @return
     */
    @OnlyIn(Dist.CLIENT)
    ResourceLocation modifyTexture(ResourceLocation texture);

    void setCustomHolder(FormeHolder holder);

    @Nullable
    /**
     * 
     * @return the {@link FormeHolder} which we presently have.
     */
    FormeHolder getCustomHolder();

    /**
     * Moves us to the player's shoulder.
     * 
     * @param player - player to put us on
     * @return whether we ended up on the shoulder.
     */
    default boolean moveToShoulder(final Player player)
    {
        if (this.getEntity() instanceof ShoulderRidingEntity mob)
        {
            if (player instanceof ServerPlayer splayer) return mob.setEntityOnShoulder(splayer);
        }
        else
        {
            if (this.getEntity().isAlive() && !this.getEntity().isPassenger() && player.getPassengers().isEmpty())
            {
                this.getEntity().getPersistentData().putBoolean("__on_shoulder__", true);
                this.setLogicState(LogicStates.SITTING, true);
                this.getEntity().startRiding(player, true);
                return true;
            }
        }
        return false;
    }

    /**
     * This method should only be used to update any Alleles objects that are
     * stored for the mob's genes.
     */
    default void onGenesChanged()
    {

    }

    /**
     * called when we return to the pokecube
     * 
     * @param onDeath - whether this occured on death
     */
    void onRecall(boolean onDeath);

    /** The mob returns to its pokecube. */
    default void onRecall()
    {
        if (this.getBossInfo() != null)
        {
            this.getBossInfo().removeAllPlayers();
            this.getBossInfo().setVisible(false);
        }
        this.onRecall(false);
    }

    /** Called to init the mob after it went out of its pokecube. */
    void onSendOut();

    /**
     * Loads values from nbt
     * 
     * @param tag - the nbt tag to load from.
     */
    void read(CompoundTag tag);

    /** Sets the value obtained by getAttackCooldown() */
    @Override
    void setAttackCooldown(int timer);

    /**
     * Sets our {@link DataSync} object used to synchronize values between
     * client and server
     * 
     * @param sync
     */
    void setDataSync(DataSync sync);

    /**
     * Sets our pitch heading direction.
     * 
     * @param pitch
     */
    void setDirectionPitch(float pitch);

    /**
     * 1 for about to explode, -1 for reset.
     *
     * @param i
     */
    void setExplosionState(int i);

    /**
     * Sets the flavour amount for that index.
     *
     * @param index
     * @param amount
     */
    void setFlavourAmount(int index, int amount);

    /**
     * Sets the experience.
     *
     * @param exp - exp to set
     * @return The IPokemob after the exp setting, may not be this if we
     *         evolved.
     */
    default IPokemob setForSpawn(final int exp)
    {
        return this.setForSpawn(exp, true);
    }

    /**
     * Sets the experience.
     *
     * @param exp    - exp to set
     * @param evolve - whether we should try to evolve if possible.
     * @return The IPokemob after the exp setting, may not be this if we
     *         evolved.
     */
    IPokemob setForSpawn(int exp, boolean evolve);

    /**
     * Sets our held item stack
     * 
     * @param stack - item to hold
     */
    default void setHeldItem(ItemStack stack)
    {
        this.getInventory().setItem(1, stack);
    }

    /**
     * Called when held item is changed, allows modifying the stack.
     * 
     * @param newStack - stack to hold
     * @return possibly modified stack to hold
     */
    default ItemStack onHeldItemChanged(ItemStack newStack)
    {
        return newStack;
    }

    /**
     * Sets the default home location and roam distance. This is probably better
     * managed via the IGuardAICapability.
     *
     * @param x
     * @param y
     * @param z
     * @param distance
     */
    void setHome(int x, int y, int z, int distance);

    /** sets the personality value for the pokemob, see getRNGValue() */
    void setRNGValue(int value);

    /**
     * {@link #MALE} or {@link #FEMALE} or {@link #NOSEXE}
     *
     * @param sexe the byte sexe
     */
    void setSexe(byte sexe);

    /**
     * Sets if we are a "shiny" pokemob, ie return value of {@link #isShiny()}
     * 
     * @param shiny
     */
    void setShiny(boolean shiny);

    /**
     * Called when the mob spawns naturally. Used to set held item for example.
     */
    default IPokemob spawnInit()
    {
        this.resetLoveStatus();
        return this.spawnInit(null);
    }

    /**
     * This is called when the mob is added to the world, it can return a
     * different pokemob if it evolves, in that case, this will have
     * markRemoved() called for it.
     *
     * @return
     */
    default IPokemob onAddedInit()
    {
        return this;
    }

    /**
     * This is called to mark this pokemob as "removed", if that is the case, it
     * will immediately despawn the next tick, without drops, etc
     */
    void markRemoved();

    /**
     * Returns true if markRemoved() was called!
     *
     * @return
     */
    boolean isRemoved();

    /**
     * Called when we are spawning from a SpawnRule
     * 
     * @param info - spawn rule which may modify us.
     * @return this or otherwise modified pokemob
     */
    IPokemob spawnInit(SpawnRule info);

    /**
     * @return whether we can swim under water well.
     */
    default boolean swims()
    {
        return this.getPokedexEntry().swims();
    }

    /**
     * Returns the held item this pokemob should have when found wild.
     *
     * @param mob
     * @return
     */
    default ItemStack wildHeldItem(final Mob mob)
    {
        return this.getPokedexEntry().getRandomHeldItem(mob);
    }

    /**
     * Brings us back from fainted/dead status.
     * 
     * @param fullHp - if true, we will recover to full health.
     */
    default void revive(boolean fullHp)
    {
        this.setCombatState(CombatStates.FAINTED, false);
        this.setHungerTime(0);
        this.onSetTarget(null, true);
        this.healStatus();
        this.healChanges();
        final Mob mob = this.getEntity();
        mob.setHealth(fullHp ? this.getStat(Stats.HP, false) : 1);
        mob.hurtTime = 0;
        mob.deathTime = 0;
        this.setDeathTime(0);
    }

    /**
     * 
     * @return the time of death of this mob, if 0 or below, mob is not dead. This
     *         time is set from {@link #setDeathTime(long)}, and should be set
     *         the the value of {@link Tracker#getTick()}
     */
    long getDeathTime();

    /**
     * Sets the time of death, if revived, this should be set to 0;
     * 
     * @param time
     */
    void setDeathTime(long time);

    /**
     * Saves our state to NBT
     * 
     * @return nbt containing our info.
     */
    CompoundTag write();

    /**
     * @return the {@link Battle} we are presently in.
     */
    public Battle getBattle();

    /**
     * 
     * @param battle - the {@link Battle} we are presently in.
     */
    public void setBattle(Battle battle);

    /**
     * @return the wrapped ICopyMob for registering as the capability, This is
     *         used for transform, etc.
     */
    ICopyMob getCopy();

    /**
     * @return current ServerBossEvent for us, this allows giving server-side
     *         boss health bars for custom pokemobs.
     */
    ServerBossEvent getBossInfo();

    /**
     * Sets the return value of {@link #getBossInfo()}
     * 
     * @param event
     */
    void setBossInfo(ServerBossEvent event);
}
