package pokecube.api.moves;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.init.InitMoveEntry;
import pokecube.api.events.pokemobs.combat.MoveUse.MoveWorldAction;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.json.Moves.MoveHolder;
import thut.api.maths.Vector3;

public class MoveEntry implements IMoveConstants
{
    public static interface TypeProvider
    {
        @Nullable
        PokeType getType(final IPokemob user);
    }

    public static interface PowerProvider
    {
        int getPWR(final IPokemob user, final LivingEntity target, int pwr);
    }

    public static record MoveSounds(SoundEvent onSource, SoundEvent onTarget)
    {
    };

    public static enum Category
    {
        OTHER, SPECIAL, PHYSICAL;
    }

    public static HashMap<String, MoveEntry> movesNames = new HashMap<>();
    public static HashSet<String> protectionMoves = new HashSet<>();
    public static HashSet<String> unBlockableMoves = new HashSet<>();
    public static HashSet<String> oneHitKos = new HashSet<>();

    public static int TOTALHP = 1;
    public static int DAMAGEDEALT = 2;
    public static int RELATIVEHP = 4;
    public static int MISS = 8;

    public static int NODAMAGE = -2;
    public static int FULLHP = -1;
    public static int LEVEL = -5;
    public static int SPECIAL = -4;
    public static int FLEE = -3;

    public static final MoveEntry CONFUSED;

    static
    {
        CONFUSED = new MoveEntry("pokemob.status.confusion");
        MoveEntry.CONFUSED.type = PokeType.unknown;
        MoveEntry.CONFUSED.category = IMoveConstants.PHYSICAL;
        MoveEntry.CONFUSED.attackCategory = IMoveConstants.CATEGORY_CONTACT + IMoveConstants.CATEGORY_SELF;
        MoveEntry.CONFUSED.power = 40;
        MoveEntry.CONFUSED.canHitNonTarget = false;
    }

    public static MoveEntry get(String name)
    {
        if (name.equals(CONFUSED.name)) return CONFUSED;
        // Then return or add a new entry, make a warning if no json entry was
        // present, but accept it anyway.
        return MoveEntry.movesNames.computeIfAbsent(name, n -> {
            PokecubeAPI.LOGGER.warn("Warning, auto-generating a move entry for un-registered move " + n);
            return new MoveEntry(n);
        });
    }

    public static List<MoveEntry> values()
    {
        return Lists.newArrayList(MoveEntry.movesNames.values());
    }

    public final String name;
    public PokeType type;

    /** Distance, contact, etc. */
    public int attackCategory;
    public int power = 0;
    public int accuracy;
    public int pp;
    public byte statusChange;
    public float statusChance;
    public byte change = IMoveConstants.CHANGE_NONE;
    public float chanceChance = 0;
    public int[] attackerStatModification =
    { 0, 0, 0, 0, 0, 0, 0, 0 };
    public float attackerStatModProb = 1;
    public int[] attackedStatModification =
    { 0, 0, 0, 0, 0, 0, 0, 0 };
    public float attackedStatModProb = 1;

    public float damageHeal = 0;
    public float selfHealRatio = 0;
    public float targetHealRatio = 0;

    public float[] customSize = null;

    private boolean multiTarget = false;
    private boolean canHitNonTarget = true;

    public int crit;
    public boolean soundType = false;
    public boolean fixed = false;
    public float selfDamage = 0;
    public int selfDamageType;
    public int priority = 0;
    public boolean defrosts = false;
    public boolean mirrorcoated = false;

    public boolean ohko = false;

    /**
     * Scaling factor on cooldown, if not specified in the json, this gets set
     * to 4 for moves like hyperbeam
     */
    public float cooldown_scale = 1.0f;

    /** Status, Special, Physical */

    public byte category = -1;
    public String animDefault = "none";

    public MoveHolder root_entry;

    public String soundEffectSource;
    public String soundEffectTarget;

    private MoveSounds sounds;

    private IMoveAnimation animation;

    public boolean hasStatModSelf = false;
    public boolean hasStatModTarget = false;

    public TypeProvider typer = user -> this.type;
    public PowerProvider powerp = (user, target, power) -> power;

    public MoveEntry(final String name)
    {
        this.name = name;
    }

    public void postInit()
    {
        boolean mod = false;
        for (final int i : this.attackedStatModification) if (i != 0)
        {
            mod = true;
            break;
        }
        if (!mod) this.attackedStatModProb = 0;
        mod = false;
        for (final int i : this.attackerStatModification) if (i != 0)
        {
            mod = true;
            break;
        }
        if (!mod) this.attackerStatModProb = 0;

        if (this.attackedStatModProb > 0) this.hasStatModTarget = true;
        if (this.attackerStatModProb > 0) this.hasStatModSelf = true;

        PokecubeAPI.MOVE_BUS.post(new InitMoveEntry(this));
    }

    public boolean isMultiTarget()
    {
        if (this.root_entry != null) return this.root_entry._multi_target;
        return this.multiTarget;
    }

    public boolean notInterceptable()
    {
        if (this.root_entry != null) return this.root_entry._interceptable;
        return false;
    }

    public boolean canHitNonTarget()
    {
        return this.canHitNonTarget;
    }

    public void setCanHitNonTarget(final boolean b)
    {
        this.canHitNonTarget = b;
    }

    public boolean isAoE()
    {
        // TODO AoE
        return false;
    }

