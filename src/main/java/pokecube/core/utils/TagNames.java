package pokecube.core.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;

public interface TagNames
{
    public static final String FORGECAPS = "ForgeCaps";

    public static final String POKEMOBCAP = "pokecube:pokemob";

    public static final String GENESCAP = GeneticsManager.POKECUBEGENETICS.toString();

    /** The NBTTag name for the root of info */
    public static final String POKEMOBTAG = "pokemob_tag";
    public static final String VERSION    = "version";
    // Sub tags under POKEMOBTAG
    public static final String BREEDINGTAG = "sexe_tag";

    public static final String OWNERSHIPTAG = "ownership_tag";
    public static final String STATSTAG     = "stats_tag";

    public static final String MOVESTAG     = "moves_tag";
    public static final String VISUALSTAG   = "visuals_tag";
    public static final String AITAG        = "ai_tag";
    public static final String INVENTORYTAG = "inventory_tag";
    public static final String MISCTAG      = "misc_tag";
    // Tag names for Pokemob Information
    public static final String OT       = "OTUUID";
    public static final String OWNER    = "OwnerID";
    public static final String ISTRADED = "traded";

    public static final String PLAYERS      = "playerOwned";
    public static final String ANCIENT      = "isAncient";
    public static final String UID          = "pokemobUID";
    public static final String RNGVAL       = "personalityValue";
    public static final String FORME        = "forme";
    public static final String WASSHADOW    = "wasShadow";
    public static final String COLOURS      = "colours";
    public static final String SHINY        = "shiny";
    public static final String SPECIALTAG   = "specialInfo";
    public static final String SCALE        = "scale";
    public static final String NEWMOVES     = "newMoves";
    public static final String MOVEINDEX    = "index";
    public static final String EXP          = "exp";
    public static final String SEXE         = "sexe";
    public static final String SEXETIME     = "loveTimer";
    public static final String POKEDEXNB    = "pokedexNb";
    public static final String TEAM         = "team";
    public static final String STATUS       = "status";
    public static final String HAPPY        = "bonusHappiness";
    public static final String NICKNAME     = "nickname";
    public static final String EVS          = "EVS";
    public static final String IVS          = "IVS";
    public static final String MOVES        = "moves";
    public static final String MOVELIST     = "movesList";
    public static final String LASTUSED     = "lastMove";
    public static final String COOLDOWN     = "cooldown";
    public static final String DISABLED     = "disabled";
    public static final String NATURE       = "nature";
    public static final String ABILITY      = "ability";
    public static final String ABILITYINDEX = "abilityIndex";
    public static final String FLAVOURSTAG  = "flavours";
    public static final String POKECUBE     = "pokecube";
    public static final String LOGICSTATE   = "logicState";
    public static final String GENERALSTATE = "generalState";
    public static final String COMBATSTATE  = "combatState";
    public static final String DYNAPOWER    = "dynamaxPower";
    public static final String MODELHOLDER  = "customModel";

    public static final String AIROUTINES = "aiRoutines";
    public static final String HUNGER     = "hunger";
    public static final String ITEMS      = "items";
    // Tag names for Pokecubes
    public static final String POKEMOB  = "Pokemob";
    public static final String OTHERMOB = "Othermob";
    public static final String MOBID    = "mobID";

    public static final String POKESEAL = "Explosion";

    public static final String SHEARTIME = "pokecube:shearedtime";

    public static CompoundNBT getEntityPokemobTag(final CompoundNBT entityRootTag)
    {
        final CompoundNBT ret = new CompoundNBT();
        if (entityRootTag.contains(TagNames.POKEMOBTAG)) return entityRootTag.getCompound(TagNames.POKEMOBTAG);
        else if (entityRootTag.contains(TagNames.FORGECAPS))
        {
            final CompoundNBT caps = entityRootTag.getCompound(TagNames.FORGECAPS);
            if (caps.contains(TagNames.POKEMOBCAP)) return caps.getCompound(TagNames.POKEMOBCAP);
        }
        return ret;
    }

    public static INBT getPokecubeGenesTag(final CompoundNBT itemRootTag)
    {
        final CompoundNBT ret = new CompoundNBT();
        if (itemRootTag.contains(TagNames.POKEMOB))
        {
            final CompoundNBT entityRootTag = itemRootTag.getCompound(TagNames.POKEMOB);
            if (entityRootTag.contains(TagNames.FORGECAPS))
            {
                final CompoundNBT caps = entityRootTag.getCompound(TagNames.FORGECAPS);
                if (caps.contains(TagNames.GENESCAP)) return caps.getCompound(TagNames.GENESCAP).get("V");
            }
        }
        return ret;
    }

    public static CompoundNBT getPokecubePokemobTag(final CompoundNBT itemRootTag)
    {
        final CompoundNBT ret = new CompoundNBT();
        if (itemRootTag.contains(TagNames.POKEMOB))
        {
            final CompoundNBT entityRootTag = itemRootTag.getCompound(TagNames.POKEMOB);
            if (entityRootTag.contains(TagNames.POKEMOBTAG)) return entityRootTag.getCompound(TagNames.POKEMOBTAG);
            else if (entityRootTag.contains(TagNames.FORGECAPS))
            {
                final CompoundNBT caps = entityRootTag.getCompound(TagNames.FORGECAPS);
                if (caps.contains(TagNames.POKEMOBCAP)) return caps.getCompound(TagNames.POKEMOBCAP);
            }
        }
        return ret;
    }
}