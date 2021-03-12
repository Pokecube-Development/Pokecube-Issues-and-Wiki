package pokecube.core.ai.tasks.idle;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.ai.tasks.idle.hunger.EatFromChest;
import pokecube.core.ai.tasks.idle.hunger.EatPlant;
import pokecube.core.ai.tasks.idle.hunger.EatRedstone;
import pokecube.core.ai.tasks.idle.hunger.EatRock;
import pokecube.core.ai.tasks.idle.hunger.EatWater;
import pokecube.core.ai.tasks.idle.hunger.IBlockEatTask;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.TimePeriod;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

/**
 * This IAIRunnable is responsible for finding food for the mobs. It also is
 * what adds berries to their inventories based on which biome they are
 * currently in.
 */
public class HungerTask extends BaseIdleTask
{
    public static final ResourceLocation FOODTAG = new ResourceLocation(PokecubeCore.MODID, "pokemob_food");

    private static class GenBerries implements IRunnable
    {
        final IPokemob pokemob;

        public GenBerries(final IPokemob mob)
        {
            this.pokemob = mob;
        }

        @Override
        public boolean run(final World world)
        {
            final ItemStack stack = BerryGenManager.getRandomBerryForBiome(world, this.pokemob.getEntity()
                    .blockPosition());
            if (!stack.isEmpty())
            {
                ItemStackTools.addItemStackToInventory(stack.copy(), this.pokemob.getInventory(), 2);
                this.pokemob.eat(stack);
            }
            return true;
        }
    }

