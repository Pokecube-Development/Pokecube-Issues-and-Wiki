package thut.concrete.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import thut.api.block.IDyedBlock;
import thut.api.block.flowing.IFlowingBlock;
import thut.concrete.Concrete;
import thut.concrete.block.ConcreteBlock;

public class PaintBrush extends BrushItem
{
    private static final double MAX_BRUSH_DISTANCE = Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) - 1.0D;
    private final DyeColor colour;

    public PaintBrush(Properties properties, DyeColor colour)
    {
        super(properties);
        this.colour = colour;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (colour == null) return super.useOn(context);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

//        if (level instanceof ServerLevel)
//        {
//            ExplosionCustom boom = new ExplosionCustom(level, null, Vector3.getNewVector().set(pos), 200);
//            boom.breaker = new ChamberBoom(4);
//            boom.doExplosion();
//        }

        if (state.getBlock() instanceof IDyedBlock b)
        {
            if (colour != b.getColour() && b.getFor(colour) != null)
            {
                BlockState painted = IFlowingBlock.copyValidTo(state, b.getFor(colour).defaultBlockState());
                level.setBlock(pos, painted, 3);
                ItemStack stack = context.getItemInHand();
                if (context.getPlayer() instanceof ServerPlayer player)
                {
                    boolean broke = stack.hurt(1, player.getRandom(), player);
                    if (broke) stack = new ItemStack(Concrete.BRUSHES[16].get());
                    player.setItemInHand(context.getHand(), stack);
                }
                return InteractionResult.CONSUME;
            }
        }
        else if (ConcreteBlock.VANILLAREV.containsKey(state.getBlock()))
        {
            DyeColor old = ConcreteBlock.VANILLAREV.get(state.getBlock());
            if (old != this.colour)
            {
                ItemStack stack = context.getItemInHand();
                if (context.getPlayer() instanceof ServerPlayer player)
                {
                    boolean broke = stack.hurt(1, player.getRandom(), player);
                    if (broke) stack = new ItemStack(Concrete.BRUSHES[16].get());
                    player.setItemInHand(context.getHand(), stack);
                }
                Block newBlock = ConcreteBlock.VANILLA.get(colour);
                level.setBlock(pos, newBlock.defaultBlockState(), 3);
                return InteractionResult.CONSUME;
            }
        }
        return super.useOn(context);
    }

    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int ticks) {
        if (ticks >= 0 && entity instanceof Player player)
        {
            HitResult hitresult = this.calculateHitResult(entity);
            if (hitresult instanceof BlockHitResult hitResult)
            {
                if (hitresult.getType() == HitResult.Type.BLOCK)
                {
                    int i = this.getUseDuration(stack) - ticks + 1;
                    boolean flag = i % 10 == 5;
                    if (flag)
                    {
                        BlockPos pos = hitResult.getBlockPos();
                        BlockState state = world.getBlockState(pos);
                        HumanoidArm arm = entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                        this.spawnDustParticles(world, hitResult, state, entity.getViewVector(0.0F), arm);
                        Block block = state.getBlock();
                        SoundEvent soundevent;
                        if (block instanceof BrushableBlock) {
                            BrushableBlock brushableblock = (BrushableBlock)block;
                            soundevent = brushableblock.getBrushSound();
                        } else {
                            soundevent = SoundEvents.BRUSH_GENERIC;
                        }
                        world.playSound(player, pos, soundevent, SoundSource.BLOCKS);
                    }
                    return;
                }
            }
            entity.releaseUsingItem();
        } else {
            entity.releaseUsingItem();
        }
    }

    public HitResult calculateHitResult(LivingEntity entity) {
        return ProjectileUtil.getHitResultOnViewVector(entity, (player) -> {
            return !player.isSpectator() && player.isPickable();
        }, MAX_BRUSH_DISTANCE);
    }
}
