/**
 *
 */
package pokecube.api.moves.utils;

import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Mob;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.item.ItemList;

/** @author Manchou */
public interface IMoveConstants extends IMoveNames
{
    static ResourceLocation BURROWS = new ResourceLocation(PokecubeAPI.MODID, "burrowers");

    static final Predicate<IPokemob> isBee = pokemob -> {
        final Mob entity = pokemob.getEntity();
        final boolean isBee = entity.getType().is(EntityTypeTags.BEEHIVE_INHABITORS);
        // Only care about bees
        if (!isBee) return false;
        // Only process stock pokemobs
        if (!pokemob.getPokedexEntry().stock) return false;
        return true;
    };

    static final Predicate<IPokemob> burrows = pokemob -> {
        final Mob entity = pokemob.getEntity();
        final boolean isAnt = ItemList.is(IMoveConstants.BURROWS, entity);
        // Only care about bees
        if (!isAnt) return false;
        // Only process stock pokemobs
        if (!pokemob.getPokedexEntry().stock) return false;
        return true;
    };

    static final Predicate<IPokemob> canFly = pokemob -> {
        final PokedexEntry entry = pokemob.getPokedexEntry();
        // These are our 3 criteria for if something is able to stay airborne
        return entry.shouldFly || entry.floats() || entry.flys();
    };

    static final Predicate<IPokemob> canOpenDoors = pokemob -> {
        // Only tame pokemobs can do this.
        return pokemob.isPlayerOwned();
    };

    public static enum ContactCategory
    {
        RANGED, CONTACT, OTHER;
    }

    public static enum AttackCategory
    {
        SPECIAL, PHYSICAL, STATUS, OTHER;
    }

    /*
     * exclusive Status Effects
     */
    int STATUS_NON = 0;
    int STATUS_BRN = 1;
    int STATUS_FRZ = 2;
    int STATUS_PAR = 4;
    int STATUS_PSN = 8, STATUS_PSN2 = 24;
    int STATUS_SLP = 32;
    /*
     * non-exclusive status effects
     */
    int CHANGE_NONE = 64;
    int CHANGE_CONFUSED = 128;
    int CHANGE_FLINCH = 256;
    int CHANGE_CURSE = 512;
    /*
     * Stats Modifiers
     */
    byte ATTACK = 1;
    byte DEFENSE = 2;
    byte SPATACK = 4;
    byte SPDEFENSE = 8;
    byte VIT = 16;
    byte ACCURACY = 32;

    byte EVASION = 64;
    /*
     * Stats Changes
     */
    byte HARSH = -2;
    byte FALL = -1;
    byte RAISE = 1;
    byte SHARP = 2;
    byte DRASTICALLY = 3;

    // Special Moves, ie ones needed for specific logic
    // No move move for just sitting there
    String MOVE_NONE = "none";

    String DEFAULT_MOVE = "tackle";
    /*
     * Flavours
     */
    byte SPICY = 0; // red
    byte DRY = 1; // blue
    byte SWEET = 2; // pink
    byte BITTER = 3; // green

    byte SOUR = 4; // yellow
}
