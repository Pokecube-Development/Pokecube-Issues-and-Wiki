/**
 *
 */
package pokecube.core.interfaces;

/** @author Manchou */
public interface IMoveConstants extends IMoveNames
{
    public static enum AIRoutine
    {
        //@formatter:off
        GATHER,         //Does the pokemob gather item drops and harvest crops.
        STORE(false),   //Does the pokemob store its inventory when full.
        WANDER,         //Does the pokemob wander around randomly
        MATE,           //Does the pokemob breed.
        FOLLOW,         //Does the pokemob follow its owner.
        AGRESSIVE,      //Does the pokemob find targets to attack.
        AIRBORNE;       //Does the pokemob fly around, or can it only walk.
        //@formatter:on

        private final boolean default_;

        private AIRoutine()
        {
            this.default_ = true;
        }

        private AIRoutine(boolean value)
        {
            this.default_ = value;
        }

        /** @return default state for this routine. */
        public boolean getDefault()
        {
            return this.default_;
        }
    }

    /*
     * exclusive Status Effects
     */
    byte STATUS_NON  = 0;
    byte STATUS_BRN  = 1;
    byte STATUS_FRZ  = 2;
    byte STATUS_PAR  = 4;
    byte STATUS_PSN  = 8;
    byte STATUS_PSN2 = 24;

    byte STATUS_SLP = 32;
    /*
     * Stats Modifiers
     */
    byte ATTACK    = 1;
    byte DEFENSE   = 2;
    byte SPATACK   = 4;
    byte SPDEFENSE = 8;
    byte VIT       = 16;
    byte ACCURACY  = 32;

    byte EVASION = 64;
    /*
     * Stats Changes
     */
    byte HARSH = -2;
    byte FALL  = -1;
    byte RAISE = 1;
    byte SHARP = 2;

    byte DRASTICALLY = 3;
    /*
     * non-exclusive status effects
     */
    byte CHANGE_NONE     = 0;
    byte CHANGE_CONFUSED = 1;
    byte CHANGE_FLINCH   = 2;

    byte CHANGE_CURSE = 4;
    /*
     * Move Categories
     */
    byte CATEGORY_CONTACT  = 1;
    byte CATEGORY_DISTANCE = 2;
    byte CATEGORY_SELF     = 4;

    byte CATEGORY_SELF_EFFECT = 8;
    /*
     * Move damage category
     */
    byte SPECIAL = 1;

    byte PHYSICAL = 2;
    // Special Moves, ie ones needed for specific logic
    // No move move for just sitting there
    String MOVE_NONE = "none";

    String DEFAULT_MOVE = "tackle";
    /*
     * Flavours
     */
    byte SPICY  = 0;       // red
    byte DRY    = 1;       // blue
    byte SWEET  = 2;       // pink
    byte BITTER = 3;       // green

    byte SOUR = 4;       // yellow
}