    public static float calculateHunger(final IPokemob pokemob)
    {
        final float full = PokecubeCore.getConfig().pokemobLifeSpan / 4 + PokecubeCore.getConfig().pokemobLifeSpan;
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

    public static float EATTHRESHOLD  = 0.75f;
    public static float HUNTTHRESHOLD = 0.6f;
    public static float BERRYGEN      = 0.55f;
    public static float MATERESET     = 0.5f;
    public static float DAMAGE        = 0.3f;
    public static float DEATH         = 0.0f;

    int lastMessageTick1 = -1;
    int lastMessageTick2 = -1;

    boolean sleepy = false;

    float hungerValue = 1;

    List<NearBlock> blocks = null;

    Vector3 v  = Vector3.getNewVector();
    Vector3 v1 = Vector3.getNewVector();
    Random  rand;

    public HungerTask(final IPokemob pokemob)
    {
        super(pokemob);
    }

    /**
     * Swimming things look for fish hooks to try to go eat.
     *
     * @return found a hook.
     */
    protected boolean checkBait()
    {
        if (this.pokemob.getPokedexEntry().swims())
        {
            final AxisAlignedBB bb = this.v.set(this.entity).addTo(0, this.entity.getEyeHeight(), 0).getAABB().inflate(
                    PokecubeCore.getConfig().fishHookBaitRange);
            final List<FishingBobberEntity> hooks = this.entity.getCommandSenderWorld().getEntitiesOfClass(
                    FishingBobberEntity.class, bb);
            if (!hooks.isEmpty())
            {
                final double moveSpeed = 1.5;
                Collections.shuffle(hooks);
                final FishingBobberEntity hook = hooks.get(0);
                if (this.v.isVisible(this.world, this.v1.set(hook)))
                {
                    this.setWalkTo(hook.position(), moveSpeed, 0);
                    if (this.entity.distanceToSqr(hook) < 2)
                    {
                        hook.hookedIn = this.entity;
                        this.pokemob.eat(hook);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks for a variety of nearby food supplies, returns true if it finds
     * food.
     *
     * @return found food
     */
    protected boolean checkHunt()
    {
        if (!this.hitThreshold(HungerTask.HUNTTHRESHOLD)) return false;
        if (this.pokemob.isPhototroph()) if (this.checkPhotoeat()) return true;
        if (this.entity.tickCount % PokecubeCore.getConfig().huntUpdateRate != 0) return false;
        for (final IBlockEatTask task : HungerTask.EATTASKS)
            if (task.tryEat(this.pokemob, this.blocks).test()) return true;
        // If none of these, then lets actually try to hunt.
        if (this.pokemob.getPokedexEntry().hasPrey() && this.entity.getBrain().hasMemoryValue(MemoryModuleType.VISIBLE_LIVING_ENTITIES))
        {
            final List<LivingEntity> targets = this.entity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get();
            for (final LivingEntity mob : targets)
            {
                final IPokemob other = CapabilityPokemob.getPokemobFor(mob);
                if (other != null && this.pokemob.getPokedexEntry().isFood(other.getPokedexEntry()))
                {
                    final boolean isValid = other.getLevel() - this.pokemob.getLevel() < 5;
                    if (isValid)
                    {
                        this.pokemob.setCombatState(CombatStates.HUNTING, true);
                        BrainUtils.setHuntTarget(this.entity, mob);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hitThreshold(final float threshold)
    {
        return HungerTask.hitThreshold(this.hungerValue, threshold);
    }

    /**
     * Checks its own inventory for berries to eat, returns true if it finds
     * some.
     *
     * @return found any berries to eat in inventory.
     */
    protected boolean checkInventory()
    {
        // Too hungry to check inventory.
        if (this.hitThreshold(HungerTask.DEATH)) return false;

        for (int i = 2; i < this.pokemob.getInventory().getContainerSize(); i++)
        {
            final ItemStack stack = this.pokemob.getInventory().getItem(i);
            if (ItemList.is(HungerTask.FOODTAG, stack))
            {
                final int size = stack.getCount();
                this.pokemob.eat(stack);
                if (size == stack.getCount()) stack.shrink(1);
                if (stack.isEmpty()) this.pokemob.getInventory().setItem(i, ItemStack.EMPTY);
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
    protected boolean checkPhotoeat()
    {
        if (this.entity.getCommandSenderWorld().isDay() && this.v.canSeeSky(this.world))
        {
            this.pokemob.applyHunger(-PokecubeCore.getConfig().pokemobLifeSpan / 4);
            this.pokemob.setCombatState(CombatStates.HUNTING, false);
            return true;
        }
        return false;
    }

    /**
     * Check for places and times to sleep, this sets path to sleeping place
     * and returns false if it finds somewhere, but doesn't set sleep.
     *
     * @return went to sleep.
     */
    protected boolean checkSleep()
    {
        this.sleepy = true;
        for (final TimePeriod p : this.pokemob.getPokedexEntry().activeTimes())
            // TODO AR-like support.
            if (p != null && p.contains(this.entity.getCommandSenderWorld().getDayTime(), 24000))
            {
                this.sleepy = false;
                this.pokemob.setLogicState(LogicStates.SLEEPING, false);
                break;
            }
        final BlockPos c = this.v.getPos();
        final boolean ownedSleepCheck = this.pokemob.getGeneralState(GeneralStates.TAMED) && !this.pokemob
                .getGeneralState(GeneralStates.STAYING);
        if (this.sleepy && this.hitThreshold(HungerTask.EATTHRESHOLD) && !ownedSleepCheck)
        {
            final double moveSpeed = 1;
            if (!this.isGoodSleepingSpot(c)) this.setWalkTo(this.pokemob.getHome(), moveSpeed, 0);
            else if (this.entity.getNavigation().isDone())
            {
                this.pokemob.setLogicState(LogicStates.SLEEPING, true);
                this.pokemob.setCombatState(CombatStates.HUNTING, false);
                return true;
            }
            else if (!this.entity.getNavigation().isDone()) this.pokemob.setLogicState(LogicStates.SLEEPING, false);
        }
        else if (!this.pokemob.getLogicState(LogicStates.TIRED)) this.pokemob.setLogicState(LogicStates.SLEEPING,
                false);
        if (ownedSleepCheck) this.pokemob.setLogicState(LogicStates.SLEEPING, false);
        return false;
    }

    // 0 is sunrise, 6000 noon, 12000 dusk, 18000 midnight, 23999
    public boolean isGoodSleepingSpot(final BlockPos c)
    {
        if (this.pokemob.getHome() == null || this.pokemob.getHome().equals(BlockPos.ZERO))
        {
            this.v1.set(this.entity);
            this.pokemob.setHome(this.v1.intX(), this.v1.intY(), this.v1.intZ(), 16);
        }
        if (this.pokemob.hasHomeArea() && this.entity.blockPosition().distSqr(this.pokemob.getHome()) > 9)
            return false;
        // TODO search for possible better place to sleep
        return true;
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
        this.v.set(this.entity);

        // Check if we should go after bait. The Math.random() > 0.99 is to
        // allow non-hungry fish to also try to get bait.
        if (Math.random() > 0.99) this.checkBait();

        // Do not run this if not really hungry
        if (!this.hitThreshold(HungerTask.EATTHRESHOLD)) return;

        // Check if we are hunting or should be
        // Reset hunting status if we are not actually hungry
        if (this.hitThreshold(HungerTask.HUNTTHRESHOLD)) this.checkHunt();

        final boolean hunting = this.pokemob.getCombatState(CombatStates.HUNTING);
        if (this.pokemob.getLogicState(LogicStates.SLEEPING) && !hunting) if (hunting) this.pokemob.setCombatState(
                CombatStates.HUNTING, false);
    }

    @Override
    public boolean shouldRun()
    {
        final int hungerTicks = HungerTask.TICKRATE;
        // This can be set in configs to disable.
        if (hungerTicks < 0) return false;

        // Ensure we are not set to hunt if we shouldn't be
        if (!this.hitThreshold(HungerTask.EATTHRESHOLD) && this.pokemob.getCombatState(CombatStates.HUNTING))
            this.pokemob.setCombatState(CombatStates.HUNTING, false);

        // Do not run if the mob is in battle.
        if (this.pokemob.getCombatState(CombatStates.ANGRY)) return false;

        if (this.pokemob.neverHungry())
        {
            this.pokemob.setHungerTime(0);
            this.pokemob.setCombatState(CombatStates.HUNTING, false);
            return false;
        }

        // Apply cooldowns and increment hunger.
        this.pokemob.setHungerCooldown(this.pokemob.getHungerCooldown() - hungerTicks);
        this.pokemob.applyHunger(hungerTicks);

        this.calculateHunger();

        // Do not run this if on cooldown
        if (this.pokemob.getHungerCooldown() > 0) return false;
        // We are already hunting something!
        if (BrainUtils.hasHuntTarget(this.entity)) return false;

        final List<NearBlock> blocks = BrainUtils.getNearBlocks(this.entity);

        if (blocks != null) if (this.blocks == null) this.blocks = Lists.newArrayList(blocks);
        else
        {
            this.blocks.clear();
            this.blocks.addAll(blocks);
        }

        // We are hunting for food, so can run.
        return true;
    }

    private void calculateHunger()
    {
        this.hungerValue = HungerTask.calculateHunger(this.pokemob);
    }

    @Override
    public void tick()
    {

        this.v.set(this.entity);
        final int hungerTicks = HungerTask.TICKRATE;

        // Check if we should go to sleep instead.
        this.checkSleep();

        final Random rand = new Random(this.pokemob.getRNGValue());
        final int cur = this.entity.tickCount / hungerTicks;
        final int tick = rand.nextInt(10);

        if (!this.hitThreshold(HungerTask.EATTHRESHOLD)) return;
        /*
         * Check the various hunger types if it is hunting.
         * And if so, refresh the hunger time counter.
         */
        this.calculateHunger();

        // Everything after here only applies about once per second.
        if (this.entity.tickCount % hungerTicks != 0) return;

        // Check own inventory for berries to eat, and then if the mob is
        // allowed to, collect berries if none to eat.
        if (this.hitThreshold(HungerTask.EATTHRESHOLD) && !this.checkInventory())
        {
            // Pokemobs set to stay can collect berries, or wild ones,
            boolean tameCheck = this.pokemob.getGeneralState(GeneralStates.STAYING) || this.pokemob
                    .getOwnerId() == null;
            if (this.entity.getPersistentData().contains("lastInteract"))
            {
                final long time = this.entity.getPersistentData().getLong("lastInteract");
                final long diff = this.entity.getCommandSenderWorld().getGameTime() - time;
                if (diff < PokecubeCore.getConfig().pokemobLifeSpan) tameCheck = false;
            }
            // If they are allowed to, find the berries.
            // Only run this if we are getting close to hurt damage, mostly
            // to allow trying other food sources first.
            if (tameCheck && this.hitThreshold(HungerTask.BERRYGEN)) new GenBerries(this.pokemob).run(this.world);

            // Otherwise take damage.
            if (this.hitThreshold(HungerTask.DAMAGE))
            {
                final float ratio = (HungerTask.DAMAGE - this.hungerValue) / HungerTask.DAMAGE;
                final boolean dead = this.pokemob.getMaxHealth() * ratio > this.pokemob.getHealth() || this
                        .hitThreshold(HungerTask.DEATH);
                // Ensure it dies if it should.
                final float damage = dead ? this.pokemob.getMaxHealth() * 20 : this.pokemob.getMaxHealth() * ratio;
                if (damage >= 1 && ratio >= 0.0625 && this.entity.getHealth() > 0)
                {
                    this.entity.hurt(DamageSource.STARVE, damage);
                    if (!dead)
                    {
                        if (this.lastMessageTick1 < this.entity.getCommandSenderWorld().getGameTime())
                        {
                            this.lastMessageTick1 = (int) (this.entity.getCommandSenderWorld().getGameTime() + 100);
                            this.pokemob.displayMessageToOwner(new TranslationTextComponent("pokemob.hungry.hurt",
                                    this.pokemob.getDisplayName()));
                        }
                    }
                    else if (this.lastMessageTick2 < this.entity.getCommandSenderWorld().getGameTime())
                    {
                        this.lastMessageTick2 = (int) (this.entity.getCommandSenderWorld().getGameTime() + 100);
                        this.pokemob.displayMessageToOwner(new TranslationTextComponent("pokemob.hungry.dead",
                                this.pokemob.getDisplayName()));
                    }
                }
            }
        }

        // cap hunger.
        final int hungerTime = this.pokemob.getHungerTime();
        final int hunger = Math.max(hungerTime, -PokecubeCore.getConfig().pokemobLifeSpan / 4);
        if (hunger != hungerTime) this.pokemob.setHungerTime(hunger);

        // Regenerate health if out of battle.
        if (!BrainUtils.hasAttackTarget(this.entity) && this.pokemob.getHealth() > 0 && !this.entity
                .getCommandSenderWorld().isClientSide && this.pokemob.getHungerCooldown() < 0 && this.pokemob.getHungerTime() < 0
                && cur % 10 == tick)
        {
            final float dh = Math.max(1, this.pokemob.getMaxHealth() * 0.05f);
            final float toHeal = this.pokemob.getHealth() + dh;
            this.pokemob.setHealth(Math.min(toHeal, this.pokemob.getMaxHealth()));
        }
    }
}