    /**
     * Sets the move animation
     *
     * @param anim
     * @return
     */
    public MoveEntry setAnimation(final IMoveAnimation anim)
    {
        this.animation = anim;
        return this;
    }

    /**
     * Gets the {@link IMoveAnimation} for this move.
     *
     * @return
     */
    public IMoveAnimation getAnimation()
    {
        return this.animation;
    }

    /**
     * User sensitive version of {@link Move_Base#getAnimation()}
     *
     * @param user
     * @return
     */
    public IMoveAnimation getAnimation(final IPokemob user)
    {
        return this.getAnimation();
    }

    /**
     * Applys world effects of the move
     *
     * @param attacker - mob using the move
     * @param location - locaton move hits
     */
    public void doWorldAction(IPokemob attacker, Vector3 location)
    {
        final Vector3 origin = new Vector3().set(attacker.getEntity().getEyePosition(0));
        final Vector3 direction = location.subtract(origin).norm().scalarMultBy(0.5);
        location = location.add(direction);
        final MoveWorldAction.PreAction preEvent = new MoveWorldAction.PreAction(this, attacker, location);
        if (!PokecubeAPI.MOVE_BUS.post(preEvent))
        {
            final MoveWorldAction.OnAction onEvent = new MoveWorldAction.OnAction(this, attacker, location);
            PokecubeAPI.MOVE_BUS.post(onEvent);
            final MoveWorldAction.PostAction postEvent = new MoveWorldAction.PostAction(this, attacker, location);
            PokecubeAPI.MOVE_BUS.post(postEvent);
        }
    }

    /**
     * Type getter
     *
     * @return the type of this move
     */
    public PokeType getType(final IPokemob user)
    {
        PokeType type = typer.getType(user);
        return type == null ? this.type : type;
    }

    /**
     * Applies hunger cost to attacker when this move is used. Hunger is used
     * instead of PP in pokecube
     *
     * @param attacker
     */
    public void applyHungerCost(final IPokemob attacker)
    {
        float relative = (50 - pp) / 30;
        relative = relative * relative;
        attacker.applyHunger((int) (relative * 100));
    }

    /**
     * PWR getter
     *
     * @return the power of this move
     */
    public int getPWR()
    {
        return this.power;
    }

    /**
     * PWR getter
     *
     * @return the power of this move
     */
    public int getPWR(final IPokemob user, final LivingEntity target)
    {
        return powerp.getPWR(user, target, this.power);
    }

    /**
     * Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT} or
     * {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor.
     *
     * @return the attack category
     */
    public byte getAttackCategory()
    {
        return (byte) this.attackCategory;
    }

    /**
     * Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT} or
     * {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor. <br>
     * <br>
     * This version is user aware
     *
     * @return the attack category
     */
    public byte getAttackCategory(final IPokemob user)
    {
        return (byte) this.attackCategory;
    }

    /** @return Move category for this move. */
    public Category getCategory()
    {
        return Category.values()[this.category];
    }

    /**
     * User sensitive version of {@link Move_Base#getCategory()}
     *
     * @param user
     * @return
     */
    public Category getCategory(final IPokemob user)
    {
        return this.getCategory();
    }

    public String getName()
    {
        return this.name;
    }

    /** @return Does this move targer the user. */
    public boolean isSelfMove()
    {
        return (this.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0;
    }

    /** @return Does this move targer the user. */
    public boolean isSelfMove(IPokemob user)
    {
        return (this.getAttackCategory(user) & IMoveConstants.CATEGORY_SELF) > 0;
    }

    public boolean isRanged(IPokemob user)
    {
        return (this.getAttackCategory(user) & IMoveConstants.CATEGORY_DISTANCE) > 0;
    }

    public void applyMove(IPokemob user, LivingEntity target)
    {
        MoveApplication apply = new MoveApplication(this, user, target);
        apply.apply();
    }

    public void playSounds(MoveApplication application)
    {
        Mob attacker = application.getUser().getEntity();
        LivingEntity attacked = application.target;

        if (attacked == null) attacked = attacker;

        if (sounds == null)
        {
            SoundEvent user = null;
            SoundEvent target = null;
            if (this.soundEffectSource != null)
            {
                user = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(this.soundEffectSource));
                if (user == null) PokecubeAPI.LOGGER
                        .error("No Sound found for `" + this.soundEffectSource + "` for attack " + this.getName());
                this.soundEffectSource = null;
            }
            if (this.soundEffectTarget != null)
            {
                target = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(this.soundEffectTarget));
                if (target == null) PokecubeAPI.LOGGER
                        .error("No Sound found for `" + this.soundEffectTarget + "` for attack " + this.getName());
                this.soundEffectTarget = null;
            }
            sounds = new MoveSounds(user, target);
        }

        MoveSounds sounds = this.sounds;
        if (application.sounds != null) sounds = application.sounds;

        final Vector3 pos = new Vector3();
        final float scale = (float) PokecubeCore.getConfig().moveVolumeCry;
        final Level world = attacker.getLevel();
        final float pitch = 1;
        final float volume = 1 * scale;

        pos.set(attacker);
        if (sounds.onSource() != null)
            world.playLocalSound(pos.x, pos.y, pos.z, sounds.onSource(), SoundSource.HOSTILE, volume, pitch, true);

        pos.set(attacked);
        if (sounds.onTarget() != null)
            world.playLocalSound(pos.x, pos.y, pos.z, sounds.onTarget(), SoundSource.HOSTILE, volume, pitch, true);
    }
}
