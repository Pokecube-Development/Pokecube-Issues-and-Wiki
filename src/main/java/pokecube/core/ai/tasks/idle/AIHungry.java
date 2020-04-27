package pokecube.core.ai.tasks.idle;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IBerryFruitBlock;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.ChunkCoordinate;
import pokecube.core.utils.TimePeriod;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

/** This IAIRunnable is responsible for finding food for the mobs. It also is
 * what adds berries to their inventories based on which biome they are
 * currently in. */
public class AIHungry extends AIBase
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
            final ItemStack stack = BerryGenManager.getRandomBerryForBiome(world,
                    this.pokemob.getEntity().getPosition());
            if (!stack.isEmpty())
            {
                ItemStackTools.addItemStackToInventory(stack.copy(), this.pokemob.getInventory(), 2);
                this.pokemob.eat(new ItemEntity(world, 0, 0, 0, stack));
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

    public static int TICKRATE = 20;

    public static float EATTHRESHOLD  = 0.75f;
    public static float HUNTTHRESHOLD = 0.6f;
    public static float BERRYGEN      = 0.55f;
    public static float MATERESET     = 0.5f;
    public static float DAMAGE        = 0.3f;
    public static float DEATH         = 0.0f;

    // final World world;
    final ItemEntity berry;
    final double     distance;
    int              lastMessageTick1 = -1;
    int              lastMessageTick2 = -1;
    Vector3          foodLoc          = null;
    boolean          block            = false;
    boolean          sleepy           = false;
    float            hungerValue      = 1;
    double           moveSpeed;
    Vector3          v                = Vector3.getNewVector();
    Vector3          v1               = Vector3.getNewVector();
    Random           rand;

    public AIHungry(final IPokemob pokemob, final ItemEntity berry_, final double distance)
    {
        super(pokemob);
        this.berry = berry_;
        this.distance = distance;
        this.moveSpeed = this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue() * 1.75;
    }

    /**
     * Swimming things look for fish hooks to try to go eat.
     *
     * @return found a hook. */
    protected boolean checkBait()
    {
        if (this.pokemob.getPokedexEntry().swims())
        {
            final AxisAlignedBB bb = this.v.set(this.entity).addTo(0, this.entity.getEyeHeight(), 0).getAABB()
                    .grow(PokecubeCore.getConfig().fishHookBaitRange);
            final List<FishingBobberEntity> hooks = this.entity.getEntityWorld()
                    .getEntitiesWithinAABB(FishingBobberEntity.class, bb);
            this.pokemob.setCombatState(CombatStates.HUNTING, true);
            if (!hooks.isEmpty())
            {
                Collections.shuffle(hooks);
                final FishingBobberEntity hook = hooks.get(0);
                if (this.v.isVisible(this.world, this.v1.set(hook)))
                {
                    final Path path = this.entity.getNavigator().getPathToEntity(hook, 0);
                    this.addEntityPath(this.entity, path, this.moveSpeed);
                    if (this.entity.getDistanceSq(hook) < 2)
                    {
                        hook.caughtEntity = this.entity;
                        this.pokemob.eat(hook);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /** Checks for redstone blocks nearby.
     *
     * @return found redstone block. */
    protected boolean checkElectricEat()
    {
        final int num = this.v.blockCount(this.world, Blocks.REDSTONE_BLOCK, 8);
        if (num >= 1)
        {
            this.pokemob.setHungerTime(this.pokemob.getHungerTime() - PokecubeCore.getConfig().pokemobLifeSpan / 4);
            this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
            return true;
        }
        return false;
    }

    /** Checks for a variety of nearby food supplies, returns true if it finds
     * food.
     *
     * @return found food */
    protected boolean checkHunt()
    {
        if (!this.hitThreshold(AIHungry.HUNTTHRESHOLD)) return false;
        if (this.pokemob.isPhototroph()) if (this.checkPhotoeat()) return true;
        if (this.pokemob.isLithotroph()) if (this.checkRockEat()) return true;
        if (this.pokemob.isElectrotroph()) if (this.checkElectricEat()) return true;
        return false;
    }

    private boolean hitThreshold(final float threshold)
    {
        return AIHungry.hitThreshold(this.hungerValue, threshold);
    }

    /**
     * Checks its own inventory for berries to eat, returns true if it finds
     * some.
     *
     * @return found any berries to eat in inventory. */
    protected boolean checkInventory()
    {
        // Too hungry to check inventory.
        if (this.hitThreshold(AIHungry.DEATH)) return false;

        for (int i = 2; i < 7; i++)
        {
            final ItemStack stack = this.pokemob.getInventory().getStackInSlot(i);
            if (PokecubeItems.is(AIHungry.FOODTAG, stack))
            {
                this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
                this.pokemob.eat(this.berry);
                stack.shrink(1);
                if (stack.isEmpty()) this.pokemob.getInventory().setInventorySlotContents(i, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    /** Checks for light to eat.
     *
     * @return found light */
    protected boolean checkPhotoeat()
    {
        if (this.entity.getEntityWorld().dimension.isDaytime() && this.v.canSeeSky(this.world))
        {
            this.pokemob.setHungerTime(this.pokemob.getHungerTime() - PokecubeCore.getConfig().pokemobLifeSpan / 4);
            this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
            return true;
        }
        return false;
    }

    /** Checks for rocks nearby to eat
     *
     * @return found and ate rocks. */
    protected boolean checkRockEat()
    {
        final BlockState state = this.v.offset(Direction.DOWN).getBlockState(this.world);
        final Block b = state.getBlock();
        // Look for nearby rocks.
        if (!PokecubeTerrainChecker.isRock(state))
        {
            final Predicate<BlockState> checker = (b2) -> PokecubeTerrainChecker.isRock(b2);
            final Vector3 temp = this.v.findClosestVisibleObject(this.world, true, (int) this.distance, checker);
            if (temp != null)
            {
                this.block = true;
                this.foodLoc = temp.copy();
            }
            if (this.foodLoc != null) return true;
        }
        else
        {
            // Check configs, and if so, actually eat the rocks
            if (PokecubeCore.getConfig().pokemobsEatRocks)
            {
                this.v.set(this.entity).offsetBy(Direction.DOWN);
                if (MoveEventsHandler.canEffectBlock(this.pokemob, this.v))
                    if (b == Blocks.COBBLESTONE) this.v.setBlock(this.world, Blocks.GRAVEL.getDefaultState());
                    else if (b == Blocks.GRAVEL && PokecubeCore.getConfig().pokemobsEatGravel) this.v.setBlock(this.world, Blocks.AIR.getDefaultState());
                    else if (state.getMaterial() == Material.ROCK) this.v.setBlock(this.world, Blocks.COBBLESTONE.getDefaultState());
            }
            // Apply the eating of the item.
            this.berry.setItem(new ItemStack(b));
            this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
            this.pokemob.eat(this.berry);
            this.foodLoc = null;
            return true;
        }
        return false;
    }

    /** Check for places and times to sleep, this sets path to sleeping place
     * and returns false if it finds somewhere, but doesn't set sleep.
     *
     * @return went to sleep. */
    protected boolean checkSleep()
    {
        this.sleepy = true;
        for (final TimePeriod p : this.pokemob.getPokedexEntry().activeTimes())
        {// TODO find some way to determine actual length of day for things like
         // AR support.
            if (p != null && p.contains(this.entity.getEntityWorld().getDayTime(), 24000));
            {
                this.sleepy = false;
                this.pokemob.setLogicState(LogicStates.SLEEPING, false);
                break;
            }
        }
        final ChunkCoordinate c = new ChunkCoordinate(this.v, this.entity.dimension.getId());
        final boolean ownedSleepCheck = this.pokemob.getGeneralState(GeneralStates.TAMED) && !this.pokemob
                .getGeneralState(GeneralStates.STAYING);
        if (this.sleepy && this.hitThreshold(AIHungry.EATTHRESHOLD) && !ownedSleepCheck)
        {
            if (!this.isGoodSleepingSpot(c))
            {
                Path path = this.entity.getNavigator().getPathToPos(this.pokemob.getHome(), 0);
                if (path != null && path.getCurrentPathLength() > 32) path = null;
                this.addEntityPath(this.entity, path, this.moveSpeed);
            }
            else if (this.entity.getNavigator().noPath())
            {
                this.pokemob.setLogicState(LogicStates.SLEEPING, true);
                this.pokemob.setCombatState(CombatStates.HUNTING, false);
                return true;
            }
            else if (!this.entity.getNavigator().noPath()) this.pokemob.setLogicState(LogicStates.SLEEPING, false);
        }
        else if (!this.pokemob.getLogicState(LogicStates.TIRED))
            this.pokemob.setLogicState(LogicStates.SLEEPING, false);
        if (ownedSleepCheck) this.pokemob.setLogicState(LogicStates.SLEEPING, false);
        return false;
    }

    /** Eats a berry
     *
     * @param b
     *            the berry
     * @param distance
     *            to the berry */
    protected void eatBerry(final BlockState b, final double distance)
    {
        final ItemStack fruit = ((IBerryFruitBlock) b.getBlock()).getBerryStack(this.world, this.foodLoc.getPos());

        if (fruit.isEmpty())
        {
            this.foodLoc = null;
            this.pokemob.noEat(null);
            return;
        }

        if (distance < 3)
        {
            this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
            this.berry.setItem(fruit);
            this.pokemob.eat(this.berry);
            this.toRun.add(new InventoryChange(this.entity, 2, fruit, true));
            this.foodLoc.setBlock(this.world, Blocks.AIR.getDefaultState());
            this.foodLoc = null;
        }
        else if (this.entity.ticksExisted % 20 == this.rand.nextInt(20))
        {
            boolean shouldChangePath = true;
            if (!this.entity.getNavigator().noPath())
            {
                final Vector3 p = this.v.set(this.entity.getNavigator().getPath().getFinalPathPoint());
                final Vector3 v = this.v1.set(this.foodLoc);
                if (p.distToSq(v) <= 16) shouldChangePath = false;
            }
            Path path = null;
            if (shouldChangePath && (path = this.entity.getNavigator().getPathToPos(this.foodLoc.x, this.foodLoc.y,
                    this.foodLoc.z, 0)) == null)
            {
                this.addEntityPath(this.entity, path, this.moveSpeed);
                this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
                this.berry.setItem(fruit);
                this.pokemob.noEat(this.berry);
                this.foodLoc.clear();
            }
            else this.addEntityPath(this.entity, path, this.moveSpeed);
        }
    }

    /** Eats a plant.
     *
     * @param b
     *            the plant
     * @param location
     *            where the plant is
     * @param dist
     *            distance to the plant */
    protected void eatPlant(final BlockState b, final Vector3 location, final double dist)
    {
        double diff = 1.5;
        diff = Math.max(diff, this.entity.getWidth());
        if (dist < diff)
        {
            this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
            this.berry.setItem(new ItemStack(b.getBlock()));
            this.pokemob.eat(this.berry);
            if (PokecubeCore.getConfig().pokemobsEatPlants)
            {
                if (b.getMaterial() != Material.PLANTS)
                {
                    final List<ItemStack> list = Block.getDrops(b, this.world, this.foodLoc.getPos(), null);
                    for (final ItemStack stack : list)
                        this.toRun.add(new InventoryChange(this.entity, 2, stack, true));
                }
                if (b.getMaterial() == Material.ORGANIC) this.world.setBlockState(location.getPos(), Blocks.DIRT
                        .getDefaultState());
                else this.world.destroyBlock(location.getPos(), false);
            }
            this.foodLoc = null;
            this.addEntityPath(this.entity, null, this.moveSpeed);
        }
        else
        {
            boolean shouldChangePath = true;
            this.block = false;
            this.v.set(this.entity).add(0, this.entity.getHeight(), 0);
            if (!this.entity.getNavigator().noPath())
            {
                Vector3 pathEnd, destination;
                pathEnd = this.v.set(this.entity.getNavigator().getPath().getFinalPathPoint());
                destination = this.v1.set(this.foodLoc);
                if (pathEnd.distToSq(destination) < 1) shouldChangePath = false;
            }
            Path path = null;
            if (shouldChangePath)
            {
                path = this.entity.getNavigator().getPathToPos(this.foodLoc.x, this.foodLoc.y, this.foodLoc.z, 0);
                if (path == null)
                {
                    this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
                    this.berry.setItem(new ItemStack(b.getBlock()));
                    this.pokemob.noEat(this.berry);
                    this.foodLoc = null;
                    this.addEntityPath(this.entity, null, this.moveSpeed);
                }
                else this.addEntityPath(this.entity, path, this.moveSpeed);
            }
        }
    }

    /** Eats a rock.
     *
     * @param b
     *            the rock
     * @param location
     *            where the rock is
     * @param dist
     *            distance to the rock */
    protected void eatRocks(final BlockState b, final Vector3 location, final double dist)
    {
        double diff = 2;
        diff = Math.max(diff, this.entity.getWidth());
        if (dist < diff)
        {
            if (PokecubeCore.getConfig().pokemobsEatRocks) if (b.getBlock() == Blocks.COBBLESTONE) location.setBlock(
                    this.world, Blocks.GRAVEL.getDefaultState());
            else if (b.getBlock() == Blocks.GRAVEL && PokecubeCore.getConfig().pokemobsEatGravel) location.setBlock(
                    this.world, Blocks.AIR.getDefaultState());
            else if (b.getMaterial() == Material.ROCK) location.setBlock(this.world, Blocks.COBBLESTONE
                    .getDefaultState());
            this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
            this.berry.setItem(new ItemStack(b.getBlock()));
            this.pokemob.eat(this.berry);
            this.foodLoc = null;
            this.addEntityPath(this.entity, null, this.moveSpeed);
        }
        else if (this.entity.ticksExisted % 20 == this.rand.nextInt(20))
        {
            boolean shouldChangePath = true;
            this.block = false;
            this.v.set(this.entity).add(0, this.entity.getHeight(), 0);

            final Predicate<BlockState> checker = (b2) -> PokecubeTerrainChecker.isRock(b2);
            final Vector3 temp = this.v.findClosestVisibleObject(this.world, true, (int) this.distance, checker);
            if (temp != null)
            {
                this.block = true;
                this.foodLoc = temp.copy();
            }

            Vector3 p, m;
            if (!this.entity.getNavigator().noPath())
            {
                p = this.v.set(this.entity.getNavigator().getPath().getFinalPathPoint());
                m = this.v1.set(this.foodLoc);
                if (p.distToSq(m) >= 16) shouldChangePath = false;
            }
            boolean pathed = false;
            Path path = null;
            if (shouldChangePath)
            {
                path = this.entity.getNavigator().getPathToPos(this.foodLoc.x, this.foodLoc.y, this.foodLoc.z, 0);
                pathed = path != null;
                this.addEntityPath(this.entity, path, this.moveSpeed);
            }
            if (shouldChangePath && !pathed)
            {
                this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
                this.berry.setItem(new ItemStack(b.getBlock()));
                this.pokemob.noEat(this.berry);
                this.foodLoc = null;
                if (this.pokemob.hasHomeArea())
                {
                    path = this.entity.getNavigator().getPathToPos(this.pokemob.getHome().getX(),
                            this.pokemob.getHome().getY(), this.pokemob.getHome().getZ(), 0);
                    this.addEntityPath(this.entity, path, this.moveSpeed);
                }
                else this.addEntityPath(this.entity, null, this.moveSpeed);
            }
        }
    }

    protected void findFood()
    {
        this.v.set(this.entity).addTo(0, this.entity.getEyeHeight(), 0);

        final boolean tameCheck = this.pokemob.getGeneralState(GeneralStates.TAMED) && !this.pokemob.getGeneralState(
                GeneralStates.STAYING);

        /*
         * Tame pokemon can eat berries out of trapped chests, so check for one
         * of those here.
         */
        if (tameCheck)
        {
            IInventory container = null;
            this.v.set(this.entity).add(0, this.entity.getHeight(), 0);

            final Vector3 temp = this.v.findClosestVisibleObject(this.world, true, 10, Blocks.TRAPPED_CHEST);

            if (temp != null && temp.getBlock(this.world) == Blocks.TRAPPED_CHEST)
            {
                container = (IInventory) temp.getTileEntity(this.world);

                for (int i1 = 0; i1 < container.getSizeInventory(); i1++)
                {
                    final ItemStack stack = container.getStackInSlot(i1);
                    if (PokecubeItems.is(AIHungry.FOODTAG, stack))
                    {
                        stack.shrink(1);
                        if (stack.isEmpty()) container.setInventorySlotContents(i1, ItemStack.EMPTY);
                        this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
                        final Path path = this.entity.getNavigator().getPathToPos(temp.x, temp.y, temp.z, 0);
                        this.addEntityPath(this.entity, path, this.moveSpeed);
                        this.pokemob.eat(this.berry);
                        return;
                    }
                }
            }
        }

        // No food already obtained, reset mating rules, hungry things don't
        // mate
        if (this.hitThreshold(AIHungry.MATERESET)) this.pokemob.resetLoveStatus();

        if (tameCheck && this.pokemob.getLogicState(LogicStates.SITTING))
        {
            this.pokemob.setHungerCooldown(100);
            // Still let them go hunting if they really want to.
            return;
        }
        this.block = false;
        this.v.set(this.entity, true);

        if (this.foodLoc == null)
        {
            if (!this.block && this.pokemob.isHerbivore())
            {
                final Predicate<BlockState> checker = (b2) -> this.isHerb(b2);
                final Vector3 temp = this.v.findClosestVisibleObject(this.world, true, (int) this.distance, checker);
                if (temp != null)
                {
                    this.block = true;
                    this.foodLoc = temp.copy();
                }
            }
            if (!this.block && this.pokemob.isLithotroph())
            {
                final Predicate<BlockState> checker = (b2) -> PokecubeTerrainChecker.isRock(b2);
                final Vector3 temp = this.v.findClosestVisibleObject(this.world, true, (int) this.distance, checker);
                if (temp != null)
                {
                    this.block = true;
                    this.foodLoc = temp.copy();
                }
            }
            if (!this.block && this.pokemob.filterFeeder())
            {
                final Vector3 temp = this.v.findClosestVisibleObject(this.world, true, (int) this.distance,
                        Blocks.WATER);
                if (this.entity.isInWater())
                {
                    this.pokemob.eat(this.berry);
                    this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
                    return;
                }
                if (temp != null)
                {
                    this.block = true;
                    this.foodLoc = temp.copy();
                }
            }
            if (!this.block && this.pokemob.eatsBerries()) if (this.pokemob.getGeneralState(GeneralStates.TAMED))
            {
                final Vector3 temp = this.v.findClosestVisibleObject(this.world, true, (int) this.distance,
                        IBerryFruitBlock.class);
                if (temp != null)
                {
                    this.block = true;
                    this.foodLoc = temp.copy();
                }
            }
        }

        if (this.foodLoc == null) this.pokemob.setHungerCooldown(10);
    }

    // 0 is sunrise, 6000 noon, 12000 dusk, 18000 midnight, 23999
    public boolean isGoodSleepingSpot(final ChunkCoordinate c)
    {
        if (this.pokemob.getHome() == null || this.pokemob.getHome().equals(BlockPos.ZERO))
        {
            this.v1.set(this.entity);
            this.pokemob.setHome(this.v1.intX(), this.v1.intY(), this.v1.intZ(), 16);
        }
        if (this.pokemob.hasHomeArea() && this.entity.getPosition().distanceSq(this.pokemob.getHome()) > 9)
            return false;
        // TODO search for possible better place to sleep
        return true;
    }

    @Override
    public void reset()
    {
    }

    private boolean isHerb(final BlockState state)
    {
        return PokecubeTerrainChecker.isFruit(state) || PokecubeTerrainChecker.isEdiblePlant(state);
    }

    @Override
    public void run()
    {
        if (this.foodLoc == null) this.findFood();
        else
        {
            this.rand = new Random(this.pokemob.getRNGValue());
            // Go find and eat the block
            final double d = this.foodLoc.addTo(0.5, 0.5, 0.5).distToEntity(this.entity);
            this.foodLoc.addTo(-0.5, -0.5, -0.5);
            final BlockState b = this.foodLoc.getBlockState(this.world);
            if (b == null)
            {
                this.foodLoc = null;
                return;
            }
            if (b.getBlock() instanceof IBerryFruitBlock) this.eatBerry(b, d);
            else if (this.isHerb(b)) this.eatPlant(b, this.foodLoc, d);
            else if (PokecubeTerrainChecker.isRock(b) && this.pokemob.isLithotroph()) this.eatRocks(b, this.foodLoc, d);
            else this.foodLoc = null;
        }
    }

    @Override
    public boolean shouldRun()
    {
        final int hungerTicks = AIHungry.TICKRATE;
        // This can be set in configs to disable.
        if (hungerTicks < 0) return false;

        // Only run this every few ticks.
        if (this.entity.ticksExisted % hungerTicks != 0) return false;

        // Ensure we are not set to hunt if we shouldn't be
        if (!this.hitThreshold(AIHungry.EATTHRESHOLD) && this.pokemob.getCombatState(CombatStates.HUNTING)) this.pokemob
                .setCombatState(CombatStates.HUNTING, false);

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
        this.pokemob.setHungerTime(this.pokemob.getHungerTime() + hungerTicks);

        this.calculateHunger();

        // Do not run this if on cooldown
        if (this.pokemob.getHungerCooldown() > 0) return false;

        this.v.set(this.entity);

        // Check if we should go after bait. The Math.random() > 0.99 is to
        // allow non-hungry fish to also try to get bait.
        if (Math.random() > 0.99) this.checkBait();

        // Do not run this if not really hungry
        if (!this.hitThreshold(AIHungry.EATTHRESHOLD)) return false;

        // Check if we are hunting or should be
        // Reset hunting status if we are not actually hungry
        if (this.hitThreshold(AIHungry.HUNTTHRESHOLD)) if (!this.pokemob.getCombatState(CombatStates.HUNTING))
            this.pokemob.setCombatState(CombatStates.HUNTING, true);

        final boolean hunting = this.pokemob.getCombatState(CombatStates.HUNTING);
        if (this.pokemob.getLogicState(LogicStates.SLEEPING) && !hunting)
        {
            if (hunting) this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
            return false;
        }
        // Ensure food location is not too far away.
        if (this.foodLoc != null && this.foodLoc.distToEntity(this.entity) > 32) this.foodLoc = null;
        // We have a location, so can run.
        if (this.foodLoc != null) return true;
        // We are hunting for food, so can run.
        return true;
    }

    private void calculateHunger()
    {
        this.hungerValue = AIHungry.calculateHunger(this.pokemob);
    }

    @Override
    public void tick()
    {
        // Configs can set this to -1 to disable ticking.
        if (AIHungry.TICKRATE < 0) return;

        this.v.set(this.entity);
        final int hungerTicks = AIHungry.TICKRATE;

        // Everything after here only applies about once per second.
        if (this.entity.ticksExisted % hungerTicks != 0) return;

        // Check if we should go to sleep instead.
        this.checkSleep();

        final Random rand = new Random(this.pokemob.getRNGValue());
        final int cur = this.entity.ticksExisted / hungerTicks;
        final int tick = rand.nextInt(10);

        /*
         * Check the various hunger types if it is hunting. And if so, refresh
         * the hunger time counter.
         */
        if (this.pokemob.getCombatState(CombatStates.HUNTING)) if (this.checkHunt()) this.calculateHunger();

        // Check own inventory for berries to eat, and then if the mob is
        // allowed to, collect berries if none to eat.
        if (this.hitThreshold(AIHungry.EATTHRESHOLD) && !this.checkInventory())
        {
            // Pokemobs set to stay can collect berries, or wild ones,
            boolean tameCheck = this.pokemob.getGeneralState(GeneralStates.STAYING)
                    || this.pokemob.getOwnerId() == null;
            if (this.entity.getPersistentData().contains("lastInteract"))
            {
                final long time = this.entity.getPersistentData().getLong("lastInteract");
                final long diff = this.entity.getEntityWorld().getGameTime() - time;
                if (diff < PokecubeCore.getConfig().pokemobLifeSpan) tameCheck = false;
            }
            // If they are allowed to, find the berries.
            if (tameCheck)
            {
                // Only run this if we are getting close to hurt damage, mostly
                // to allow trying other food sources first.
                if (this.hitThreshold(AIHungry.BERRYGEN)) this.toRun.add(new GenBerries(this.pokemob));
            }
            // Otherwise take damage.
            else if (this.hitThreshold(AIHungry.DAMAGE))
            {
                final float ratio = (AIHungry.DAMAGE - this.hungerValue) / AIHungry.DAMAGE;
                final boolean dead = this.pokemob.getMaxHealth() * ratio > this.pokemob.getHealth() || this
                        .hitThreshold(AIHungry.DEATH);
                // Ensure it dies if it should.
                final float damage = dead ? this.pokemob.getMaxHealth() * 20 : this.pokemob.getMaxHealth() * ratio;
                if (damage >= 1 && ratio >= 0.0625 && this.entity.getHealth() > 0)
                {
                    this.entity.attackEntityFrom(DamageSource.STARVE, damage);
                    if (!dead)
                    {
                        if (this.lastMessageTick1 < this.entity.getEntityWorld().getGameTime())
                        {
                            this.lastMessageTick1 = (int) (this.entity.getEntityWorld().getGameTime() + 100);
                            this.pokemob.displayMessageToOwner(
                                    new TranslationTextComponent("pokemob.hungry.hurt", this.pokemob.getDisplayName()));
                        }
                    }
                    else if (this.lastMessageTick2 < this.entity.getEntityWorld().getGameTime())
                    {
                        this.lastMessageTick2 = (int) (this.entity.getEntityWorld().getGameTime() + 100);
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
        if (this.entity.getAttackTarget() == null && this.pokemob.getHealth() > 0 && this.entity.isAlive()
                && !this.entity.getEntityWorld().isRemote && this.pokemob.getHungerCooldown() < 0
                && this.pokemob.getHungerTime() < 0 && cur % 10 == tick)
        {
            final float dh = Math.max(1, this.pokemob.getMaxHealth() * 0.05f);
            final float toHeal = this.pokemob.getHealth() + dh;
            this.pokemob.setHealth(Math.min(toHeal, this.pokemob.getMaxHealth()));
        }
    }
}
