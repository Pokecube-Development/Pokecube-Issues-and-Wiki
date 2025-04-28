package pokecube.legends.conditions;

import pokecube.api.PokecubeAPI;
import pokecube.legends.conditions.data.ConditionLoader;
import pokecube.legends.handlers.GeneProtector;
import pokecube.legends.spawns.LegendarySpawn;
import thut.core.common.ThutCore;

public class LegendaryConditions
{
    public static final ConditionLoader CONDITIONS = new ConditionLoader("database/legend_conditions/");

    public void init()
    {
        // Registring Event Lengendary Spawns
        // Register the thng that prevents genetic modification of protected
        // mobs
        ThutCore.FORGE_BUS.register(new GeneProtector());
        PokecubeAPI.POKEMOB_BUS.register(new GeneProtector());
        ThutCore.FORGE_BUS.register(LegendarySpawn.class);
    }
}
