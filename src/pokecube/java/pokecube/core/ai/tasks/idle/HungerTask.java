package pokecube.core.ai.tasks.idle;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.ai.tasks.idle.hunger.BaitCheckEvent;
import pokecube.core.ai.tasks.idle.hunger.EatFromChest;
import pokecube.core.ai.tasks.idle.hunger.EatPlant;
import pokecube.core.ai.tasks.idle.hunger.EatRedstone;
import pokecube.core.ai.tasks.idle.hunger.EatRock;
import pokecube.core.ai.tasks.idle.hunger.EatWater;
import pokecube.core.ai.tasks.idle.hunger.IBlockEatTask;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.inventory.pokemob.PokemobInventory;
import pokecube.core.moves.damage.effects.Sleep;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.utils.TimePeriod;
import thut.api.Tracker;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This IAIRunnable is responsible for finding food for the mobs. It also is what adds berries to their inventories
 * based on which biome they are currently in.
 */
public class HungerTask extends BaseIdleTask
{
    public static final ResourceLocation FOODTAG = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID,
            "pokemob_food");

    private static class GenBerries implements IRunnable
    {
        final IPokemob pokemob;

        public GenBerries(final IPokemob mob)
        {
            this.pokemob = mob;
        }

        @Override
        public boolean run(final Level world)
        {
            final ItemStack stack = BerryGenManager.getRandomBerryForBiome(world,
                    this.pokemob.getEntity().blockPosition());
            if (!stack.isEmpty())
            {
                ItemStackTools.addItemStackToInventory(stack.copy(), this.pokemob.getInventory(), 2,
                        PokemobInventory.MAIN_INVENTORY_SIZE);
                this.pokemob.eat(stack);
            }
            return true;
        }
    }

    public static float calculateHunger(final IPokemob pokemob)
    {
        final float full = PokecubeCore.getConfig().pokemobLifeSpan / 4f + PokecubeCore.getConfig().pokemobLifeSpan;
        final float current = -(pokemob.getHungerTime() - PokecubeCore.getConfig().pokemobLifeSpan);
        // Convert to a scale
        float hungerValue = current / full;
        hungerValue = Math.max(0, hungerValue);
        hungerValue = Math.min(1, hungerValue);
        return hungerValue;
    }

    public static boolean hitThreshold(final float hungerValue, final float threshold)
    {
        return hungerValue <= threshold;
    }

    public static final List<IBlockEatTask> EATTASKS = Lists.newArrayList();

    static
    {
        HungerTask.EATTASKS.add(new EatWater());
        HungerTask.EATTASKS.add(new EatRedstone());
        HungerTask.EATTASKS.add(new EatRock());
        HungerTask.EATTASKS.add(new EatPlant());
        HungerTask.EATTASKS.add(new EatFromChest());
    }

    public static int TICKRATE = 20;
    public static int BAITRATE = 20;
    public static int HEALRATE = 10;

    public static float EATTHRESHOLD = 0.75f;
    public static float HUNTTHRESHOLD = 0.60f;
    public static float MATERESET = 0.50f;
    public static float BERRYGEN = 0.30f;
    public static float DAMAGE = 0.25f;
    public static float DEATH = 0.0f;

    int lastMessageTick1 = -1;
    int lastMessageTick2 = -1;

    boolean sleepy = false;

    float hungerValue = 1;

    List<NearBlock> blocks = null;

    Vector3 v = new Vector3();
    Vector3 v1 = new Vector3();

    public HungerTask()
    {
        super();
    }

    /**
     * Swimming things look for fish hooks to try to go eat, everything has the
     * BaitCheckEvent fired to see if other bait is present.
     */
    protected void checkBait(ServerLevel level, IPokemob pokemob)
    {
        // Fire the bait check event, if it is cancelled, someone processed it, so we return early.
        var event = new BaitCheckEvent(pokemob, this);
        PokecubeAPI.POKEMOB_BUS.post(event);
        if(event.isCanceled()) return;

        if (pokemob.getPokedexEntry().swims() && Math.random() > 0.99)
        {
            var entity = pokemob.getEntity();
            final List<FishingHook> hooks = new ArrayList<>();
            List<Projectile> projectiles = BrainUtils.getNearProjectiles(entity);
            if (projectiles != null) for (var p : projectiles) if (p instanceof FishingHook hook) hooks.add(hook);
            if (!hooks.isEmpty())
            {
                final double moveSpeed = 1.5;
                Collections.shuffle(hooks);
                final FishingHook hook = hooks.getFirst();
                if (this.v.isVisible(level, this.v1.set(hook)))
                {
                    this.setWalkTo(entity, hook.position(), moveSpeed, 0);
                    if (entity.distanceToSqr(hook) < 2)
                    {
                        hook.setHookedEntity(entity);
                        pokemob.eat(hook);
                    }
                }
            }
        }
    }

    /**
     * Checks for a variety of nearby food supplies, returns true if it finds food.
     */
    protected void checkHunt(ServerLevel level, IPokemob pokemob)
    {
        if (!this.hitThreshold(HungerTask.HUNTTHRESHOLD)) return;
        if (pokemob.isPhototroph()) if (this.checkPhotoeat(level, pokemob)) return;
        var entity = pokemob.getEntity();
        if (entity.tickCount % PokecubeCore.getConfig().huntUpdateRate != 0) return;
        for (final IBlockEatTask task : HungerTask.EATTASKS)
            if (task.tryEat(pokemob, this.blocks).test()) return;
        // If none of these, then lets actually try to hunt.
        if (pokemob.getPokedexEntry().hasPrey() && entity.getBrain()
                .hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
        {
            final Iterable<LivingEntity> targets = entity.getBrain()
                    .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findAll(e -> true);
            for (final LivingEntity mob : targets)
            {
                final IPokemob other = PokemobCaps.getPokemobFor(mob);
                if (other != null && pokemob.getPokedexEntry().isFood(other.getPokedexEntry()))
                {
                    final boolean isValid = other.getLevel() - pokemob.getLevel() < 5;
                    if (isValid)
                    {
                        pokemob.setCombatState(CombatStates.HUNTING, true);
                        BrainUtils.setHuntTarget(entity, mob);
                        return;
                    }
                }
            }
        }
    }

    public boolean hitThreshold(final float threshold)
    {
        return HungerTask.hitThreshold(this.hungerValue, threshold);
    }

    /**
     * Checks its own inventory for berries to eat, returns true if it finds some.
     *
     * @return found any berries to eat in inventory.
     */
    protected boolean checkInventory(IPokemob pokemob)
    {
        // Too hungry to check inventory.
        if (this.hitThreshold(HungerTask.DEATH)) return false;

        for (int i = 2; i < pokemob.getInventory().getContainerSize(); i++)
        {
            final ItemStack stack = pokemob.getInventory().getItem(i);
            if (ItemList.is(HungerTask.FOODTAG, stack))
            {
                final int size = stack.getCount();
                pokemob.eat(stack);
                if (size == stack.getCount()) stack.shrink(1);
                if (stack.isEmpty()) pokemob.getInventory().setItem(i, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for light to eat.
     *
     * @return found light
     */
    protected boolean checkPhotoeat(ServerLevel level, IPokemob pokemob)
    {
        if (level.isDay() && this.v.canSeeSky(level))
        {
            pokemob.applyHunger(-PokecubeCore.getConfig().pokemobLifeSpan / 4);
            pokemob.setCombatState(CombatStates.HUNTING, false);
            return true;
        }
        return false;
    }

    /**
     * Check for places and times to sleep, this sets path to sleeping place and returns false if it finds somewhere,
     * but doesn't set sleep.
     */
    protected void checkSleep(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        boolean sleeping = entity.hasEffect(StatusEffects.SLEEP);
        this.sleepy = true;
        for (final TimePeriod p : pokemob.getPokedexEntry().activeTimes())
            if (p != null && p.contains(TimePeriod.getTime(entity.level())))
            {
                this.sleepy = false;
                if (sleeping) entity.removeEffect(StatusEffects.SLEEP);
                break;
            }
        final BlockPos c = this.v.getPos();
        final boolean ownedSleepCheck =
                pokemob.getGeneralState(GeneralStates.TAMED) && !pokemob.getGeneralState(GeneralStates.STAYING);
        if (this.sleepy && !this.hitThreshold(HungerTask.EATTHRESHOLD) && !ownedSleepCheck)
        {
            final double moveSpeed = 1;
            if (!this.isGoodSleepingSpot(c, pokemob)) this.setWalkTo(entity, pokemob.getHome(), moveSpeed, 0);
            else if (entity.getNavigation().isDone())
            {
                StatusEffects.setStatus(entity, entity, StatusEffects.SLEEP, 30, Sleep.NATURAL_SLEEP);
                pokemob.setCombatState(CombatStates.HUNTING, false);
                return;
            }
            else if (!entity.getNavigation().isDone()) if (sleeping) entity.removeEffect(StatusEffects.SLEEP);
        }
        else if (!pokemob.getLogicState(LogicStates.TIRED)) if (sleeping) entity.removeEffect(StatusEffects.SLEEP);
        if (ownedSleepCheck) if (sleeping) entity.removeEffect(StatusEffects.SLEEP);
    }

    // 0 is sunrise, 6000 noon, 12000 dusk, 18000 midnight, 23999
    public boolean isGoodSleepingSpot(final BlockPos c, IPokemob pokemob)
    {
        if (pokemob.getHome() == null || pokemob.getHome().equals(BlockPos.ZERO))
        {
            this.v1.set(c);
            pokemob.setHome(this.v1.intX(), this.v1.intY(), this.v1.intZ(), 16);
        }
        // TODO search for possible better place to sleep
        return !pokemob.hasHomeArea() || !(c.distSqr(pokemob.getHome()) > 9);
    }

    @Override
    public void reset(Mob entityIn)
    {}

    @Override
    public boolean loadThrottle()
    {
        return false;
    }

    @Override
    protected void tick(final ServerLevel level, final Mob entity, final long gameTime)
    {
        var pokemob = PokemobCaps.getPokemobFor(entity);
        this.v.set(entity);
        final int hungerTicks = HungerTask.TICKRATE;
        final Random rand = new Random(pokemob.getRNGValue());
        boolean isHungerTick = entity.tickCount % BAITRATE == rand.nextInt(BAITRATE);

        label:
        {   // Check if we should go after bait.
            if(isHungerTick) this.checkBait(level, pokemob);

            // Do not run this if not really hungry
            if (!this.hitThreshold(HungerTask.EATTHRESHOLD)) break label;

            // Check if we are hunting or should be
            // Reset hunting status if we are not actually hungry
            if (this.hitThreshold(HungerTask.HUNTTHRESHOLD)) this.checkHunt(level, pokemob);

            final boolean hunting = pokemob.getCombatState(CombatStates.HUNTING);
            if (pokemob.getLogicState(LogicStates.SLEEPING) && hunting)
                pokemob.setCombatState(CombatStates.HUNTING, false);
        }
        // Check if we should go to sleep instead.
        this.checkSleep(pokemob);

        final int cur = entity.tickCount / hungerTicks;

        // Hunting could have reset the threshold here, so return now if that is the case.
        if (!this.hitThreshold(HungerTask.EATTHRESHOLD)) return;
        /*
         * Check the various hunger types if it is hunting. And if so, refresh
         * the hunger time counter.
         */
        this.getHunger(pokemob);

        // Everything after here only applies about once per second.
        if (!isHungerTick) return;

        // Check own inventory for berries to eat, and then if the mob is
        // allowed to, collect berries if none to eat.
        if (this.hitThreshold(HungerTask.EATTHRESHOLD) && !this.checkInventory(pokemob))
        {
            // Pokemobs set to stay can collect berries, or wild ones,
            boolean wildOrStay = pokemob.getGeneralState(GeneralStates.STAYING) || pokemob.getOwnerId() == null;
            if (entity.getPersistentData().contains("lastInteract"))
            {
                final long time = entity.getPersistentData().getLong("lastInteract");
                final long diff = Tracker.instance().getTick() - time;
                if (diff < PokecubeCore.getConfig().pokemobLifeSpan) wildOrStay = false;
            }
            // If they are allowed to, find the berries.
            // Only run this if we are getting close to hurt damage, mostly
            // to allow trying other food sources first.
            if (wildOrStay && this.hitThreshold(HungerTask.BERRYGEN)) new GenBerries(pokemob).run(level);

            // Otherwise take damage.
            if (this.hitThreshold(HungerTask.DAMAGE))
            {
                final float ratio = (HungerTask.DAMAGE - this.hungerValue) / HungerTask.DAMAGE;
                final boolean dead =
                        pokemob.getMaxHealth() * ratio > pokemob.getHealth() || this.hitThreshold(HungerTask.DEATH);
                // Ensure it dies if it should.
                final float damage = dead ? pokemob.getMaxHealth() * 20 : pokemob.getMaxHealth() * ratio;
                if (damage >= 1 && ratio >= 0.0625 && entity.getHealth() > 0)
                {
                    entity.hurt(entity.damageSources().starve(), damage);
                    if (!dead)
                    {
                        if (this.lastMessageTick1 < entity.level().getGameTime())
                        {
                            this.lastMessageTick1 = (int) (entity.level().getGameTime() + 100);
                            pokemob.displayMessageToOwner(
                                    Component.translatable("pokemob.hungry.hurt", pokemob.getDisplayName()));
                        }
                    }
                    else if (this.lastMessageTick2 < entity.level().getGameTime())
                    {
                        this.lastMessageTick2 = (int) (entity.level().getGameTime() + 100);
                        pokemob.displayMessageToOwner(
                                Component.translatable("pokemob.hungry.dead", pokemob.getDisplayName()));
                    }
                }
            }
        }

        // cap hunger.
        final int hungerTime = pokemob.getHungerTime();
        final int hunger = Math.max(hungerTime, -PokecubeCore.getConfig().pokemobLifeSpan / 4);
        if (hunger != hungerTime) pokemob.setHungerTime(hunger);

        // Regenerate health if out of battle.
        if (!BrainUtils.hasAttackTarget(entity) && pokemob.getHealth() > 0 && pokemob.getHungerCooldown() < 0
                && pokemob.getHungerTime() < 0 && cur % HEALRATE == rand.nextInt(HEALRATE))
        {
            final float dh = Math.max(1, pokemob.getMaxHealth() * 0.05f);
            final float toHeal = pokemob.getHealth() + dh;
            pokemob.setHealth(Math.min(toHeal, pokemob.getMaxHealth()));
        }
    }

    @Override
    public boolean shouldRun(Mob entity)
    {
        final int hungerTicks = HungerTask.TICKRATE;
        // This can be set in configs to disable.
        if (hungerTicks < 0) return false;
        var pokemob = PokemobCaps.getPokemobFor(entity);
        // Ensure we are not set to hunt if we shouldn't be
        if (!this.hitThreshold(HungerTask.EATTHRESHOLD) && pokemob.getCombatState(CombatStates.HUNTING))
            pokemob.setCombatState(CombatStates.HUNTING, false);

        // Do not run if the mob is in battle.
        if (pokemob.getCombatState(CombatStates.BATTLING)) return false;

        if (pokemob.neverHungry())
        {
            pokemob.setHungerTime(0);
            pokemob.setCombatState(CombatStates.HUNTING, false);
            return false;
        }

        // Apply cooldowns and increment hunger.
        pokemob.setHungerCooldown(pokemob.getHungerCooldown() - hungerTicks);
        pokemob.applyHunger(hungerTicks);

        this.getHunger(pokemob);

        // Do not run this if on cooldown
        if (pokemob.getHungerCooldown() > 0) return false;
        // We are already hunting something!
        if (BrainUtils.hasHuntTarget(entity)) return false;

        final List<NearBlock> blocks = BrainUtils.getNearBlocks(entity);

        if (blocks != null) if (this.blocks == null) this.blocks = Lists.newArrayList(blocks);
        else
        {
            this.blocks.clear();
            this.blocks.addAll(blocks);
        }

        // We are hunting for food, so can run.
        return true;
    }

    private void getHunger(IPokemob pokemob)
    {
        this.hungerValue = HungerTask.calculateHunger(pokemob);
    }
}
