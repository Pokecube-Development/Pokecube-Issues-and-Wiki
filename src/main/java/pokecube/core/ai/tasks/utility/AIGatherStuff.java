package pokecube.core.ai.tasks.utility;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

/**
 * This IAIRunnable gets the mob to look for and collect dropped items and
 * berries. It requires an AIStoreStuff to have located a suitable storage
 * before it will run.
 */
public class AIGatherStuff extends AIBase
{
    /**
     * This manages the pokemobs replanting anything that they gather.
     *
     * @author Patrick
     */
    private static class ReplantTask implements IRunnable
    {
        final Entity    entity;
        final ItemStack seeds;
        final BlockPos  pos;

        public ReplantTask(final Entity entity, final ItemStack seeds, final BlockPos pos)
        {
            this.seeds = seeds.copy();
            this.pos = new BlockPos(pos);
            this.entity = entity;
        }

        @Override
        public boolean run(final World world)
        {
            if (this.seeds.isEmpty()) return true;
            // Check if is is plantable.
            if (this.seeds.getItem() instanceof IPlantable)
            {
                final BlockPos down = this.pos.down();

                if (!world.isAirBlock(down))
                {
                    // Use the fakeplayer to plant it
                    final PlayerEntity player = PokecubeMod.getFakePlayer(world);
                    player.setPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ());
                    player.setHeldItem(Hand.MAIN_HAND, this.seeds);
                    final ItemUseContext context = new ItemUseContext(player, Hand.MAIN_HAND, new BlockRayTraceResult(
                            new Vec3d(0.5, 1, 0.5), Direction.UP, this.pos, false));
                    // Attempt to plant it.
                    this.seeds.getItem().onItemUse(context);
                }
                IPokemob pokemob;
                // Add the "returned" stack to the inventory (ie remaining
                // seeds)
                if (!this.seeds.isEmpty() && (pokemob = CapabilityPokemob.getPokemobFor(this.entity)) != null)
                    if (!ItemStackTools.addItemStackToInventory(this.seeds, pokemob.getInventory(), 2)) this.entity
                            .entityDropItem(this.seeds, 0);
            }
            return true;
        }
    }

    public static int COOLDOWN_SEARCH  = 200;
    public static int COOLDOWN_COLLECT = 5;

    public static int COOLDOWN_PATH = 5;
    // Matcher used to determine if a block is a fruit or crop to be picked.
    private static final Predicate<BlockState> berryMatcher = input -> PokecubeTerrainChecker.isFruit(input);

    private static final Predicate<ItemEntity> deaditemmatcher = input -> !input.isAlive() || !input.addedToChunk
            || !input.isAddedToWorld();

    final double       distance;
    boolean            block           = false;
    List<ItemEntity>   stuff           = Lists.newArrayList();
    Vector3            stuffLoc        = Vector3.getNewVector();
    boolean            hasRoom         = true;
    int                collectCooldown = 0;
    int                pathCooldown    = 0;
    final AIStoreStuff storage;
    Vector3            seeking         = Vector3.getNewVector();
    Vector3            v               = Vector3.getNewVector();
    Vector3            v1              = Vector3.getNewVector();

    public AIGatherStuff(final IPokemob mob, final double distance, final AIStoreStuff storage)
    {
        super(mob);
        this.distance = distance;
        this.storage = storage;
        this.setMutex(1);
    }

    private void findStuff()
    {
        // Only mobs that are standing with homes should look for stuff.
        if (this.pokemob.getHome() == null || this.pokemob.getGeneralState(GeneralStates.TAMED) && this.pokemob
                .getLogicState(LogicStates.SITTING)) return;
        this.block = false;
        this.v.set(this.pokemob.getHome()).add(0, this.entity.getHeight(), 0);

        final int distance = this.pokemob.getGeneralState(GeneralStates.TAMED) ? PokecubeCore
                .getConfig().tameGatherDistance : PokecubeCore.getConfig().wildGatherDistance;

        final List<ItemEntity> list = this.getEntitiesWithinDistance(this.entity, distance, ItemEntity.class);
        this.stuff.clear();
        double closest = 1000;

        // Check for items to possibly gather.
        for (final Entity o : list)
        {
            final ItemEntity e = (ItemEntity) o;
            final double dist = e.getPosition().distanceSq(this.pokemob.getHome());
            this.v.set(e);
            if (dist < closest && Vector3.isVisibleEntityFromEntity(this.entity, e))
            {
                this.stuff.add(e);
                closest = dist;
            }
        }
        // Found an item, return.
        if (!this.stuff.isEmpty())
        {
            Collections.sort(this.stuff, (o1, o2) ->
            {
                final int dist1 = (int) o1.getDistanceSq(AIGatherStuff.this.entity);
                final int dist2 = (int) o2.getDistanceSq(AIGatherStuff.this.entity);
                return dist1 - dist2;
            });
            this.stuffLoc.set(this.stuff.get(0));
            return;
        }
        this.v.set(this.entity).addTo(0, this.entity.getEyeHeight(), 0);
        // check for berries to collect.
        if (!this.block && this.pokemob.eatsBerries())
        {
            final Vector3 temp = this.v.findClosestVisibleObject(this.world, true, distance,
                    AIGatherStuff.berryMatcher);
            if (temp != null)
            {
                this.block = true;
                this.stuffLoc.set(temp);
            }
        }
        if (this.pokemob.isElectrotroph())
        {

        }
        // Nothing found, enter cooldown.
        if (this.stuffLoc.isEmpty()) this.collectCooldown = AIGatherStuff.COOLDOWN_SEARCH;
    }

    private void gatherStuff(final boolean mainThread)
    {
        if (!mainThread)
        {
            if (this.pathCooldown-- > 0) return;
            // Set path to the stuff found.
            final double speed = this.entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
            if (this.stuff != null)
            {
                this.stuffLoc.set(this.stuff);
                final Path path = this.entity.getNavigator().func_225466_a(this.stuffLoc.x, this.stuffLoc.y,
                        this.stuffLoc.z, 0);
                this.addEntityPath(this.entity, path, speed);
            }
            else
            {
                final Path path = this.entity.getNavigator().func_225466_a(this.stuffLoc.x, this.stuffLoc.y,
                        this.stuffLoc.z, 0);
                this.addEntityPath(this.entity, path, speed);
            }
            this.pathCooldown = AIGatherStuff.COOLDOWN_PATH;
        }
        else if (!this.stuffLoc.isEmpty())
        {
            double diff = 3;
            diff = Math.max(diff, this.entity.getWidth());
            final double dist = this.stuffLoc.distToEntity(this.entity);
            this.v.set(this.entity).subtractFrom(this.stuffLoc);
            final double dot = this.v.normalize().dot(Vector3.secondAxis);
            // This means that the item is directly above the pokemob, assume it
            // can pick up to 3 blocks upwards.
            if (dot < -0.9 && this.entity.onGround) diff = Math.max(3, diff);
            if (dist < diff)
            {
                this.setCombatState(this.pokemob, CombatStates.HUNTING, false);
                final BlockState state = this.stuffLoc.getBlockState(this.entity.getEntityWorld());
                this.stuffLoc.setAir(this.world);
                if (state.getMaterial() != Material.PLANTS)
                {
                    final List<ItemStack> list = Block.getDrops(state, this.world, this.stuffLoc.getPos(), null);
                    boolean replanted = false;
                    // See if anything dropped was a seed for the thing we
                    // picked.
                    for (final ItemStack stack : list)
                        // If so, Replant it.
                        if (stack.getItem() instanceof IPlantable && !replanted)
                        {
                            this.toRun.add(new ReplantTask(this.entity, stack.copy(), this.stuffLoc.getPos()));
                            replanted = true;
                        }
                        else this.toRun.add(new InventoryChange(this.entity, 2, stack.copy(), true));
                    if (!replanted) // Try to find a seed in our inventory for
                                    // this plant.
                        for (int i = 2; i < this.pokemob.getInventory().getSizeInventory(); i++)
                        {
                        final ItemStack stack = this.pokemob.getInventory().getStackInSlot(i);
                        if (!stack.isEmpty() && stack.getItem() instanceof IPlantable)
                        {
                        final IPlantable plantable = (IPlantable) stack.getItem();
                        final BlockState plantState = plantable.getPlant(this.world, this.stuffLoc.getPos().up());
                        if (plantState.getBlock() == state.getBlock())
                        {
                        this.toRun.add(new ReplantTask(this.entity, stack.copy(), this.stuffLoc.getPos()));
                        replanted = true;
                        break;
                        }
                        }
                        }
                }
                this.stuffLoc.clear();
                this.addEntityPath(this.entity, null, 0);
            }
        }
    }

    @Override
    public void reset()
    {
        this.stuffLoc.clear();
        this.stuff.clear();
    }

    @Override
    public void run()
    {
        if (this.stuffLoc.isEmpty() && this.collectCooldown-- < 0) this.findStuff();
        if (!this.stuffLoc.isEmpty()) this.gatherStuff(false);
    }

    @Override
    public boolean shouldRun()
    {
        // Check if gather is enabled first.
        if (!this.pokemob.isRoutineEnabled(AIRoutine.GATHER)) return false;
        final boolean wildCheck = !PokecubeCore.getConfig().wildGather && !this.pokemob.getGeneralState(
                GeneralStates.TAMED);
        // Check if this should be doing something else instead, if so return
        // false.
        if (this.tameCheck() || this.entity.getAttackTarget() != null || wildCheck) return false;
        final int rate = this.pokemob.getGeneralState(GeneralStates.TAMED) ? PokecubeCore.getConfig().tameGatherDelay
                : PokecubeCore.getConfig().wildGatherDelay;
        final Random rand = new Random(this.pokemob.getRNGValue());
        // Check if it has a location, if so, apply a delay and return false if
        // not correct tick for this pokemob.
        if (this.pokemob.getHome() == null || this.entity.ticksExisted % rate != rand.nextInt(rate)) return false;

        // Apply cooldown.
        if (this.collectCooldown < -2000) this.collectCooldown = AIGatherStuff.COOLDOWN_SEARCH;
        // If too far, clear location.
        if (this.stuffLoc.distToEntity(this.entity) > 32) this.stuffLoc.clear();

        // check if pokemob has room in inventory for stuff, if so, return true.
        final IInventory inventory = this.pokemob.getInventory();
        for (int i = 3; i < inventory.getSizeInventory(); i++)
        {
            this.hasRoom = inventory.getStackInSlot(i).isEmpty();
            if (this.hasRoom) return true;
        }
        // Otherwise return false.
        return false;
    }

    /**
     * Only tame pokemobs set to "stay" should run this AI.
     *
     * @return
     */
    private boolean tameCheck()
    {
        return this.pokemob.getGeneralState(GeneralStates.TAMED) && (!this.pokemob.getGeneralState(
                GeneralStates.STAYING) || !PokecubeCore.getConfig().tameGather);
    }

    @Override
    public void tick()
    {

        // if (collectCooldown-- > 0) return;
        synchronized (this.stuffLoc)
        {
            // check stuff for being still around.
            if (!this.stuff.isEmpty())
            {
                final int num = this.stuff.size();
                this.stuff.removeIf(AIGatherStuff.deaditemmatcher);
                Collections.sort(this.stuff, (o1, o2) ->
                {
                    final int dist1 = (int) o1.getDistanceSq(AIGatherStuff.this.entity);
                    final int dist2 = (int) o2.getDistanceSq(AIGatherStuff.this.entity);
                    return dist1 - dist2;
                });

                if (this.stuff.isEmpty())
                {
                    this.reset();
                    return;
                }

                if (this.stuff.size() != num)
                {
                    this.stuffLoc.set(this.stuff.get(0));
                    return;
                }
            }
            else if (!this.stuffLoc.isEmpty()) if (!this.stuff.isEmpty())
            {
                final ItemEntity itemStuff = this.stuff.get(0);
                if (!itemStuff.isAlive() || !itemStuff.addedToChunk || !itemStuff.isAddedToWorld())
                {
                    this.stuff.remove(0);
                    return;
                }
                double close = this.entity.getWidth() * this.entity.getWidth();
                close = Math.max(close, 2);
                if (itemStuff.getDistance(this.entity) < close)
                {
                    ItemStackTools.addItemStackToInventory(itemStuff.getItem(), this.pokemob.getInventory(), 2);
                    itemStuff.remove();
                    this.stuff.remove(0);
                    if (this.stuff.isEmpty()) this.reset();
                    else this.stuffLoc.set(this.stuff.get(0));
                }
            }
            else this.gatherStuff(true);
        }
    }
}
