package pokecube.api.moves;

import java.util.HashMap;
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
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.data.moves.Moves.MoveHolder;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.init.InitMoveEntry;
import pokecube.api.events.pokemobs.combat.MoveUse.MoveWorldAction;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.database.tags.Tags;
import thut.api.maths.Vector3;

public class MoveEntry implements IMoveConstants
{
    public static interface TypeProvider
    {
        @Nullable
        PokeType getType(@Nullable IPokemob user);
    }

    public static interface PowerProvider
    {
        int getPWR(final IPokemob user, final LivingEntity target, int pwr);
    }

    public static interface CategoryProvider
    {
        ContactCategory getAttackCategory(@Nullable IPokemob user);
    }

    public static record MoveSounds(SoundEvent onSource, SoundEvent onTarget)
    {
    };

    private static HashMap<String, MoveEntry> movesNames = new HashMap<>();
    private static HashMap<String, MoveEntry> legacyMoveNames = new HashMap<>();

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
        MoveEntry.CONFUSED.category = AttackCategory.PHYSICAL;
        MoveEntry.CONFUSED.attackCategory = ContactCategory.CONTACT;
        MoveEntry.CONFUSED.power = 40;
        MoveEntry.CONFUSED.canHitNonTarget = false;
        // Blank root entry for us.
        CONFUSED.root_entry = new MoveHolder();
        CONFUSED.root_entry._manually_defined = true;
        CONFUSED.root_entry._target_type = "user";
        MoveEntry.movesNames.put(CONFUSED.name, CONFUSED);
    }

    public static MoveEntry get(String name)
    {
        return movesNames.getOrDefault(name, legacyMoveNames.get(name));
    }

    public static void removeMove(MoveEntry move)
    {
        movesNames.remove(move.name);
        legacyMoveNames.remove(move.legacy_name);
    }

    public static boolean reloading = false;

    public static void addMove(MoveEntry move)
    {
        if (!reloading && movesNames.containsKey(move.name)) PokecubeAPI.LOGGER.warn(
                "Warning, adding duplicate entry for {}, this will replace the previous one, call removeMove first if this was intentional!",
                move.name);
        movesNames.put(move.name, move);
        legacyMoveNames.put(move.legacy_name, move);
    }

    public static List<MoveEntry> values()
    {
        return Lists.newArrayList(MoveEntry.movesNames.values());
    }

    public static List<String> keys()
    {
        return Lists.newArrayList(MoveEntry.movesNames.keySet());
    }

    public final String name;
    public PokeType type;

    private final String legacy_name;

    /** Distance, contact, etc. */
    public ContactCategory attackCategory = ContactCategory.OTHER;
    public int power = 0;
    public int accuracy;
    public int pp;

    public float[] customSize = null;

    public int crit;

    private boolean canHitNonTarget = true;

    public boolean ohko = false;
    public boolean fixed = false;
    public boolean defrosts = false;

    /** Status, Special, Physical */

    public AttackCategory category = AttackCategory.OTHER;

    public MoveHolder root_entry;

    public String soundEffectSource;
    public String soundEffectTarget;

    private MoveSounds sounds;

    private IMoveAnimation animation;

    public TypeProvider typer = user -> this.type;
    public PowerProvider powerp = (user, target, power) -> power;
    public CategoryProvider categoryProvider = user -> this.getAttackCategory();
    public CategoryProvider _categoryProvider = categoryProvider;

    public MoveEntry(final String name)
    {
        this.name = name;
        this.legacy_name = name.replace("-", "");
    }

    public void postInit()
    {
        boolean contact = Tags.MOVE.isIn("contact-moves", this.getName());
        boolean ranged = Tags.MOVE.isIn("ranged-moves", this.getName());
        this.ohko = root_entry._ohko;
        this.attackCategory = contact ? ContactCategory.CONTACT
                : ranged ? ContactCategory.RANGED : ContactCategory.OTHER;
        PokecubeAPI.MOVE_BUS.post(new InitMoveEntry(this));
    }

    public boolean checkValid()
    {
        boolean valid = root_entry._manually_defined;
        valid |= root_entry._infatuates;
        valid |= root_entry._ohko;
        valid |= root_entry._protects;
        valid |= root_entry._drain != 0;
        valid |= root_entry._healing != 0;
        valid |= root_entry._status_chance != 0;
        valid |= root_entry._stat_chance != 0;
        valid |= this.power > 0;
        return valid;
    }

    public boolean canHitNonTarget()
    {
        return this.canHitNonTarget;
    }

    public boolean isAoE()
    {
        return this.root_entry._aoe;
    }

    public boolean isMultiTarget()
    {
        return this.root_entry._multi_target;
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
        if (target == null) return this.power;
        return powerp.getPWR(user, target, this.power);
    }

    /**
     * Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT} or
     * {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor.
     *
     * @return the attack category
     */
    public ContactCategory getAttackCategory()
    {
        return this.attackCategory;
    }

    /**
     * Attack category getter. Can be {@link IMoveConstants#CATEGORY_CONTACT} or
     * {@link IMoveConstants#CATEGORY_DISTANCE}. Set by the constructor. <br>
     * <br>
     * This version is user aware
     *
     * @return the attack category
     */
    public ContactCategory getAttackCategory(final IPokemob user)
    {
        return categoryProvider.getAttackCategory(user);
    }

    /** @return Move category for this move. */
    public AttackCategory getCategory()
    {
        return this.category;
    }

    /**
     * User sensitive version of {@link Move_Base#getCategory()}
     *
     * @param user
     * @return
     */
    public AttackCategory getCategory(final IPokemob user)
    {
        return this.getCategory();
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isRanged(IPokemob user)
    {
        return (this.getAttackCategory(user)) == ContactCategory.RANGED;
    }

    public boolean isContact(IPokemob user)
    {
        return (this.getAttackCategory(user)) == ContactCategory.CONTACT;
    }

    public MoveApplication applyMove(IPokemob user, @Nullable LivingEntity target, @Nullable Vector3 targetPos)
    {
        MoveApplication apply = new MoveApplication(this, user, target);
        MoveApplicationRegistry.apply(apply);
        return apply;
    }

    public float getPostDelayFactor(IPokemob attacker)
    {
        return this.root_entry._post_attack_delay_factor;
    }

    public void playSounds(MoveApplication application)
    {
        Mob attacker = application.getUser().getEntity();
        LivingEntity attacked = application.getTarget();

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
