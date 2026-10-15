package pokecube.api.ai.combat;

import com.google.common.collect.Maps;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.Tools;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.moves.MovesUtils;
import thut.api.ThutAPI;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class CombatModes
{
    public static Map<String, IMoveSelector> MODES = Maps.newHashMap();

    public static IMoveSelector GUARD_MODE = new IMoveSelector() {
        @Override
        public void checkMoves(IPokemob pokemob)
        {
            var target = pokemob.getMoveStats().getTargetEnemy();
            if (target == null) return;
            // See if we need to recover health
            float relHP = pokemob.getHealth() / pokemob.getMaxHealth();
            var entity = pokemob.getEntity();
            int index = pokemob.getMoveIndex();
            int maxDamageI = index, maxHealI = -1;
            float maxdamage = 0;
            double dist = entity.distanceToSqr(target.getX(), target.getY(), target.getZ());
            for (int i = 0; i < 4; i++)
            {
                String s = pokemob.getMove(i);
                // Cannot select a disabled move.
                if (pokemob.getDisableTimer(i) > 0) continue;
                if (s != null)
                {
                    final MoveEntry m = MovesUtils.getMove(s);
                    if (m == null) continue;
                    float temp = Tools.getPower(s, pokemob, target);
                    // Scale up expected damage from ranged moves if we are far away
                    if (dist > 3 && m.isRanged(pokemob)) temp *= 1.5f;
                    if (temp > maxdamage)
                    {
                        maxDamageI = i;
                        maxdamage = temp;
                    }
                    if (m.root_entry._healing > 0 || m.root_entry._drain > 0) maxHealI = i;
                }
            }
            if (relHP < 0.5f && maxHealI >= 0) index = maxHealI;
            else index = maxDamageI;
            selectMove(pokemob, index, "pokecube:guard");
        }
    };

    public static IMoveSelector WILD_MODE = new IMoveSelector() {
        @Override
        public void checkMoves(IPokemob pokemob)
        {
            final Random rand = ThutAPI.newRandom();
            int timer = pokemob.getEntity().getBrain().getMemory(MemoryModules.TIMER_SWAPMOVE.get()).orElse(0);
            if (timer++ > rand.nextInt(30))
            {
                final int nb = rand.nextInt(5);
                int index = 0;
                for (int i = 0; i < 4; i++)
                {
                    index = (nb + i) % 4;
                    if (pokemob.getDisableTimer(index) > 0) continue;
                    if (pokemob.getMove(index) == null) continue;
                    break;
                }
                timer = 0;
                selectMove(pokemob, index, "pokecube:wild");
            }
            pokemob.getEntity().getBrain().setMemory(MemoryModules.TIMER_SWAPMOVE.get(), timer);
        }
    };

    public static Function<IPokemob, String> MODE_SUPPLIER = (pokemob) -> {
        if (pokemob.getCombatState(CombatStates.GUARDING) || pokemob.getCombatState(CombatStates.HUNTING))
            return "pokecube:guard";
        if (pokemob.getOwnerId() == null || pokemob.getGeneralState(GeneralStates.STAYING)) return "pokecube:wild";
        return "";
    };

    public static IMoveSelector getMode(IPokemob pokemob)
    {
        var key = MODE_SUPPLIER.apply(pokemob);
        return MODES.get(key);
    }

    static
    {
        MODES.put("pokecube:wild", WILD_MODE);
        MODES.put("pokecube:guard", GUARD_MODE);
    }
}
