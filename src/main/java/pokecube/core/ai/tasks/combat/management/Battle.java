package pokecube.core.ai.tasks.combat.management;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.AITools;

public class Battle
{
    public static final ConcurrentHashMap<LivingEntity, Battle> battles = new ConcurrentHashMap<>();

    private final ArrayList<Battle> enemies;
    private final ArrayList<Battle> allies;
    private final LivingEntity      mob;
    private final IPokemob          pokemob;
    private LivingEntity            mainTarget;
    private Battle                  enemyBattle;

    Battle(final LivingEntity mob, final LivingEntity targetMob)
    {
        this.enemies = new ArrayList<>(10);
        this.allies = new ArrayList<>(10);
        this.mob = mob;
        this.mainTarget = targetMob;

        this.pokemob = CapabilityPokemob.getPokemobFor(mob);

        if (this.pokemob != null) this.pokemob.setBattle(this);

        if (!Battle.battles.containsKey(mob)) Battle.battles.put(mob, this);
        if (!Battle.battles.containsKey(targetMob)) Battle.createBattle(targetMob, mob);

        this.enemyBattle = Battle.battles.get(targetMob);

        this.allies.addAll(this.enemyBattle.getEnemies());
        this.enemies.addAll(this.enemyBattle.getAllies());

        this.enemyBattle.addBattleAsEnemy(this);

        for (final Battle ally : this.allies)
            ally.addBattleAsAlly(this);

        for (final Battle enemy : this.enemies)
            enemy.addBattleAsEnemy(this);
    }

    public void start()
    {
        if (this.mob instanceof MobEntity)
        {
            BrainUtils.initiateCombat((MobEntity) this.mob, this.mainTarget);
            if (this.pokemob != null && this.pokemob.getAbility() != null) this.pokemob.getAbility().start(this.pokemob);
        }
    }

    public void end()
    {
        this.allies.clear();

        if (!this.enemies.isEmpty())
        {

            this.enemies.remove(this.enemyBattle);

            this.enemies.sort((o1, o2) -> (int) (o1.getMob().getDistanceSq(this.mob) - o2.getMob().getDistanceSq(
                    this.mob)));

            final ArrayList<Battle> copy = new ArrayList<>(this.enemies);

            for (final Battle battle : copy)
                if (Battle.battles.contains(battle))
                {
                    this.enemyBattle = battle;
                    this.mainTarget = this.enemyBattle.mob;
                    this.allies.addAll(this.enemyBattle.enemies);
                    break;
                }
                else this.enemies.remove(battle);

            if (!this.enemies.isEmpty()) this.start();

        }
        else
        {
            BrainUtils.deagro(this.mob);
            BrainUtils.deagro(this.mainTarget);

            if (this.pokemob != null && this.pokemob.getAbility() != null) this.pokemob.getAbility().end(this.pokemob);

            Battle.battles.remove(this.mob);
        }
    }

    public static boolean createBattle(final LivingEntity mob, final LivingEntity targetMob)
    {
        if (targetMob != null && AITools.validTargets.test(targetMob))
        {
            Battle battle;
            if (!Battle.battles.containsKey(mob)) battle = new Battle(mob, targetMob);
            else battle = Battle.battles.get(mob);

            battle.start();
            return true;
        }
        else
        {
            BrainUtils.deagro(mob);
            return false;
        }
    }

    public ArrayList<Battle> getEnemies()
    {
        return this.enemies;
    }

    public ArrayList<Battle> getAllies()
    {
        return this.allies;
    }

    public void addBattleAsEnemy(final Battle battle)
    {
        if (battle == this) return;
        this.enemies.add(battle);
    }

    public void addBattleAsAlly(final Battle battle)
    {
        if (battle == this) return;
        this.allies.add(battle);
    }

    public LivingEntity getMob()
    {
        return this.mob;
    }
}
