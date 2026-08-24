package pokecube.core.eventhandlers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.moves.MoveApplicationRegistry;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.combat.ComputeStatEvent;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.events.pokemobs.combat.MoveUse.MoveWorldAction;
import pokecube.api.events.pokemobs.combat.StatusEvent;
import pokecube.api.items.IPokemobUseable;
import pokecube.api.moves.Battle;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveNames;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.StatusApplier;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.database.tags.Tags;
import pokecube.core.eventhandlers.SpawnHandler.ForbidReason;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.impl.entity.impl.NonPersistantStatusEffect;
import pokecube.core.impl.entity.impl.NonPersistantStatusEffect.Effect;
import pokecube.core.impl.entity.impl.OngoingMoveEffect;
import pokecube.core.init.Config;
import pokecube.core.init.ItemGenerator;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.moves.world.DefaultAction;
import pokecube.core.moves.world.DefaultElectricAction;
import pokecube.core.moves.world.DefaultFireAction;
import pokecube.core.moves.world.DefaultIceAction;
import pokecube.core.moves.world.DefaultWaterAction;
import pokecube.core.recipes.MoveRecipe;
import pokecube.core.utils.Permissions;
import thut.api.entity.event.BreakTestEvent;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.core.common.commands.CommandTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static void addOrMergeActions(IMoveWorldEffect action)
    {
        actionsLists.compute(action.getMoveName(), (name, list) -> {
            if (list == null) list = new ArrayList<>();
            list.add(action);
            return list;
        });
    }

    private static void removeAction(IMoveWorldEffect action)
    {
        actionsLists.computeIfPresent(action.getMoveName(), (name, list) -> {
            if (list == null) return null;
            list.remove(action);
            return list.isEmpty() ? null : list;
        });
    }

    private static final Map<String, List<IMoveWorldEffect>> actionsLists = new HashMap<>();
    private static final List<IMoveWorldEffect> recipeActions = new ArrayList<>();

    public static boolean canAffectBlock(final IPokemob user, final Vector3 location, final String move)
    {
        return MoveEventsHandler.canAffectBlock(user, location, move, true, true);
    }

    /**
     * This method should be called before any block setting by any move effects.
     */
    public static boolean canAffectBlock(final IPokemob user, final Vector3 location, final String move,
            final boolean repelWarning, final boolean denyMessage)
    {
        for (final String s : PokecubeCore.getConfig().damageBlocksBlacklist) if (s.equals(move)) return false;

        ServerLevel level = null;
        if (user.getEntity().level() instanceof ServerLevel level2) level = level2;

        deny:
        if (!Config.Rules.canAffectBlocks(level))
        {
            for (final String s : PokecubeCore.getConfig().damageBlocksWhitelist) if (s.equals(move)) break deny;
            return false;
        }
        LivingEntity owner = user.getOwner();
        final boolean repel = SpawnHandler.getNoSpawnReason(user.getEntity().level(), location.intX(), location.intY(),
                location.intZ()) == ForbidReason.REPEL;
        if (!(owner instanceof Player)) owner = PokecubeMod.getFakePlayer(user.getEntity().level());
        if (repel)
        {
            if (!user.inCombat() && repelWarning) CommandTools.sendError(owner, "pokemob.action.denyrepel");
            return false;
        }
        final Player player = (Player) owner;
        if (!BreakTestEvent.testBreak(player.level(), location.getPos(), location.getBlockState(player.level()),
                player))
        {
            final MutableComponent message = Component.translatable("pokemob.createbase.deny.noperms");
            if (!user.inCombat() && denyMessage) thut.lib.ChatHelper.sendSystemMessage(player, message);
            return false;
        }
        return true;
    }

    public static UseContext getContext(final Level world, final IPokemob user, final BlockState toPlace,
            final Vector3 target)
    {
        final ItemStack stack = new ItemStack(toPlace.getBlock());
        final Player player = user.getOwner() instanceof Player
                ? (Player) user.getOwner()
                : PokecubeMod.getFakePlayer(world);
        final Vector3 origin = new Vector3().set(user.getEntity());
        final Vec3 start = origin.toVec3d();
        final Vec3 end = target.toVec3d();
        final ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, Fluid.ANY,
                user.getEntity());
        final BlockHitResult hit = world.clip(context);
        return new UseContext(world, player, InteractionHand.MAIN_HAND, stack, hit);
    }

    public static UseContext getContext(final Level world, final Entity user, final BlockState toPlace,
            final Vector3 target)
    {
        final ItemStack stack = new ItemStack(toPlace.getBlock());
        final Player player = user instanceof Player ? (Player) user : PokecubeMod.getFakePlayer(world);
        final Vector3 origin = new Vector3().set(user);
        final Vec3 start = origin.toVec3d();
        final Vec3 end = target.toVec3d();
        final ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, Fluid.ANY, user);
        final BlockHitResult hit = world.clip(context);
        return new UseContext(world, player, InteractionHand.MAIN_HAND, stack, hit);
    }

    public static void register(IMoveWorldEffect move)
    {
        addOrMergeActions(move);
    }

    public static void register()
    {
        // In initialize some effect types
        IOngoingAffected.EFFECTS.put(NonPersistantStatusEffect.ID, NonPersistantStatusEffect.class);
        IOngoingAffected.EFFECTS.put(OngoingMoveEffect.ID, OngoingMoveEffect.class);
        Effect.initDefaults();

        // These are all lowest, and false so that addons can override the
        // behaviour as needed

        // This handles after effects on the moves, like consuming held items,
        // and ability application for effects after move use.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onDuringUsePost);
        // This handles mob processing for the move, before damage/effects are
        // applied. It processes things like, Item Use, Abilities, 1HKOs,
        // Protection moves, Substitute, etc
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onDuringUsePre);
        // This handles application of world actions for the moves.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onWorldAction);
        // This handles application of world actions for the moves.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::preStatusAdded);
        // This handles application of world actions for the moves.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onComputeStats);
        // Setup recipes for moves that may have loaded in.
        ThutCore.FORGE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::initServerMoveRecipes);
    }

    /**
     * Here we apply a major accuracy hit for friendly fire moves. If an attack is not set as one which
     * applies to your own side in battle, then this will decrease the user accuracy, and increase the
     * target evasion for the attack's application on the target, if the target and user are on the same side.
     */
    private static void onComputeStats(ComputeStatEvent event)
    {
        var moveApplication = event.context;
        var us = event.affected.getEntity();
        var them = moveApplication.getTarget();
        if (them == us || them == null) them = moveApplication.getUser().getEntity();
        if (them == null) return; // No target...
        var battleA = event.affected.getBattle();
        var battleB = event.affected == moveApplication.getUser() ? battleA : Battle.getBattle(them);

        if (battleA != null && battleB != null)
        {
            // Both are in battle, so we check based on the sides
            if (battleB != battleA) return; // Different battle, we will ignore it.
            if (!battleA.getAllies(them).contains(us)) return; // Are on different sides, so ignore.
        }
        else
        {
            // Otherwise we check based on team, this checks owner internally
            if(!TeamManager.sameTeam(them, us)) return; // Different teams, so ignored.
        }

        var validator = MoveApplicationRegistry.getValidator(moveApplication.getMove());
        if (validator.test(moveApplication)) return; // was a valid move application

        // Halve accuracy, double evasion, this should result in a 1/4 chance to hit with default settings
        if (event.stat == IPokemob.Stats.ACCURACY) event.newValue *= PokecubeCore.getConfig().IFFAccuracyModifier;
        if (event.stat == IPokemob.Stats.EVASION) event.newValue *= PokecubeCore.getConfig().IFFEvasionModifier;
    }

    private static void initServerMoveRecipes(ServerAboutToStartEvent event)
    {
        // first remove all old recipe actions
        recipeActions.forEach(MoveEventsHandler::removeAction);

        // Now add the ones from recipes
        event.getServer().getRecipeManager().getRecipes().forEach(holder -> {
            if (holder.value() instanceof MoveRecipe recipe)
            {
                for (var move : MovesUtils.getKnownMoves())
                {
                    if (recipe.match.test(move.getName()))
                    {
                        var action = new MoveRecipe.RecipeAction(move.getName(), recipe);
                        register(action);
                        recipeActions.add(action);
                    }
                }
            }
        });

        // Finally re-init all of the actions
        actionsLists.values().forEach(l -> l.forEach(IMoveWorldEffect::init));
    }

    private static void preStatusAdded(StatusEvent.PreAdd event)
    {
        var pokemob = event.getPokemob();
        if (pokemob == null || event.getResult() != TriState.DEFAULT) return;
        var status = event.getStatus();
        if (status == StatusEffects.BURN)
        {
            if (pokemob.isType(PokeType.getType("fire")))
            {
                event.setResult(TriState.FALSE);
            }
        }
        else if (status == StatusEffects.PARALYSIS)
        {
            if (pokemob.isType(PokeType.getType("electric")))
            {
                event.setResult(TriState.FALSE);
            }
        }
        else if (status == StatusEffects.FREEZE)
        {
            if (pokemob.isType(PokeType.getType("ice")))
            {
                event.setResult(TriState.FALSE);
            }
        }
        else if (status == StatusEffects.POISON)
        {
            if (pokemob.isType(PokeType.getType("poison")) || pokemob.isType(PokeType.getType("steel")))
            {
                event.setResult(TriState.FALSE);
            }
        }
    }

    private static void onDuringUsePost(final MoveUse.DuringUse.Post evt)
    {
        final MoveApplication move = evt.getPacket();
        IPokemob attacker = move.getUser();
        final Entity attacked = move.getTarget();
        final IPokemob target = PokemobCaps.getPokemobFor(attacked);

        final IPokemobUseable attackerheld = PokemobCaps.getPokemobUsable(attacker.getHeldItem());
        if (attackerheld != null)
        {
            final InteractionResultHolder<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(),
                    move, false);
            if (result.getResult() == InteractionResult.SUCCESS) attacker.setHeldItem(result.getObject());
        }
        if (target != null)
        {
            final IPokemobUseable targetheld = PokemobCaps.getPokemobUsable(target.getHeldItem());
            if (targetheld != null)
            {
                final InteractionResultHolder<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(),
                        move, false);
                if (result.getResult() == InteractionResult.SUCCESS) target.setHeldItem(result.getObject());
            }
        }

        if (target != null && target.getHeldItem() != null)
            ItemGenerator.processHeldItemUse(move, target, target.getHeldItem());

        Ability ab;
        if (target != null && (ab = target.getAbility()) != null) ab.postMoveUse(target, move);
        if ((ab = attacker.getAbility()) != null) ab.postMoveUse(attacker, move);
    }

    private static void onDuringUsePre(final MoveUse.DuringUse.Pre evt)
    {
        final MoveApplication move = evt.getPacket();
        final MoveEntry attack = move.getMove();
        final IPokemob attacker = move.getUser();
        final Entity attacked = move.getTarget();
        final IPokemob target = PokemobCaps.getPokemobFor(attacked);

        final IPokemobUseable attackerheld = PokemobCaps.getPokemobUsable(attacker.getHeldItem());
        if (attackerheld != null)
        {
            final InteractionResultHolder<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(),
                    move, true);
            if (result.getResult() == InteractionResult.SUCCESS) attacker.setHeldItem(result.getObject());
        }
        if (target != null)
        {
            final IPokemobUseable targetheld = PokemobCaps.getPokemobUsable(target.getHeldItem());
            if (targetheld != null)
            {
                final InteractionResultHolder<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(),
                        move, true);
                if (result.getResult() == InteractionResult.SUCCESS) target.setHeldItem(result.getObject());
            }
        }

        if (target == null) return;
        target.getEntity().getPersistentData().putString("lastMoveHitBy", move.getMove().name);
        if (target != null && target.getMoveStats().substituteHP > 0)
        {
            final float damage = MovesUtils.getAttackStrength(attacker, target, move.getMove().getCategory(attacker),
                    move.pwr, move.getMove(), move.stat_multipliers);
            MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.substitute.absorb");
            target.getMoveStats().substituteHP -= damage;
            if (target.getMoveStats().substituteHP < 0)
                MovesUtils.sendPairedMessages(attacked, attacker, "pokemob.substitute.break",
                        attacked.getDisplayName());
            move.failed = true;
            move.pwr = 0;
            move.status = StatusApplier.NOOP;
        }

        if (attack.getName().equals(IMoveNames.MOVE_SUBSTITUTE))
            target.getMoveStats().substituteHP = target.getEntity().getMaxHealth() / 4;

        if (target.getHeldItem() != null) ItemGenerator.processHeldItemUse(move, target, target.getHeldItem());

        Ability ab;
        if ((ab = attacker.getAbility()) != null) ab.preMoveUse(attacker, move);
        if (target != null && (ab = target.getAbility()) != null) ab.preMoveUse(target, move);

        if (attack.getName().equals(IMoveNames.MOVE_FALSESWIPE)) move.noFaint = true;
        boolean blockMove = Tags.MOVE.isIn("block-moves", move.getName()); // If we are using a "block" move (e.g. protect)
        boolean unblockable = Tags.MOVE.isIn("no-block-moves", move.getName()); // If we are using a move that goes through protect (e.g. phantom force)

        // Reset attacker's protect parameters if they are set to block moves when they did not use protect
        if (attacker.getLastMoveUsed() != null && attacker.getMoveStats().blocked && attacker.getMoveStats().blockTimer-- <= 0)
        {
            if (Tags.MOVE.isIn("block-moves", attacker.getLastMoveUsed()))
            {
                attacker.getMoveStats().blocked = false;
                attacker.getMoveStats().blockTimer = 0;
                attacker.getMoveStats().BLOCKCOUNTER = 0;
            }
        }

        // Performs protect checks if we are using a protecting move.
        if (blockMove && attacker.getMoveStats().blockTimer != -1)
        {
            final float count = Math.max(0, attacker.getMoveStats().BLOCKCOUNTER - 2);
            final float chance = count != 0 ? Math.max(0.125f, 1 / count) : 1;
            if (chance > Math.random()) { // When the move is successful.
                attacker.getMoveStats().blockTimer = PokecubeCore.getConfig().attackCooldown;
                attacker.getMoveStats().blocked = true;
                attacker.getMoveStats().BLOCKCOUNTER += 2;
                MovesUtils.sendPairedMessages(target.getEntity(), attacker, "pokemob.move.protect");
            }
            else { // When the move fails due to successive use.
                attacker.getMoveStats().blocked = false;
                attacker.getMoveStats().blockTimer = -1; // blockTimer is set to -1 to show protect has previously failed.
                MovesUtils.sendPairedMessages(target.getEntity(), attacker, "pokemob.move.failed");
            }
        }

        // Set the move to fail if the target is set to block moves and we are not using an unblockable move.
        if (attacker != target && !unblockable && target.getMoveStats().blocked)
        {
            move.failed = true;
            MovesUtils.sendPairedMessages(attacker.getEntity(), target, "pokemob.move.protect");
        }
    }

    private static void onWorldAction(final MoveWorldAction.OnAction evt)
    {
        final IPokemob attacker = evt.getUser();
        final Vector3 location = evt.getLocation();
        final MoveEntry move = evt.getMove();
        var actions = MoveEventsHandler.actionsLists.getOrDefault(move.name, Collections.emptyList());
        if (PokecubeCore.getConfig().permsMoveAction && attacker.getOwner() instanceof ServerPlayer player)
        {
            if (!Permissions.canUseWorldAction(player, move.name))
            {
                if (PokecubeCore.getConfig().debug_moves)
                    PokecubeAPI.logInfo("Denied use of " + move.name + " for " + player);
                return;
            }
        }
        // Apply each action based on in combat status
        if (attacker.inCombat()) actions.forEach(a -> a.applyInCombat(attacker, location));
        else actions.forEach(a -> a.applyOutOfCombat(attacker, location));

        // Now apply defaults if they exist
        DefaultAction _action;
        actions:
        {
            if ((_action = new DefaultWaterAction(move)).isValid()) break actions;
            if ((_action = new DefaultIceAction(move)).isValid()) break actions;
            if ((_action = new DefaultElectricAction(move)).isValid()) break actions;
            if ((_action = new DefaultFireAction(move)).isValid()) break actions;
            _action = null;
        }
        if (_action != null)
        {
            _action.init();
            if (attacker.inCombat()) _action.applyInCombat(attacker, location);
            else _action.applyOutOfCombat(attacker, location);
        }
    }
}