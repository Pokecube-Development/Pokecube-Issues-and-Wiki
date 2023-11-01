package pokecube.core.eventhandlers;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
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
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.events.pokemobs.combat.MoveUse.MoveWorldAction;
import pokecube.api.items.IPokemobUseable;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveNames;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.StatusApplier;
import pokecube.core.PokecubeCore;
import pokecube.core.database.tags.Tags;
import pokecube.core.eventhandlers.SpawnHandler.ForbidReason;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.impl.entity.impl.NonPersistantStatusEffect;
import pokecube.core.impl.entity.impl.NonPersistantStatusEffect.Effect;
import pokecube.core.impl.entity.impl.OngoingMoveEffect;
import pokecube.core.impl.entity.impl.PersistantStatusEffect;
import pokecube.core.impl.entity.impl.PersistantStatusEffect.Status;
import pokecube.core.init.Config;
import pokecube.core.init.ItemGenerator;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.world.DefaultAction;
import pokecube.core.moves.world.DefaultElectricAction;
import pokecube.core.moves.world.DefaultFireAction;
import pokecube.core.moves.world.DefaultIceAction;
import pokecube.core.moves.world.DefaultWaterAction;
import pokecube.core.utils.Permissions;
import thut.api.entity.event.BreakTestEvent;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;
import thut.lib.TComponent;

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

    private static class Action implements IMoveWorldEffect
    {
        final IMoveWorldEffect wrapped;
        private IMoveWorldEffect custom;
        private boolean checked = false;

        public Action(final IMoveWorldEffect wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public boolean applyOutOfCombat(final IPokemob user, final Vector3 location)
        {
            if (!this.checked)
            {
                this.checked = true;
                this.custom = MoveEventsHandler.customActions.get(this.getMoveName());
            }
            final boolean customApplied = this.custom != null && this.custom.applyOutOfCombat(user, location);
            return this.wrapped.applyOutOfCombat(user, location) || customApplied;
        }

        @Override
        public boolean applyInCombat(final IPokemob user, final Vector3 location)
        {
            if (!this.checked)
            {
                this.checked = true;
                this.custom = MoveEventsHandler.customActions.get(this.getMoveName());
            }
            final boolean customApplied = this.custom != null && this.custom.applyInCombat(user, location);
            return this.wrapped.applyInCombat(user, location) || customApplied;
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

    public static class WrappedAction implements IMoveWorldEffect
    {
        public IMoveWorldEffect parent;
        public IMoveWorldEffect other;

        public WrappedAction(final IMoveWorldEffect parent, final IMoveWorldEffect other)
        {
            this.parent = parent;
            this.other = other;
        }

        @Override
        public boolean applyOutOfCombat(final IPokemob user, final Vector3 location)
        {
            // Only applies other action if parent action failed.
            return this.parent.applyOutOfCombat(user, location) || this.other.applyOutOfCombat(user, location);
        }

        @Override
        public boolean applyInCombat(IPokemob user, Vector3 location)
        {
            // Only applies other action if parent action failed.
            return this.parent.applyInCombat(user, location) || this.other.applyInCombat(user, location);
        }

        @Override
        public String getMoveName()
        {
            return this.parent.getMoveName();
        }

        @Override
        public void init()
        {
            this.parent.init();
            this.other.init();
        }
    }

    public static void addOrMergeActions(IMoveWorldEffect action)
    {
        if (MoveEventsHandler.customActions.containsKey(action.getMoveName()))
        {
            final IMoveWorldEffect prev = MoveEventsHandler.customActions.get(action.getMoveName());
            if (prev instanceof WrappedAction edit)
            {
                edit.other = action;
                action = prev;
            }
            else action = new WrappedAction(MoveEventsHandler.customActions.get(action.getMoveName()), action);
        }
        MoveEventsHandler.customActions.put(action.getMoveName(), action);
    }

    public static final Map<String, IMoveWorldEffect> customActions = Maps.newHashMap();

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
        for (final String s : PokecubeCore.getConfig().damageBlocksBlacklist) if (s.equals(move)) return false;

        ServerLevel level = null;
        if (user.getEntity().getLevel() instanceof ServerLevel level2) level = level2;

        deny:
        if (!Config.Rules.canAffectBlocks(level))
        {
            for (final String s : PokecubeCore.getConfig().damageBlocksWhitelist) if (s.equals(move)) break deny;
            return false;
        }
        LivingEntity owner = user.getOwner();
        final boolean repel = SpawnHandler.getNoSpawnReason(user.getEntity().getLevel(), location.intX(),
                location.intY(), location.intZ()) == ForbidReason.REPEL;
        if (!(owner instanceof Player)) owner = PokecubeMod.getFakePlayer(user.getEntity().getLevel());
        if (repel)
        {
            if (!user.inCombat() && repelWarning) CommandTools.sendError(owner, "pokemob.action.denyrepel");
            return false;
        }
        final Player player = (Player) owner;
        if (!BreakTestEvent.testBreak(player.getLevel(), location.getPos(), location.getBlockState(player.getLevel()),
                player))
        {
            final MutableComponent message = TComponent.translatable("pokemob.createbase.deny.noperms");
            if (!user.inCombat() && denyMessage) thut.lib.ChatHelper.sendSystemMessage(player, message);
            return false;
        }
        return true;
    }

    public static UseContext getContext(final Level world, final IPokemob user, final BlockState toPlace,
            final Vector3 target)
    {
        final ItemStack stack = new ItemStack(toPlace.getBlock());
        final Player player = user.getOwner() instanceof Player ? (Player) user.getOwner()
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
        if (!(move instanceof Action)) move = new Action(move);
        MoveEventsHandler.actionMap.put(move.getMoveName(), move);
    }

    public static boolean hasAction(MoveEntry move)
    {
        return actionMap.containsKey(move.getName());
    }

    private static Map<String, IMoveWorldEffect> actionMap = Maps.newHashMap();

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
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onDuringUsePost);
        // This handles mob processing for the move, before damage/effects are
        // applied. It processes things like, Item Use, Abilities, 1HKOs,
        // Protection moves, Substitute, etc
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onDuringUsePre);
        // This handles application of world actions for the moves.
        PokecubeAPI.MOVE_BUS.addListener(EventPriority.LOWEST, false, MoveEventsHandler::onWorldAction);
    }

    private static void onDuringUsePost(final MoveUse.DuringUse.Post evt)
    {
        final MoveApplication move = evt.getPacket();
        IPokemob attacker = move.getUser();
        final Entity attacked = move.getTarget();
        final IPokemob target = PokemobCaps.getPokemobFor(attacked);

        final IPokemobUseable attackerheld = IPokemobUseable.getUsableFor(attacker.getHeldItem());
        if (attackerheld != null)
        {
            final InteractionResultHolder<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(),
                    move, false);
            if (result.getResult() == InteractionResult.SUCCESS) attacker.setHeldItem(result.getObject());
        }
        if (target != null)
        {
            final IPokemobUseable targetheld = IPokemobUseable.getUsableFor(target.getHeldItem());
            if (targetheld != null)
            {
                final InteractionResultHolder<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(),
                        move, false);
                if (result.getResult() == InteractionResult.SUCCESS) target.setHeldItem(result.getObject());
            }
        }

        IPokemob applied = target;
        if (applied != null && applied.getHeldItem() != null)
            ItemGenerator.processHeldItemUse(move, applied, applied.getHeldItem());

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

        final IPokemobUseable attackerheld = IPokemobUseable.getUsableFor(attacker.getHeldItem());
        if (attackerheld != null)
        {
            final InteractionResultHolder<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(),
                    move, true);
            if (result.getResult() == InteractionResult.SUCCESS) attacker.setHeldItem(result.getObject());
        }
        if (target != null)
        {
            final IPokemobUseable targetheld = IPokemobUseable.getUsableFor(target.getHeldItem());
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
            if (target.getMoveStats().substituteHP < 0) MovesUtils.sendPairedMessages(attacked, attacker,
                    "pokemob.substitute.break", attacked.getDisplayName());
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
        boolean blockMove = Tags.MOVE.isIn("block-move", move.getName());

        if (!blockMove && target.getMoveStats().blocked && target.getMoveStats().blockTimer-- <= 0)
        {
            target.getMoveStats().blocked = false;
            target.getMoveStats().blockTimer = 0;
            target.getMoveStats().BLOCKCOUNTER = 0;
        }
        boolean unblockable = Tags.MOVE.isIn("no-block-move", move.getName());
        if (attacker != target && !unblockable && target.getMoveStats().BLOCKCOUNTER > 0)
        {
            final float count = Math.max(0, target.getMoveStats().BLOCKCOUNTER - 2);
            final float chance = count != 0 ? Math.max(0.125f, 1 / count) : 1;
            if (chance > Math.random()) move.failed = true;
        }
        if (blockMove)
        {
            target.getMoveStats().blockTimer = PokecubeCore.getConfig().attackCooldown * 2;
            target.getMoveStats().blocked = true;
            target.getMoveStats().BLOCKCOUNTER += 2;
        }
        if (target.getMoveStats().BLOCKCOUNTER > 0) target.getMoveStats().BLOCKCOUNTER--;
    }

    private static void onWorldAction(final MoveWorldAction.OnAction evt)
    {
        final IPokemob attacker = evt.getUser();
        final Vector3 location = evt.getLocation();
        final MoveEntry move = evt.getMove();
        IMoveWorldEffect action = MoveEventsHandler.actionMap.get(move.name);
        if (action == null)
        {
            DefaultAction _action = null;
            actions:
            {
                if ((_action = new DefaultWaterAction(move)).isValid()) break actions;
                if ((_action = new DefaultIceAction(move)).isValid()) break actions;
                if ((_action = new DefaultElectricAction(move)).isValid()) break actions;
                if ((_action = new DefaultFireAction(move)).isValid()) break actions;
                _action = null;
            }
            action = _action == null ? new DefaultAction(move) : _action;
            MoveEventsHandler.register(action);
            action.init();
        }
        if (PokecubeCore.getConfig().permsMoveAction && attacker.getOwner() instanceof ServerPlayer player)
        {
            if (!Permissions.canUseWorldAction(player, move.name))
            {
                if (PokecubeCore.getConfig().debug_moves)
                    PokecubeAPI.logInfo("Denied use of " + move.name + " for " + player);
                return;
            }
        }
        if (attacker.inCombat()) action.applyInCombat(attacker, location);
        else action.applyOutOfCombat(attacker, location);
    }
}