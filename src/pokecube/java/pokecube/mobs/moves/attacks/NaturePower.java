package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.LoadedMove.PreProcessor;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.mobs.moves.world.ActionNaturePower;
import thut.api.level.terrain.TerrainManager;
import thut.api.maths.Vector3;

import java.util.HashMap;
import java.util.Map;

@MoveProvider(name = "nature-power")
public class NaturePower implements PreProcessor
{
    public static Map<String, PokemobTerrainEffects.EffectType> TERRAIN = new HashMap<>();

    public static void init()
    {
        ActionNaturePower.initData();
        TERRAIN.put("psychic", PokemobTerrainEffects.TerrainEffectType.PSYCHIC);
        TERRAIN.put("electric", PokemobTerrainEffects.TerrainEffectType.ELECTRIC);
        TERRAIN.put("grassy", PokemobTerrainEffects.TerrainEffectType.GRASS);
        TERRAIN.put("watery", PokemobTerrainEffects.TerrainEffectType.WATER);
        TERRAIN.put("muddy", PokemobTerrainEffects.TerrainEffectType.MUD);
        TERRAIN.put("misty", PokemobTerrainEffects.TerrainEffectType.MISTY);
    }

    @Override
    public void preProcess(MoveApplication t)
    {
        if (t.getTarget() == null) return;
        // check terrain effects first
        var terrain = TerrainManager.getInstance().getTerrainForEntity(t.getUser().getEntity());
        PokemobTerrainEffects effects = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemob_effects");
        for (String s : ActionNaturePower.BY_TERRAIN.keySet())
        {
            var effect = TERRAIN.get(s);
            if (effect != null && effects.isEffectActive(effect))
            {
                String name = ActionNaturePower.BY_TERRAIN.get(s).move;
                var move = MoveEntry.get(name);
                if (move != null)
                {
                    t.setMove(move);
                    return;
                }
            }
        }
        // Next, if we got here, try by location
        SpawnCheck checker = new SpawnCheck(new Vector3(t.getUser()), t.getUser().getEntity().level());
        for (var rule : ActionNaturePower.NATURE_RULES)
        {
            var matcher = SpawnBiomeMatcher.get(rule);
            if (matcher.matches(checker))
            {
                String name = rule.move;
                var move = MoveEntry.get(name);
                if (move != null)
                {
                    t.setMove(move);
                    return;
                }
            }
        }
    }
}
