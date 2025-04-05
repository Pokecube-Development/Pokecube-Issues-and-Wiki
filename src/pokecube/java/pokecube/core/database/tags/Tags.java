package pokecube.core.database.tags;

import pokecube.core.database.genes.MutationHelper;
import thut.api.data.StringTag;

public class Tags
{
    public static final StringTag<?> ABILITY = new StringTag<>("tags/pokemob_abilities/");
    public static final StringTag<?> MOVE = new StringTag<>("tags/pokemob_moves/");
    public static final StringTag<?> BREEDING = new StringTag<>("tags/pokemob_egg_groups/");
    public static final StringTag<?> CREATURES = new StringTag<>("tags/creature_types/");
    public static final StringTag<?> POKEMOB = new StringTag<>("tags/pokemob/");
    public static final StringTag<Float> MOVEMENT = new StringTag<>("tags/pokemob_movements/", Float.class);

    public static final MutationHelper GENES = new MutationHelper("database/genes/");
}
