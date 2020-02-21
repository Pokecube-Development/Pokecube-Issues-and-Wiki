/**
 *
 */
package pokecube.core.interfaces;

import java.util.List;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
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
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasMobAIStates;
import pokecube.core.interfaces.pokemob.IHasMoves;
import pokecube.core.interfaces.pokemob.IHasOwner;
import pokecube.core.interfaces.pokemob.IHasStats;
import pokecube.core.utils.PokeType;
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

    public static enum Stats
    {
        HP, ATTACK, DEFENSE, SPATTACK, SPDEFENSE, VIT, ACCURACY, EVASION,
    }

    /*
     * Genders of pokemobs
     */
    byte MALE = 1;

    byte FEMALE = 2;

    byte NOSEXE = -1;

    byte SEXLEGENDARY = -2;

    int TYPE_CRIT = 2;

    default void onTick()
    {

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
        return this.getPokedexEntry().flys() && !this.isGrounded();
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
        return this.getEntity().getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
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

    void setCustomTexDetails(String male, String female);

    default void setCustomTexDetails(final String tex)
    {
        this.setCustomTexDetails(tex, tex);
    }

    String getMaleCustomTex();

    String getFemaleCustomTex();

    default boolean moveToShoulder(final PlayerEntity player)
    {
        if (this.getEntity() instanceof ShoulderRidingEntity)
        {
            if (player instanceof ServerPlayerEntity) ((ShoulderRidingEntity) this.getEntity()).func_213439_d(
                    (ServerPlayerEntity) player);
            return true;
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
    IPokemob spawnInit();

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

    CompoundNBT write();
}
