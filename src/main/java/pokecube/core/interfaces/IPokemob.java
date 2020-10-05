/**
 *
 */
package pokecube.core.interfaces;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader.DefaultFormeHolder;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasMobAIStates;
import pokecube.core.interfaces.pokemob.IHasMoves;
import pokecube.core.interfaces.pokemob.IHasOwner;
import pokecube.core.interfaces.pokemob.IHasStats;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.utils.PokeType;
import thut.api.ModelHolder;
import thut.api.entity.IBreedingMob;
import thut.api.entity.IHungrymob;
import thut.api.entity.IMobColourable;
import thut.api.entity.IShearable;
import thut.api.entity.ai.IAIRunnable;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.DataSync;

/** @author Manchou */
public interface IPokemob extends IHasMobAIStates, IHasMoves, ICanEvolve, IHasOwner, IHasStats, IHungrymob,
        IBreedingMob, IHasCommands, IMobColourable, IShearable
{
    public static class FormeHolder extends ModelHolder
    {
        public static FormeHolder load(final CompoundNBT nbt)
        {
            final String name = nbt.getString("key");
            final String anim = nbt.contains("anim") ? nbt.getString("anim") : null;
            final String model = nbt.contains("model") ? nbt.getString("model") : null;
            final String tex = nbt.contains("tex") ? nbt.getString("tex") : null;
            final FormeHolder holder = FormeHolder.get(model != null ? PokecubeItems.toPokecubeResource(model) : null,
                    tex != null ? PokecubeItems.toPokecubeResource(tex) : null, anim != null ? PokecubeItems
                            .toPokecubeResource(anim) : null, PokecubeItems.toPokecubeResource(name));
            return holder;
        }

        public static FormeHolder get(final ResourceLocation model, final ResourceLocation texture,
                final ResourceLocation animation, final ResourceLocation name)
        {
            if (Database.formeHolders.containsKey(name)) return Database.formeHolders.get(name);
            final FormeHolder holder = new FormeHolder(model, texture, animation, name);
            Database.formeHolders.put(name, holder);
            return holder;
        }

        // This key is used for unique identifier for lookup in renderer for
        // initialization.
        public ResourceLocation   key;
        public DefaultFormeHolder loaded_from;

        private FormeHolder(final ResourceLocation model, final ResourceLocation texture,
                final ResourceLocation animation, final ResourceLocation name)
        {
            super(model, texture, animation, name.toString());
            this.key = name;
        }

        public CompoundNBT save()
        {
            final CompoundNBT ret = new CompoundNBT();
            ret.putString("key", this.key.toString());
            if (this.model != null) ret.putString("model", this.model.toString());
            if (this.animation != null) ret.putString("anim", this.animation.toString());
            if (this.texture != null) ret.putString("tex", this.texture.toString());
            return ret;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof FormeHolder) return this.key.equals(((FormeHolder) obj).key);
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.key.hashCode();
        }
    }

    public static enum HappinessType
    {
        TIME(2, 2, 1), LEVEL(5, 3, 2), BERRY(3, 2, 1), EVBERRY(10, 5, 2), FAINT(-1, -1, -1), TRADE(0, 0, 0);

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

    public static interface ITargetFinder
    {
        void clear();
    }

    public static enum Stats
    {
        HP, ATTACK, DEFENSE, SPATTACK, SPDEFENSE, VIT, ACCURACY, EVASION,
    }

    static final UUID              FLYSPEEDFACTOR_ID = UUID.fromString("662A6B8D-DA3E-4C1C-1235-96EA6097278D");
    static final AttributeModifier FLYSPEEDFACTOR    = new AttributeModifier(IPokemob.FLYSPEEDFACTOR_ID,
            "following speed boost", 1F, AttributeModifier.Operation.MULTIPLY_TOTAL).setSaved(false);

    static final UUID              SWIMSPEEDFACTOR_ID = UUID.fromString("662A6B8D-DA3E-4C1C-1236-96EA6097278D");
    static final AttributeModifier SWIMSPEEDFACTOR    = new AttributeModifier(IPokemob.FLYSPEEDFACTOR_ID,
            "following speed boost", 0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL).setSaved(false);

    /*
     * Genders of pokemobs
     */
    byte MALE = 1;

    byte FEMALE = 2;

    byte NOSEXE = -1;

    byte SEXLEGENDARY = -2;

    int TYPE_CRIT = 2;

    void setTargetFinder(ITargetFinder tracker);

    ITargetFinder getTargetFinder();

    default void onTick()
    {
        this.tickTimeSinceCombat();
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

    DataSync dataSync();

    @Override
    void eat(Object eaten);

    default boolean floats()
    {
        return this.getPokedexEntry().floats() && !this.isGrounded();
    }

    // TODO also include effects from external float reasons here
    default boolean flys()
    {
        return (this.getPokedexEntry().flys() || this.canUseFly() && this.getEntity().isBeingRidden()) && !this
                .isGrounded();
    }

    /** If this is larger than 0, the pokemob shouldn't be allowed to attack. */
    @Override
    int getAttackCooldown();

    /**
     * See IMultiplePassengerEntity.getPitch() TODO remove this infavour of the
     * IMultiplePassengerentity implementation
     *
     * @return
     */
    float getDirectionPitch();

    /**
     * Returns the name to display in any GUI. Can be the nickname or the
     * Pokemob translated name.
     *
     * @return the name to display
     */
    default ITextComponent getDisplayName()
    {
        if (this.getPokemonNickname().isEmpty()) return new TranslationTextComponent(this.getPokedexEntry()
                .getUnlocalizedName());
        return new StringTextComponent(this.getPokemonNickname());
    }

    /**
     * The evolution tick will be set when the mob evolves and then is
     * decreased each tick. It is used to render a special effect.
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

    default double getFloatHeight()
    {
        return this.getPokedexEntry().preferedHeight;
    }

    /** @return how happy is the pokemob, see {@link HappinessType} */
    int getHappiness();

    default ItemStack getHeldItem()
    {
        if (this.getEntity() == null) return ItemStack.EMPTY;
        return this.getEntity().getHeldItemMainhand();
    }

    BlockPos getHome();

    float getHomeDistance();

    IInventory getInventory();

    Vector3 getMobSizes();

    default double getMovementSpeed()
    {
        final IAttributeInstance iattributeinstance = this.getEntity().getAttribute(
                SharedMonsterAttributes.MOVEMENT_SPEED);
        final boolean swimming = this.getEntity().isInWater() || this.getEntity().isInLava() && this.getEntity()
                .isImmuneToFire();
        final boolean flying = !swimming && !this.getEntity().onGround;

        final boolean hasFlyBoost = iattributeinstance.getModifier(IPokemob.FLYSPEEDFACTOR_ID) != null;
        final boolean hasSwimBoost = iattributeinstance.getModifier(IPokemob.SWIMSPEEDFACTOR_ID) != null;

        if (flying && !hasFlyBoost) iattributeinstance.applyModifier(IPokemob.FLYSPEEDFACTOR);
        else if (hasFlyBoost && !flying) iattributeinstance.removeModifier(IPokemob.FLYSPEEDFACTOR_ID);

        if (swimming && !hasSwimBoost) iattributeinstance.applyModifier(IPokemob.SWIMSPEEDFACTOR);
        else if (hasSwimBoost && !swimming) iattributeinstance.removeModifier(IPokemob.SWIMSPEEDFACTOR_ID);

        final double speed = iattributeinstance.getValue();
        return speed;
    }

    List<IAIRunnable> getTasks();

    /**
     * Note: This only returns a unique number for player owned pokemobs. All
     * other pokemobs will return -1
     *
     * @return
     */
    int getPokemonUID();

    /**
     * The personality value for the pokemob, used to determine nature,
     * ability, etc.<br>
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
    @Override
    byte getSexe();

    default SoundEvent getSound()
    {
        return this.getPokedexEntry().getSoundEvent();
    }

    /**
     * Statuses: {@link IMoveConstants#STATUS_PSN} for example.
     *
     * @return the status
     */
    byte getStatus();

    /**
     * The timer for SLP. When reach 0, the mob wakes up.
     *
     * @return the actual value of the timer.
     */
    short getStatusTimer();

    boolean hasHomeArea();

    /** Removes the current status. */
    void healStatus();

    boolean isGrounded();

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
    FormeHolder getCustomHolder();

    default boolean moveToShoulder(final PlayerEntity player)
    {
        if (this.getEntity() instanceof ShoulderRidingEntity) if (player instanceof ServerPlayerEntity)return ((ShoulderRidingEntity) this.getEntity()).func_213439_d(
                (ServerPlayerEntity) player);
        return false;
    }

    /**
     * This method should only be used to update any Alleles objects that are
     * stored for the mob's genes.
     */
    default void onGenesChanged()
    {

    }

    void onRecall(boolean onDeath);

    /** The mob returns to its pokecube. */
    default void onRecall()
    {
        this.onRecall(false);
    }

    /** Called to init the mob after it went out of its pokecube. */
    void onSendOut();

    void read(CompoundNBT tag);

    /** Sets the value obtained by getAttackCooldown() */
    @Override
    void setAttackCooldown(int timer);

    void setDataSync(DataSync sync);

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
     * @param exp
     * @param notifyLevelUp
     *            should be false in an initialize step and true in a true exp
     *            earning
     */
    default IPokemob setForSpawn(final int exp)
    {
        return this.setForSpawn(exp, true);
    }

    IPokemob setForSpawn(int exp, boolean evolve);

    default void setHeldItem(final ItemStack stack)
    {
        this.getEntity().setHeldItem(Hand.MAIN_HAND, stack);
    }

    /**
     * Sets the default home location and roam distance. This is probably
     * better managed via the IGuardAICapability.
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
     * @param sexe
     *            the byte sexe
     */
    @Override
    void setSexe(byte sexe);

    void setShiny(boolean shiny);

    /**
     * Called when the mob spawns naturally. Used to set held item for
     * example.
     */
    default IPokemob spawnInit()
    {
        this.resetLoveStatus();
        return this.spawnInit(null);
    }

    IPokemob spawnInit(SpawnRule info);

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
    default ItemStack wildHeldItem(final MobEntity mob)
    {
        return this.getPokedexEntry().getRandomHeldItem(mob);
    }

    default void revive()
    {
        this.setCombatState(CombatStates.FAINTED, false);
        this.setCombatState(CombatStates.ANGRY, false);
        this.setHungerTime(0);
        this.onSetTarget(null, true);
        this.healStatus();
        this.healChanges();
        final MobEntity mob = this.getEntity();
        mob.setHealth(this.getOwnerId() == null ? this.getStat(Stats.HP, false) : 1);
        mob.hurtTime = 0;
        mob.deathTime = 0;
    }

    CompoundNBT write();
}
