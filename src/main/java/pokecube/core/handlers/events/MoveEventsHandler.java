package pokecube.core.handlers.events;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.events.pokemob.combat.MoveUse;
import pokecube.core.events.pokemob.combat.MoveUse.MoveWorldAction;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.impl.NonPersistantStatusEffect;
import pokecube.core.interfaces.entity.impl.NonPersistantStatusEffect.Effect;
import pokecube.core.interfaces.entity.impl.OngoingMoveEffect;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect.Status;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class MoveEventsHandler
{
    public static class UseContext extends BlockItemUseContext
    {
        protected UseContext(final World worldIn, final PlayerEntity playerIn, final Hand handIn,
                final ItemStack stackIn, final BlockRayTraceResult rayTraceResultIn)
        {
            super(worldIn, playerIn, handIn, stackIn, rayTraceResultIn);
        }

        public BlockPos getHitPos()
        {
            return this.rayTraceResult.getPos();
        }

        public BlockState getHitState()
        {
            return this.getWorld().getBlockState(this.getHitPos());
        }
    }

    public static class ActionWrapper implements IMoveAction
    {
        final IMoveAction   wrapped;
        private IMoveAction custom;
        private boolean     checked = false;

        public ActionWrapper(final IMoveAction wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public boolean applyEffect(final IPokemob user, final Vector3 location)
        {
            if (!this.checked)
            {
                this.checked = true;
                this.custom = MoveEventsHandler.customActions.get(this.getMoveName());
            }
            final boolean customApplied = this.custom != null && this.custom.applyEffect(user, location);
            return this.wrapped.applyEffect(user, location) || customApplied;
        }

        @Override
        public String getMoveName()
        {
            return this.wrapped.getMoveName();
        }

        @Override
        public void init()
        {
            this.wrapped.init();
            if (this.custom != null) this.custom.init();
        }
    }

    private static class DefaultAction implements IMoveAction
    {
        Move_Base move;

        public DefaultAction(final Move_Base move)
        {
            this.move = move;
        }

        @Override
        public boolean applyEffect(final IPokemob attacker, final Vector3 location)
        {
            if (!MoveEventsHandler.canEffectBlock(attacker, location)) return false;
            if (this.move.getType(attacker) == PokeType.getType("water")) return MoveEventsHandler.doDefaultWater(
                    attacker, this.move, location);
            if (this.move.getType(attacker) == PokeType.getType("ice") && (this.move.getAttackCategory()
                    & IMoveConstants.CATEGORY_DISTANCE) > 0 && this.move.move.power > 0) return MoveEventsHandler
                            .doDefaultIce(attacker, this.move, location);
            if (this.move.getType(attacker) == PokeType.getType("electric")) MoveEventsHandler.doDefaultElectric(
                    attacker, this.move, location);
            if (this.move.getType(attacker) == PokeType.getType("fire")) return MoveEventsHandler.doDefaultFire(
                    attacker, this.move, location);
            return false;
        }

        @Override
        public String getMoveName()
        {
            return this.move.name;
        }
    }

    public static int WATERSTRONG = 100;

    public static int FIRESTRONG = 100;

    public static int ELECTRICSTRONG = 100;

    public static final Map<String, IMoveAction> customActions = Maps.newHashMap();

    private static MoveEventsHandler INSTANCE;

    public static boolean attemptSmelt(final IPokemob attacker, final Vector3 location)
    {
        final World world = attacker.getEntity().getEntityWorld();
        final List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, location.getAABB().grow(1));
        if (!items.isEmpty())
        {
            boolean smelt = false;
            final AbstractFurnaceTileEntity tile = new FurnaceTileEntity();
            tile.setWorldAndPos(world, location.getPos());
            for (final ItemEntity item2 : items)
            {
                final ItemEntity item = item2;
                final ItemStack stack = item.getItem();
                final int num = stack.getCount();
                tile.setInventorySlotContents(0, stack);
                tile.setInventorySlotContents(1, stack);
                final IRecipe<?> irecipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, tile, world).orElse(
                        null);
                if (irecipe == null) continue;
                ItemStack newstack = irecipe.getRecipeOutput();
                if (newstack != null)
                {
                    newstack = newstack.copy();
                    newstack.setCount(num);
                    int i1 = num;
                    float f = ((AbstractCookingRecipe) irecipe).getExperience();
                    if (f == 0.0F) i1 = 0;
                    else if (f < 1.0F)
                    {
                        int j = MathHelper.floor(i1 * f);
                        if (j < MathHelper.ceil(i1 * f) && Math.random() < i1 * f - j) ++j;

                        i1 = j;
                    }
                    f = i1;
                    while (i1 > 0)
                    {
                        final int k = ExperienceOrbEntity.getXPSplit(i1);
                        i1 -= k;
                        world.addEntity(new ExperienceOrbEntity(world, location.x, location.y + 1.5D, location.z + 0.5D,
                                k));
                    }
                    int hunger = PokecubeCore.getConfig().baseSmeltingHunger * num;
                    hunger = (int) Math.max(1, hunger / (float) attacker.getLevel());
                    if (f > 0) hunger *= f;
                    attacker.setHungerTime(attacker.getHungerTime() + hunger);
                    item.setItem(newstack);
                    item.lifespan += 6000;
                    smelt = true;
                }
            }
            return smelt;
        }
        return false;
    }

    /**
     * This method should be called before any block setting by any move
     * effects.
     *
     * @param user
     * @param location
     * @return
     */
    public static boolean canEffectBlock(final IPokemob user, final Vector3 location)
    {
        LivingEntity owner = user.getOwner();
        final boolean repel = SpawnHandler.getNoSpawnReason(user.getEntity().getEntityWorld(), location.intX(), location
                .intY(), location.intZ()) == ForbidReason.REPEL;
        if (!(owner instanceof PlayerEntity)) owner = PokecubeMod.getFakePlayer(user.getEntity().getEntityWorld());
        if (repel)
        {
            if (!user.getCombatState(CombatStates.ANGRY)) CommandTools.sendError(owner, "pokemob.action.denyrepel");
            return false;
        }
        final PlayerEntity player = (PlayerEntity) owner;
        final BreakEvent evt = new BreakEvent(player.getEntityWorld(), location.getPos(), location.getBlockState(player
                .getEntityWorld()), player);
        MinecraftForge.EVENT_BUS.post(evt);
        if (evt.isCanceled())
        {
            final TranslationTextComponent message = new TranslationTextComponent("pokemob.createbase.deny.noperms");
            if (!user.getCombatState(CombatStates.ANGRY)) owner.sendMessage(message);
            return false;
        }
        return true;
    }

    /**
     * This will have the following effects, for "Strong" electric type moves:
     * Melt sand to glass
     */
    public static boolean doDefaultElectric(final IPokemob attacker, final Move_Base move, final Vector3 location)
    {
        if (move.getPWR() < MoveEventsHandler.ELECTRICSTRONG || !PokecubeCore.getConfig().defaultElectricActions)
            return false;

        final World world = attacker.getEntity().getEntityWorld();
        final BlockState state = location.getBlockState(world);
        final Block block = state.getBlock();
        final Vector3 nextBlock = Vector3.getNewVector().set(attacker.getEntity()).subtractFrom(location).reverse()
                .norm().addTo(location);
        final BlockState nextState = nextBlock.getBlockState(world);
        if (block == Blocks.SAND)
        {
            location.setBlock(world, Blocks.GLASS.getDefaultState());
            return true;
        }
        else if (state.isReplaceable(MoveEventsHandler.getContext(world, attacker, Blocks.GLASS.getDefaultState(),
                location)) && nextState.getBlock() == Blocks.SAND)
        {
            nextBlock.setBlock(world, Blocks.GLASS.getDefaultState());
            return true;
        }
        return false;
    }

    /**
     * This will have the following effects, for fire type moves:
     * Ignite flamable blocks
     * Melt snow
     * If strong, melt obsidian to lava
     * If none of the above, attempt to cook items nearby
     */
    public static boolean doDefaultFire(final IPokemob attacker, final Move_Base move, final Vector3 location)
    {
        if (move.getPWR() <= 0 || !PokecubeCore.getConfig().defaultFireActions) return false;
        final World world = attacker.getEntity().getEntityWorld();
        final UseContext context = MoveEventsHandler.getContext(world, attacker, Blocks.LAVA.getDefaultState(),
                location);
        final BlockState state = context.getHitState();
        Block block = state.getBlock();
        final BlockPos hitPos = context.getHitPos();
        final BlockPos prevPos = context.getPos();
        final int flamNext = block.getFlammability(state, world, prevPos, context.getFace());
        final BlockState prev = world.getBlockState(prevPos);

        // Start fires
        if (flamNext != 0 && prev.isReplaceable(context))
        {
            world.setBlockState(prevPos, Blocks.FIRE.getDefaultState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW_BLOCK)
        {
            world.setBlockState(hitPos, Blocks.WATER.getDefaultState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            final int level = state.get(SnowBlock.LAYERS);
            world.setBlockState(hitPos, Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            world.setBlockState(hitPos, Blocks.WATER.getDefaultState());
            return true;
        }
        block = prev.getBlock();

        // Melt Snow
        if (block == Blocks.SNOW_BLOCK)
        {
            world.setBlockState(prevPos, Blocks.WATER.getDefaultState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            final int level = prev.get(SnowBlock.LAYERS);
            world.setBlockState(prevPos, Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            world.setBlockState(prevPos, Blocks.WATER.getDefaultState());
            return true;
        }
        // Otherwise smelt attempt
        if (move.getPWR() < MoveEventsHandler.FIRESTRONG) return MoveEventsHandler.attemptSmelt(attacker, location);

        // Melt obsidian
        if (block == Blocks.OBSIDIAN)
        {
            world.setBlockState(hitPos, Blocks.LAVA.getDefaultState());
            return true;
        }
        // Evapourate water
        else if (block == Blocks.WATER)
        {
            world.setBlockState(hitPos, Blocks.AIR.getDefaultState());
            return true;
        }
        // Otherwise smelt attempt
        return MoveEventsHandler.attemptSmelt(attacker, location);
    }

    /**
     * This will have the following effects, for ice type moves:
     * Place snow
     * Freeze water
     */
    public static boolean doDefaultIce(final IPokemob attacker, final Move_Base move, final Vector3 location)
    {
        if (!PokecubeCore.getConfig().defaultIceActions) return false;
        final World world = attacker.getEntity().getEntityWorld();
        final UseContext context = MoveEventsHandler.getContext(world, attacker, Blocks.SNOW.getDefaultState(),
                location);
        final BlockState state = context.getHitState();
        final Block block = state.getBlock();
        // // First attempt to freeze the water
        if (block == Blocks.WATER && state.get(FlowingFluidBlock.LEVEL) == 0)
        {
            world.setBlockState(context.getPos(), Blocks.ICE.getDefaultState());
            return true;
        }
        final ActionResultType result = context.getItem().onItemUse(context);
        return result == ActionResultType.SUCCESS;
    }

    /**
     * This will have the following effects, for water type moves:
     * Extinguish fires
     * if strong:
     * turn lava to obsidian
     * water farmland
     */
    public static boolean doDefaultWater(final IPokemob attacker, final Move_Base move, final Vector3 location)
    {
        if (!PokecubeCore.getConfig().defaultWaterActions) return false;
        if (move.isSelfMove()) return false;
        final World world = attacker.getEntity().getEntityWorld();
        final UseContext context = MoveEventsHandler.getContext(world, attacker, Blocks.WATER.getDefaultState(),
                location);
        final BlockState state = context.getHitState();
        final Block block = state.getBlock();
        final BlockPos hitPos = context.getHitPos();
        // Put out fires
        if (block == Blocks.FIRE)
        {
            location.setAir(world);
            return true;
        }
        if (state.getProperties().contains(FarmlandBlock.MOISTURE))
        {
            final int level = state.get(FarmlandBlock.MOISTURE);
            if (level < 7)
            {
                world.setBlockState(hitPos, state.with(FarmlandBlock.MOISTURE, 7));
                return true;
            }
        }
        if (move.getPWR() < MoveEventsHandler.WATERSTRONG) return false;
        // Freeze lava
        if (block == Blocks.LAVA)
        {
            final int level = state.get(FlowingFluidBlock.LEVEL);
            final BlockState replacement = level == 0 ? Blocks.OBSIDIAN.getDefaultState()
                    : Blocks.STONE.getDefaultState();
            world.setBlockState(hitPos, replacement);
            return true;
        }
        final BlockPos prevPos = context.getPos();
        final BlockState prev = world.getBlockState(prevPos);

        // Freeze lava
        if (prev.getBlock() == Blocks.LAVA)
        {
            final int level = state.get(FlowingFluidBlock.LEVEL);
            final BlockState replacement = level == 0 ? Blocks.OBSIDIAN.getDefaultState()
                    : Blocks.STONE.getDefaultState();
            world.setBlockState(prevPos, replacement);
            return true;
        }

        // Attempt to place some water
        if (prev.isReplaceable(context)) world.setBlockState(prevPos, Blocks.WATER.getDefaultState().with(
                FlowingFluidBlock.LEVEL, 2));
        return false;
    }

    public static UseContext getContext(final World world, final IPokemob user, final BlockState toPlace,
            final Vector3 target)
    {
        final ItemStack stack = new ItemStack(toPlace.getBlock());
        final PlayerEntity player = user.getOwner() instanceof PlayerEntity ? (PlayerEntity) user.getOwner()
                : PokecubeMod.getFakePlayer(world);
        final Vector3 origin = Vector3.getNewVector().set(user.getEntity());
        final Vec3d start = origin.toVec3d();
        final Vec3d end = target.toVec3d();
        final RayTraceContext context = new RayTraceContext(start, end, BlockMode.COLLIDER, FluidMode.ANY, user
                .getEntity());
        final BlockRayTraceResult hit = world.rayTraceBlocks(context);
        return new UseContext(world, player, Hand.MAIN_HAND, stack, hit);
    }

    public static MoveEventsHandler getInstance()
    {
        return MoveEventsHandler.INSTANCE == null ? MoveEventsHandler.INSTANCE = new MoveEventsHandler()
                : MoveEventsHandler.INSTANCE;
    }

    public static void register(IMoveAction move)
    {
        if (!(move instanceof ActionWrapper)) move = new ActionWrapper(move);
        MoveEventsHandler.getInstance().actionMap.put(move.getMoveName(), move);
    }

    public Map<String, IMoveAction> actionMap = Maps.newHashMap();

    private MoveEventsHandler()
    {
        PokecubeCore.MOVE_BUS.register(this);
        IOngoingAffected.EFFECTS.put(NonPersistantStatusEffect.ID, NonPersistantStatusEffect.class);
        IOngoingAffected.EFFECTS.put(PersistantStatusEffect.ID, PersistantStatusEffect.class);
        IOngoingAffected.EFFECTS.put(OngoingMoveEffect.ID, OngoingMoveEffect.class);
        Status.initDefaults();
        Effect.initDefaults();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onEvent(final MoveUse.DuringUse.Post evt)
    {
        final MovePacket move = evt.getPacket();
        final IPokemob attacker = move.attacker;
        final Entity attacked = move.attacked;
        final IPokemob target = CapabilityPokemob.getPokemobFor(attacked);

        final IPokemobUseable attackerheld = IPokemobUseable.getUsableFor(attacker.getHeldItem());
        if (attackerheld != null)
        {
            final ActionResult<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(), move);
            if (result.getType() == ActionResultType.SUCCESS) attacker.setHeldItem(result.getResult());
        }
        if (target != null)
        {
            final IPokemobUseable targetheld = IPokemobUseable.getUsableFor(target.getHeldItem());
            if (targetheld != null)
            {
                final ActionResult<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(), move);
                if (result.getType() == ActionResultType.SUCCESS) target.setHeldItem(result.getResult());
            }
        }

        final boolean user = evt.isFromUser();
        final IPokemob applied = user ? attacker : target;
        if (applied != null && applied.getHeldItem() != null) ItemGenerator.processHeldItemUse(move, applied, applied
                .getHeldItem());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onEvent(final MoveUse.DuringUse.Pre evt)
    {
        final MovePacket move = evt.getPacket();
        final Move_Base attack = move.getMove();
        final boolean user = evt.isFromUser();
        final IPokemob attacker = move.attacker;
        final Entity attacked = move.attacked;
        final IPokemob target = CapabilityPokemob.getPokemobFor(attacked);
        final IPokemob applied = user ? attacker : target;
        final IPokemob other = user ? target : attacker;

        final IPokemobUseable attackerheld = IPokemobUseable.getUsableFor(attacker.getHeldItem());
        if (attackerheld != null)
        {
            final ActionResult<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(), move);
            if (result.getType() == ActionResultType.SUCCESS) attacker.setHeldItem(result.getResult());
        }
        if (target != null)
        {
            final IPokemobUseable targetheld = IPokemobUseable.getUsableFor(target.getHeldItem());
            if (targetheld != null)
            {
                final ActionResult<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(), move);
                if (result.getType() == ActionResultType.SUCCESS) target.setHeldItem(result.getResult());
            }
        }

        if (applied == null) return;
        if (!user) applied.getEntity().getPersistentData().putString("lastMoveHitBy", move.attack);
        if (MoveEntry.oneHitKos.contains(attack.name) && target != null && target.getLevel() < attacker.getLevel())
            move.failed = true;
        if (target != null && target.getMoveStats().substituteHP > 0 && !user)
        {
            final float damage = MovesUtils.getAttackStrength(attacker, target, move.getMove().getCategory(attacker),
                    move.PWR, move);
            MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.substitute.absorb");
            target.getMoveStats().substituteHP -= damage;
            if (target.getMoveStats().substituteHP < 0) MovesUtils.sendPairedMessages(attacked, attacker,
                    "pokemob.substitute.break", attacked.getDisplayName());
            move.failed = true;
            move.PWR = 0;
            move.changeAddition = 0;
            move.statusChange = 0;
        }

        if (user && attack.getName().equals(IMoveNames.MOVE_SUBSTITUTE)) applied.getMoveStats().substituteHP = applied
                .getEntity().getMaxHealth() / 4;

        if (applied.getHeldItem() != null) ItemGenerator.processHeldItemUse(move, applied, applied.getHeldItem());

        if (applied.getAbility() != null) applied.getAbility().onMoveUse(applied, move);

        if (attack.getName().equals(IMoveNames.MOVE_FALSESWIPE)) move.noFaint = true;
        boolean blockMove = false;
        for (final String s : MoveEntry.protectionMoves)
            if (s.equals(move.attack))
            {
                blockMove = true;
                break;
            }

        if (user && !blockMove && applied.getMoveStats().blocked && applied.getMoveStats().blockTimer-- <= 0)
        {
            applied.getMoveStats().blocked = false;
            applied.getMoveStats().blockTimer = 0;
            applied.getMoveStats().BLOCKCOUNTER = 0;
        }
        boolean unblockable = false;
        for (final String s : MoveEntry.unBlockableMoves)
            if (s.equals(move.attack))
            {
                unblockable = true;
                break;
            }
        if (move.attacked != move.attacker && !unblockable && other != null && other.getMoveStats().BLOCKCOUNTER > 0)
        {
            final float count = Math.max(0, other.getMoveStats().BLOCKCOUNTER - 2);
            final float chance = count != 0 ? Math.max(0.125f, 1 / count) : 1;
            if (chance > Math.random()) move.failed = true;
        }
        if (attack.getName().equals(IMoveNames.MOVE_PROTECT) || attack.getName().equals(IMoveNames.MOVE_DETECT))
        {
            applied.getMoveStats().blockTimer = PokecubeCore.getConfig().attackCooldown * 2;
            applied.getMoveStats().blocked = true;
            applied.getMoveStats().BLOCKCOUNTER += 2;
        }
        if (applied.getMoveStats().BLOCKCOUNTER > 0) applied.getMoveStats().BLOCKCOUNTER--;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onEvent(final MoveWorldAction.OnAction evt)
    {
        final IPokemob attacker = evt.getUser();
        final Vector3 location = evt.getLocation();
        final Move_Base move = evt.getMove();
        IMoveAction action = this.actionMap.get(move.name);
        if (action == null)
        {
            MoveEventsHandler.register(action = new DefaultAction(move));
            action.init();
        }
        if (PokecubeCore.getConfig().permsMoveAction && attacker.getOwner() instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) attacker.getOwner();
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            if (!handler.hasPermission(player.getGameProfile(), Permissions.MOVEWORLDACTION.get(move.name), context))
            {
                if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Denied use of " + move.name + " for " + player);
                return;
            }
        }
        action.applyEffect(attacker, location);
    }
}