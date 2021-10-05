package pokecube.core.handlers.events;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.database.abilities.Ability;
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
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class MoveEventsHandler
{
    public static class UseContext extends BlockPlaceContext
    {
        protected UseContext(final Level worldIn, final Player playerIn, final InteractionHand handIn,
                final ItemStack stackIn, final BlockHitResult rayTraceResultIn)
        {
            super(worldIn, playerIn, handIn, stackIn, rayTraceResultIn);
        }

        public BlockPos getHitPos()
        {
            return this.getHitResult().getBlockPos();
        }

        public BlockState getHitState()
        {
            return this.getLevel().getBlockState(this.getHitPos());
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

    public static boolean attemptSmelt(final IPokemob attacker, final Vector3 location)
    {
        final Level world = attacker.getEntity().getCommandSenderWorld();
        final List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, location.getAABB().inflate(1));
        if (!items.isEmpty())
        {
            boolean smelt = false;
            final AbstractFurnaceBlockEntity tile = new FurnaceBlockEntity(location.getPos(), location.getBlockState(
                    world));
            tile.setLevel(world);
            for (final ItemEntity item2 : items)
            {
                final ItemEntity item = item2;
                final ItemStack stack = item.getItem();
                final int num = stack.getCount();
                tile.setItem(0, stack);
                tile.setItem(1, stack);
                final Recipe<?> irecipe = world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, tile, world)
                        .orElse(null);
                if (irecipe == null) continue;
                ItemStack newstack = irecipe.getResultItem();
                if (newstack != null)
                {
                    newstack = newstack.copy();
                    newstack.setCount(num);
                    int i1 = num;
                    float f = ((AbstractCookingRecipe) irecipe).getExperience();
                    if (f == 0.0F) i1 = 0;
                    else if (f < 1.0F)
                    {
                        int j = Mth.floor(i1 * f);
                        if (j < Mth.ceil(i1 * f) && Math.random() < i1 * f - j) ++j;

                        i1 = j;
                    }
                    f = i1;
                    while (i1 > 0)
                    {
                        final int k = ExperienceOrb.getExperienceValue(i1);
                        i1 -= k;
                        world.addFreshEntity(new ExperienceOrb(world, location.x, location.y + 1.5D, location.z + 0.5D,
                                k));
                    }
                    int hunger = PokecubeCore.getConfig().baseSmeltingHunger * num;
                    hunger = (int) Math.max(1, hunger / (float) attacker.getLevel());
                    if (f > 0) hunger *= f;
                    attacker.applyHunger(hunger);
                    item.setItem(newstack);
                    item.lifespan += 6000;
                    smelt = true;
                }
            }
            return smelt;
        }
        return false;
    }

    public static boolean canAffectBlock(final IPokemob user, final Vector3 location, final String move)
    {
        return MoveEventsHandler.canAffectBlock(user, location, move, true, true);
    }

    /**
     * This method should be called before any block setting by any move
     * effects.
     *
     * @param user
     * @param location
     * @return
     */
    public static boolean canAffectBlock(final IPokemob user, final Vector3 location, final String move,
            final boolean repelWarning, final boolean denyMessage)
    {
        for (final String s : PokecubeCore.getConfig().damageBlocksBlacklist)
            if (s.equals(move)) return false;
        deny:
        if (!PokecubeCore.getConfig().pokemobsDamageBlocks)
        {
            for (final String s : PokecubeCore.getConfig().damageBlocksWhitelist)
                if (s.equals(move)) break deny;
            return false;
        }
        LivingEntity owner = user.getOwner();
        final boolean repel = SpawnHandler.getNoSpawnReason(user.getEntity().getCommandSenderWorld(), location.intX(),
                location.intY(), location.intZ()) == ForbidReason.REPEL;
        if (!(owner instanceof Player)) owner = PokecubeMod.getFakePlayer(user.getEntity().getCommandSenderWorld());
        if (repel)
        {
            if (!user.inCombat() && repelWarning) CommandTools.sendError(owner, "pokemob.action.denyrepel");
            return false;
        }
        final Player player = (Player) owner;
        final BreakEvent evt = new BreakEvent(player.getCommandSenderWorld(), location.getPos(), location.getBlockState(
                player.getCommandSenderWorld()), player);
        MinecraftForge.EVENT_BUS.post(evt);
        if (evt.isCanceled())
        {
            final TranslatableComponent message = new TranslatableComponent("pokemob.createbase.deny.noperms");
            if (!user.inCombat() && denyMessage) owner.sendMessage(message, Util.NIL_UUID);
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
        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(attacker, location, move.getName())) return false;

        final Level world = attacker.getEntity().getCommandSenderWorld();
        final BlockState state = location.getBlockState(world);
        final Block block = state.getBlock();
        final Vector3 nextBlock = Vector3.getNewVector().set(attacker.getEntity()).subtractFrom(location).reverse()
                .norm().addTo(location);
        final BlockState nextState = nextBlock.getBlockState(world);
        if (block == Blocks.SAND)
        {
            location.setBlock(world, Blocks.GLASS.defaultBlockState());
            return true;
        }
        else if (state.canBeReplaced(MoveEventsHandler.getContext(world, attacker, Blocks.GLASS.defaultBlockState(),
                location)) && nextState.getBlock() == Blocks.SAND)
        {
            nextBlock.setBlock(world, Blocks.GLASS.defaultBlockState());
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
        final Level world = attacker.getEntity().getCommandSenderWorld();
        final UseContext context = MoveEventsHandler.getContext(world, attacker, Blocks.LAVA.defaultBlockState(),
                location);
        final BlockState state = context.getHitState();
        Block block = state.getBlock();
        final BlockPos hitPos = context.getHitPos();
        final BlockPos prevPos = context.getClickedPos();
        final BlockPos placePos = prevPos;
        final boolean light = BaseFireBlock.canBePlacedAt(world, placePos, context.getHorizontalDirection());
        final BlockState prev = world.getBlockState(prevPos);

        final boolean smelted = MoveEventsHandler.attemptSmelt(attacker, location);
        // First try to smelt items
        if (smelted) return true;

        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(attacker, location, move.getName())) return false;

        // Melt Snow
        if (block == Blocks.SNOW_BLOCK)
        {
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            final int level = state.getValue(SnowLayerBlock.LAYERS);
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        block = prev.getBlock();

        // Melt Snow
        if (block == Blocks.SNOW_BLOCK)
        {
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            final int level = prev.getValue(SnowLayerBlock.LAYERS);
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState());
            return true;
        }

        // Start fires
        if (light && move.getPWR() < MoveEventsHandler.FIRESTRONG)
        {
            final BlockState fire = BaseFireBlock.getState(world, placePos);
            world.setBlockAndUpdate(placePos, fire);
            return true;
        }
        if (move.getPWR() < MoveEventsHandler.FIRESTRONG) return false;

        block = state.getBlock();
        // Melt obsidian
        if (block == Blocks.OBSIDIAN)
        {
            world.setBlockAndUpdate(hitPos, Blocks.LAVA.defaultBlockState());
            return true;
        }
        // Evapourate water
        else if (block == Blocks.WATER)
        {
            world.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
            return true;
        }
        block = prev.getBlock();
        if (block == Blocks.OBSIDIAN)
        {
            world.setBlockAndUpdate(hitPos, Blocks.LAVA.defaultBlockState());
            return true;
        }
        // Evapourate water
        else if (block == Blocks.WATER)
        {
            world.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
            return true;
        }
        // Start fires
        else if (light)
        {
            final BlockState fire = BaseFireBlock.getState(world, placePos);
            world.setBlockAndUpdate(placePos, fire);
            return true;
        }
        return false;
    }

    /**
     * This will have the following effects, for ice type moves:
     * Place snow
     * Freeze water
     */
    public static boolean doDefaultIce(final IPokemob attacker, final Move_Base move, final Vector3 location)
    {
        if (!PokecubeCore.getConfig().defaultIceActions) return false;

        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(attacker, location, move.getName())) return false;

        final Level world = attacker.getEntity().getCommandSenderWorld();
        final UseContext context = MoveEventsHandler.getContext(world, attacker, Blocks.SNOW.defaultBlockState(),
                location);
        final BlockState state = context.getHitState();
        final Block block = state.getBlock();
        // // First attempt to freeze the water
        if (block == Blocks.WATER && state.getValue(LiquidBlock.LEVEL) == 0)
        {
            world.setBlockAndUpdate(context.getClickedPos(), Blocks.ICE.defaultBlockState());
            return true;
        }
        final InteractionResult result = context.getItemInHand().useOn(context);
        return result == InteractionResult.SUCCESS;
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
        final Level world = attacker.getEntity().getCommandSenderWorld();
        final UseContext context = MoveEventsHandler.getContext(world, attacker, Blocks.WATER.defaultBlockState(),
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
        if (state.getProperties().contains(FarmBlock.MOISTURE))
        {
            final int level = state.getValue(FarmBlock.MOISTURE);
            if (level < 7)
            {
                world.setBlockAndUpdate(hitPos, state.setValue(FarmBlock.MOISTURE, 7));
                return true;
            }
        }
        if (move.getPWR() < MoveEventsHandler.WATERSTRONG) return false;

        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(attacker, location, move.getName())) return false;

        // Freeze lava
        if (block == Blocks.LAVA)
        {
            final int level = state.getValue(LiquidBlock.LEVEL);
            final BlockState replacement = level == 0 ? Blocks.OBSIDIAN.defaultBlockState()
                    : Blocks.STONE.defaultBlockState();
            world.setBlockAndUpdate(hitPos, replacement);
            return true;
        }
        final BlockPos prevPos = context.getClickedPos();
        final BlockState prev = world.getBlockState(prevPos);

        // Freeze lava
        if (prev.getBlock() == Blocks.LAVA)
        {
            final int level = state.getValue(LiquidBlock.LEVEL);
            final BlockState replacement = level == 0 ? Blocks.OBSIDIAN.defaultBlockState()
                    : Blocks.STONE.defaultBlockState();
            world.setBlockAndUpdate(prevPos, replacement);
            return true;
        }

        // Attempt to place some water
        if (prev.canBeReplaced(context)) world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState().setValue(
                LiquidBlock.LEVEL, 2));
        return false;
    }

    public static UseContext getContext(final Level world, final IPokemob user, final BlockState toPlace,
            final Vector3 target)
    {
        final ItemStack stack = new ItemStack(toPlace.getBlock());
        final Player player = user.getOwner() instanceof Player ? (Player) user.getOwner()
                : PokecubeMod.getFakePlayer(world);
        final Vector3 origin = Vector3.getNewVector().set(user.getEntity());
        final Vec3 start = origin.toVec3d();
        final Vec3 end = target.toVec3d();
        final ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, Fluid.ANY, user
                .getEntity());
        final BlockHitResult hit = world.clip(context);
        return new UseContext(world, player, InteractionHand.MAIN_HAND, stack, hit);
    }

    public static UseContext getContext(final Level world, final Entity user, final BlockState toPlace,
            final Vector3 target)
    {
        final ItemStack stack = new ItemStack(toPlace.getBlock());
        final Player player = user instanceof Player ? (Player) user : PokecubeMod.getFakePlayer(world);
        final Vector3 origin = Vector3.getNewVector().set(user);
        final Vec3 start = origin.toVec3d();
        final Vec3 end = target.toVec3d();
        final ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, Fluid.ANY, user);
        final BlockHitResult hit = world.clip(context);
        return new UseContext(world, player, InteractionHand.MAIN_HAND, stack, hit);
    }

    public static void register(IMoveAction move)
    {
        if (!(move instanceof ActionWrapper)) move = new ActionWrapper(move);
        MoveEventsHandler.actionMap.put(move.getMoveName(), move);
    }

    private static Map<String, IMoveAction> actionMap = Maps.newHashMap();

    public static void register()
    {
        // In initialize some effect types
        IOngoingAffected.EFFECTS.put(NonPersistantStatusEffect.ID, NonPersistantStatusEffect.class);
        IOngoingAffected.EFFECTS.put(PersistantStatusEffect.ID, PersistantStatusEffect.class);
        IOngoingAffected.EFFECTS.put(OngoingMoveEffect.ID, OngoingMoveEffect.class);
        Status.initDefaults();
        Effect.initDefaults();

        // These are all lowest, and false so that addons can override the
        // behaviour as needed

        // This handles after effects on the moves, like consuming held items,
        // and ability application for effects after move use.
        PokecubeCore.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onDuringUsePost);
        // This handles mob processing for the move, before damage/effects are
        // applied. It processes things like, Item Use, Abilities, 1HKOs,
        // Protection moves, Substitute, etc
        PokecubeCore.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onDuringUsePre);
        // This handles application of world actions for the moves.
        PokecubeCore.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onWorldAction);
    }

    private static void onDuringUsePost(final MoveUse.DuringUse.Post evt)
    {
        final MovePacket move = evt.getPacket();
        IPokemob attacker = move.attacker;
        final Entity attacked = move.attacked;
        final IPokemob target = CapabilityPokemob.getPokemobFor(attacked);

        final IPokemobUseable attackerheld = IPokemobUseable.getUsableFor(attacker.getHeldItem());
        if (attackerheld != null)
        {
            final InteractionResultHolder<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(),
                    move);
            if (result.getResult() == InteractionResult.SUCCESS) attacker.setHeldItem(result.getObject());
        }
        if (target != null)
        {
            final IPokemobUseable targetheld = IPokemobUseable.getUsableFor(target.getHeldItem());
            if (targetheld != null)
            {
                final InteractionResultHolder<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(),
                        move);
                if (result.getResult() == InteractionResult.SUCCESS) target.setHeldItem(result.getObject());
            }
        }

        final boolean user = evt.isFromUser();
        IPokemob applied = user ? attacker : target;
        if (applied != null && applied.getHeldItem() != null) ItemGenerator.processHeldItemUse(move, applied, applied
                .getHeldItem());

        Ability ab;
        if (target != null && (ab = target.getAbility()) != null) ab.onMoveUse(applied, move);
        // Reset this incase it was changed!
        attacker = move.attacker;
        applied = user ? attacker : target;
        if ((ab = attacker.getAbility()) != null) ab.onMoveUse(applied, move);
    }

    private static void onDuringUsePre(final MoveUse.DuringUse.Pre evt)
    {
        final MovePacket move = evt.getPacket();
        final Move_Base attack = move.getMove();
        final boolean user = evt.isFromUser();
        IPokemob attacker = move.attacker;
        final Entity attacked = move.attacked;
        final IPokemob target = CapabilityPokemob.getPokemobFor(attacked);
        IPokemob applied = user ? attacker : target;
        IPokemob other = user ? target : attacker;

        final IPokemobUseable attackerheld = IPokemobUseable.getUsableFor(attacker.getHeldItem());
        if (attackerheld != null)
        {
            final InteractionResultHolder<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(),
                    move);
            if (result.getResult() == InteractionResult.SUCCESS) attacker.setHeldItem(result.getObject());
        }
        if (target != null)
        {
            final IPokemobUseable targetheld = IPokemobUseable.getUsableFor(target.getHeldItem());
            if (targetheld != null)
            {
                final InteractionResultHolder<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(),
                        move);
                if (result.getResult() == InteractionResult.SUCCESS) target.setHeldItem(result.getObject());
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

        Ability ab;
        if ((ab = attacker.getAbility()) != null) ab.onMoveUse(applied, move);
        // Reset this incase it was changed!
        attacker = move.attacker;
        applied = user ? attacker : target;
        other = user ? target : attacker;
        if (target != null && (ab = target.getAbility()) != null) ab.onMoveUse(applied, move);
        // Reset this incase it was changed!
        attacker = move.attacker;
        applied = user ? attacker : target;
        other = user ? target : attacker;

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

    private static void onWorldAction(final MoveWorldAction.OnAction evt)
    {
        final IPokemob attacker = evt.getUser();
        final Vector3 location = evt.getLocation();
        final Move_Base move = evt.getMove();
        IMoveAction action = MoveEventsHandler.actionMap.get(move.name);
        if (action == null)
        {
            MoveEventsHandler.register(action = new DefaultAction(move));
            action.init();
        }
        if (PokecubeCore.getConfig().permsMoveAction && attacker.getOwner() instanceof Player)
        {
            final Player player = (Player) attacker.getOwner();
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