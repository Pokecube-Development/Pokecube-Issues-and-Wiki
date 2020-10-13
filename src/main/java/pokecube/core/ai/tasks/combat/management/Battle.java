package pokecube.core.ai.tasks.combat.management;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.AITools;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Battle {
    public static final ConcurrentHashMap<LivingEntity, Battle> battles = new ConcurrentHashMap<>();

    private final ArrayList<Battle> enemies;
    private final ArrayList<Battle> allies;
    private final LivingEntity mob;
    private final IPokemob pokemob;
    private LivingEntity mainTarget;
    private Battle enemyBattle;

    Battle(LivingEntity mob, LivingEntity targetMob) {
        enemies = new ArrayList<>(10);
        allies = new ArrayList<>(10);
        this.mob = mob;
        this.mainTarget = targetMob;

        pokemob = CapabilityPokemob.getPokemobFor(mob);

        if (pokemob != null) {
            pokemob.setBattle(this);
        }

        if (!battles.containsKey(targetMob)) {
            Battle.createBattle(targetMob, mob);
        }

        enemyBattle = battles.get(targetMob);

        allies.addAll(enemyBattle.getEnemies());
        enemies.addAll(enemyBattle.getAllies());

        enemyBattle.addBattleAsEnemy(this);

        for (Battle ally : allies) {
            ally.addBattleAsAlly(this);
        }

        for (Battle enemy : enemies) {
            enemy.addBattleAsEnemy(this);
        }
    }

    public void start() {
        if (mob instanceof MobEntity) {
            BrainUtils.initiateCombat((MobEntity) mob, mainTarget);
            if(pokemob != null) {
                pokemob.getAbility().start(pokemob);
            }
        }
    }

    public void end() {
        allies.clear();

        if(!enemies.isEmpty()) {

            enemies.remove(enemyBattle);

            enemies.sort((o1, o2) -> (int) (o1.getMob().getDistanceSq(mob) -
                    o2.getMob().getDistanceSq(mob)));

            ArrayList<Battle> copy = new ArrayList<>(enemies);

            for(Battle battle : copy){
                if(battles.contains(battle)){
                    enemyBattle = battle;
                    mainTarget = enemyBattle.mob;
                    allies.addAll(enemyBattle.enemies);
                    break;
                }else {
                    enemies.remove(battle);
                }
            }

            if(!enemies.isEmpty()){
                this.start();
            }

        }else{
            BrainUtils.deagro(mob);
            BrainUtils.deagro(mainTarget);

            if (pokemob != null) {
                pokemob.getAbility().end(pokemob);
            }

            battles.remove(mob);
        }
    }

    public static boolean createBattle(LivingEntity mob, LivingEntity targetMob) {
        if (targetMob != null && AITools.validTargets.test(targetMob)) {
            Battle battle;
            if (!battles.containsKey(mob)) {
                battle = new Battle(mob, targetMob);
                battles.put(mob, battle);
            } else {
                battle = battles.get(mob);
            }

            battle.start();
            return true;
        } else {
            BrainUtils.deagro(mob);
            return false;
        }
    }

    public ArrayList<Battle> getEnemies() {
        return enemies;
    }

    public ArrayList<Battle> getAllies() {
        return allies;
    }

    public void addBattleAsEnemy(Battle battle) {
        enemies.add(battle);
    }

    public void addBattleAsAlly(Battle battle) {
        allies.add(battle);
    }

    public LivingEntity getMob() {
        return mob;
    }
}
